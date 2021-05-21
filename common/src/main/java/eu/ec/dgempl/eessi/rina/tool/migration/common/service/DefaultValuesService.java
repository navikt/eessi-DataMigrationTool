package eu.ec.dgempl.eessi.rina.tool.migration.common.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.GsonWrapper;

/**
 * Service that provides default values for requested fields
 */
@Service
public class DefaultValuesService {

    private final Properties properties = new Properties();
    private Map usernamesMap;
    private Map userGroupsMap;

    public DefaultValuesService(
            @Value("${defaults.file}") final String defaultsFile,
            @Value("${default.usernames.file}") final String defaultUsernamesFile,
            @Value("${default.usergroups.file}") final String defaultUserGroupsFile) {

        try (FileInputStream fis = new FileInputStream(defaultsFile)) {
            properties.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Error reading the defaults file", e);
        }

        if (StringUtils.isNotBlank(defaultUsernamesFile)) {
            try {
                usernamesMap = GsonWrapper.loadFromDisk(defaultUsernamesFile, Map.class);
            } catch (IOException e) {
                throw new RuntimeException("Error reading the default usernames file", e);
            }
        }

        if (StringUtils.isNotBlank(defaultUserGroupsFile)) {
            try {
                userGroupsMap = GsonWrapper.loadFromDisk(defaultUserGroupsFile, Map.class);
            } catch (IOException e) {
                throw new RuntimeException("Error reading the default user groups file", e);
            }
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

    public String getDefaultUsernameByTenantId(String tenantId) {
        return getDefaultValueByTenantId(tenantId, usernamesMap);
    }

    public String getDefaultUserGroupByTenantId(String tenantId) {
        return getDefaultValueByTenantId(tenantId, userGroupsMap);
    }

    @Nullable
    private String getDefaultValueByTenantId(final String tenantId, final Map map) {
        if (StringUtils.isNotBlank(tenantId)
                && MapUtils.isNotEmpty(map)
                && map.containsKey(tenantId)) {
            Object defaultValue = map.get(tenantId);
            if (defaultValue instanceof String) {
                return (String) defaultValue;
            }
        }
        return null;
    }
}
