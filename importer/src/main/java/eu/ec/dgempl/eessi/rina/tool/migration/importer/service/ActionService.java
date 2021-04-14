package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
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
     * @param actionDO
     */
    public void saveAction(final ActionDO actionDO) {

        PreconditionsHelper.notNull(actionDO, "actionDO");
        Action action = beanMapper.map(actionDO, Action.class);
        actionRepo.saveAndFlush(action);
    }

}
