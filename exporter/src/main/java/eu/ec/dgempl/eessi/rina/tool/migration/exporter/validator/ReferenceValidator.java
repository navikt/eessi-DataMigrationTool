package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndex;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache.CacheEntry;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.CacheService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.IdHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.JsonPathHelper;

/**
 * Validates if object is a valid reference
 */
public class ReferenceValidator extends AbstractValidator {
    private static final Logger logger = LoggerFactory.getLogger(ReferenceValidator.class);

    private final EsClientService elasticClient;
    private final CacheService cacheService;

    public ReferenceValidator(EsClientService elasticClient, CacheService cacheService, String... params) {
        super(params);
        this.elasticClient = elasticClient;
        this.cacheService = cacheService;
    }

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        String index = params[0];
        String type = params[1];
        String format = "";
        if (params.length == 3) {
            format = params[2];
        }

        Map<String, Object> document = context.getDocument().getObject();
        String documentType = context.getDocument().getType();
        String normalisedPath = JsonPathHelper.normalisePath(path);

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
            return results;
        }

        // add a special handler for documents with status "empty" to ignore validation of creator.id and versions.user.id
        Object status = document.get("status");
        if (status != null && status instanceof String && ((String) status).equalsIgnoreCase("empty")) {
            if (normalisedPath.equalsIgnoreCase("creator.id") || normalisedPath.equalsIgnoreCase("versions.user.id")) {
                logger.info(
                        "Validation skipped. Documents with status=empty are created by the SYSTEM user. Context=[index={},type={},id={},path={},value={}]",
                        context.getDocument().getIndex(), context.getDocument().getType(), context.getDocument().getObjectId(), path, obj);

                // no need for validation
                results.add(ValidationResult.ok(path, obj));
                return results;
            }
        }

        if (obj instanceof String) {
            // add a special handler for comments with documentId="0" (a.k.a. case comments)
            if (documentType.equalsIgnoreCase(EEsType.COMMENT.value()) && normalisedPath.equalsIgnoreCase("documentid")
                    && obj.equals("0")) {
                logger.info("Validation skipped. Case comments have documentId=0. Context=[index={},type={},id={},path={},value={}]",
                        context.getDocument().getIndex(), context.getDocument().getType(), context.getDocument().getObjectId(), path, obj);

                // no need for validation
                results.add(ValidationResult.ok(path, obj));
                return results;
            }

            // add a special handler for user.id="System" and user.id="0" (a.k.a. system user)
            if (index.equalsIgnoreCase(EEsIndex.IDENTITY_V1.value()) && type.equalsIgnoreCase(EEsType.USER.value())) {
                if (((String) obj).equalsIgnoreCase("system") || ((String) obj).equalsIgnoreCase("0")) {
                    logger.info("Validation skipped. SYSTEM user doesn't exist in ES. Context=[index={},type={},id={},path={},value={}]",
                            context.getDocument().getIndex(), context.getDocument().getType(), context.getDocument().getObjectId(), path,
                            obj);

                    // no need for validation
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }
            }

            // add a special handler for documentcontents related to subdocuments
            // the elasticsearch type is overwritten to "subdocument"
            // the format is overwritten to "caseId__id" (yes, double "_")
            if (documentType.equalsIgnoreCase(EEsType.DOCUMENTCONTENT.value()) && normalisedPath.equalsIgnoreCase("id")
                    && document.get("parentDocumentId") != null) {
                type = EEsType.SUBDOCUMENT.value();
                format = "caseId__id";
            }

            // construct the documentId based on the params
            String id = "";
            if (StringUtils.isEmpty(format)) {
                id = (String) obj;
            } else {
                String[] tokens = format.split("_");
                for (int i = 0; i < tokens.length - 1; i++) {
                    String token = tokens[i];
                    if (StringUtils.isEmpty(token)) {
                        id += "_";
                    } else {
                        Object found = JsonPathHelper.getObjectInMapByPath(document, token);
                        if (found == null || !(found instanceof String)) {
                            String details = String.format("Could not create the reference id [params=%s]", String.join(",", params));
                            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE, details));
                            return results;
                        } else {
                            id += found + "_";
                        }
                    }
                }
                String token = tokens[tokens.length - 1];
                if (token.equals(path)) {
                    id += (String) obj;
                } else {
                    Object found = JsonPathHelper.getObjectInMapByPath(document, token);
                    if (found == null || !(found instanceof String)) {
                        String details = String.format("Could not create the reference id [params=%s]", String.join(",", params));
                        results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE, details));
                        return results;
                    } else {
                        id += found;
                    }
                }
            }

            // search for the document
            CacheEntry cacheEntry = cacheService.get(index, type, id);
            if (cacheEntry == null) {
                try {
                    EsDocument parent = context.getParent();
                    String cacheEntryContext = IdHelper.getDocumentReference(parent.getIndex(), parent.getType(), parent.getObjectId());

                    if (elasticClient.exists(index, type, id)) {
                        CacheEntry entry = new CacheEntry(true, index, type, id, cacheEntryContext);
                        cacheService.add(entry);
                        results.add(ValidationResult.ok(path, obj));
                    } else {
                        CacheEntry entry = new CacheEntry(false, index, type, id, cacheEntryContext);
                        cacheService.add(entry);
                        String details = String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, id);
                        results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE, details));
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else if (cacheEntry.isExists() == true) {
                results.add(ValidationResult.ok(path, obj));
            } else {
                String details = String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, id);
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE, details));
            }
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE));
        }

        return results;
    }

    /**
     * The first param (required) is the elasticsearch index. The second param (required) is the elasticsearch type. The third param
     * (optional) is the format used for composing the reference. This format must be: path[_path]*. For example, a reference to a document
     * is formed as pathToTheCaseId_pathToTheDocumentId
     * 
     * @param params
     *            the injected parameters
     * @return
     */
    @Override
    boolean checkParams(String... params) {
        if (params.length < 2 || params.length > 3) {
            return false;
        }
        try {
            if (EEsIndex.fromValue(params[0]) == null || EEsType.fromValue(params[1]) == null) {
                return false;
            } else {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
