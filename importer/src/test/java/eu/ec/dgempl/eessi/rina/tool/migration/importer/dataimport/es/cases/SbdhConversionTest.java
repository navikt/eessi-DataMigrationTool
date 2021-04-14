package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.SbdhUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.buc.core.model.sbdh.ContactTypeIdentifier;
import eu.ec.dgempl.eessi.rina.buc.core.model.sbdh.StandardBusinessDocumentHeader;

public class SbdhConversionTest {

    @Test
    public void testConversion() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        File sbdhJsonFIle = new File(SbdhConversionTest.class.getClassLoader().getResource("./sbdh/sbdh.json").getFile());
        Map<String, Object> sbdh = objectMapper.readValue(sbdhJsonFIle, Map.class);

        fixSbdhEnumValues(sbdh, "");

        String convertedSbdh = objectMapper.writeValueAsString(sbdh);
        StandardBusinessDocumentHeader standardBusinessDocumentHeader = objectMapper.readValue(convertedSbdh,
                StandardBusinessDocumentHeader.class);

        Assert.assertEquals(ContactTypeIdentifier.CASE_OWNER, standardBusinessDocumentHeader.getSender().getContactTypeIdentifier());
    }

    @Test
    public void testConversionWithAttachments() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        File sbdhJsonFIle = new File(
                SbdhConversionTest.class.getClassLoader().getResource("./sbdh/sbdhWithAttachments.json").getFile());
        Map<String, Object> sbdh = objectMapper.readValue(sbdhJsonFIle, Map.class);

        fixSbdhEnumValues(sbdh, "");

        String convertedSbdh = objectMapper.writeValueAsString(sbdh);
        StandardBusinessDocumentHeader standardBusinessDocumentHeader = objectMapper.readValue(convertedSbdh,
                StandardBusinessDocumentHeader.class);

        Assert.assertEquals(ContactTypeIdentifier.CASE_OWNER, standardBusinessDocumentHeader.getSender().getContactTypeIdentifier());
    }
}

