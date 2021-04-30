package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDefVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.MapToActionMapper;

@RunWith(MockitoJUnitRunner.class)
public class MapToActionMapperTest {

    @Mock
    private DocumentRepo documentRepo;
    @Mock
    private DocumentTypeVersionRepo documentTypeVersionRepo;
    @Mock
    private RinaCaseRepo rinaCaseRepo;

    private ObjectMapper objectMapper;
    private MapToActionMapper mapToActionMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        mapToActionMapper = new MapToActionMapper(documentRepo, documentTypeVersionRepo, rinaCaseRepo);
    }

    @Test
    public void testIgnoreInvalidDocumentReferences_conditionsMet() throws IOException {

        // Prepare data

        File actionDocumentFile = new File(MapToActionMapperTest.class.getClassLoader()
                .getResource("documents/action_document_with_invalid_reference.json").getFile());
        Map<String, Object> actionDocument = objectMapper.readValue(actionDocumentFile, Map.class);
        MapHolder holder = new MapHolder(actionDocument, new ConcurrentHashMap<>(), "");
        Action action = new Action();

        // Set values

        String methodName = "mapDocument";

        RinaCase rinaCase = new RinaCase(null, null, "caseId");
        rinaCase.setApplicationRole(EApplicationRole.PO);
        ProcessDefVersion processDefVersion = new ProcessDefVersion();
        ProcessDef processDef = new ProcessDef();
        processDef.setId("S_BUC_18");
        processDefVersion.setProcessDef(processDef);
        rinaCase.setProcessDefVersion(processDefVersion);
        DocumentType documentType = new DocumentType(processDefVersion, "S006");
        rinaCase.setStarterDocumentType(documentType);
        rinaCase.setStarterSent(true);

        when(rinaCaseRepo.findById(anyString())).thenReturn(rinaCase);

        // Call method

        try {
            Whitebox.invokeMethod(mapToActionMapper, methodName, holder, action);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertNull(action.getDocument());
    }

    @Test(expected = RuntimeException.class)
    public void testIgnoreInvalidDocumentReferences_notPO_throwException() throws Exception {

        // Prepare data

        File actionDocumentFile = new File(MapToActionMapperTest.class.getClassLoader()
                .getResource("documents/action_document_with_invalid_reference.json").getFile());
        Map<String, Object> actionDocument = objectMapper.readValue(actionDocumentFile, Map.class);
        MapHolder holder = new MapHolder(actionDocument, new ConcurrentHashMap<>(), "");
        Action action = new Action();

        // Set values

        String methodName = "mapDocument";

        RinaCase rinaCase = new RinaCase(null, null, "caseId");
        rinaCase.setApplicationRole(EApplicationRole.CP);
        ProcessDefVersion processDefVersion = new ProcessDefVersion();
        ProcessDef processDef = new ProcessDef();
        processDef.setId("S_BUC_18");
        processDefVersion.setProcessDef(processDef);
        rinaCase.setProcessDefVersion(processDefVersion);
        DocumentType documentType = new DocumentType(processDefVersion, "S006");
        rinaCase.setStarterDocumentType(documentType);
        rinaCase.setStarterSent(true);

        when(rinaCaseRepo.findById(anyString())).thenReturn(rinaCase);

        // Call method

        Whitebox.invokeMethod(mapToActionMapper, methodName, holder, action);
        fail("Expected RuntimeException was not thrown.");
    }

    @Test(expected = RuntimeException.class)
    public void testIgnoreInvalidDocumentReferences_notMultiStarter_throwException() throws Exception {

        // Prepare data

        File actionDocumentFile = new File(MapToActionMapperTest.class.getClassLoader()
                .getResource("documents/action_document_with_invalid_reference.json").getFile());
        Map<String, Object> actionDocument = objectMapper.readValue(actionDocumentFile, Map.class);
        MapHolder holder = new MapHolder(actionDocument, new ConcurrentHashMap<>(), "");
        Action action = new Action();

        // Set values

        String methodName = "mapDocument";

        RinaCase rinaCase = new RinaCase(null, null, "caseId");
        rinaCase.setApplicationRole(EApplicationRole.PO);
        ProcessDefVersion processDefVersion = new ProcessDefVersion();
        ProcessDef processDef = new ProcessDef();
        processDef.setId("X_BUC_18");
        processDefVersion.setProcessDef(processDef);
        rinaCase.setProcessDefVersion(processDefVersion);
        DocumentType documentType = new DocumentType(processDefVersion, "S006");
        rinaCase.setStarterDocumentType(documentType);
        rinaCase.setStarterSent(true);

        when(rinaCaseRepo.findById(anyString())).thenReturn(rinaCase);

        // Call method

        Whitebox.invokeMethod(mapToActionMapper, methodName, holder, action);
        fail("Expected RuntimeException was not thrown.");
    }

    @Test(expected = RuntimeException.class)
    public void testIgnoreInvalidDocumentReferences_notStarterFromMultiStarterBUC_throwException() throws Exception {

        // Prepare data

        File actionDocumentFile = new File(MapToActionMapperTest.class.getClassLoader()
                .getResource("documents/action_document_with_invalid_reference.json").getFile());
        Map<String, Object> actionDocument = objectMapper.readValue(actionDocumentFile, Map.class);
        MapHolder holder = new MapHolder(actionDocument, new ConcurrentHashMap<>(), "");
        Action action = new Action();

        // Set values

        String methodName = "mapDocument";

        RinaCase rinaCase = new RinaCase(null, null, "caseId");
        rinaCase.setApplicationRole(EApplicationRole.PO);
        ProcessDefVersion processDefVersion = new ProcessDefVersion();
        ProcessDef processDef = new ProcessDef();
        processDef.setId("S_BUC_18");
        processDefVersion.setProcessDef(processDef);
        rinaCase.setProcessDefVersion(processDefVersion);
        DocumentType documentType = new DocumentType(processDefVersion, "X006");
        rinaCase.setStarterDocumentType(documentType);
        rinaCase.setStarterSent(true);

        when(rinaCaseRepo.findById(anyString())).thenReturn(rinaCase);

        // Call method

        Whitebox.invokeMethod(mapToActionMapper, methodName, holder, action);
        fail("Expected RuntimeException was not thrown.");
    }

    @Test
    public void testIgnoreInvalidDocumentReferences_validReference() throws IOException {

        // Prepare data

        File actionDocumentFile = new File(
                MapToActionMapperTest.class.getClassLoader().getResource("documents/action_document.json").getFile());
        Map<String, Object> actionDocument = objectMapper.readValue(actionDocumentFile, Map.class);
        MapHolder holder = new MapHolder(actionDocument, new ConcurrentHashMap<>(), "");
        Action action = new Action();

        // Set values

        String methodName = "mapDocument";

        Document document = new Document();
        document.setId("testId");
        when(documentRepo.findByIdAndRinaCaseId(anyString(), anyString())).thenReturn(document);

        // Call method

        try {
            Whitebox.invokeMethod(mapToActionMapper, methodName, holder, action);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertNotNull(action.getDocument());
        Assert.assertEquals("testId", action.getDocument().getId());
    }
}