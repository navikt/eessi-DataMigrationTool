package eu.ec.dgempl.eessi.rina.tool.migration.common.model;

/**
 * Enum that defines the combinations of elasticsearch indices and types
 */
public enum EEsIndexType {
    // @formatter:off
    ADMIN_ADMINNOTIFICATIONTYPE("admin_adminNotificationType"),
    AUDIT_AUDITENTRY("audit_auditentry"),
    BUSINESSEXCEPTIONS_EXCEPTION("businessexceptions_exception"),
    BUSINESSEXCEPTIONS_DOCUMENTCONTENT("businessexceptions_documentcontent"),
    BUSINESSEXCEPTIONS_PENDINGMESSAGE("businessexceptions_pendingmessage"),
//    BUSINESSEXCEPTIONS_PENDINGRECEIPT("businessexceptions_pendingreceipt"),
    BUSINESSEXCEPTIONS_SIGNATURE("businessexceptions_signature"),
    CASES_ATTACHMENT("cases_attachment"),
    CASES_ATTACHMENTCONTENT("cases_attachmentcontent"),
    CASES_COMMENT("cases_comment"),
    CASES_DOCUMENT("cases_document"),
    CASES_DOCUMENTCONTENT("cases_documentcontent"),
    CASES_CASEMETADATA("cases_casemetadata"),
    CASES_SIGNATURE("cases_signature"),
    CASES_CASESTRUCTUREDMETADATA("cases_casestructuredmetadata"),
    CASES_SUBDOCUMENT("cases_subdocument"),
    CASES_TASKMETADATA("cases_taskmetadata"),
//    CASES_THUMBNAILCONTENT("cases_thumbnailcontent"),
    CASES_USERMESSAGEHEADER("cases_usermessageheader"),
    CHECKS_CHECKBUCKET("checks_checkbucket"),
    CHECKS_CHECKDEFINITION("checks_checkdefinition"),
    CHECKS_CHECKINSTANCE("checks_checkinstance"),
    CONFIGURATIONS_CONFIGURATION("configurations_configuration"),
    CONFIGURATIONS_APPLICATIONPROFILE("configurations_applicationprofile"),
    CONFIGURATIONS_ASSIGNMENTPOLICY("configurations_assignmentpolicy"),
    CONFIGURATIONS_ASSIGNMENTTARGET("configurations_assignmenttarget"),
    CONFIGURATIONS_DEFAULTDOCUMENTCONTENT("configurations_defaultdocumentcontent"),
    CONFIGURATIONS_FIELDCHOOSER("configurations_fieldchooser"),
    CONFIGURATIONS_PROCESSDEFINITION("configurations_processdefinition"),
//    CONFIGURATIONS_RINACOMPONENTS("configurations_rinacomponents"),
    CONFIGURATIONS_SEARCHDEFINITION("configurations_searchdefinition"),
    CONFIGURATIONS_USERPROFILE("configurations_userprofile"),
    ENTITIES_ORGANISATION("entities_organisation"),
    GLOBALCONFIGURATIONS_APPLICATIONPROFILE("globalconfigurations_applicationprofile"),
    IDENTITY_ACTIVITY("identity_activity"),
    IDENTITY_GROUP("identity_group"),
    IDENTITY_USER("identity_user"),
    NOTIFICATIONS_NOTIFICATION("notifications_notification"),
    NOTIFICATIONS_ALARM("notifications_alarm"),
    RESOURCES_ORGANISATION("resources_organisation"),
    RESOURCES_FORM("resources_form"),
    RESOURCES_INITIALDOC("resources_initialdoc"),
    RESOURCES_LETTERTEMPLATE("resources_letterTemplate"),
    RESOURCES_LOCALIZATION("resources_localization"),
    RESOURCES_PROCESS("resources_process"),
    RESOURCES_REPORT("resources_report"),
    RESOURCES_SBDH("resources_sbdh"),
    RESOURCES_SED("resources_sed"),
    RESOURCES_TRANSACTION("resources_transaction"),
    RESOURCES_VOCABULARY("resources_vocabulary"),
    SEQUENCES_SEQUENCES("sequences_sequences"),
    SEQUENCES_VERSIONS("sequences_versions"),
    SYNCHRONIZATIONS_JOB("synchronizations_job"),
    VOCABULARIES_CONCEPT("vocabularies_concept");
    // @formatter:on

    // APCLIENTLOG_LOGENTRY("apclientlog_logentry"),
    // BONITALOG_LOGENTRY("bonitalog_logentry"),
    // LETTERTEMPLATES_LETTERTEMPLATEENTRY("lettertemplates_lettertemplateentry"),
    // RESTLOG_LOGENTRY("restlog_logentry"),
    // TOKEN_EESSIAUTHKEYTOACCESSMAP("token_eessiAuthKeyToAccessMap"),
    // TOKEN_EESSIREFRESHTOACCESSMAP("token_eessiRefreshToAccessMap"),
    // TOKEN_EESSITOKEN("token_eessiToken"),

    private final String value;

    EEsIndexType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EEsIndexType fromValue(String v) {
        for (EEsIndexType c : EEsIndexType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
