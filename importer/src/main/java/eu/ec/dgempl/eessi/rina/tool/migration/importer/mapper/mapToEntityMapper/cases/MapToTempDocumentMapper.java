package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentDirection;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempDocument;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToTempDocumentMapper extends AbstractMapToEntityMapper<MapHolder, TempDocument> {

    private static final Logger logger = LoggerFactory.getLogger(MapToTempDocumentMapper.class);

    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToTempDocumentMapper(final RinaCaseRepo rinaCaseRepo, final RinaJsonMapper rinaJsonMapper) {
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final TempDocument b, final MappingContext context) {
        b.setId(a.string(DocumentFields.ID));
        b.setStarter(a.bool(DocumentFields.STARTER, Boolean.FALSE));
        b.setType(a.string(DocumentFields.TYPE));
        b.setParentDocumentId(a.string(DocumentFields.PARENT_DOCUMENT_ID));

        mapCaseId(a, b);
        mapStatus(a, b);
        mapDirection(a, b);
        mapJson(a, b);
    }

    private void mapStatus(final MapHolder a, final TempDocument b) {
        String status = a.string(DocumentFields.STATUS);
        EDocumentStatus eDocumentStatus = EDocumentStatus.lookup(status).orElseThrow(
                () -> new DmtEnumNotFoundException(EDocumentStatus.class, a.addPath(DocumentFields.STATUS), status));

        b.setStatus(eDocumentStatus);
    }

    private void mapDirection(final MapHolder a, final TempDocument b) {
        String direction = a.string(DocumentFields.DIRECTION);
        EDocumentDirection eDocumentDirection = EDocumentDirection.lookup(direction).orElseThrow(
                () -> new DmtEnumNotFoundException(EDocumentDirection.class, a.addPath(DocumentFields.DIRECTION), direction));

        b.setDirection(eDocumentDirection);
    }

    private void mapCaseId(final MapHolder a, final TempDocument b) {
        String caseId = a.string(DocumentFields.CASE_ID);

        RinaCase rinaCase = findById(caseId, rinaCaseRepo::findById, RinaCase.class);

        b.setRinaCase(rinaCase);
    }

    private void mapJson(MapHolder a, TempDocument b) {
        try {
            b.setJson(rinaJsonMapper.mapToJson(a.getHolding()));
        } catch (JsonProcessingException e) {
            logger.error("Could not map JSON for temp document with id [{}] and caseId [{}]", b.getId(), b.getRinaCase().getId());
            throw new RuntimeException(
                    String.format("Could not map JSON for temp document with id [%s] and caseId [%s]", b.getId(), b.getRinaCase().getId()));
        }
    }
}
