package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.ConversationParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentConversationMapper extends AbstractMapToEntityMapper<MapHolder, DocumentConversation> {

    @Override
    public void mapAtoB(final MapHolder a, final DocumentConversation b, final MappingContext context) {
        b.setId(a.string(DocumentFields.CONVERSATION_ID));
        mapDate(a, DocumentFields.DATE, b::setDate);
        mapDate(a, DocumentFields.RECEIVE_DATE, b::setReceivedDate);
        mapParticipants(a, b);
        mapUserMessages(a, b);
    }

    private void mapParticipants(final MapHolder a, final DocumentConversation b) {
        b.getConversationParticipants();
        mapChildren(a, DocumentFields.PARTICIPANTS, b::addConversationParticipant, ConversationParticipant.class);
    }

    private void mapUserMessages(final MapHolder a, final DocumentConversation b) {
        b.getUserMessages();
        mapChildren(a, DocumentFields.USER_MESSAGES, b::addUserMessage, UserMessage.class);
    }

    private <T extends PersistableWithSid> void mapChildren(
            final MapHolder a,
            final String key,
            final Consumer<T> childConsumer,
            Class<T> childClass) {

        List<MapHolder> children = a.listToMapHolder(key);
        if (CollectionUtils.isNotEmpty(children)) {
            children.stream()
                    .map(child -> mapperFacade.map(child, childClass))
                    .forEach(childConsumer);
        }
    }

}
