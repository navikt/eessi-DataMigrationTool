package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.ArchivingVolume;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ArchivingVolumeFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToArchivingVolumeMapper extends AbstractMapToEntityMapper<MapHolder, ArchivingVolume> {

    @Override
    public void mapAtoB(MapHolder a, ArchivingVolume b, MappingContext context) {
        b.setArchivingVolumeId(a.string(ArchivingVolumeFields.ARCHIVING_VOLUME_ID));
        b.setArchivingVolume(a.string(ArchivingVolumeFields.ARCHIVING_VOLUME));
        b.setAchivingMinSpaceThreshold(a.integer(ArchivingVolumeFields.ACHIVING_MIN_SPACE_THRESHOLD));
        b.setPhysicalLocations(
                String.join(",", a.list(ArchivingVolumeFields.PHYSICAL_LOCATIONS, String.class, false))
        );
    }
}
