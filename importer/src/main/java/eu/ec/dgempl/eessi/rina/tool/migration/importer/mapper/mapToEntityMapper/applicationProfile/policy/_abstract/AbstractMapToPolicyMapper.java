package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile.policy._abstract;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESector;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Policy;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.repo.SectorRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.PolicyFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

public abstract class AbstractMapToPolicyMapper<T extends Policy> extends AbstractMapToEntityMapper<MapHolder, Policy> {

    private ProcessDefRepo processDefRepo;
    private SectorRepo sectorRepo;
    private TenantRepo tenantRepo;

    @Override
    public void mapAtoB(MapHolder a, Policy b, MappingContext context) {
        b.setId(a.string("id"));

        mapSector(a, b);
        mapProcessDef(a, b);
        mapApplicationRole(a, b);
        mapPolicy(a, b);
        //        b.setTenant(tenantRepo.findById());
    }

    private void mapPolicy(final MapHolder a, final Policy b) {
        Integer policy = a.integer(PolicyFields.POLICY);
        b.setPolicy(policy != null ? policy : 0);
    }

    private void mapProcessDef(final MapHolder a, final Policy b) {
        b.setProcessDef(processDefRepo.findById(a.string(PolicyFields.BUC_TYPE_ID)));
    }

    private void mapSector(final MapHolder a, final Policy b) {
        String sectorId = a.string(PolicyFields.SECTOR_ID);

        ESector eSector = Arrays.stream(ESector.values())
                .filter(sector -> sector.getCode().equalsIgnoreCase(sectorId)
                        || sector.getDisplayName().equalsIgnoreCase(sectorId)
                        || sector.name().equalsIgnoreCase(sectorId))
                .findFirst()
                .orElseThrow(EnumNotFoundEessiRuntimeException::new);

        b.setSector(sectorRepo.findByName(eSector));
    }

    private void mapApplicationRole(final MapHolder a, final Policy b) {
        String applicationRole = a.string(PolicyFields.APPLICATION_ROLE_ID);
        if (StringUtils.isNotBlank(applicationRole)) {
            EApplicationRole eApplicationRole = EApplicationRole.lookup(applicationRole)
                    .orElseThrow(EnumNotFoundEessiRuntimeException::new);
            b.setApplicationRole(eApplicationRole);
        }
    }

    @Autowired
    public void setProcessDefRepo(ProcessDefRepo processDefRepo) {
        this.processDefRepo = processDefRepo;
    }

    @Autowired
    public void setSectorRepo(SectorRepo sectorRepo) {
        this.sectorRepo = sectorRepo;
    }

    @Autowired
    public void setTenantRepo(TenantRepo tenantRepo) {
        this.tenantRepo = tenantRepo;
    }
}
