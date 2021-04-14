package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document.MapToDocumentConversationMapper;

@RunWith(MockitoJUnitRunner.class)
public class DocumentConversationMapperTest {

    @Test
    public void testReplaceRole() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodName = "replaceRole";

        MapToDocumentConversationMapper mapToDocumentConversationMapper = new MapToDocumentConversationMapper();

        // Prepare data

        File conversation1File = new File(DocumentConversationMapperTest.class.getClassLoader().getResource("documents/document_conversation_1.json").getFile());
        File conversation2File = new File(DocumentConversationMapperTest.class.getClassLoader().getResource("documents/document_conversation_2.json").getFile());
        File conversation3File = new File(DocumentConversationMapperTest.class.getClassLoader().getResource("documents/document_conversation_3.json").getFile());
        Map<String, Object> conversation1 = objectMapper.readValue(conversation1File, Map.class);
        Map<String, Object> conversation2 = objectMapper.readValue(conversation2File, Map.class);
        Map<String, Object> conversation3 = objectMapper.readValue(conversation3File, Map.class);
        MapHolder holder1 = new MapHolder(conversation1, new ConcurrentHashMap<>(), "");
        MapHolder holder2 = new MapHolder(conversation2, new ConcurrentHashMap<>(), "");
        MapHolder holder3 = new MapHolder(conversation3, new ConcurrentHashMap<>(), "");

        // Call method

        List<MapHolder> children1 = holder1.listToMapHolder( DocumentFields.PARTICIPANTS);
        try {
            Whitebox.invokeMethod(mapToDocumentConversationMapper, methodName, children1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<MapHolder> children2 = holder2.listToMapHolder( DocumentFields.PARTICIPANTS);
        try {
            Whitebox.invokeMethod(mapToDocumentConversationMapper, methodName, children2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<MapHolder> children3 = holder3.listToMapHolder( DocumentFields.PARTICIPANTS);
        try {
            Whitebox.invokeMethod(mapToDocumentConversationMapper, methodName, children3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertEquals(3, children1.size());
        Assert.assertEquals(1, children1.stream().filter(x -> EConversationParticipantRole.SENDER.value().equals(x.getHolding().get("role"))).count());
        Assert.assertEquals(2, children1.stream().filter(x -> EConversationParticipantRole.RECEIVER.value().equals(x.getHolding().get("role"))).count());

        Assert.assertEquals(3, children2.size());
        Assert.assertEquals(2, children2.stream().filter(x -> EConversationParticipantRole.RECEIVER.value().equals(x.getHolding().get("role"))).count());
        Assert.assertEquals(1, children2.stream().filter(x -> "CounterParty".equals(x.getHolding().get("role"))).count());

        Assert.assertEquals(6, children3.size());
        Assert.assertEquals(1, children3.stream().filter(x -> EConversationParticipantRole.SENDER.value().equals(x.getHolding().get("role"))).count());
        Assert.assertEquals(4, children3.stream().filter(x -> EConversationParticipantRole.RECEIVER.value().equals(x.getHolding().get("role"))).count());
        Assert.assertEquals(1, children3.stream().filter(x -> "CounterParty".equals(x.getHolding().get("role"))).count());
    }
}