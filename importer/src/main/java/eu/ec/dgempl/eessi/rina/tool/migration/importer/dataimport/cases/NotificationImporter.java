package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Notification;
import eu.ec.dgempl.eessi.rina.repo.AssignmentRequestRepo;
import eu.ec.dgempl.eessi.rina.repo.NotificationRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

@Component
@ElasticTypeImporter(type = EElasticType.NOTIFICATION)
public class NotificationImporter extends AbstractDataImporter implements CaseImporter {

    private final NotificationRepo notificationRepo;
    private final AssignmentRequestRepo assignmentRequestRepo;

    public NotificationImporter(final NotificationRepo notificationRepo, final AssignmentRequestRepo assignmentRequestRepo) {
        this.notificationRepo = notificationRepo;
        this.assignmentRequestRepo = assignmentRequestRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processNotificationData, caseId);
    }

    public DocumentsReport importDataWithoutTransaction(final String caseId) {
        return run(this::processNotificationData, caseId, true);
    }

    private void processNotificationData(final MapHolder doc) {
        Notification notification = beanMapper.map(doc, Notification.class);
        notificationRepo.saveAndFlush(notification);

        saveInBulk(notification::getAssignmentRequests, () -> assignmentRequestRepo);
    }
}