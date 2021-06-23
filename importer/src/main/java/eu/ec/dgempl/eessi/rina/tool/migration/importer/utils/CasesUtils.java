package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CasePrefill;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public final class CasesUtils {

    public static void addCasePrefills(MapHolder a, String prefillKey, RinaCase b, ECasePrefillGroup eCasePrefillGroup) {
        MapHolder prefills = a.getMapHolder(prefillKey);
        getPrefills(prefills, eCasePrefillGroup, "")
                .forEach(b::addCasePrefill);
    }

    private static List<CasePrefill> getPrefills(final MapHolder prefill, final ECasePrefillGroup prefillGroup, String path) {
        List<CasePrefill> casePrefills = new ArrayList<>();
        prefill.fields()
                .stream()
                .filter(key -> prefill.get(key, true) != null)
                .forEach(key -> {
                    Object value = prefill.get(key, true);

                    if (value instanceof String) {
                        CasePrefill casePrefill = new CasePrefill();
                        casePrefill.setKey(addPath(path, key));
                        casePrefill.setValue(String.valueOf(value));
                        casePrefill.setPrefillGroup(prefillGroup);
                        casePrefills.add(casePrefill);
                    }

                    if (value instanceof Map) {
                        casePrefills.addAll(getPrefills(
                                new MapHolder((Map<String, Object>) value, prefill.getVisitedFields(), prefill.addPath(key)),
                                prefillGroup,
                                addPath(path, key)));
                    }
                });

        return casePrefills;
    }

    public static String getCaseId(String caseId) {
        return isDefaultCase(caseId) ? null : caseId;
    }

    public static boolean isDefaultCase(String caseId) {
        return CaseFields.DEFAULT_CASE_ID.equalsIgnoreCase(caseId);
    }

    private static String addPath(String path, String currentKey) {
        return StringUtils.isNotBlank(path) ? path + "." + currentKey : currentKey;
    }

}
