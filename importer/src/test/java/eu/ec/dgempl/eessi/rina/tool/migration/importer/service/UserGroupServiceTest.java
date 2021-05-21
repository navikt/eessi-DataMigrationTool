package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;

@RunWith(MockitoJUnitRunner.class)
public class UserGroupServiceTest {

    @Mock
    private IamGroupRepo iamGroupRepo;

    private String groupId;
    private String groupIdBlank;
    private String tenantId1;
    private String tenantId2;
    private String defaultTenantGroup1;
    private String defaultTenantGroup2;
    private String tenantIdNotFound;

    private IamGroup iamGroup;
    private DefaultValuesService defaultsService;
    private UserGroupService userGroupService;

    @Before
    public void setUp() throws URISyntaxException {
        String defaultsPath = new File(this.getClass().getClassLoader().getResource("defaults.properties").toURI()).getAbsolutePath();
        String defaultUsersPath = new File(this.getClass().getClassLoader().getResource("defaultUsers.json").toURI()).getAbsolutePath();
        String defaultUserGroupsPath = new File(this.getClass().getClassLoader().getResource("defaultUserGroups.json").toURI()).getAbsolutePath();
        defaultsService = new DefaultValuesService(defaultsPath, defaultUsersPath, defaultUserGroupsPath);

        userGroupService = new UserGroupService(iamGroupRepo, defaultsService);

        groupId = "groupId";
        groupIdBlank = "";
        tenantId1 = "tenantId1";
        tenantId2 = "tenantId2";
        defaultTenantGroup1 = "defaultTenantGroup1";
        defaultTenantGroup2 = "defaultTenantGroup2";
        tenantIdNotFound = "tenantIdNotFound";

        iamGroup = new IamGroup();
    }

    @Test
    public void testResolveGroup_foundById() {

        when(iamGroupRepo.findById(groupId)).thenReturn(iamGroup);

        Assert.assertNotNull(userGroupService.resolveUserGroup(groupId, tenantId1));
    }

    @Test
    public void testResolveGroup_foundById_tenantNull() {

        when(iamGroupRepo.findById(groupId)).thenReturn(iamGroup);

        Assert.assertNotNull(userGroupService.resolveUserGroup(groupId, null));
    }

    @Test(expected = RuntimeException.class)
    public void testResolveGroup_notFound_missingGroupIdAndTenantId_throwException() {

        Assert.assertNull(userGroupService.resolveUserGroup(null, ""));
        fail("Expected runtime exception");
    }

    @Test
    public void testResolveUser_foundByTenantId() {

        when(iamGroupRepo.findById(defaultTenantGroup1)).thenReturn(iamGroup);

        Assert.assertNotNull(userGroupService.resolveUserGroup(groupIdBlank, tenantId1));
    }

    @Test
    public void testResolveUser_foundByTenantId2() {

        when(iamGroupRepo.findById(defaultTenantGroup2)).thenReturn(iamGroup);

        Assert.assertNotNull(userGroupService.resolveUserGroup(null, tenantId2));
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUser_tenantIdNotFound_throwException() {

        Assert.assertNull(userGroupService.resolveUserGroup(null, tenantIdNotFound));
        fail("Expected runtime exception");
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUser_foundByTenantId_defaultGroupNotFound_throwException() {

        when(defaultsService.getDefaultUserGroupByTenantId(tenantId1)).thenReturn(defaultTenantGroup1);

        Assert.assertNull(userGroupService.resolveUserGroup(null, tenantId1));
        fail("Expected runtime exception");
    }
}
