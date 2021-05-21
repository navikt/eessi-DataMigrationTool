package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.subdocument;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.DateUtils.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.date.ZonedDateTimePeriod;
import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESubdocPrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Subdocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentPrefill;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.SubdocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSubdocumentMapper extends AbstractMapToEntityMapper<MapHolder, Subdocument> {

    private final DocumentRepo documentRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;
    private final UserService userService;

    public MapToSubdocumentMapper(final DocumentRepo documentRepo, final RinaCaseRepo rinaCaseRepo,
            final RinaJsonMapper rinaJsonMapper, final UserService userService) {
        this.documentRepo = documentRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Subdocument b, final MappingContext context) {
        String caseId = (String) context.getProperty("caseId");
        Long no = (Long) context.getProperty("no");

        mapRinaCase(caseId, b);
        mapParentDocument(a, b);

        b.setId(a.string(SubdocumentFields.ID));
        b.setNo(no);
        b.setBusinessReference(a.string(SubdocumentFields.REFERENCE_ID));

        mapValidationErrors(a, b);
        mapIsValid(a, b);
        mapPrefill(a, b);
        mapSearchMetadata(a, b);
        mapBVersions(a, b);
        mapAttachments(a, b);
        mapAudit(a, b);
    }

    private void mapRinaCase(final String caseId, final Subdocument b) {
        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);
        b.setRinaCase(rinaCase);
    }

    private void mapAttachments(final MapHolder a, final Subdocument b) {
        List<MapHolder> attachmentMapHolders = a.listToMapHolder(SubdocumentFields.ATTACHMENTS);
        if (CollectionUtils.isNotEmpty(attachmentMapHolders)) {
            attachmentMapHolders.stream().map(subdocument -> mapSubdocumentAttachment(subdocument, b)).forEach(b::addSubdocumentAttachment);
        }
    }

    private SubdocumentAttachment mapSubdocumentAttachment(final MapHolder a, final Subdocument b) {
        SubdocumentAttachment subdocumentAttachment = mapperFacade.map(a, SubdocumentAttachment.class);
        List<SubdocumentBversion> subdocumentBversions = b.getSubdocumentBversions();
        List<MapHolder> versions = a.listToMapHolder(SubdocumentFields.VERSIONS);
        if (CollectionUtils.isNotEmpty(versions)) {
            Map<ZonedDateTimePeriod, Integer> intervalPairs = getIntervalsMap(subdocumentBversions);

            versions.forEach(version -> {
                ZonedDateTime creationDate = version.date(SubdocumentFields.DATE);

                if (creationDate == null) {
                    throw new RuntimeException(String.format(
                            "Could not link subDocumentAttachment with id %s to the version of subDocument with id %s.",
                            subdocumentAttachment.getId(),
                            b.getId()));
                }

                int intervalIndex = getIntervalIndex(intervalPairs, creationDate);
                if (intervalIndex > -1) {
                    for (int idx = intervalIndex; idx < subdocumentBversions.size(); idx++) {
                        subdocumentBversions.get(idx).addSubdocumentAttachment(subdocumentAttachment);
                    }
                }
            });
        } else {
            subdocumentBversions.forEach(bversion -> bversion.addSubdocumentAttachment(subdocumentAttachment));
        }

        return subdocumentAttachment;
    }

    private void mapBVersions(final MapHolder a, final Subdocument b) {
        List<MapHolder> versions = a.listToMapHolder(SubdocumentFields.VERSIONS);
        if (CollectionUtils.isNotEmpty(versions)) {
            versions.stream().map(version -> mapperFacade.map(version,
                    SubdocumentBversion.class,
                    mctxb()
                            .addProp("rinaCase", b.getRinaCase())
                            .build()))
                    .forEach(b::addSubdocumentBversion);
        }
    }

    private void mapParentDocument(final MapHolder a, final Subdocument b) {
        String parentDocumentId = a.string(SubdocumentFields.PARENT_DOCUMENT_ID);

        Document document = documentRepo.findByIdAndRinaCase(parentDocumentId, b.getRinaCase());
        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(Document.class, UniqueIdentifier.id, parentDocumentId, UniqueIdentifier.caseId,
                    b.getRinaCase().getId());
        }

        b.setDocument(document);
    }

    private void mapAudit(final MapHolder a, final Subdocument b) {
        setDefaultAudit(b::setAudit);

        mapDate(a, "creationDate", b.getAudit()::setCreatedAt);
        mapDate(a, "lastUpdate", b.getAudit()::setUpdatedAt);

        String userId = a.string(SubdocumentFields.CREATOR_ID, true);
        String username = a.string(SubdocumentFields.CREATOR_NAME, true);
        // In this case we don't want to use the fallback of the default user
        IamUser iamUser = userService.resolveUser(userId, username, null, true);

        if (iamUser != null) {
            b.getAudit().setCreatedBy(iamUser.getId());
            b.getAudit().setUpdatedBy(iamUser.getId());
        } else {
            b.getAudit().setCreatedBy(b.getDocument().getAudit().getCreatedBy());
            b.getAudit().setUpdatedBy(b.getDocument().getAudit().getUpdatedBy());
        }
    }

    private void mapIsValid(final MapHolder a, final Subdocument b) {
        String valid = a.string(SubdocumentFields.VALIDATION_STATUS, true);
        if (StringUtils.isNotBlank(valid)) {
            b.setIsValid(valid.equalsIgnoreCase("valid"));
        }
    }

    private void mapValidationErrors(final MapHolder a, final Subdocument b) {
        List<MapHolder> validationMessages = a.listToMapHolder(SubdocumentFields.VALIDATION_MESSAGES, true);
        if (CollectionUtils.isNotEmpty(validationMessages)) {
            try {
                List<Map<String, Object>> messages = validationMessages.stream().map(MapHolder::getHolding).collect(Collectors.toList());
                b.setValidationErrors(rinaJsonMapper.listOfObjectToJson(messages));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not map validation messages", e);
            }
        }
    }

    private void mapPrefill(final MapHolder a, final Subdocument b) {
        addSubdocumentPrefills(a, "preFill", b, ESubdocPrefillGroup.PREFILL);
    }

    private void mapSearchMetadata(final MapHolder a, final Subdocument b) {
        addSubdocumentPrefills(a, "searchMetadata", b, ESubdocPrefillGroup.SEARCH_METADATA);
    }

    public static void addSubdocumentPrefills(MapHolder a, String prefillKey, Subdocument b, ESubdocPrefillGroup eSubdocPrefillGroup) {
        MapHolder prefills = a.getMapHolder(prefillKey);
        getPrefills(prefills, eSubdocPrefillGroup).forEach(b::addSubdocumentPrefill);
    }

    private static List<SubdocumentPrefill> getPrefills(final MapHolder prefill, final ESubdocPrefillGroup prefillGroup) {
        return prefill.fields().stream().filter(key -> prefill.get(key, true) != null).map(key -> {
            Object value = prefill.get(key, true);

            SubdocumentPrefill subdocumentPrefill = new SubdocumentPrefill();
            subdocumentPrefill.setKey(key);
            subdocumentPrefill.setValue(value != null ? String.valueOf(value) : null);
            subdocumentPrefill.setPrefillGroup(prefillGroup);
            return subdocumentPrefill;
        }).collect(Collectors.toList());
    }
}
