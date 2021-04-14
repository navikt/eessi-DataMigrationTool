package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.activity;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.portal.EColour;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Activity;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ActivityFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToActivityMapper extends AbstractMapToEntityMapper<MapHolder, Activity> {

    private final IamUserRepo iamUserRepo;
    private final RinaCaseRepo rinaCaseRepo;

    public MapToActivityMapper(final IamUserRepo iamUserRepo, final RinaCaseRepo rinaCaseRepo) {
        this.iamUserRepo = iamUserRepo;
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Activity b, final MappingContext context) {
        mapId(a, b);
        mapRinaCase(a, b);
        mapColour(a, b);
        mapDate(a, ActivityFields.START_DATE, b::setStartDate);
        mapAudit(a, b);

        b.setIsDeleted(a.bool(ActivityFields.IS_DELETED, Boolean.FALSE));
        b.setMessage(a.string(ActivityFields.MESSAGE));

        b.setTitle(a.string(ActivityFields.TITLE));
    }

    private void mapId(final MapHolder a, final Activity b) {
        String activityId = a.string("id");
        if (StringUtils.isBlank(activityId)) {
            activityId = a.string("_id");
        }
        b.setId(activityId);
    }

    private void mapAudit(final MapHolder a, final Activity b) {
        setDefaultAudit(b::setAudit);

        String userId = a.string(ActivityFields.USER_ID);
        IamUser iamUser = RepositoryUtils.findById(userId, iamUserRepo::findById, IamUser.class);

        b.getAudit().setCreatedBy(iamUser.getId());
        b.getAudit().setUpdatedBy(iamUser.getId());
    }

    private void mapColour(final MapHolder a, final Activity b) {
        String colourName = a.string(ActivityFields.COLOUR);
        EColour eColour = Arrays.stream(EColour.values()).filter(
                colour -> colour.getByColourName(colourName) != null || colour.name().equalsIgnoreCase(colourName))
                .findFirst()
                .orElseThrow(() -> new DmtEnumNotFoundException(EColour.class, a.addPath(ActivityFields.COLOUR), colourName));

        b.setColour(eColour);
    }

    private void mapRinaCase(final MapHolder a, final Activity b) {
        String caseId = a.string(ActivityFields.CASE_ID);
        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);
        b.setRinaCase(rinaCase);
    }
}
