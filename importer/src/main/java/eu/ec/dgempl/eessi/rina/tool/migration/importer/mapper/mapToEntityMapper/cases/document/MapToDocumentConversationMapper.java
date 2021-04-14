package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import clover.org.apache.commons.lang.StringUtils;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
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
            if (DocumentFields.PARTICIPANTS.equals(key)) {
                replaceRole(children);
            }
            children.stream()
                    .map(child -> mapperFacade.map(child, childClass))
                    .forEach(childConsumer);
        }
    }

    private void replaceRole(final List<MapHolder> children) {
        // If there is already an EconversationParticipantRole.SENDER participant AND any role is "CounterParty" AND countryId is AT:*,
        // change "CounterParty" role to EconversationParticipantRole.RECEIVER
        Optional<MapHolder> sender = getSenderParticipant(children);
        if (sender.isPresent()) {
            children.forEach(child -> {
                Map<String, Object> holding = child.getHolding();
                if (holding != null && holding.containsKey(DocumentFields.ROLE)) {
                    if ("CounterParty".equalsIgnoreCase((String)holding.get(DocumentFields.ROLE))) {
                        if (holding.containsKey("organisation")) {
                            Map<String, Object> organisation = (Map<String, Object>) holding.get("organisation");
                            if (organisation.containsKey(DocumentFields.ID)) {
                                String organisationId = (String) organisation.get(DocumentFields.ID);
                                if (organisationId.startsWith("AT:")) {
                                    child.getHolding().put(DocumentFields.ROLE, EConversationParticipantRole.RECEIVER.value());
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @NotNull
    private Optional<MapHolder> getSenderParticipant(final List<MapHolder> children) {
        return children.stream()
                .filter(x -> x.getHolding() != null
                        && x.getHolding().containsKey(DocumentFields.ROLE)
                        && StringUtils.equalsIgnoreCase("sender", (String) x.getHolding().get(DocumentFields.ROLE)))
                .findFirst();
    }
}
