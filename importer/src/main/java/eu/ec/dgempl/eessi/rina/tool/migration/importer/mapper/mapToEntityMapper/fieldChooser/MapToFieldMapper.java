package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.fieldChooser;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EFieldSort;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Field;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.FieldFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToFieldMapper extends AbstractMapToEntityMapper<MapHolder, Field> {

    @Override
    public void mapAtoB(MapHolder a, Field b, MappingContext context) {
        b.setId(a.string(FieldFields.ID));
        b.setName(a.string(FieldFields.NAME));
        b.setShow(a.bool(FieldFields.SHOW));
        b.setSort(EFieldSort.valueOf(a.string(FieldFields.SORT)));
        b.setType(a.string(FieldFields.TYPE));
    }
}
