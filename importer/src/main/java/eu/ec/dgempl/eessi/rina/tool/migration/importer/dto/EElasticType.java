package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto;

/**
 * Enumeration of existing ElasticSearch types, which can be migrated to SQL. Each such type contains an index and a type to identify the
 * documents. The type is not mandatory.
 *
 * @author
 */
public enum EElasticType {

    NONE("none", "none", 1),

    // PRE-CASE IMPORTERS
    VOCABULARIES_CONCEPT("vocabularies", "concept", 2),
    PROCESSDEFINITION("configurations", "processdefinition", 3),
    ORGANISATION("entities", "organisation", 4),
    APPLICATION_PROFILE("configurations", "applicationprofile", 5),
    GLOBAL_CONFIGURATIONS_APPLICATION_PROFILE("globalconfigurations", "applicationprofile", 6),
    GROUP("identity", "group", 7),
    USER("identity", "user", 8),
    CONFIGURATIONS_FIELDCHOOSER("configurations", "fieldchooser", 9),
    USER_PROFILE("configurations", "userprofile", 10),
    CONFIGURATIONS_CONFIGURATION("configurations", "configuration", 11),
    CONFIGURATIONS_SEARCHDEFINITION("configurations", "searchdefinition", 12),
    CONFIGURATIONS_ASSIGNMENTPOLICY("configurations", "assignmentpolicy", 13),
    CONFIGURATIONS_ASSIGNMENTTARGET("configurations", "assignmenttarget", 14),
    RESOURCES_FORM("resources", "form", 15),
    RESOURCES_INITIALDOC("resources", "initialdoc", 16),
    RESOURCES_LETTERTEMPLATE("resources", "lettertemplate", 17),
    RESOURCES_LOCALIZATION("resources", "localization", 18),
    RESOURCES_ORGANISATION("resources", "organisation", 19),
    RESOURCES_SBDH("resources", "sbdh", 20),
    RESOURCES_SED("resources", "sed", 21),
    RESOURCES_TRANSACTION("resources", "transaction", 22),
    RESOURCES_PROCESS("resources", "process", 23),
    RESOURCES_REPORT("resources", "report", 24),
    RESOURCES_VOCABULARY("resources", "vocabulary", 25),
    CHECKS_CHECKDEFINITION("checks", "checkdefinition", 26),
    CHECKS_CHECKBUCKET("checks", "checkbucket", 27),
    CHECKS_CHECKINSTANCE("checks", "checkinstance", 28),
    ADMIN_ADMINNOTIFICATIONTYPE("admin", "adminNotificationType", 29),
    AUDIT_AUDITENTRY("audit", "auditentry", 30),

    // CASE IMPORTERS
    CASES_CASESTRUCTUREDMETADATA("cases", "casestructuredmetadata", 1),
    CASES_CASEMETADATA("cases", "casemetadata", 2),
    CASES_DOCUMENT("cases", "document", 3),
    CASES_ATTACHMENT("cases", "attachment", 4),
    CASES_COMMENT("cases", "comment", 5),
    CASES_SUBDOCUMENT("cases", "subdocument", 6),
    CASES_DOCUMENTCONTENT("cases", "documentcontent", 7),
    CASES_SIGNATURE("cases", "signature", 8),
    // actions need to be imported last in the case
    CASES_TASKMETADATA("cases", "taskmetadata", 9),
    NOTIFICATION("notifications", "notification", 10),
    NOTIFICATION_ALARM("notifications", "alarm", 11),

    // POST-CASE IMPORTERS
    ACTIVITY("identity", "activity", 1),
    BUSINESS_EXCEPTION_PENDING_MESSAGE("businessexceptions", "pendingmessage", 2),
    BUSINESS_EXCEPTION_EXCEPTION("businessexceptions", "exception", 3),
    BUSINESS_EXCEPTION_PENDING_SIGNATURE("businessexceptions", "signature", 4);

    private final String index;

    private final String type;

    private final int order;

    EElasticType(final String index, final String type, final int order) {
        this.index = index;
        this.type = type;
        this.order = order;
    }

    public String getIndex() {
        return this.index;
    }

    public String getType() {
        return this.type;
    }

    public int getOrder() {
        return order;
    }

}
