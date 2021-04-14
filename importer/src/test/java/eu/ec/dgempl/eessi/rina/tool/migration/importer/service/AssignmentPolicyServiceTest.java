package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;

public class AssignmentPolicyServiceTest {

    @Test
    public void testGetActorName() throws IOException {
        String path = AssignmentPolicyServiceTest.class.getClassLoader().getResource("ProcessDefinitionAssignments.json").getPath();
        AssignmentPolicyService service = new AssignmentPolicyService(new RinaJsonMapper(new ObjectMapper()), path);

        String actorName = service.getActorName("PO", "R_BUC_05", "7596");
        Assert.assertEquals(ERole.AUDITOR.name(), actorName);

        actorName = service.getActorName("CP", "R_BUC_05", "6000");
        Assert.assertEquals(ERole.AUTHORIZED_CLERK.name(), actorName);

        actorName = service.getActorName("CP", "FB_BUC_01", "5340");
        Assert.assertEquals(ERole.UNAUTHORIZED_CLERK.name(), actorName);
    }
}
