package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.notification;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NotificationAlarm;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NotificationAlarmFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToNotificationAlarmMapper extends AbstractMapToEntityMapper<MapHolder, NotificationAlarm> {

    private final RinaCaseRepo rinaCaseRepo;
    private final UserService userService;

    public MapToNotificationAlarmMapper(final RinaCaseRepo rinaCaseRepo,
            final UserService userService) {
        this.rinaCaseRepo = rinaCaseRepo;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final NotificationAlarm b, final MappingContext context) {
        b.setId(a.string(NotificationAlarmFields.ID));
        b.setDescription(a.string(NotificationAlarmFields.DESCRIPTION));

        mapDate(a, NotificationAlarmFields.DATE, b::setDate);
        mapCase(a, b);
        mapAudit(a, b);
    }

    private void mapCase(final MapHolder a, final NotificationAlarm b) {
        String caseId = a.string(NotificationAlarmFields.CASE_ID);

        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);

        b.setRinaCase(rinaCase);
    }

    private void mapAudit(final MapHolder a, final NotificationAlarm b) {
        setDefaultAudit(b::setAudit);

        String creatorId = a.string(NotificationAlarmFields.CREATOR_ID, true);
        String creatorName = a.string(NotificationAlarmFields.CREATOR_NAME, true);
        IamUser creator = userService.resolveUser(creatorId, creatorName, b.getRinaCase());

        b.getAudit().setCreatedBy(creator.getId());
        b.getAudit().setUpdatedBy(creator.getId());

        mapDate(a, NotificationAlarmFields.CREATION_DATE, b.getAudit()::setCreatedAt);
        mapDate(a, NotificationAlarmFields.CREATION_DATE, b.getAudit()::setUpdatedAt);
    }
}
