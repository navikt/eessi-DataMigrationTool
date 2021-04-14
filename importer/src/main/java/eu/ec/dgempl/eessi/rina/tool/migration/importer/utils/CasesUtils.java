package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CasePrefill;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CommentFields;

public final class CasesUtils {

    public static void addCasePrefills(MapHolder a, String prefillKey, RinaCase b, ECasePrefillGroup eCasePrefillGroup) {
        MapHolder prefills = a.getMapHolder(prefillKey);
        getPrefills(prefills, eCasePrefillGroup)
                .forEach(b::addCasePrefill);
    }

    private static List<CasePrefill> getPrefills(final MapHolder prefill, final ECasePrefillGroup prefillGroup) {
        return prefill.fields()
                .stream()
                .filter(key -> prefill.get(key, true) != null)
                .map(key -> {
                    Object value = prefill.get(key, true);

                    CasePrefill casePrefill = new CasePrefill();
                    casePrefill.setKey(key);
                    casePrefill.setValue(value != null ? String.valueOf(value) : null);
                    casePrefill.setPrefillGroup(prefillGroup);
                    return casePrefill;
                })
                .collect(Collectors.toList());
    }

    public static String getCaseId(String caseId) {
        return isDefaultCase(caseId) ? null : caseId;
    }

    public static boolean isDefaultCase(String caseId) {
        return CaseFields.DEFAULT_CASE_ID.equalsIgnoreCase(caseId);
    }

    public static class Comments {

        public static IamUser getCreator(MapHolder a, IamUserRepo iamUserRepo, IamUserRepoExtended iamUserRepoExtended) {
            String creatorId = a.string(CommentFields.CREATOR_ID, true);
            if (StringUtils.isNotBlank(creatorId)) {
                return RepositoryUtils.findById(creatorId, iamUserRepo::findById, IamUser.class);
            } else {
                String creatorName = a.string(CommentFields.CREATOR_NAME, true);
                return RepositoryUtils.getIamUser(() -> creatorName, () -> iamUserRepoExtended);
            }
        }
    }
}
