package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.assignment;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ESector;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyRule;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Sector;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.repo.SectorRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder;

@RunWith(MockitoJUnitRunner.class)
public class MapToAssignmentPolicyRuleMapperTest {

    @Mock
    private IamUserRepo iamUserRepo;

    @Mock
    private IamGroupRepo iamGroupRepo;

    @Mock
    private ProcessDefRepo processDefRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private SectorRepo sectorRepo;

    @Mock
    private OrganisationService organisationService;

    @Test
    public void testMapAssignmentPolicyRuleMapper() throws IOException {
        MapToAssignmentPolicyRuleMapper mapToAssignmentPolicyRuleMapper = new MapToAssignmentPolicyRuleMapper(
                iamUserRepo,
                iamGroupRepo,
                processDefRepo,
                roleRepo,
                sectorRepo,
                organisationService);

        when(sectorRepo.findByName(ESector.RECOVERY)).thenReturn(new Sector(ESector.RECOVERY));
        when(iamGroupRepo.findBySimpleNaturalId(anyString())).thenReturn(createRandomIamGroup());
        when(iamUserRepo.findBySimpleNaturalId(anyString())).thenReturn(createRandomIamUser());

        MapHolder assignmentPolicyRuleHolder = loadFromResource(
                this.getClass().getClassLoader(),
                "documents/assignmentPolicies/assignment_policy_rule.json");

        AssignmentPolicyRule assignmentPolicyRule = new AssignmentPolicyRule(createRandomAssignmentPolicy(), randomString());
        mapToAssignmentPolicyRuleMapper.mapAtoB(assignmentPolicyRuleHolder, assignmentPolicyRule, MappingContextBuilder.mctxb().build());

        List<Sector> sectors = assignmentPolicyRule.getSectors();

        Assert.assertNotNull(sectors);
        Assert.assertEquals(1, sectors.size());
    }

}