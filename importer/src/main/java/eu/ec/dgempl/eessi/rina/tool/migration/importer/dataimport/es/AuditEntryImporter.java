package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditEvent;
import eu.ec.dgempl.eessi.rina.repo.AuditEventRepo;
import eu.ec.dgempl.eessi.rina.repo.AuditObjectRepo;
import eu.ec.dgempl.eessi.rina.repo.AuditParticipantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.AUDIT_AUDITENTRY)
public class AuditEntryImporter extends AbstractDataImporter {

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
    public void importData() {
        run(this::processAuditEntryData);
    }

    private void processAuditEntryData(final MapHolder doc) {
        AuditEvent auditEvent = beanMapper.map(doc, AuditEvent.class);
        auditEventRepo.saveAndFlush(auditEvent);

        saveInBulk(auditEvent::getAuditObjects, () -> auditObjectRepo);
        saveInBulk(auditEvent::getAuditParticipants, () -> auditParticipantRepo);
    }
}
