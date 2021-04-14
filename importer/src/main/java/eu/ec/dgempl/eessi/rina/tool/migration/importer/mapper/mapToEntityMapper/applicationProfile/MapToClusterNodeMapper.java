package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.ClusterNode;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.GlobalConfigurationApplicationProfileFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToClusterNodeMapper extends AbstractMapToEntityMapper<MapHolder, ClusterNode> {

    @Override
    public void mapAtoB(MapHolder a, ClusterNode b, MappingContext context) {
        b.setName(a.string(GlobalConfigurationApplicationProfileFields.NAME));
        b.setPmodePath(a.string(GlobalConfigurationApplicationProfileFields.PMODE_PATH));
    }
}
