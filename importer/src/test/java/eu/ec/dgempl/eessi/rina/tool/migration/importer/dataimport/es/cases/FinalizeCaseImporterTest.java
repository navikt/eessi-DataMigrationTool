package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.repo.CasePrefillRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases.FinalizeCaseImporter;

@ExtendWith(MockitoExtension.class)
public class FinalizeCaseImporterTest {

    @Mock
    private CasePrefillRepo casePrefillRepo;
    @Mock
    private RinaCaseRepo rinaCaseRepo;
    @Mock
    private DocumentRepoExtended documentRepo;

    @Test
    public void testMultipleValidDocumentCase() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        final String methodName = "getValidDocument";

        Document document1 = null;
        Document document2 = null;
        List<Document> documents;

        // Get document values
        try {
            File fileDocument1 = new File(FinalizeCaseImporterTest.class.getClassLoader().getResource("./documents/document_X001_1.json").getFile());
            File fileDocument2 = new File(FinalizeCaseImporterTest.class.getClassLoader().getResource("./documents/document_X001_2.json").getFile());
            document1 = objectMapper.readValue(fileDocument1, Document.class);
            document2 = objectMapper.readValue(fileDocument2, Document.class);

            ZonedDateTime now = ZonedDateTime.now();
            document1.setReceivedAt(now);
            document2.setReceivedAt(now.minusHours(1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (document1 != null && document2 != null) {
            documents = List.of(document1, document2);

            // Call method
            FinalizeCaseImporter finalizeCaseImporter = new FinalizeCaseImporter(casePrefillRepo, rinaCaseRepo, documentRepo);
            Optional<Document> document = Optional.empty();
            try {
                document = Whitebox.invokeMethod(finalizeCaseImporter, methodName, documents);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Assert.assertEquals(document.get(), document1);
        }
    }
}
