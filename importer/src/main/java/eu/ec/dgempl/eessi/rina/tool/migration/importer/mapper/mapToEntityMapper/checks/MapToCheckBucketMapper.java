package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.checks;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CheckBucketFields.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckBucket;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToCheckBucketMapper extends AbstractMapToEntityMapper<MapHolder, CheckBucket> {

    @Override
    public void mapAtoB(final MapHolder a, final CheckBucket b, final MappingContext context) {
        b.setId(a.string(ID));

        mapDate(a, START_DATE, b::setStartDate);
    }
}