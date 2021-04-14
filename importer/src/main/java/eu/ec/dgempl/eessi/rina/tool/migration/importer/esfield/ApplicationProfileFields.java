package eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield;

import static eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam.*;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;

public class ApplicationProfileFields {

    public static final String TENANTS = "tenants";
    public static final String MESSAGING_SETTINGS_INSTITUTION_ALIAS_MAPPINGS = "messagingSettings.institutionAliasMappings";
    public static final String DEFAULT_USER_PROFILE = "defaultUserProfile";

    public static final String CASE_RETENTION_POLICIES = "casesRetentionPolicies";
    public static final String CASE_RETENTION_POLICIES_TABLE = "caseRetentionPoliciesTable";

    public static final String MESSAGE_RETENTION_POLICIES = "messagesRetentionPolicies";

    public static final String ARCHIVING_REPOSITORY_POLICIES = "archivingRepositoryPolicies";

    public static final String ARCHIVING_REPOSITORIES = "archivingRepositories";
    public static final String ARCHIVING_REPOSITORIES_LIST = "archivingRepositoriesList";

    public static final String ARCHIVING_POLICIES = "archivingPolicies";
    public static final String ARCHIVING_POLICIES_TABLE = "archivingPoliciesTable";

    public static final String NIE_SUBSCRIPTIONS = "nieSubscriptions";
    public static final String EVENTS_SUBSCRIPTIONS = "eventsSubscriptions";

    public static class ApplicationProfile {
        public static final String ADMIN_MANAGER_PASSWORD = "adminManagerPassword";
        public static final String ORGANISATION_SYSTEM_PASSWORD = "organizationSystemPassword";
        public static final String ATTACHMENT_DIRECTORY_PATH = "attachmentDirectoryPath";
        public static final String ATTACHMENT_MAX_FILE_SIZE = "attachmentMaxFileSize";
        public static final String AUTO_SYNC_CDM = "autoSyncCDM";
        public static final String AUTO_SYNC_IR = "autoSyncIR";
        public static final String BULK_SED_MAX_NUMBER_CHILDREN = "bulkSEDMaxNumberChildren";
        public static final String CHARACTER_SET = "characterSet";
        public static final String DEFAULT_CRITICALITY = "defaultCriticality";
        public static final String DEFAULT_IMPORTANCE = "defaultImportance";
        public static final String DEFAULT_LANGUAGE = "defaultLanguage";
        public static final String FORM_REPOSITORY_PATH = "formRepositoryPath";
        public static final String LETTER_TEMPLATE_REPOSITORY_PATH = "letterTemplatesRepositoryPath";
        public static final String LOCALIZATION_REPOSITORY_PATH = "localizationRepositoryPath";
        public static final String MAX_IDLE_TIME = "maxIdleTime";
        public static final String PDP_ASSIGNMENTS_IMPL_CLASS = "pdpAssignmentsImplClass";
        public static final String PROCESS_ASSIGNMENTS_POLICY = "processAssignmentsPolicy";
        public static final String RESOURCES_DISK_PATH = "resourcesDiskPath";
        public static final String SED_VALIDATION_MODE = "sedValidationMode";
        public static final String SHOW_FULL_ERROR = "showFullError";
        public static final String SHOW_PROCESS_VERSION = "showProcessVersion";
        public static final String SHOW_SOFTWARE_VERSIONS = "showSoftwareVersions";
        public static final String TICKET_DESTINATION_EMAIL = "ticketDestinationEmail";
        public static final String TICKET_RELAY_EMAIL = "ticketRelayEmail";
        public static final String TICKET_RELAY_PASSWORD = "ticketRelayPassword";
        public static final String TICKET_RELAY_SERVER = "ticketRelayServer";
        public static final String APPLICATION_ID = "applicationId";
        public static final String USE_APPLICATION_ID = "useApplicationId";
        public static final String VALIDATE_SED = "validateSED";
        public static final String NAME = "name";
        public static final String RECOVERY_VOLUME_PATH = "recoveryVolume";
        public static final String WHOAMI_ID = "whoamiId";
        public static final String MULTITENANT_MODE = "multitenantMode";

