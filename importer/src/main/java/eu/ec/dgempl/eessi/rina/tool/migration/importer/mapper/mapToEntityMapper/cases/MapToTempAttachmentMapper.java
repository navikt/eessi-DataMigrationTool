package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAttachment;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseAttachmentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.AttachmentsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToTempAttachmentMapper extends AbstractMapToEntityMapper<MapHolder, TempAttachment> {

    private static final Logger logger = LoggerFactory.getLogger(MapToTempAttachmentMapper.class);

    private final AttachmentsHelper attachmentsHelper;
    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToTempAttachmentMapper(
            final AttachmentsHelper attachmentsHelper,
            final RinaCaseRepo rinaCaseRepo,
            final RinaJsonMapper rinaJsonMapper) {
        this.attachmentsHelper = attachmentsHelper;
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final TempAttachment b, final MappingContext context) {
        b.setId(a.string(CaseAttachmentFields.ID));
        b.setCreatedBy(a.string(CaseAttachmentFields.CREATOR_ID));
        b.setName(a.string(CaseAttachmentFields.NAME));
        b.setFilename(a.string(CaseAttachmentFields.FILE_NAME));

        mapRinaCase(a, b);
        mapPathname(a, b);
        mapJson(a, b);
    }

    private void mapRinaCase(final MapHolder a, final TempAttachment b) {
        String caseId = a.string(CaseAttachmentFields.CASE_ID);

        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById);
        if (rinaCase != null) {
            b.setRinaCase(rinaCase);
        }
    }

    private void mapPathname(final MapHolder a, final TempAttachment b) {
        String caseId = a.string(CaseAttachmentFields.CASE_ID);
        String attachmentId = a.string(CaseAttachmentFields.ID);
        b.setPathname(attachmentsHelper.getAttachmentPathname(caseId, attachmentId));
    }

    private void mapJson(MapHolder a, TempAttachment b) {
        try {
            b.setJson(rinaJsonMapper.mapToJson(a.getHolding()));
        } catch (JsonProcessingException e) {
            logger.error("Could not map JSON for temp attachment with id [{}] and caseId [{}]", b.getId(), b.getRinaCase().getId());
            throw new RuntimeException(String.format(
                    "Could not map JSON for temp attachment with id [%s] and caseId [%s]",
                    b.getId(),
                    b.getRinaCase().getId()));
        }
    }
}
