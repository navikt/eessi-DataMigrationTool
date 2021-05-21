package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ec.dgempl.eessi.rina.repo.AuditEventRepo;
import eu.ec.dgempl.eessi.rina.repo.AuditObjectRepo;
import eu.ec.dgempl.eessi.rina.repo.AuditParticipantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.BeanMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.audit.MapToAuditEventMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.spring.config.ApplicationContextConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationContextConfiguration.class)
public class AuditEntryImporterTest {

    @Autowired
    private ApplicationContext context;

    @Mock
    private AuditEventRepo auditEventRepo;
    @Mock
    private AuditObjectRepo auditObjectRepo;
    @Mock
    private AuditParticipantRepo auditParticipantRepo;

    private AuditEntryImporter auditEntryImporter;

    @Before
    public void setUp() {
        auditEntryImporter = new AuditEntryImporter(auditEventRepo, auditObjectRepo, auditParticipantRepo);
        BeanMapper beanMapper = new BeanMapper();
        beanMapper.setApplicationContext(this.context);
        MapToAuditEventMapper mapToAuditEventMapper = new MapToAuditEventMapper();
        beanMapper.addMapper(mapToAuditEventMapper);
        auditEntryImporter.setBeanMapper(beanMapper);
    }

    @Test
    public void testImportEmptyAudit() throws Exception {

        MapHolder auditEntryHolder = loadFromResource(this.getClass().getClassLoader(), "documents/audit/audit_entry_empty.json");

        Whitebox.invokeMethod(auditEntryImporter, "processAuditEntryData", auditEntryHolder);

        verify(auditEventRepo, never()).saveAndFlush(any());
    }

    public void testImportAudit() {

    }

}