        public static BiMap<String, EGlobalParam> GLOBAL_PARAM_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(ADMIN_MANAGER_PASSWORD, APP_PROFILE_ADMIN_MANAGER_PASSWORD)
                .put(ORGANISATION_SYSTEM_PASSWORD, APP_PROFILE_ORGANIZATION_SYSTEM_PASSWORD)
                .put(ATTACHMENT_DIRECTORY_PATH, APP_PROFILE_ATTACHMENT_DIRECTORY_PATH)
                .put(ATTACHMENT_MAX_FILE_SIZE, APP_PROFILE_ATTACHMENT_FILE_SIZE_MAX)
                .put(AUTO_SYNC_CDM, APP_PROFILE_AUTOSYNC_CDM)
                .put(AUTO_SYNC_IR, APP_PROFILE_AUTOSYNC_IR)
                .put(BULK_SED_MAX_NUMBER_CHILDREN, APP_PROFILE_BULKSED_CHILDREN_NO_MAX)
                .put(CHARACTER_SET, APP_PROFILE_CHARACTER_SET)
                .put(DEFAULT_CRITICALITY, APP_PROFILE_DEFAULT_CRITICALITY)
                .put(DEFAULT_IMPORTANCE, APP_PROFILE_DEFAULT_IMPORTANCE)
                .put(DEFAULT_LANGUAGE, APP_PROFILE_DEFAULT_LANGUAGE)
                .put(FORM_REPOSITORY_PATH, APP_PROFILE_FORM_REPOSITORY_PATH)
                .put(LETTER_TEMPLATE_REPOSITORY_PATH, APP_PROFILE_LETTERTEMPLATES_REPOSITORY_PATH)
                .put(LOCALIZATION_REPOSITORY_PATH, APP_PROFILE_LOCALISATION_REPOSITORY_PATH)
                .put(MAX_IDLE_TIME, APP_PROFILE_IDLETIME_MAX)
                .put(PDP_ASSIGNMENTS_IMPL_CLASS, APP_PROFILE_PDP_ASSIGNMENT_IMPL_CLASS)
                .put(PROCESS_ASSIGNMENTS_POLICY, APP_PROFILE_PROCESS_ASSIGNMENTS_POLICY)
                .put(RESOURCES_DISK_PATH, APP_PROFILE_RESOURCES_PATH)
                .put(SED_VALIDATION_MODE, APP_PROFILE_SED_VALIDATION_MODE)
                .put(SHOW_FULL_ERROR, APP_PROFILE_FULL_ERROR_SHOW_FLAG)
                .put(SHOW_PROCESS_VERSION, APP_PROFILE_PROCESS_VERSION_FHOW_FLAG)
                .put(SHOW_SOFTWARE_VERSIONS, APP_PROFILE_SOFTWARE_VERSIONS_SHOW_FLAG)
                .put(TICKET_DESTINATION_EMAIL, APP_PROFILE_TICKET_DESTINATION_EMAIL)
                .put(TICKET_RELAY_EMAIL, APP_PROFILE_TICKET_RELAY_EMAIL)
                .put(TICKET_RELAY_PASSWORD, APP_PROFILE_TICKET_RELAY_PASSWORD)
                .put(TICKET_RELAY_SERVER, APP_PROFILE_TICKET_RELAY_SERVER)
                .put(APPLICATION_ID, APP_PROFILE_APPLICATION_ID)
                .put(USE_APPLICATION_ID, APP_PROFILE_USE_APPLICATION_ID_FLAG)
                .put(VALIDATE_SED, APP_PROFILE_SED_VALIDATE_FLAG)
                .put(NAME, APP_PROFILE_NAME)
                .put(RECOVERY_VOLUME_PATH, APP_PROFILE_RECOVERY_VOLUME_PATH)
                .put(WHOAMI_ID, APP_PROFILE_WHOAMI)
                .build();
    }

    public static class DefaultUserProfile {
        public static final String FILTER_ACTION_TYPE = "filterActionType";
        public static final String CASE_CLASSIC_VIEW_GROUP_BY_MONTH = "caseClassicView.groupByMonth";
        public static final String CASE_CLASSIC_VIEW_SHOW_PREVIEW = "caseClassicView.showPreview";
        public static final String CASE_TIMELINE_VIEW_DISPLAY_MODE = "caseTimelineView.displayMode";
        public static final String CASE_TIMELINE_VIEW_DISPLAY_THUMBNAILS = "caseTimelineView.displayThumbnails";
        public static final String DOCUMENTS_DISPLAY_MODE = "documents.displayMode";
        public static final String DOCUMENTS_SHOW_FLAGS = "documents.showFlags";
        public static final String DOCUMENTS_SORT_BY = "documents.sortBy";
        public static final String LOCALIZATION_SETTINGS_CURRENCY = "localizationSettings.currency";
        public static final String LOCALIZATION_SETTINGS_DATE_FORMAT = "localizationSettings.dateFormat";
        public static final String LOCALIZATION_SETTINGS_LANGUAGE = "localizationSettings.language";
        public static final String LOCALIZATION_SETTINGS_NUMBER_FORMAT = "localizationSettings.numberFormat";
        public static final String LOCALIZATION_SETTINGS_TIME_FORMAT = "localizationSettings.timeFormat";
        public static final String LOCALIZATION_SETTINGS_TIME_ZONE = "localizationSettings.timeZone";
        public static final String ALARM_SETTINGS_AUTO_SED_ON_SEND = "alarmSettings.autoSetOnSend";
        public static final String ALARM_SETTINGS_AUTO_SET_DAYS = "alarmSettings.autoSetDays";

