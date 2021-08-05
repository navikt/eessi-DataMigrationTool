package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.model.enumtypes.portal.ETagCategory;
import eu.ec.dgempl.eessi.rina.model.enumtypes.portal.ETagType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ActionTag;
import eu.ec.dgempl.eessi.rina.repo.ActionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.BeanMapper;

/**
 * Service class responsible for operations on {@code ActionDO} objects from the BUC engine model.
 */
@Service
public class ActionService {

    private static final Logger logger = LoggerFactory.getLogger(ActionService.class);

    @Autowired
    private ActionRepo actionRepo;

    @Autowired
    private BeanMapper beanMapper;

    /**
     * Persists the given {@code actionDO} object.
     *
     * @param actionDO the generated BUC-Engine action
     */
    public void saveAction(final ActionDO actionDO) {

        Action action = mapAction(actionDO, null, null, null, null);
        actionRepo.saveAndFlush(action);
    }

    /**
     * Persists the given {@code actionDO} object and the newly created actionTag
     *
     * @param actionDO the generated BUC-Engine action
     */
    public void saveActionAndTag(final ActionDO actionDO,
            final String displayName,
            final String displayType,
            final String template,
            final String templateVersion) {
        Action action = mapAction(actionDO, displayName, displayType, template, templateVersion);

        createAndSetActionTag(action);
        action.setIsCaseRelated(true);
        action.setIsDocumentRelated(false);
        action.setCanClose(true);
        action.setHasBusinessValidation(true);
        actionRepo.saveAndFlush(action);
    }

    private Action mapAction(
            final ActionDO actionDO,
            final String displayName,
            final String displayType,
            final String template,
            final String templateVersion
    ) {
        PreconditionsHelper.notNull(actionDO, "actionDO");
        Action action = beanMapper.map(actionDO, Action.class);
        action.setDisplayName(displayName);
        action.setDisplayType(displayType);
        action.setTemplate(template);
        action.setTemplateVersion(templateVersion);
        return action;
    }

    private void createAndSetActionTag(Action action) {
        ActionTag actionTag = new ActionTag(action, ETagType.ADMIN, ETagCategory.CASE_ACTIONS);
        action.setActionTag(actionTag);
    }

}
