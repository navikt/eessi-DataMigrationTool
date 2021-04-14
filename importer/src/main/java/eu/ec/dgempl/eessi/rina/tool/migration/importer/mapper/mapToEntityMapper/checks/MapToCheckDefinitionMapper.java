package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.checks;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CheckDefinitionFields.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckDefinition;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToCheckDefinitionMapper extends AbstractMapToEntityMapper<MapHolder, CheckDefinition> {

    @Override
    public void mapAtoB(final MapHolder a, final CheckDefinition b, final MappingContext context) {
        b.setId(a.string(ID));
        b.setCheckCategory(a.string(CHECK_CATEGORY));
        b.setName(a.string(NAME));
        b.setDescription(a.string(DESCRIPTION));
        b.setIsValid(a.bool(VALID));

        String properties = "";
        //TODO: no mapping found for properties
        b.setProperties(properties);

    }
}