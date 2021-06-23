package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EActionStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAction;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ActionFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToTempActionMapper extends AbstractMapToEntityMapper<MapHolder, TempAction> {

    private static final Logger logger = LoggerFactory.getLogger(MapToTempActionMapper.class);

    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToTempActionMapper(final RinaCaseRepo rinaCaseRepo, final RinaJsonMapper rinaJsonMapper) {
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final TempAction b, final MappingContext context) {
        b.setId(a.string(ActionFields.ID));
        b.setName(a.string(ActionFields.NAME));
        b.setDisplayName(a.string(ActionFields.DISPLAY_NAME));
        b.setDocumentType(a.string(ActionFields.DOCUMENT_TYPE));
        b.setDocumentId(a.string(ActionFields.DOCUMENT_ID));
        b.setParentDocumentId(a.string(ActionFields.PARENT_DOCUMENT_ID));

        mapRinaCase(a, b);
        mapStatus(a, b);
        mapJson(a, b);
    }

    private void mapRinaCase(final MapHolder a, final TempAction b) {
        String caseId = a.string(ActionFields.CASE_ID);

        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById);
        if (rinaCase != null) {
            b.setRinaCase(rinaCase);
        }
    }

    private void mapStatus(final MapHolder a, final TempAction b) {
        String status = a.string(ActionFields.STATUS);
        EActionStatus eActionStatus = EActionStatus.fromString(status);
        b.setStatus(eActionStatus);
    }

    private void mapJson(MapHolder a, TempAction b) {
        try {
            b.setJson(rinaJsonMapper.mapToJson(a.getHolding()));
        } catch (JsonProcessingException e) {
            logger.error("Could not map JSON for temp action with id [{}] and caseId [{}]", b.getId(), b.getRinaCase().getId());
            throw new RuntimeException(
                    String.format("Could not map JSON for temp action with id [%s] and caseId [%s]", b.getId(), b.getRinaCase().getId()));
        }
    }
}
