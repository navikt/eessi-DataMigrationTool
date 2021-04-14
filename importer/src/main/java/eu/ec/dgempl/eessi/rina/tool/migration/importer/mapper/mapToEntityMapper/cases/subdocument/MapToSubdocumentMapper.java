package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.subdocument;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESubdocPrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.*;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSubdocumentMapper extends AbstractMapToEntityMapper<MapHolder, Subdocument> {

    private final DocumentRepo documentRepo;
    private final IamUserRepo iamUserRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToSubdocumentMapper(final DocumentRepo documentRepo, final IamUserRepo iamUserRepo, final RinaCaseRepo rinaCaseRepo,
            final RinaJsonMapper rinaJsonMapper) {
        this.documentRepo = documentRepo;
        this.iamUserRepo = iamUserRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Subdocument b, final MappingContext context) {
        String caseId = (String) context.getProperty("caseId");
        Long no = (Long) context.getProperty("no");

        mapRinaCase(caseId, b);
        mapParentDocument(a, b);

        b.setId(a.string("id"));
        b.setNo(no);
        b.setBusinessReference(a.string("referenceId"));

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
        List<MapHolder> attachmentMapHolders = a.listToMapHolder("attachments");
        if (CollectionUtils.isNotEmpty(attachmentMapHolders)) {
            attachmentMapHolders.stream().map(subdocument -> mapSubdocumentAttachment(subdocument, b)).forEach(b::addSubdocumentAttachment);
        }
    }

    private SubdocumentAttachment mapSubdocumentAttachment(final MapHolder a, final Subdocument b) {
        SubdocumentAttachment subdocumentAttachment = mapperFacade.map(a, SubdocumentAttachment.class);
        List<MapHolder> versions = a.listToMapHolder("versions");
        if (CollectionUtils.isNotEmpty(versions)) {
            versions.stream().map(version -> Integer.parseInt(version.string("id")))
                    .map(versionId -> b.getSubdocumentBversions().stream().filter(bversion -> bversion.getId() == versionId).findAny())
                    .filter(Optional::isPresent).map(Optional::get)
                    .forEach(bversion -> bversion.addSubdocumentAttachment(subdocumentAttachment));
        }
        return subdocumentAttachment;
    }

    private void mapBVersions(final MapHolder a, final Subdocument b) {
        List<MapHolder> versions = a.listToMapHolder("versions");
        if (CollectionUtils.isNotEmpty(versions)) {
            versions.stream().map(version -> mapperFacade.map(version, SubdocumentBversion.class))
                    .peek(bversion -> bversion.setSubdocument(b)).forEach(b::addSubdocumentBversion);
        }
    }

    private void mapParentDocument(final MapHolder a, final Subdocument b) {
        String parentDocumentId = a.string("parentDocumentId");

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

        String userId = a.string("creator.id", true);
        IamUser iamUser = RepositoryUtils.findById(userId, iamUserRepo::findById);

        if (iamUser != null) {
            b.getAudit().setCreatedBy(iamUser.getId());
            b.getAudit().setUpdatedBy(iamUser.getId());
        } else {
            b.getAudit().setCreatedBy(b.getDocument().getAudit().getCreatedBy());
            b.getAudit().setUpdatedBy(b.getDocument().getAudit().getUpdatedBy());
        }

    }

    private void mapIsValid(final MapHolder a, final Subdocument b) {
        String valid = a.string("validation.status", true);
        if (StringUtils.isNotBlank(valid)) {
            b.setIsValid(valid.equalsIgnoreCase("valid"));
        }
    }

    private void mapValidationErrors(final MapHolder a, final Subdocument b) {
        List<MapHolder> validationMessages = a.listToMapHolder("validation.messages", true);
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
