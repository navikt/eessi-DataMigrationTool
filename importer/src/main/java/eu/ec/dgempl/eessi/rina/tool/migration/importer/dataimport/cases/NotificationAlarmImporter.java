package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.NotificationAlarm;
import eu.ec.dgempl.eessi.rina.repo.NotificationAlarmRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ElasticTypeImporter(type = EElasticType.NOTIFICATION_ALARM)
public class NotificationAlarmImporter extends AbstractDataImporter implements CaseImporter {

    private final NotificationAlarmRepo notificationAlarmRepo;

    public NotificationAlarmImporter(final NotificationAlarmRepo notificationAlarmRepo) {
        this.notificationAlarmRepo = notificationAlarmRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processNotificationData, caseId);
    }

    private void processNotificationData(final MapHolder doc) {
        NotificationAlarm notificationAlarm = beanMapper.map(doc, NotificationAlarm.class);
        notificationAlarmRepo.saveAndFlush(notificationAlarm);
    }
}


