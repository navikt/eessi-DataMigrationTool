package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.common.BucProcessDefinition;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAction;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempDocument;
import eu.ec.dgempl.eessi.rina.repo.AssignmentRepo;
import eu.ec.dgempl.eessi.rina.repo.CaseParticipantRepo;
import eu.ec.dgempl.eessi.rina.repo.CasePrefillRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.repo.TempActionRepo;
import eu.ec.dgempl.eessi.rina.repo.TempAttachmentRepo;
import eu.ec.dgempl.eessi.rina.repo.TempDocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.service.tx.util.ProcessDefUtil;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.AttachmentsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.BeanMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToAssignmentMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToRinaCaseMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToTempActionMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToTempAttachmentMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToTempDocumentMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.spring.config.ApplicationContextConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationContextConfiguration.class)
public class CaseStructuredMetadataImporterTest {

    @Autowired
    ApplicationContext context;

    @Mock
    private AssignmentRepo assignmentRepo;

    @Mock
    private CaseParticipantRepo caseParticipantRepo;

    @Mock
    private CasePrefillRepo casePrefillRepo;

    @Mock
    private DocumentTypeRepo documentTypeRepo;

    @Mock
    private IamUserRepo iamUserRepo;

    @Mock
    private IamGroupRepo iamGroupRepo;

    @Mock
    private ProcessDefVersionRepo processDefVersionRepo;

    @Mock
    private RinaCaseRepo rinaCaseRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private TempActionRepo tempActionRepo;

    @Mock
    private TempAttachmentRepo tempAttachmentRepo;

    @Mock
    private TempDocumentRepo tempDocumentRepo;

    @Mock
    private TenantRepo tenantRepo;

    @Mock
    private DefaultValuesService defaultsService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private UserService userService;

    @Mock
    private AttachmentsHelper attachmentsHelper;

    private CaseStructuredMetadataImporter caseStructuredMetadataImporter;

    @Before
    public void setUp() {
        caseStructuredMetadataImporter = new CaseStructuredMetadataImporter(
                assignmentRepo,
                casePrefillRepo,
                caseParticipantRepo,
                rinaCaseRepo,
                tempActionRepo,
                tempAttachmentRepo,
                tempDocumentRepo);

        BeanMapper beanMapper = new BeanMapper();
        beanMapper.setApplicationContext(this.context);

        MapToRinaCaseMapper mapToRinaCaseMapper = new MapToRinaCaseMapper(
                documentTypeRepo,
                processDefVersionRepo,
                tenantRepo,
                defaultsService,
                organisationService,
                userService);

        beanMapper.addMapper(mapToRinaCaseMapper);

        RinaJsonMapper rinaJsonMapper = new RinaJsonMapper(new ObjectMapper());

        MapToTempActionMapper mapToTempActionMapper = new MapToTempActionMapper(rinaCaseRepo, rinaJsonMapper);
        beanMapper.addMapper(mapToTempActionMapper);

        MapToTempAttachmentMapper mapToTempAttachmentMapper = new MapToTempAttachmentMapper(
                attachmentsHelper,
                rinaCaseRepo,
                rinaJsonMapper);
        beanMapper.addMapper(mapToTempAttachmentMapper);

        MapToTempDocumentMapper mapToTempDocumentMapper = new MapToTempDocumentMapper(rinaCaseRepo, rinaJsonMapper);
        beanMapper.addMapper(mapToTempDocumentMapper);

        MapToAssignmentMapper mapToAssignmentMapper = new MapToAssignmentMapper(iamGroupRepo, iamUserRepo, roleRepo);
        beanMapper.addMapper(mapToAssignmentMapper);

        caseStructuredMetadataImporter.setBeanMapper(beanMapper);
    }

    @Test
    public void testMapCaseStructuredMetadata() throws Exception {

        MapHolder caseStructuredMetadataHolder = loadFromResource(
                this.getClass().getClassLoader(),
                "documents/cases/casestructuredmetadata.json");

        String caseId = caseStructuredMetadataHolder.string(CaseFields.ID);
        RinaCase rinaCase = createRinaCase(caseId);
        when(rinaCaseRepo.saveAndFlush(any())).thenReturn(rinaCase);
        when(rinaCaseRepo.findById(caseId)).thenReturn(rinaCase);

        String processDefName = caseStructuredMetadataHolder.string(PROCESS_DEFINITION_NAME);
        BucProcessDefinition bucProcessDefinition = ProcessDefUtil.retrieveProcessDefinitionInfo(processDefName);
        String processDefinitionName = bucProcessDefinition.getProcessDefinitionName();

        when(processDefVersionRepo.findByProcessDefIdAndBusinessVersion(processDefinitionName, bucProcessDefinition.getVersion()))
                .thenReturn(createRandomProcessDefVersion());

        when(organisationService.getOrSaveOrganisation(any())).thenReturn(createRandomOrg());

        String tenantId = caseStructuredMetadataHolder.string(WHOAMI_ID, true);
        when(tenantRepo.findById(tenantId)).thenReturn(createRandomTenant());

        when(defaultsService.getDefaultValue(any())).thenReturn("1");

        String creatorId = caseStructuredMetadataHolder.string("creator.id", true);
        when(userService.resolveUser(eq(creatorId), any(), any())).thenReturn(createRandomIamUser());

        when(roleRepo.findByName(any())).thenReturn(createRandomRole());
        when(iamGroupRepo.findBySimpleNaturalId(any())).thenReturn(createRandomIamGroup());
        when(attachmentsHelper.getAttachmentPathname(any(), any())).thenReturn("defaultFileName.json");

        List<TempAction> tempActions = Whitebox.invokeMethod(
                caseStructuredMetadataImporter,
                "getTempDocuments",
                caseStructuredMetadataHolder,
                ACTIONS,
                TempAction.class);

        assertNotNull(tempActions);
        assertEquals(1, tempActions.size());

        List<TempAttachment> tempAttachments = Whitebox.invokeMethod(
                caseStructuredMetadataImporter,
                "getTempDocuments",
                caseStructuredMetadataHolder,
                ATTACHMENTS,
                TempAttachment.class);

        assertNotNull(tempAttachments);
        assertEquals(2, tempAttachments.size());
        assertTrue(tempAttachments.stream().anyMatch(tempAttachment -> tempAttachment.getRinaCase() == null));

        List<TempDocument> tempDocuments = Whitebox.invokeMethod(
                caseStructuredMetadataImporter,
                "getTempDocuments",
                caseStructuredMetadataHolder,
                DOCUMENTS,
                TempDocument.class);

        assertNotNull(tempDocuments);
        assertEquals(1, tempDocuments.size());

        Whitebox.invokeMethod(caseStructuredMetadataImporter, "processCaseStructuredMetadata", caseStructuredMetadataHolder);

        verify(tempActionRepo, times(1)).saveAll(tempActions);
        verify(tempDocumentRepo, times(1)).saveAll(tempDocuments);

    }
}