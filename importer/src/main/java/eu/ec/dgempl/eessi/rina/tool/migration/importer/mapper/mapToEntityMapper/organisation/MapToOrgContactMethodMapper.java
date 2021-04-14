package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.organisation;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.OrgContactMethod;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToOrgContactMethodMapper extends AbstractMapToEntityMapper<MapHolder, OrgContactMethod> {

    @Override
    public void mapAtoB(MapHolder a, OrgContactMethod b, MappingContext mappingContext) {
        // TODO there is no mapping available in ES for OrgContactMethod
    }
}
