package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.audit;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EActionType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.ECategoryType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EComponentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EEventType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EOutcomeType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditEvent;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditObject;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AuditEventFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAuditEventMapper extends AbstractMapToEntityMapper<MapHolder, AuditEvent> {

    @Override
    public void mapAtoB(final MapHolder a, final AuditEvent b, final MappingContext context) {
        MapHolder message = a.getMapHolder(AuditEventFields.MESSAGE);

        mapActionType(message, b);
        mapAudit(message, b);
        mapOutcomeType(message, b);
        mapSource(message, b);

        b.setId(message.string(AuditEventFields.ID));
        b.setNetLocationIp(message.string(AuditEventFields.NETWORK_LOCATION_IP, true));
        b.setNetLocationMachine(message.string(AuditEventFields.NETWORK_LOCATION_MACHINE, true));
        b.setOutcomeDetails(message.string(AuditEventFields.OUTCOME_DETAILS));

        mapAuditedObjects(message, b);
        mapAuditedParticipants(message, b);

        b.setUsername(a.string(AuditEventFields.USER_NAME));
    }

    private void mapAuditedObjects(final MapHolder a, final AuditEvent b) {
        List<MapHolder> auditedObjects = a.listToMapHolder(AuditEventFields.AUDITED_OBJECTS);
        if (CollectionUtils.isNotEmpty(auditedObjects)) {
            auditedObjects.stream()
                    .map(auditedObject -> mapperFacade.map(auditedObject, AuditObject.class))
                    .forEach(b::addAuditObject);
        }
    }

    private void mapAuditedParticipants(final MapHolder a, final AuditEvent b) {
        List<MapHolder> participants = a.listToMapHolder(AuditEventFields.PARTICIPANTS);
        if (CollectionUtils.isNotEmpty(participants)) {
            participants.stream()
                    .map(participant -> mapperFacade.map(participant, AuditParticipant.class))
                    .forEach(b::addAuditParticipant);
        }
    }

    private void mapSource(final MapHolder a, final AuditEvent b) {
        MapHolder source = a.getMapHolder(AuditEventFields.SOURCE);

        String categoryType = source.string(AuditEventFields.CATEGORY_TYPE);
        ECategoryType eCategoryType = ECategoryType.lookup(categoryType).orElseThrow(
                () -> new DmtEnumNotFoundException(ECategoryType.class, source.addPath(AuditEventFields.CATEGORY_TYPE), categoryType)
        );
        b.setCategoryType(eCategoryType);

        String componentType = source.string(AuditEventFields.COMPONENT_TYPE);
        EComponentType eComponentType = EComponentType.lookup(componentType).orElseThrow(
                () -> new DmtEnumNotFoundException(EComponentType.class, source.addPath(AuditEventFields.COMPONENT_TYPE), componentType)
        );
        b.setComponentType(eComponentType);

        String eventType = source.string(AuditEventFields.EVENT_TYPE);
        EEventType eEventType = EEventType.lookup(eventType).orElseThrow(
                () -> new DmtEnumNotFoundException(EEventType.class, source.addPath(AuditEventFields.EVENT_TYPE), eventType)
        );
        b.setEventType(eEventType);
    }

    private void mapOutcomeType(final MapHolder mapHolder, final AuditEvent b) {
        String outcome = mapHolder.string(AuditEventFields.OUTCOME);
        EOutcomeType eOutcomeType = EOutcomeType.lookup(outcome).orElseThrow(
                () -> new DmtEnumNotFoundException(EOutcomeType.class, mapHolder.addPath(AuditEventFields.OUTCOME), outcome)
        );
        b.setOutcomeType(eOutcomeType);
    }

    private void mapAudit(final MapHolder a, final AuditEvent b) {
        Audit audit = new Audit();

        mapDate(a, AuditEventFields.DATE, audit::setCreatedAt);
        mapDate(a, AuditEventFields.DATE, audit::setUpdatedAt);

        audit.setCreatedBy("-1");
        audit.setUpdatedBy("-1");

        b.setAudit(audit);
    }

    private void mapActionType(final MapHolder a, final AuditEvent b) {
        String action = a.string(AuditEventFields.ACTION);
        EActionType eActionType = EActionType.lookup(action).orElseThrow(
                () -> new DmtEnumNotFoundException(EActionType.class, a.addPath(AuditEventFields.ACTION), action)
        );
        b.setActionType(eActionType);
    }
}
