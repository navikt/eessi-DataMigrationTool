package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.userProfile;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserProfileFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ELanguage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserProfile;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToUserProfileMapper extends AbstractMapToEntityMapper<MapHolder, UserProfile> {

    private final IamUserRepoExtended iamUserRepo;
    private final ProcessDefRepo processDefRepo;

    public MapToUserProfileMapper(
            final IamUserRepoExtended iamUserRepo,
            final ProcessDefRepo processDefRepo) {
        this.iamUserRepo = iamUserRepo;
        this.processDefRepo = processDefRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, UserProfile b, MappingContext context) {

        String processDefType = a.string(CURRENT_PROCESS_DEFINITION);
        ProcessDef processDef = processDefRepo.findById(processDefType);

        if (processDef != null) {
            b.setCurrentProcessDef(processDef);
        }

        String userName = a.string(USER_NAME);
        b.setIamUser(getIamUser(() -> userName, () -> iamUserRepo));

        b.setFilterActionType(a.string(FILTER_ACTION_TYPE));

        b.setAlarmAutoSetDays(a.integer(ALARM_SETTINGS_AUTO_SET_DAYS, true));
        b.setAlarmAutoSetOnSend(a.bool(ALARM_SETTINGS_AUTO_SET_ON_SEND, true));

        b.setClassicGroupByMonth(a.bool(CASE_CLASSIC_VIEW_GROUP_BY_MONTH, true));
        b.setClassicShowPreview(a.bool(CASE_CLASSIC_VIEW_SHOW_PREVIEW, true));

        b.setDocumentDsplayMode(a.string(DOCUMENTS_DISPLAY_MODE, true));
        b.setDocumentShowflags(a.bool(DOCUMENTS_SHOW_FLAGS, true));
        b.setDocumentSortby(a.string(DOCUMENTS_SORT_BY, true));

        b.setLocaleNumberFormat(a.string(LOCALIZATION_SETTINGS_NUMBER_FORMAT, true));
        b.setLocaleCurrency(a.string(LOCALIZATION_SETTINGS_CURRENCY, true));
        b.setLocaleDateFormat(a.string(LOCALIZATION_SETTINGS_DATE_FORMAT, true));
        b.setLocaleLang(ELanguage.lookup(a.string(LOCALIZATION_SETTINGS_LANGUAGE, true)).orElse(null));
        b.setLocaleTimeFormat(a.string(LOCALIZATION_SETTINGS_TIME_FORMAT, true));
        b.setLocaleTimezone(a.string(LOCALIZATION_SETTINGS_TIME_ZONE, true));

        b.setTimelineDisplayMode(a.string(CASE_TIMELINE_VIEW_DISPLAY_MODE, true));
        b.setTimelineDisplayThumbnails(a.bool(CASE_TIMELINE_VIEW_DISPLAY_THUMBNAILS, true));

        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final UserProfile b) {
        setDefaultAudit(b::setAudit);
    }
}
