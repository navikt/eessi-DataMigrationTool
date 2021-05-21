package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
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
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESector;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamOrigin;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDefVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Role;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Sector;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAction;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempDocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
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
import eu.ec.dgempl.eessi.rina.repo.TempDocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.service.tx.util.ProcessDefUtil;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.BeanMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToAssignmentMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToRinaCaseMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToTempActionMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToTempDocumentMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.spring.config.ApplicationContextConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationContextConfiguration.class)
public class CaseStructuredMetadataImporterTest {

    private static final int RANDOM_STRING_SIZE = 255;
    private static final String NULL_UTF8_CHAR = "\u0000";
    private static final String SPACE = "";

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
    private TempDocumentRepo tempDocumentRepo;

    @Mock
    private TenantRepo tenantRepo;

    @Mock
    private DefaultValuesService defaultsService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;
    private CaseStructuredMetadataImporter caseStructuredMetadataImporter;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();

        caseStructuredMetadataImporter = new CaseStructuredMetadataImporter(
                assignmentRepo,
                casePrefillRepo,
                caseParticipantRepo,
                rinaCaseRepo,
                tempActionRepo,
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

        RinaJsonMapper rinaJsonMapper = new RinaJsonMapper(objectMapper);

        MapToTempActionMapper mapToTempActionMapper = new MapToTempActionMapper(rinaCaseRepo, rinaJsonMapper);
        beanMapper.addMapper(mapToTempActionMapper);

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
        creatorId = "system".equalsIgnoreCase(creatorId) ? "0" : creatorId;

        when(iamUserRepo.findById(creatorId)).thenReturn(createRandomIamUser());

        when(roleRepo.findByName(any())).thenReturn(createRandomRole());

        when(iamGroupRepo.findBySimpleNaturalId(any())).thenReturn(createRandomIamGroup());

        List<TempAction> tempActions = Whitebox.invokeMethod(
                caseStructuredMetadataImporter,
                "getTempDocuments",
                caseStructuredMetadataHolder,
                CaseFields.ACTIONS,
                TempAction.class);

        assertNotNull(tempActions);
        assertEquals(1, tempActions.size());

        List<TempDocument> tempDocuments = Whitebox.invokeMethod(
                caseStructuredMetadataImporter,
                "getTempDocuments",
                caseStructuredMetadataHolder,
                CaseFields.DOCUMENTS,
                TempDocument.class);

        assertNotNull(tempDocuments);
        assertEquals(1, tempDocuments.size());

        Whitebox.invokeMethod(caseStructuredMetadataImporter, "processCaseStructuredMetadata", caseStructuredMetadataHolder);

        verify(tempActionRepo, times(1)).saveAll(tempActions);
        verify(tempDocumentRepo, times(1)).saveAll(tempDocuments);

    }

    private RinaCase createRinaCase(String caseId) {
        ProcessDefVersion processDefVersion = createRandomProcessDefVersion();
        Tenant tenant = createRandomTenant();

        return new RinaCase(tenant, processDefVersion, caseId);
    }

    private Organisation createRandomOrg() {
        return new Organisation(randomString(), ECountryCode.BE);
    }

    private Tenant createRandomTenant() {
        return new Tenant(createRandomOrg(), randomString());
    }

    private Sector createRandomSector() {
        return new Sector(ESector.PENSION);
    }

    private ProcessDef createRandomProcessDef() {
        return new ProcessDef(randomString(), createRandomSector(), randomString());
    }

    private ProcessDefVersion createRandomProcessDefVersion() {
        return new ProcessDefVersion(createRandomProcessDef(), randomString());
    }

    private IamUser createRandomIamUser() {
        return new IamUser(createRandomTenant(), createRandomIamOrigin(), randomString(), randomString());
    }

    private IamGroup createRandomIamGroup() {
        return new IamGroup(createRandomTenant(), randomString(), randomString());
    }

    private IamOrigin createRandomIamOrigin() {
        return new IamOrigin(randomString());
    }

    private Role createRandomRole() {
        return new Role(ERole.SUPERVISOR);
    }

    private static String randomString() {
        final String s = RandomStringUtils.random(RANDOM_STRING_SIZE);
        return s.replaceAll(NULL_UTF8_CHAR, SPACE);
    }

}