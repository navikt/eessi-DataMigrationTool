package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.NotificationAlarm;
import eu.ec.dgempl.eessi.rina.repo.NotificationAlarmRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import org.springframework.stereotype.Component;

@Component
@ElasticTypeImporter(type = EElasticType.NOTIFICATION_ALARM)
public class NotificationAlarmImporter extends AbstractDataImporter {

    private final NotificationAlarmRepo notificationAlarmRepo;

    public NotificationAlarmImporter(final NotificationAlarmRepo notificationAlarmRepo) {
        this.notificationAlarmRepo = notificationAlarmRepo;
    }

    @Override
    public void importData() {
        run(this::processNotificationData);
    }

    private void processNotificationData(final MapHolder doc) {
        NotificationAlarm notificationAlarm = beanMapper.map(doc, NotificationAlarm.class);
        notificationAlarmRepo.saveAndFlush(notificationAlarm);
    }
}


