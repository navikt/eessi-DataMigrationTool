package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.tenant;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ETenantConfigurationType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TenantParamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.TenantParamGroupFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToTenantParamGroupMapper extends AbstractMapToEntityMapper<MapHolder, TenantParamGroup> {

    private final TenantRepo tenantRepo;

    public MapToTenantParamGroupMapper(TenantRepo tenantRepo) {
        this.tenantRepo = tenantRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, TenantParamGroup b, MappingContext context) {
        mapTenant(a, b);
        b.setName(ETenantConfigurationType.MESSAGE_SETTINGS_PARAMETERS_MAPPING);
        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final TenantParamGroup b) {
        setDefaultAudit(b::setAudit);
    }

    private void mapTenant(final MapHolder a, final TenantParamGroup b) {
        String tenantId = (String) a.getDeep(TenantParamGroupFields.ORGANISATION_ID);
        Tenant tenant = tenantRepo.findById(tenantId);
        if (tenant == null) {
            throw new EntityNotFoundEessiRuntimeException(Tenant.class, UniqueIdentifier.id, tenantId);
        }

        b.setTenant(tenant);
    }
}
