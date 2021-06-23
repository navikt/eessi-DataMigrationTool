package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ConversationParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import clover.org.apache.commons.lang.StringUtils;
import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentConversationMapper extends AbstractMapToEntityMapper<MapHolder, DocumentConversation> {

    @Override
    public void mapAtoB(final MapHolder a, final DocumentConversation b, final MappingContext context) {
        b.setId(a.string(DocumentFields.CONVERSATION_ID));
        mapDate(a, DocumentFields.DATE, b::setDate);
        mapDate(a, DocumentFields.RECEIVE_DATE, b::setReceivedDate);
        mapParticipants(a, b);
        mapUserMessages(a, b, context);
    }

    private void mapParticipants(final MapHolder a, final DocumentConversation b) {
        b.getConversationParticipants();
        mapChildren(a, DocumentFields.PARTICIPANTS, b::addConversationParticipant, ConversationParticipant.class);
    }

    private void mapUserMessages(final MapHolder a, final DocumentConversation b, final MappingContext context) {
        b.getUserMessages();
        context.setProperty("conversation", b);
        mapChildren(a, DocumentFields.USER_MESSAGES, b::addUserMessage, UserMessage.class, context);
    }

    private <T extends PersistableWithSid> void mapChildren(
            final MapHolder a,
            final String key,
            final Consumer<T> childConsumer,
            Class<T> childClass) {

        mapChildren(a, key, childConsumer, childClass, null);
    }

    private <T extends PersistableWithSid> void mapChildren(
            final MapHolder a,
            final String key,
            final Consumer<T> childConsumer,
            Class<T> childClass,
            MappingContext context) {

        List<MapHolder> children = a.listToMapHolder(key);
        if (CollectionUtils.isNotEmpty(children)) {
            if (DocumentFields.PARTICIPANTS.equals(key)) {
                replaceRole(children);
            }
            children.stream()
                    .map(child -> {
                        if (context != null) {
                            return mapperFacade.map(child, childClass, context);
                        }
                        return mapperFacade.map(child, childClass);
                    })
                    .forEach(childConsumer);
        }
    }

    private void replaceRole(final List<MapHolder> children) {
        // If there is already an EconversationParticipantRole.SENDER participant AND any role is "CounterParty" AND countryId is AT:* or DE:*,
        // change "CounterParty" role to EconversationParticipantRole.RECEIVER
        Optional<MapHolder> sender = getSenderParticipant(children);
        if (sender.isPresent()) {
            children.forEach(child -> {
                if (child.containsKey(DocumentFields.ROLE)) {
                    if ("CounterParty".equalsIgnoreCase(child.string(DocumentFields.ROLE))) {
                        String orgId = child.string("organisation.id", true);
                        if (StringUtils.isNotBlank(orgId) && (orgId.startsWith("AT:") || orgId.startsWith("DE:"))) {
                            child.put(DocumentFields.ROLE, EConversationParticipantRole.RECEIVER.value());
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
