package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.buc.api.utils.IdHelper;
import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseActionType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ConversationParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document.MapToUserMessageMapper;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MappingContext;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUserMessagesMapperTest {

    public static final Organisation DUMMY_ORG_1 = new Organisation("DummyOrg", ECountryCode.BE);
    public static final Organisation DUMMY_ORG_2 = new Organisation("DummyOrg 2", ECountryCode.BE);
    private final RinaJsonMapper rinaJsonMapper = new RinaJsonMapper(new ObjectMapper());
    @Mock
    private MapperFacade mapperFacade;

    @Test
    public void testSetActionTypeIfSbdhIsMissing1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodNameSbdh = "mapSbdh";
        final String methodNameAction = "mapAction";

        MapToUserMessageMapper mapToUserMessageMapper = new MapToUserMessageMapper(rinaJsonMapper);
        mapToUserMessageMapper.setMapperFacade(mapperFacade);
        File conversationFile = new File(
                DocumentUserMessagesMapperTest.class.getClassLoader().getResource("documents/document_with_user_message.json").getFile());
        Map<String, Object> conversation = objectMapper.readValue(conversationFile, Map.class);
        MapHolder holder = new MapHolder(conversation, new ConcurrentHashMap<>(), "");

        // Prepare data

        UserMessage userMessage = createUserMessage();

        RinaCase rinaCase = createRinaCase();
        rinaCase.setStarterSent(true);

        Document document = createDocument(rinaCase);
        document.setIsStarter(true);

        DocumentConversation documentConversation = createDocumentConversation(document);

        MappingContext mappingContext = new MappingContext(Map.of("doc", document, "conversation", documentConversation));

        // Call method

        try {
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameSbdh, holder, userMessage, mappingContext);
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameAction, holder, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertNotNull(userMessage.getSbdh());
        Assert.assertEquals(ECaseActionType.NEW, userMessage.getAction());
    }

    @NotNull
    private DocumentConversation createDocumentConversation(Document document) {
        ConversationParticipant conversationParticipant = new ConversationParticipant(DUMMY_ORG_2, EConversationParticipantRole.RECEIVER);

        DocumentConversation documentConversation = new DocumentConversation(document, IdHelper.getNewUUID());
        documentConversation.setConversationParticipants(Collections.singletonList(conversationParticipant));
        return documentConversation;
    }

    @NotNull
    private Document createDocument(RinaCase rinaCase) {
        Document document = new Document();
        document.setRinaCase(rinaCase);
        return document;
    }

    @NotNull
    private RinaCase createRinaCase() {
        return new RinaCase(null, null, "caseId");
    }

    @NotNull
    private UserMessage createUserMessage() {
        UserMessage userMessage = new UserMessage();
        userMessage.setId("userMessageId");
        userMessage.setSender(DUMMY_ORG_1);
        userMessage.setReceiver(DUMMY_ORG_2);
        return userMessage;
    }

    @Test
    public void testSetActionTypeIfSbdhIsMissing2() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodNameSbdh = "mapSbdh";
        final String methodNameAction = "mapAction";

        MapToUserMessageMapper mapToUserMessageMapper = new MapToUserMessageMapper(rinaJsonMapper);
        mapToUserMessageMapper.setMapperFacade(mapperFacade);
        File conversationFile = new File(
                DocumentUserMessagesMapperTest.class.getClassLoader().getResource("documents/document_with_user_message.json").getFile());
        Map<String, Object> conversation = objectMapper.readValue(conversationFile, Map.class);
        MapHolder holder = new MapHolder(conversation, new ConcurrentHashMap<>(), "");

        // Prepare data

        UserMessage userMessage = createUserMessage();

        RinaCase rinaCase = createRinaCase();
        rinaCase.setStarterSent(false);

        Document document = createDocument(rinaCase);
        document.setIsStarter(true);

        DocumentConversation documentConversation = createDocumentConversation(document);

        MappingContext mappingContext = new MappingContext(Map.of("doc", document, "conversation", documentConversation));

        // Call method

        try {
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameSbdh, holder, userMessage, mappingContext);
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameAction, holder, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertNotNull(userMessage.getSbdh());
        Assert.assertEquals(ECaseActionType.START, userMessage.getAction());
    }

    @Test
    public void testSetActionTypeIfSbdhIsMissing3() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodNameSbdh = "mapSbdh";
        final String methodNameAction = "mapAction";

        MapToUserMessageMapper mapToUserMessageMapper = new MapToUserMessageMapper(rinaJsonMapper);
        mapToUserMessageMapper.setMapperFacade(mapperFacade);
        File conversationFile = new File(
                DocumentUserMessagesMapperTest.class.getClassLoader().getResource("documents/document_with_user_message.json").getFile());
        Map<String, Object> conversation = objectMapper.readValue(conversationFile, Map.class);
        MapHolder holder = new MapHolder(conversation, new ConcurrentHashMap<>(), "");

        // Prepare data

        UserMessage userMessage = createUserMessage();

        RinaCase rinaCase = createRinaCase();
        rinaCase.setStarterSent(true);

        Document document = createDocument(rinaCase);
        document.setIsStarter(false);

        DocumentConversation documentConversation = createDocumentConversation(document);

        MappingContext mappingContext = new MappingContext(Map.of("doc", document, "conversation", documentConversation));

        // Call method

        try {
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameSbdh, holder, userMessage, mappingContext);
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameAction, holder, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertNotNull(userMessage.getSbdh());
        Assert.assertEquals(ECaseActionType.NEW, userMessage.getAction());
    }

    @Test
    public void testSetActionTypeIfSbdhIsMissing4() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodNameSbdh = "mapSbdh";
        final String methodNameAction = "mapAction";

        MapToUserMessageMapper mapToUserMessageMapper = new MapToUserMessageMapper(rinaJsonMapper);
        mapToUserMessageMapper.setMapperFacade(mapperFacade);
        File conversationFile = new File(
                DocumentUserMessagesMapperTest.class.getClassLoader().getResource("documents/document_with_user_message.json").getFile());
        Map<String, Object> conversation = objectMapper.readValue(conversationFile, Map.class);
        MapHolder holder = new MapHolder(conversation, new ConcurrentHashMap<>(), "");

        // Prepare data

        UserMessage userMessage = createUserMessage();

        RinaCase rinaCase = createRinaCase();
        rinaCase.setStarterSent(true);

        Document document = createDocument(rinaCase);
        document.setIsStarter(false);
        document.setStatus(EDocumentStatus.EMPTY);

        DocumentConversation documentConversation = createDocumentConversation(document);

        MappingContext mappingContext = new MappingContext(Map.of("doc", document, "conversation", documentConversation));

        // Call method

        try {
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameSbdh, holder, userMessage, mappingContext);
            Whitebox.invokeMethod(mapToUserMessageMapper, methodNameAction, holder, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertNotNull(userMessage.getSbdh());
        Assert.assertEquals(ECaseActionType.UPDATE, userMessage.getAction());
    }
}