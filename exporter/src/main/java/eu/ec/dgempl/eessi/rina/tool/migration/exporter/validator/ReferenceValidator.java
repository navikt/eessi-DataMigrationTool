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
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.OrganisationLoaderService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.EsDocumentHelper;
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
    private final OrganisationLoaderService organisationLoaderService;
    private final Boolean ignoreInvalidReferencesPolicy;

    public ReferenceValidator(EsClientService elasticClient, CacheService cacheService, OrganisationLoaderService organisationLoaderService, Boolean ignoreInvalidReferencesPolicy,
            String... params) {
        super(params);
        this.elasticClient = elasticClient;
        this.cacheService = cacheService;
        this.organisationLoaderService = organisationLoaderService;
        this.ignoreInvalidReferencesPolicy = ignoreInvalidReferencesPolicy;
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
        if (status instanceof String && ((String) status).equalsIgnoreCase("empty")) {
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

            // add a special handler for user.id="System" and user.id="0" and user.id="-1" (a.k.a. system user)
            if (index.equalsIgnoreCase(EEsIndex.IDENTITY.value()) && type.equalsIgnoreCase(EEsType.USER.value())) {
                if (((String) obj).equalsIgnoreCase("system") || obj.equals("0") || obj.equals("-1")) {
                    logger.info("Validation skipped. SYSTEM user doesn't exist in ES. Context=[index={},type={},id={},path={},value={}]",
                            context.getDocument().getIndex(), context.getDocument().getType(), context.getDocument().getObjectId(), path,
                            obj);

                    // no need for validation
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }
            }

            // add a special handler for inactive organisations; check if they exist in the organisation csv
            // in the importer the organisation is fetched from the csv only if it does not exist in db
            // for the validator, it's faster to check first in the csv and avoid querying in ES
            if (index.equalsIgnoreCase(EEsIndex.ENTITIES.value()) && type.equalsIgnoreCase(EEsType.ORGANISATION.value())) {
                if (organisationLoaderService.getOrganisationFromCsvById((String) obj) != null) {
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

            // add a special handler for invalid policies references in configurations_assignmenttarget
            // if the 'ignore.invalid.references.policy' field in the application.properties file is true,
            // the importer will ignore the invalid policy references
            if (index.equalsIgnoreCase(EEsIndex.CONFIGURATIONS.value()) && type.equalsIgnoreCase(EEsType.ASSIGNMENTPOLICY.value())
                    && documentType.equalsIgnoreCase(EEsType.ASSIGNMENTTARGET.value())) {
                if (normalisedPath.equalsIgnoreCase("policies") && ignoreInvalidReferencesPolicy != null && ignoreInvalidReferencesPolicy) {
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }
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

                        if (EEsIndex.CASES.value().equalsIgnoreCase(context.getDocument().getIndex())
                                && EEsType.TASKMETADATA.value().equalsIgnoreCase(documentType)) {
                            if (!isTaskMetadataInvalidReferenceException(obj, index, type, document)) {
                                String details = String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, id);
                                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE, details));
                            } else {
                                results.add(ValidationResult.ok(path, obj));
                            }
                        } else {
                            String details = String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, id);
                            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE, details));
                        }
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
        } else if (obj instanceof Integer) {
            // add a special handler for user.id=0 and user.id=-1 (a.k.a. system user)
            if (index.equalsIgnoreCase(EEsIndex.IDENTITY.value()) && type.equalsIgnoreCase(EEsType.USER.value())) {
                int val = ((Integer) obj).intValue();
                if (val == 0 || val == -1) {
                    logger.info("Validation skipped. SYSTEM user doesn't exist in ES. Context=[index={},type={},id={},path={},value={}]",
                            context.getDocument().getIndex(), context.getDocument().getType(), context.getDocument().getObjectId(), path,
                            obj);

                    // no need for validation
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }
            }
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_REFERENCE));
        }

        return results;
    }

    private boolean isTaskMetadataInvalidReferenceException(final Object obj, final String index, final String type,
            final Map<String, Object> document) {
        if (EEsIndex.CASES.value().equalsIgnoreCase(index) && EEsType.DOCUMENT.value().equalsIgnoreCase(type)) {
            try {
                // get object from elasticsearch
                Map<String, Object> _obj = elasticClient.get(index, EEsType.CASEMETADATA.value(), (String) document.get("caseId"));
                if (_obj != null) {

                    String role = "";
                    Object _processDefinitionName = _obj.get(CaseFields.PROCESS_DEFINITION_NAME);
                    if (_processDefinitionName instanceof String && ((String) _processDefinitionName).length() > 2) {
                        role = ((String) _processDefinitionName).substring(0, 2);
                    }

                    Map<String, Object> params = Map.of(EsDocumentHelper.APPLICATION_ROLE, role,
                            EsDocumentHelper.IS_MULTI_STARTER, _obj.get(CaseFields.IS_MULTI_STARTER),
                            EsDocumentHelper.STARTER_TYPE, _obj.get(CaseFields.STARTER_TYPE),
                            EsDocumentHelper.IS_STARTER_SENT, _obj.get(CaseFields.IS_STARTER_SENT),
                            EsDocumentHelper.DOCTYPE, document.get("documentType"));

                    if (EsDocumentHelper.isTaskMetadataInvalidReferenceException(params)) {
                        logger.info(String.format(
                                "Invalid document reference %s in taskmetadata in document with id %s. This document belongs to a multi-starter case and is a draft that should have been previously removed by Bonita. Ignoring document reference.",
                                obj, document.get("id")));
                        return true;
                    }
                }
            } catch (Exception ex) {
                logger.info(String.format(
                        "Error trying to get document from type %s and index %s for case with id %s to check if this invalid reference document should be ignored in case it belongs to a multi-starter case and is a draft that should have been previously removed by Bonita",
                        index, EEsType.CASEMETADATA.value(), document.get("caseId")));
            }
        }
        return false;
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
