package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditEvent;
import eu.ec.dgempl.eessi.rina.repo.AuditEventRepo;
import eu.ec.dgempl.eessi.rina.repo.AuditObjectRepo;
import eu.ec.dgempl.eessi.rina.repo.AuditParticipantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AuditEventFields;

@Component
@ElasticTypeImporter(type = EElasticType.AUDIT_AUDITENTRY)
public class AuditEntryImporter extends AbstractDataImporter implements PreCaseImporter {

    private static final Logger logger = LoggerFactory.getLogger(AuditEntryImporter.class);

    private final AuditEventRepo auditEventRepo;
    private final AuditObjectRepo auditObjectRepo;
    private final AuditParticipantRepo auditParticipantRepo;

    public AuditEntryImporter(final AuditEventRepo auditEventRepo, final AuditObjectRepo auditObjectRepo,
            final AuditParticipantRepo auditParticipantRepo) {
        this.auditEventRepo = auditEventRepo;
        this.auditObjectRepo = auditObjectRepo;
        this.auditParticipantRepo = auditParticipantRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processAuditEntryData);
    }

    private void processAuditEntryData(final MapHolder doc) {
        MapHolder message = doc.getMapHolder(AuditEventFields.MESSAGE);
        if (message == null || MapUtils.isEmpty(message.getHolding())) {
            logger.error("Could not map audit event with id [{}]: no message found!", doc.string("_id"));
            throw new RuntimeException(String.format("Could not map audit event with id [%s]: no message found!", doc.string("_id")));
        }

        AuditEvent auditEvent = beanMapper.map(doc, AuditEvent.class);
        auditEventRepo.saveAndFlush(auditEvent);

        saveInBulk(auditEvent::getAuditObjects, () -> auditObjectRepo);
        saveInBulk(auditEvent::getAuditParticipants, () -> auditParticipantRepo);
    }
}
