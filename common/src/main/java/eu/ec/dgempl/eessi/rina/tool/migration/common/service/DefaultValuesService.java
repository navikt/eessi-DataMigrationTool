package eu.ec.dgempl.eessi.rina.tool.migration.common.service;

import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that provides default values for requested fields
 */
@Service
public class DefaultValuesService {

    private final Properties properties = new Properties();

    public DefaultValuesService(@Value("${defaults.file}") final String defaultsFile) {
        try (FileInputStream fis = new FileInputStream(defaultsFile)) {
            properties.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Error reading the defaults file", e);
        }
    }

    public String getDefaultValue(String key) {
        if (key == null) {
            return null;
        }
        switch (key.toLowerCase()) {
        case "firstname":
            return properties.getProperty("identity_user.firstName", null);
        case "lastname":
            return properties.getProperty("identity_user.lastName", null);
        case "criticality":
            return properties.getProperty("cases_casestructuredmetadata.caseAssignment.properties.criticality", null);
        case "importance":
            return properties.getProperty("cases_casestructuredmetadata.caseAssignment.properties.importance", null);
        default:
            return properties.getProperty(key, null);
        }
    }
}
