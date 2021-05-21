package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private IamUserRepo iamUserRepo;
    @Mock
    private IamUserRepoExtended iamUserRepoExtended;

    private DefaultValuesService defaultsService;

    private String userId;
    private String username;
    private String usernameNotFound;
    private String caseId;
    private String tenantId1;
    private String defaultTenantUsername1;
    private String tenantIdNotFound;

    private IamUser iamUser;

    @Before
    public void setUp() throws URISyntaxException {
        String defaultsPath = new File(this.getClass().getClassLoader().getResource("defaults.properties").toURI()).getAbsolutePath();
        String defaultUsersPath = new File(this.getClass().getClassLoader().getResource("defaultUsers.json").toURI()).getAbsolutePath();
        defaultsService = new DefaultValuesService(defaultsPath, defaultUsersPath, null);

        userId = "userId";
        username = "username";
        usernameNotFound = "usernameNotFound";
        caseId = "caseId";
        tenantId1 = "tenantId1";
        tenantIdNotFound = "tenantIdNotFound";
        defaultTenantUsername1 = "username1";

        iamUser = new IamUser();
    }

    @Test
    public void testResolveUser_foundById() {

        when(iamUserRepo.findById(userId)).thenReturn(iamUser);

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNotNull(userService.resolveUser(userId, "", null));
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUser_notFound_missingUserIdAndUsername_throwException() {

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNull(userService.resolveUser(null, "", null));
        fail("Expected runtime exception");
    }

    @Test
    public void testResolveUser_notFound_missingUserIdAndUsername_returnNull() {

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNull(userService.resolveUser(null, "", null, true));
    }

    @Test
    public void testResolveUser_foundByUsername() {

        when(iamUserRepoExtended.findByUsernameAndIsDeletedIsFalse(username)).thenReturn(iamUser);

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNotNull(userService.resolveUser(null, username, null));
    }

    @Test
    public void testResolveUser_foundByUsername2() {

        when(iamUserRepoExtended.findByUsernameAndIsDeletedIsTrue(username)).thenReturn(List.of(iamUser));

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNotNull(userService.resolveUser("", username, null));
    }

    @Test
    public void testResolveUser_defaultUser_tenantIdByCase() {

        RinaCase rinaCase = createCaseWithTenantId(tenantId1);

        when(iamUserRepoExtended.findByUsernameAndIsDeletedIsFalse(defaultTenantUsername1)).thenReturn(iamUser);

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNotNull(userService.resolveUser(null, usernameNotFound, rinaCase));
    }

    @Test
    public void testResolveUser_defaultUser_tenantIdByCase2() {

        RinaCase rinaCase = createCaseWithTenantId(tenantId1);

        when(iamUserRepoExtended.findByUsernameAndIsDeletedIsFalse(defaultTenantUsername1)).thenReturn(iamUser);

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNotNull(userService.resolveUser(null, null, rinaCase));
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUser_defaultUser_tenantIdByCase_tenantNotFound_throwException() {

        RinaCase rinaCase = createCaseWithTenantId(tenantIdNotFound);

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNull(userService.resolveUser(null, usernameNotFound, rinaCase));
        fail("Expected runtime exception");
    }

    @Test
    public void testResolveUser_defaultUser_tenantIdByCase_tenantNotFound_returnNull() {

        RinaCase rinaCase = createCaseWithTenantId(tenantIdNotFound);

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNull(userService.resolveUser(null, usernameNotFound, rinaCase, true));
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUser_defaultUser_noTenantId_throwException() {

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNotNull(userService.resolveUser(null, usernameNotFound, null));
        fail("Expected runtime exception");
    }

    @Test
    public void testResolveUser_defaultUser_noTenantId_returnNull() {

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNull(userService.resolveUser(null, usernameNotFound, null, true));
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUser_defaultUser_noTenantId_returnException() {

        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultsService);

        Assert.assertNull(userService.resolveUser(null, usernameNotFound, null, false));
        fail("Expected runtime exception");
    }

    private RinaCase createCaseWithTenantId(String tenantId) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        return new RinaCase(tenant, null, caseId);
    }
}
