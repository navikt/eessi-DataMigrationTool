package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyTarget;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.repo.AssignmentPolicyRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.assignment.MapToAssignmentPolicyTargetMapper;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentTargetPolicyMapperTest {

    @Mock
    private AssignmentPolicyRepo assignmentPolicyRepo;
    @Mock
    private TenantRepo tenantRepo;

    @Test
    public void testIgnoreInvalidReferencesPolicy_propertyTrue_1exists_2dontExist() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodName = "mapPolicies";

        // Setup

        MapToAssignmentPolicyTargetMapper mapToAssignmentPolicyTargetMapper = new MapToAssignmentPolicyTargetMapper(assignmentPolicyRepo,
                tenantRepo);
        File assignmentTargetFile = new File(
                AssignmentTargetPolicyMapperTest.class.getClassLoader().getResource("documents/assignment_target.json").getFile());
        Map<String, Object> assignmentTarget = objectMapper.readValue(assignmentTargetFile, Map.class);
        MapHolder holder = new MapHolder(assignmentTarget, new ConcurrentHashMap<>(), "");

        AssignmentPolicyTarget assignmentPolicyTarget = new AssignmentPolicyTarget();

        // Set values

        ReflectionTestUtils.setField(mapToAssignmentPolicyTargetMapper, "ignoreInvalidReferencesPolicy", true);

        AssignmentPolicy assignmentPolicy2 = new AssignmentPolicy(new Tenant(), "name2", "id2");
        Mockito.when(assignmentPolicyRepo.findById("policy2")).thenReturn(assignmentPolicy2);

        // Call method

        try {
            Whitebox.invokeMethod(mapToAssignmentPolicyTargetMapper, methodName, holder, assignmentPolicyTarget);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        Assert.assertEquals(1, assignmentPolicyTarget.getAssignmentPolicies().size());
        Assert.assertEquals("id2", assignmentPolicyTarget.getAssignmentPolicies().get(0).getId());
    }

    @Test(expected = EntityNotFoundEessiRuntimeException.class)
    public void testIgnoreInvalidReferencesPolicy_propertyFalse_1exists_2dontExist() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        final String methodName = "mapPolicies";

        // Setup

        MapToAssignmentPolicyTargetMapper mapToAssignmentPolicyTargetMapper = new MapToAssignmentPolicyTargetMapper(assignmentPolicyRepo,
                tenantRepo);
        File assignmentTargetFile = new File(
                AssignmentTargetPolicyMapperTest.class.getClassLoader().getResource("documents/assignment_target.json").getFile());
        Map<String, Object> assignmentTarget = objectMapper.readValue(assignmentTargetFile, Map.class);
        MapHolder holder = new MapHolder(assignmentTarget, new ConcurrentHashMap<>(), "");

        AssignmentPolicyTarget assignmentPolicyTarget = new AssignmentPolicyTarget();

        // Set values

        ReflectionTestUtils.setField(mapToAssignmentPolicyTargetMapper, "ignoreInvalidReferencesPolicy", false);

        AssignmentPolicy assignmentPolicy2 = new AssignmentPolicy(new Tenant(), "name2", "id2");
        Mockito.when(assignmentPolicyRepo.findById("policy2")).thenReturn(assignmentPolicy2);

        // Call method

        Whitebox.invokeMethod(mapToAssignmentPolicyTargetMapper, methodName, holder, assignmentPolicyTarget);
        fail("Expected EntityNotFoundEessiRuntimeException was not thrown.");
    }
}