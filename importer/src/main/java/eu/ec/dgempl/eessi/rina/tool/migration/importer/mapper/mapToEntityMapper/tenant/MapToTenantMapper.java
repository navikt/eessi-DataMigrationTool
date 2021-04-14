package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.TenantFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToTenantMapper extends AbstractMapToEntityMapper<MapHolder, Tenant> {

    @Autowired
    private OrganisationService organisationService;

    @Override
    public void mapAtoB(MapHolder a, Tenant b, MappingContext context) {
        b.setId(a.string(TenantFields.ID));
        b.setEnabled(a.bool(TenantFields.ENABLED));
        b.setDefault(a.bool(TenantFields.IS_DEFAULT));

        mapOrganisation(a, b);
        mapLog(a, b);
    }

    private void mapLog(final MapHolder a, final Tenant b) {
        setDefaultLog(b::setLog);
    }

    private void mapOrganisation(final MapHolder a, final Tenant b) {
        Organisation organisation = organisationService.getOrSaveOrganisation(a.string(TenantFields.ID));
        b.setOrganisation(organisation);
    }
}
