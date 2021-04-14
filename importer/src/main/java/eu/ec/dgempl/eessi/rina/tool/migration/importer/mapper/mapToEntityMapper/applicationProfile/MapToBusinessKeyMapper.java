package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.BusinessKey;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.GlobalConfigurationApplicationProfileFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToBusinessKeyMapper extends AbstractMapToEntityMapper<MapHolder, BusinessKey> {

    @Override
    public void mapAtoB(MapHolder a, BusinessKey b, MappingContext context) {
        b.setAlias(a.string(GlobalConfigurationApplicationProfileFields.CERTIFICATE_ALIAS));
        b.setPassword(a.string(GlobalConfigurationApplicationProfileFields.CERTIFICATE_ALIAS_PASSWORD));
    }
}
