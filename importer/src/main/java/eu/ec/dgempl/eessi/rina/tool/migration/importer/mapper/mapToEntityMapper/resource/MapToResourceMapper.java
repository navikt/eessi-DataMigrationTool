package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.resource;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ResourceFields.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Resource;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToResourceMapper extends AbstractMapToEntityMapper<MapHolder, Resource> {

    @Override
    public void mapAtoB(final MapHolder a, final Resource b, final MappingContext context) {
        b.setId(a.string(ID));
        b.setTag(a.string(TAG));
        b.setbVersion(a.string(VERSION));
        b.setStorageId(a.string(STORAGE_ID));

        mapDate(a, DATE, b::setDate);
        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final Resource b) {
        setDefaultAudit(b::setAudit);
    }
}
