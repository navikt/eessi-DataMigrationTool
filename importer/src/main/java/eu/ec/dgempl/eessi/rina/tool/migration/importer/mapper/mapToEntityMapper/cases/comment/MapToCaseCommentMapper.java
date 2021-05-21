package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.comment;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseComment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CommentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToCaseCommentMapper extends AbstractMapToEntityMapper<MapHolder, CaseComment> {

    private final RinaCaseRepo rinaCaseRepo;
    private final UserService userService;

    public MapToCaseCommentMapper(final RinaCaseRepo rinaCaseRepo,
            final UserService userService) {
        this.rinaCaseRepo = rinaCaseRepo;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final CaseComment b, final MappingContext context) {
        mapRinaCase(a, b);
        mapAudit(a, b);

        b.setId(a.string(CommentFields.ID));
        b.setText(a.string(CommentFields.COMMENT));

    }

    private void mapAudit(final MapHolder a, final CaseComment b) {
        setDefaultAudit(b::setAudit);

        mapDate(a, CommentFields.DATE, b.getAudit()::setCreatedAt);
        mapDate(a, CommentFields.DATE, b.getAudit()::setUpdatedAt);

        String creatorId = a.string(CommentFields.CREATOR_ID, true);
        String creatorName = a.string(CommentFields.CREATOR_NAME, true);
        IamUser creator = userService.resolveUser(creatorId, creatorName, b.getRinaCase());

        b.getAudit().setCreatedBy(creator.getId());
        b.getAudit().setUpdatedBy(creator.getId());

    }

    private void mapRinaCase(final MapHolder a, final CaseComment b) {
        String caseId = a.string(CommentFields.CASE_ID);
        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);
        b.setRinaCase(rinaCase);
    }
}
