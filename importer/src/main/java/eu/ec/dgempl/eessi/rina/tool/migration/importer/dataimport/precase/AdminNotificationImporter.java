package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AdminNotificationType;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.AdminNotificationTypeRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AdminNotificationTypeFields;

@Component
@ElasticTypeImporter(type = EElasticType.ADMIN_ADMINNOTIFICATIONTYPE)
public class AdminNotificationImporter extends AbstractDataImporter implements PreCaseImporter {

    private final AdminNotificationTypeRepoExtended adminNotificationTypeRepo;

    public AdminNotificationImporter(final AdminNotificationTypeRepoExtended adminNotificationTypeRepo) {
        this.adminNotificationTypeRepo = adminNotificationTypeRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processAdminNotificationTypeData);
    }

    private void processAdminNotificationTypeData(final MapHolder doc) {

        String notificationType = doc.string(AdminNotificationTypeFields.NOTIFICATION_TYPE);
        AdminNotificationType adminNotificationType = adminNotificationTypeRepo.findByNotificationNameIgnoreCase(notificationType);

        if (adminNotificationType == null) {
            throw new EntityNotFoundEessiRuntimeException(AdminNotificationType.class, UniqueIdentifier.name, notificationType);
        }

        beanMapper.map(doc, adminNotificationType);
        adminNotificationTypeRepo.saveAndFlush(adminNotificationType);
    }
}
