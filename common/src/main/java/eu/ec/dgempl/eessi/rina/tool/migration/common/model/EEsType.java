package eu.ec.dgempl.eessi.rina.tool.migration.common.model;

/**
 * Enum that defines the list of elasticsearch types
 */
public enum EEsType {
    // @formatter:off
    ADMINNOTIFICATIONTYPE("adminNotificationType"),
    LOGENTRY("logentry"),
    AUDITENTRY("auditentry"),
    EXCEPTION("exception"),
    DOCUMENTCONTENT("documentcontent"),
    PENDINGMESSAGE("pendingmessage"),
    PENDINGRECEIPT("pendingreceipt"),
    SIGNATURE("signature"),
    ATTACHMENT("attachment"),
    ATTACHMENTCONTENT("attachmentcontent"),
    COMMENT("comment"),
    DOCUMENT("document"),
    CASEMETADATA("casemetadata"),
    CASESTRUCTUREDMETADATA("casestructuredmetadata"),
    SUBDOCUMENT("subdocument"),
    TASKMETADATA("taskmetadata"),
    THUMBNAILCONTENT("thumbnailcontent"),
    USERMESSAGEHEADER("usermessageheader"),
    CHECKBUCKET("checkbucket"),
    CHECKDEFINITION("checkdefinition"),
    CHECKINSTANCE("checkinstance"),
    CONFIGURATION("configuration"),
    APPLICATIONPROFILE("applicationprofile"),
    ASSIGNMENTPOLICY("assignmentpolicy"),
    ASSIGNMENTTARGET("assignmenttarget"),
    DEFAULTDOCUMENTCONTENT("defaultdocumentcontent"),
    FIELDCHOOSER("fieldchooser"),
    PROCESSDEFINITION("processdefinition"),
    RINACOMPONENTS("rinacomponents"),
    SEARCHDEFINITION("searchdefinition"),
    USERPROFILE("userprofile"),
    EESSIAUTHKEYTOACCESSMAP("eessiAuthKeyToAccessMap"),
    EESSITOKEN("eessiToken"),
    EESSIREFRESHTOACCESSMAP("eessiRefreshToAccessMap"),
    ORGANISATION("organisation"),
    ACTIVITY("activity"),
    GROUP("group"),
    USER("user"),
    LETTERTEMPLATEENTRY("lettertemplateentry"),
    NOTIFICATION("notification"),
    ALARM("alarm"),
    FORM("form"),
    INITIALDOC("initialdoc"),
    LETTERTEMPLATE("letterTemplate"),
    LOCALIZATION("localization"),
    PROCESS("process"),
    REPORT("report"),
    SBDH("sbdh"),
    SED("sed"),
    TRANSACTION("transaction"),
    VOCABULARY("vocabulary"),
    SEQUENCES("sequences"),
    VERSIONS("versions"),
    JOB("job"),
    CONCEPT("concept");
    // @formatter:on

    private final String value;

    EEsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EEsType fromValue(String v) {
        for (EEsType c : EEsType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
