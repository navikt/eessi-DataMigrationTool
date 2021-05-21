package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.audit;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.ECategoryType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EComponentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EEventType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditEvent;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder;

import ma.glasnost.orika.MapperFacade;

@RunWith(MockitoJUnitRunner.class)
public class MapToAuditEventMapperTest {

    @Mock
    private MapperFacade mapperFacade;

    @Test(expected = NullPointerException.class)
    public void testMapToAuditEventEmptyEvent() throws IOException {

        MapToAuditEventMapper mapToAuditEventMapper = new MapToAuditEventMapper();
        mapToAuditEventMapper.setMapperFacade(mapperFacade);

        MapHolder auditHolder = loadFromResource(this.getClass().getClassLoader(), "documents/audit/audit_entry_empty.json");

        AuditEvent auditEvent = new AuditEvent();

        mapToAuditEventMapper.mapAtoB(auditHolder, auditEvent, MappingContextBuilder.instance().build());
    }

    @Test
    public void testMapToAuditEvent() throws IOException {

        MapToAuditEventMapper mapToAuditEventMapper = new MapToAuditEventMapper();
        mapToAuditEventMapper.setMapperFacade(mapperFacade);

        MapHolder auditHolder = loadFromResource(this.getClass().getClassLoader(), "documents/audit/audit_entry.json");

        AuditEvent auditEvent = new AuditEvent();

        mapToAuditEventMapper.mapAtoB(auditHolder, auditEvent, MappingContextBuilder.instance().build());

        assertEquals("0", auditEvent.getId());
        assertEquals(ECategoryType.BUSINESS, auditEvent.getCategoryType());
        assertEquals(EComponentType.BUSINESS_MESSAGING, auditEvent.getComponentType());
        assertEquals(EEventType.RETRIEVE_CASE_BY_BUSINESS_ID, auditEvent.getEventType());

    }

}