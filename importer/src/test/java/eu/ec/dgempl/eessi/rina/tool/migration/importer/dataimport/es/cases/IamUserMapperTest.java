package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static org.mockito.ArgumentMatchers.anyString;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.repo.IamOriginRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.user.MapToIamUserMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder;

import ma.glasnost.orika.MapperFacade;

@RunWith(MockitoJUnitRunner.class)
public class IamUserMapperTest {

    @Mock
    private IamOriginRepo iamOriginRepo;
    @Mock
    private TenantRepo tenantRepo;
    @Mock
    private MapperFacade mapperFacade;

    private DefaultValuesService defaultsService;

    @Test
    public void testEmptyValues() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String defaultsPath = this.getClass().getClassLoader().getResource("defaults.properties").getPath();
        defaultsService = new DefaultValuesService(defaultsPath, null, null);
        MapToIamUserMapper mapToIamUserMapper = new MapToIamUserMapper(iamOriginRepo, tenantRepo, defaultsService);
        mapToIamUserMapper.setMapperFacade(mapperFacade);
        Mockito.when(tenantRepo.findById(anyString())).thenReturn(new Tenant());

        File userJsonFIle = new File(IamUserMapperTest.class.getClassLoader().getResource("./documents/identity_user.json").getFile());
        File userMissingJsonFIle = new File(
                IamUserMapperTest.class.getClassLoader().getResource("./documents/identity_user_empty_values.json").getFile());
        Map<String, Object> user = objectMapper.readValue(userJsonFIle, Map.class);
        Map<String, Object> userMissing = objectMapper.readValue(userMissingJsonFIle, Map.class);

        MapHolder holder = new MapHolder(user, new ConcurrentHashMap<>(), "");
        MapHolder holderMissing = new MapHolder(userMissing, new ConcurrentHashMap<>(), "");

        // Verify values when fields are present

        IamUser userTest = new IamUser();
        mapToIamUserMapper.mapAtoB(holder, userTest, MappingContextBuilder.instance().build());

        String expectedfirstName = "test";
        String expectedLastName = "test";
        Assert.assertEquals(expectedfirstName, userTest.getFirstName());
        Assert.assertEquals(expectedLastName, userTest.getLastName());

        // Verify values when fields are missing or empty

        userTest = new IamUser();
        mapToIamUserMapper.mapAtoB(holderMissing, userTest, MappingContextBuilder.instance().build());

        Assert.assertEquals("DEFAULT_FIRSTNAME", userTest.getFirstName());
        Assert.assertEquals("DEFAULT_LASTNAME", userTest.getLastName());
    }
}