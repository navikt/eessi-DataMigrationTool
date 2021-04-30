package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.SearchDefinition;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.searchDefinition.MapToSearchDefinitionMapper;

@RunWith(MockitoJUnitRunner.class)
public class SearchDefinitionMapperTest {

    @Mock
    private IamUserRepoExtended iamUserRepo;
    @Mock
    private IamGroupRepo iamGroupRepo;
    @Mock
    private ProcessDefRepo processDefRepo;

    private ObjectMapper objectMapper;
    private DefaultValuesService defaultsService;
    private MapToSearchDefinitionMapper mapToSearchDefinitionMapper;
    private final String DEFAULT_SEARCHDEFINITION_NAME = "DEFAULT_NAME";

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        String defaultsPath = this.getClass().getClassLoader().getResource("defaults.properties").getPath();
        defaultsService = new DefaultValuesService(defaultsPath);
        mapToSearchDefinitionMapper = new MapToSearchDefinitionMapper(iamUserRepo, iamGroupRepo, processDefRepo, defaultsService);
    }

    @Test
    public void testSearchDefinitionName_nameIsPresent() throws IOException {
        final String methodName = "mapName";

        // Prepare data

        File searchDefinitionFile = new File(
                SearchDefinitionMapperTest.class.getClassLoader().getResource("documents/search_definition.json").getFile());
        Map<String, Object> searchDefinitionMap = objectMapper.readValue(searchDefinitionFile, Map.class);
        MapHolder holder = new MapHolder(searchDefinitionMap, new ConcurrentHashMap<>(), "");
        SearchDefinition searchDefinition = new SearchDefinition();

        // Call method

        try {
            Whitebox.invokeMethod(mapToSearchDefinitionMapper, methodName, holder, searchDefinition);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results
        Assert.assertEquals(searchDefinitionMap.get("name"), searchDefinition.getName());
    }

    @Test
    public void testSearchDefinitionName_nameIsNull() throws IOException {
        final String methodName = "mapName";

        // Prepare data

        File searchDefinitionFile = new File(
                SearchDefinitionMapperTest.class.getClassLoader().getResource("documents/search_definition_with_null_name.json").getFile());
        Map<String, Object> searchDefinitionMap = objectMapper.readValue(searchDefinitionFile, Map.class);
        MapHolder holder = new MapHolder(searchDefinitionMap, new ConcurrentHashMap<>(), "");
        SearchDefinition searchDefinition = new SearchDefinition();

        // Call method

        try {
            Whitebox.invokeMethod(mapToSearchDefinitionMapper, methodName, holder, searchDefinition);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results
        Assert.assertEquals(DEFAULT_SEARCHDEFINITION_NAME, searchDefinition.getName());
    }

    @Test
    public void testSearchDefinitionName_nameIsEmpty() throws IOException {
        final String methodName = "mapName";

        // Prepare data

        File searchDefinitionFile = new File(SearchDefinitionMapperTest.class.getClassLoader()
                .getResource("documents/search_definition_with_empty_name.json").getFile());
        Map<String, Object> searchDefinitionMap = objectMapper.readValue(searchDefinitionFile, Map.class);
        MapHolder holder = new MapHolder(searchDefinitionMap, new ConcurrentHashMap<>(), "");
        SearchDefinition searchDefinition = new SearchDefinition();

        // Call method

        try {
            Whitebox.invokeMethod(mapToSearchDefinitionMapper, methodName, holder, searchDefinition);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results
        Assert.assertEquals(DEFAULT_SEARCHDEFINITION_NAME, searchDefinition.getName());
    }
}