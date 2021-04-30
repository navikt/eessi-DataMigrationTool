package eu.ec.dgempl.eessi.rina.tool.migration.common.util;

import java.util.List;
import java.util.Map;

/**
 * Class with methods related to documents retrieved from Elasticsearch
 */
public class EsDocumentHelper {

    public static final String APPLICATION_ROLE = "applicationRole";
    public static final String IS_MULTI_STARTER = "isMultiStarter";
    public static final String STARTER_TYPE = "starterType";
    public static final String IS_STARTER_SENT = "isStarterSent";
    public static final String DOCTYPE = "docType";

    private static final List<String> STARTER_DOCUMENT_TYPES = List.of(
            "P5000",
            "P6000",
            "P7000",
            "P10000",
            "S006",
            "S008",
            "F018",
            "F021",
            "U001",
            "U003",
            "U001CB",
            "U005",
            "U009",
            "U007"
    );

    private static final List<String> STARTER_BUC_TYPES = List.of(
            "P_BUC_06",
            "S_BUC_18",
            "FB_BUC_3",
            "UB_BUC_02",
            "UB_BUC_01"
    );

    public static boolean isMultiStarterBUC(String type) {

        return type != null && STARTER_BUC_TYPES.contains(type);
    }

    public static boolean isTaskMetadataInvalidReferenceException(final Map<String, Object> params) {

        Object applicationRole = params.get(APPLICATION_ROLE);
        Object isMultiStarter = params.get(IS_MULTI_STARTER);
        Object starterType = params.get(STARTER_TYPE);
        Object isStarterSent = params.get(IS_STARTER_SENT);
        Object docType = params.get(DOCTYPE);

        boolean condition1 = applicationRole instanceof String && "PO".equalsIgnoreCase((String) applicationRole);
        boolean condition2 = isMultiStarter instanceof Boolean && (Boolean) isMultiStarter;
        boolean condition3 = starterType instanceof String && EsDocumentHelper.isStarterDocumentFromMultiStarterBUC((String) starterType);
        boolean condition4 = isStarterSent instanceof Boolean && (Boolean) isStarterSent;
        boolean condition5 = docType instanceof String && starterType instanceof String && !((String) docType).equalsIgnoreCase((String) starterType);

        return condition1 && condition2 && condition3 && condition4 && condition5;
    }

    private static boolean isStarterDocumentFromMultiStarterBUC(String type) {

        return type != null && STARTER_DOCUMENT_TYPES.contains(type);
    }
}