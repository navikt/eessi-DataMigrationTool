package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public class TestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static MapHolder loadFromResource(ClassLoader classLoader, String path) throws IOException {
        File auditEntryFile = new File(classLoader.getResource(path).getFile());

        Map<String, Object> map = OBJECT_MAPPER.readValue(auditEntryFile, new TypeReference<>() {
        });

        return new MapHolder(map, new ConcurrentHashMap<>(), "");
    }

}