        public static BiMap<String, EGlobalParam> DEFAULT_USER_PROFILE_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(FILTER_ACTION_TYPE, USER_PROFILE_FILTERACTION_TYPE)
                .put(CASE_CLASSIC_VIEW_GROUP_BY_MONTH, USER_PROFILE_CASECLASSICVIEW_GROUPBYMONTH_FLAG)
                .put(CASE_CLASSIC_VIEW_SHOW_PREVIEW, USER_PROFILE_CASECLASSICVIEW_SHOWPREVIEW_FLAG)
                .put(CASE_TIMELINE_VIEW_DISPLAY_MODE, USER_PROFILE_CASETIMELINEVIEW_DISPLAYMODE)
                .put(CASE_TIMELINE_VIEW_DISPLAY_THUMBNAILS, USER_PROFILE_CASETIMELINEVIEW_DISPLAYTHUMPNAILS_FLAG)
                .put(DOCUMENTS_DISPLAY_MODE, USER_PROFILE_DOCUMENTS_DISPLAYMODE)
                .put(DOCUMENTS_SHOW_FLAGS, USER_PROFILE_DOCUMENTS_SHOWFLAGS_FLAG)
                .put(DOCUMENTS_SORT_BY, USER_PROFILE_DOCUMENTS_SORTBY)
                .put(LOCALIZATION_SETTINGS_CURRENCY, USER_PROFILE_LOCALISATIONSETTING_CURRENCY)
                .put(LOCALIZATION_SETTINGS_DATE_FORMAT, USER_PROFILE_LOCALISATIONSETTING_DATEFORMAT)
                .put(LOCALIZATION_SETTINGS_LANGUAGE, USER_PROFILE_LOCALISATIONSETTING_LANGUAGE)
                .put(LOCALIZATION_SETTINGS_NUMBER_FORMAT, USER_PROFILE_LOCALISATIONSETTING_NUMBERFORMAT)
                .put(LOCALIZATION_SETTINGS_TIME_FORMAT, USER_PROFILE_LOCALISATIONSETTING_TIMEFORMAT)
                .put(LOCALIZATION_SETTINGS_TIME_ZONE, USER_PROFILE_LOCALISATIONSETTING_TIMEZONE)
                .put(ALARM_SETTINGS_AUTO_SED_ON_SEND, USER_PROFILE_ALARMSETTING_AUTOSET_SEND_FLAG)
                .put(ALARM_SETTINGS_AUTO_SET_DAYS, USER_PROFILE_ALARMSETTING_AUTOSET_DAYS)
                .build();
    }

    public static class CaseRetentionPolicies {
        public static final String DEFAULT_CASE_RETENTION_PERIOD = "defaultCaseRetentionPeriod";

        public static BiMap<String, EGlobalParam> CASE_RETENTION_POLICIES_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(DEFAULT_CASE_RETENTION_PERIOD, PROCESS_CASES_RETENTION_PERIOD)
                .build();
    }

    public static class MessageRetentionPolicies {
        public static final String DEFAULT_MESSAGE_RETENTION_PERIOD = "defaultMessageRetentionPeriod";

        public static BiMap<String, EGlobalParam> MESSAGE_RETENTION_POLICIES_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(DEFAULT_MESSAGE_RETENTION_PERIOD, MESSAGING_MESSAGE_RETENTION_PERIOD)
                .build();
    }

    public static class ArchivingRepositories {
        public static final String DEFAULT_CASE_ARCHIVING_VOLUME = "defaultCaseArchivingVolume";
        public static final String DEFAULT_MESSAGE_VOLUME = "defaultMessagesVolume";

        public static BiMap<String, EGlobalParam> ARCHIVING_REPOSITORIES_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(DEFAULT_CASE_ARCHIVING_VOLUME, ARCHIVING_REPOSITORY_CASES_VOLUME_DEFAULT)
                .put(DEFAULT_MESSAGE_VOLUME, ARCHIVING_REPOSITORY_MESSAGES_VOLUME_DEFAULT)
                .build();
    }

    public static class ArchivingPolicies {
        public static final String DEFAULT_ARCHIVING_PERIOD = "defaultArchivingPeriod";
        public static final String ARCHIVE_MESSAGES = "archiveMessages";

        public static BiMap<String, EGlobalParam> ARCHIVING_POLICIES_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(DEFAULT_ARCHIVING_PERIOD, ARCHIVING_POLICY_PERIOD_DEFAULT)
                .put(ARCHIVE_MESSAGES, ARCHIVING_POLICY_MESSAGES_FLAG)
                .build();
    }

    public static class NieSubscriptions {
        public static final String NIE_CLIENT_IMPL_CLASS = "nieClientImplClass";

        public static BiMap<String, EGlobalParam> NIE_SUBSCRIPTIONS_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
                .put(NIE_CLIENT_IMPL_CLASS, NIE_SUBSCRIPTION_CLIENTIMPLCLASS_DEFAULT)
                .build();
    }
}
