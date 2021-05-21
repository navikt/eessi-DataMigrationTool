package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.repo.DocumentAttachmentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentConversationRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.UserMessageRepo;
import eu.ec.dgempl.eessi.rina.repo.UserMessageResponseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases.DocumentImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.BeanMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document.MapToDocumentBversionMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.spring.config.ApplicationContextConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationContextConfiguration.class)
public class DocumentVersionsMapperTest {

    @Autowired
    ApplicationContext context;
    @Mock
    private DocumentRepo documentRepo;
    @Mock
    private DocumentAttachmentRepo documentAttachmentRepo;
    @Mock
    private DocumentBversionRepo documentBversionRepo;
    @Mock
    private DocumentConversationRepo documentConversationRepo;
    @Mock
    private UserMessageResponseRepo userMessageResponseRepo;
    @Mock
    private UserMessageRepo userMessageRepo;
    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;
    private DocumentImporter documentImporter;
    private String methodName;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        methodName = "mapDocumentBversions";

        documentImporter = new DocumentImporter(documentRepo, documentAttachmentRepo, documentBversionRepo,
                documentConversationRepo, userMessageResponseRepo, userMessageRepo);
        BeanMapper beanMapper = new BeanMapper();
        beanMapper.setApplicationContext(this.context);
        MapToDocumentBversionMapper mapToDocumentBversionMapper = new MapToDocumentBversionMapper(userService);
        beanMapper.addMapper(mapToDocumentBversionMapper);
        documentImporter.setBeanMapper(beanMapper);
    }

    @Test
    public void testFilterDistinctVersions_allDifferent() throws IOException {

        // Prepare data

        File conversation1File = new File(
                DocumentVersionsMapperTest.class.getClassLoader().getResource("documents/document_versions_1.json").getFile());
        Map<String, Object> conversation1 = objectMapper.readValue(conversation1File, Map.class);
        MapHolder holder1 = new MapHolder(conversation1, new ConcurrentHashMap<>(), "");

        List<Map<String, Object>> versions = (List<Map<String, Object>>) holder1.getHolding().get("versions");

        setupMockUser(versions, 0);
        setupMockUser(versions, 1);

        Document document = new Document();
        document.setAudit(new Audit());

        // Call method

        try {
            Whitebox.invokeMethod(documentImporter, methodName, holder1, document);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertEquals(2, versions.size());
        Assert.assertEquals(2, document.getDocumentBversions().size());
    }

    @Test
    public void testFilterDistinctVersions_remove1() throws IOException {

        // Prepare data

        File conversation1File = new File(
                DocumentVersionsMapperTest.class.getClassLoader().getResource("documents/document_versions_2.json").getFile());
        Map<String, Object> conversation1 = objectMapper.readValue(conversation1File, Map.class);
        MapHolder holder1 = new MapHolder(conversation1, new ConcurrentHashMap<>(), "");

        List<Map<String, Object>> versions = (List<Map<String, Object>>) holder1.getHolding().get("versions");

        setupMockUser(versions, 0);
        setupMockUser(versions, 1);
        setupMockUser(versions, 2);
        setupMockUser(versions, 3);

        Document document = new Document();
        document.setAudit(new Audit());

        // Call method

        try {
            Whitebox.invokeMethod(documentImporter, methodName, holder1, document);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertEquals(4, versions.size());
        Assert.assertEquals(3, document.getDocumentBversions().size());
    }

    private void setupMockUser(List<Map<String, Object>> versions, int index) {
        IamUser testUser = new IamUser();
        Map<String, Object> user = (Map<String, Object>) versions.get(index).get("user");
        String userId = (String) user.get("id");
        String username = (String) user.get("name");
        testUser.setId(userId);
        Mockito.when(userService.resolveUser(userId, username, null)).thenReturn(testUser);
    }
}