package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;

public class BonitaProcessDefMappingsServiceTest {

    @Test
    public void testGetActorName() throws IOException {
        String path = BonitaProcessDefMappingsServiceTest.class.getClassLoader().getResource("BonitaProcessDefinitionActors.json").getPath();
        BonitaProcessDefMappingsService service = new BonitaProcessDefMappingsService(new RinaJsonMapper(new ObjectMapper()), path);

        String actorName = service.getActorName("PO", "R_BUC_05", "6380");
        Assert.assertEquals(ERole.AUDITOR.name(), actorName);

        actorName = service.getActorName("CP", "R_BUC_05", "6844");
        Assert.assertEquals(ERole.AUTHORIZED_CLERK.name(), actorName);

        actorName = service.getActorName("CP", "FB_BUC_01", "5931");
        Assert.assertEquals(ERole.UNAUTHORIZED_CLERK.name(), actorName);
    }

    @Test
    public void testGetActorNameNoRole() throws IOException {
        String path = BonitaProcessDefMappingsServiceTest.class.getClassLoader().getResource("BonitaProcessDefinitionActorsNoRoles.json").getPath();
        BonitaProcessDefMappingsService service = new BonitaProcessDefMappingsService(new RinaJsonMapper(new ObjectMapper()), path);

        String actorName = service.getActorName("PO", "R_BUC_05", "7596");
        Assert.assertNull(actorName);

        actorName = service.getActorName("CP", "R_BUC_05", "6000");
        Assert.assertNull(actorName);

        actorName = service.getActorName("CP", "FB_BUC_01", "5340");
        Assert.assertNull(actorName);
    }
}
