package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.adminNotificationType;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AdminNotificationTypeFields.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AdminNotificationType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAdminNotificationTypeMapper extends AbstractMapToEntityMapper<MapHolder, AdminNotificationType> {

    @Override
    public void mapAtoB(final MapHolder a, final AdminNotificationType b, final MappingContext context) {
        b.setIsBe(a.bool(BE));
        b.setIsForAdmin(a.bool(FOR_ADMIN));
        b.setIsForClerk(a.bool(FOR_CLERK));
        b.setGenerateNieEvent(a.bool(GENERATE_NIE_EVENT));
        b.setNotificationName(a.string(NOTIFICATION_TYPE));
        b.setNotificationPeriod(Long.valueOf(a.integer(NOTIFICATION_PERIOD)));
        b.setRetentionPeriod(Long.valueOf(a.integer(RETENTION_PERIOD)));
        b.setShowForAdmin(a.bool(SHOW_FOR_ADMIN));
        b.setShowForClerk(a.bool(SHOW_FOR_CLERK));
    }
}
