package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.organisation;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignedBuc;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignedBucFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAssignedBucMapper extends AbstractMapToEntityMapper<MapHolder, AssignedBuc> {

    private final ProcessDefRepo processDefRepo;

    public MapToAssignedBucMapper(ProcessDefRepo processDefRepo) {
        this.processDefRepo = processDefRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, AssignedBuc b, MappingContext mappingContext) {
        mapProcessDefinition(a, b);
        mapApplicationRole(a, b);
        mapValidityStartDate(a, b);
        mapValidityEndDate(a, b);
        mapAudit(a, b);

        b.setIsEessiReady(a.bool(AssignedBucFields.IS_EESSI_READY));
    }

    private void mapAudit(final MapHolder a, final AssignedBuc b) {
        setDefaultAudit(b::setAudit);
    }

    private void mapValidityEndDate(final MapHolder a, final AssignedBuc b) {
        mapDate(a, AssignedBucFields.VALIDITY_END_DATE, b::setValidityEndDate);
    }

    private void mapValidityStartDate(final MapHolder a, final AssignedBuc b) {
        mapDate(a, AssignedBucFields.VALIDITY_START_DATE, b::setValidityStartDate);
    }

    private void mapProcessDefinition(final MapHolder a, final AssignedBuc b) {
        String processDefName = a.string(AssignedBucFields.BUC_TYPE);
        ProcessDef processDef = processDefRepo.findById(processDefName);
        if (processDef == null) {
            throw new EntityNotFoundEessiRuntimeException(ProcessDef.class, UniqueIdentifier.name, processDefName);
        }

        b.setProcessDef(processDef);
    }

    private void mapApplicationRole(final MapHolder a, final AssignedBuc b) {
        String applicationRole = a.string(AssignedBucFields.APPLICATION_ROLE);
        EApplicationRole eApplicationRole = EApplicationRole.lookup(applicationRole).orElseThrow(EnumNotFoundEessiRuntimeException::new);
        b.setApplicationRole(eApplicationRole);
    }
}
