package eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield;

import static eu.ec.dgempl.eessi.rina.model.enumtypes.ETenantParam.*;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ETenantConfigurationType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ETenantParam;

import clover.com.google.common.collect.BiMap;
import clover.com.google.common.collect.ImmutableBiMap;

public class ConfigurationsConfigurationFields {

    public static final String INSTITUTION_ID = "institutionId";
    public static final String TYPE = "type";

    private static final String PROVIDER_URL = "providerUrl";
    private static final String SECURITY_AUTHENTICATION = "securityAuthentication";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String USER_SEARCH_BASE = "userSearchBase";
    private static final String GROUP_SEARCH_BASE = "groupSearchBase";
    private static final String USER_SEARCH_STRING = "userSearchString";
    private static final String GROUP_SEARCH_STRING = "groupSearchString";

    private static final String CALLBACK_URL = "callbackUrl";
    private static final String PATTERN = "pattern";
    private static final String COUNTER_TYPE = "counterType";

    private static final String LDAP_UNIQUE_IDETIFIER = "ldapUniqueIdetifier";
    private static final String NAME = "name";
    private static final String DISPLAY_NAME = "displayName";
    private static final String DESCRIPTION = "description";
    private static final String IS_ORGANISATION_UNIT = "isOrganisationUnit";
    private static final String NEED_REASSIGNMENT = "needReassignment";
    private static final String PARENT_GROUP_ID = "parentGroupId";
    private static final String PATH = "path";
    private static final String CREATION_DATE = "creationDate";
    private static final String LAST_UPDATE = "lastUpdate";
    private static final String MEMBERS = "members";

    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String EMAIL = "email";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String ENABLED = "isEnabled";
    private static final String IS_LOCKED = "isLocked";
    private static final String ADMINISTRATOR = "isAdministrator";
    private static final String GROUPS = "groups";
    private static final String ROLE = "role";

    public static BiMap<String, ETenantParam> getTenantParamsMap(ETenantConfigurationType configurationType) {
        switch (configurationType) {
            case CASE_COUNTER_SETTINGS:
                return CASE_COUNTER_SETTINGS_MAP;
            case LDAP_CONNECTION_SETTINGS:
                return LDAP_CONNECTION_SETTINGS_MAP;
            case LDAP_USER_PARAMETERS_MAPPING:
                return LDAP_USER_PARAMETERS_MAPPING_MAP;
            case LDAP_GROUP_PARAMETERS_MAPPING:
                return LDAP_GROUP_PARAMETERS_MAPPING_MAP;
            default:
                return new ImmutableBiMap.Builder<String, ETenantParam>()
                        .build();
        }
    }

    private static final BiMap<String, ETenantParam> LDAP_CONNECTION_SETTINGS_MAP = new ImmutableBiMap.Builder<String, ETenantParam>()
            .put(PROVIDER_URL, TENANT_LDAP_CONNECTION_PROVIDER_URL)
            .put(SECURITY_AUTHENTICATION, TENANT_LDAP_CONNECTION_SECURITY_AUTHENTICATION)
            .put(USER, TENANT_LDAP_CONNECTION_USER)
            .put(PASSWORD, TENANT_LDAP_CONNECTION_PASSWORD)
            .put(USER_SEARCH_BASE, TENANT_LDAP_CONNECTION_USER_SEARCH_BASE)
            .put(GROUP_SEARCH_BASE, TENANT_LDAP_CONNECTION_GROUP_SEARCH_BASE)
            .put(USER_SEARCH_STRING, TENANT_LDAP_CONNECTION_USER_SEARCH_STRING)
            .put(GROUP_SEARCH_STRING, TENANT_LDAP_CONNECTION_GROUP_SEARCH_STRING)
            .build();

    private static final BiMap<String, ETenantParam> CASE_COUNTER_SETTINGS_MAP = new ImmutableBiMap.Builder<String, ETenantParam>()
            .put(CALLBACK_URL, TENANT_CASE_COUNTER_SETTINGS_CALLBACK_URL)
            .put(PATTERN, TENANT_CASE_COUNTER_SETTINGS_PATTERN)
            .put(COUNTER_TYPE, TENANT_CASE_COUNTER_SETTINGS_COUNTER_TYPE)
            .build();

    private static final BiMap<String, ETenantParam> LDAP_GROUP_PARAMETERS_MAPPING_MAP = new ImmutableBiMap.Builder<String, ETenantParam>()
            .put(LDAP_UNIQUE_IDETIFIER, TENANT_LDAP_GROUP_LDAP_UNIQUE_INDENTIFIER)
            .put(NAME, TENANT_LDAP_GROUP_NAME)
            .put(DISPLAY_NAME, TENANT_LDAP_GROUP_DISPLAY_NAME)
            .put(DESCRIPTION, TENANT_LDAP_GROUP_DESCRIPTION)
            .put(IS_ORGANISATION_UNIT, TENANT_LDAP_GROUP_IS_ORGANISATION_UNIT)
            .put(NEED_REASSIGNMENT, TENANT_LDAP_GROUP_NEED_REASSIGNMENT)
            .put(PARENT_GROUP_ID, TENANT_LDAP_GROUP_PARENT_GROUP_ID)
            .put(PATH, TENANT_LDAP_GROUP_PATH)
            .put(CREATION_DATE, TENANT_LDAP_GROUP_CREATION_DATE)
            .put(LAST_UPDATE, TENANT_LDAP_GROUP_LAST_UPDATE)
            .put(MEMBERS, TENANT_LDAP_GROUP_MEMBERS)
            .build();

    private static final BiMap<String, ETenantParam> LDAP_USER_PARAMETERS_MAPPING_MAP = new ImmutableBiMap.Builder<String, ETenantParam>()
            .put(LDAP_UNIQUE_IDETIFIER, TENANT_LDAP_USER_LDAP_UNIQUE_INDENTIFIER)
            .put(USERNAME, TENANT_LDAP_USER_USERNAME)
            .put(FIRST_NAME, TENANT_LDAP_USER_FIRST_NAME)
            .put(LAST_NAME, TENANT_LDAP_USER_LAST_NAME)
            .put(EMAIL, TENANT_LDAP_USER_EMAIL)
            .put(PHONE_NUMBER, TENANT_LDAP_USER_PHONE_NUMBER)
            .put(ENABLED, TENANT_LDAP_USER_ENABLED)
            .put(IS_LOCKED, TENANT_LDAP_USER_LOCKED)
            .put(ADMINISTRATOR, TENANT_LDAP_USER_ADMINISTRATOR)
            .put(CREATION_DATE, TENANT_LDAP_USER_CREATION_DATE)
            .put(LAST_UPDATE, TENANT_LDAP_USER_LAST_UPDATE)
            .put(GROUPS, TENANT_LDAP_USER_GROUPS)
            .put(ROLE, TENANT_LDAP_USER_ROLE)
            .build();

}
