package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;

@Service
public class UserGroupService {

    private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

    private final IamGroupRepo iamGroupRepo;
    private final DefaultValuesService defaultValuesService;

    public UserGroupService(final IamGroupRepo iamGroupRepo,
            final DefaultValuesService defaultValuesService) {
        this.iamGroupRepo = iamGroupRepo;
        this.defaultValuesService = defaultValuesService;
    }

    public IamGroup resolveUserGroup(final String groupId, final String tenantId) {

        IamGroup group = null;
        String defaultGroupName = null;

        if (StringUtils.isNotBlank(groupId)) {
            group = iamGroupRepo.findById(groupId);
        } else if (StringUtils.isNotBlank(tenantId)) {
            defaultGroupName = defaultValuesService.getDefaultUserGroupByTenantId(tenantId);
            logger.info(
                    String.format("Resolving user group with default user group name=%s for tenantId=%s from default user groups file",
                            defaultGroupName, tenantId));
            group = iamGroupRepo.findByTenantIdAndName(tenantId, defaultGroupName);
        }

        if (group == null) {
            throw new EntityNotFoundEessiRuntimeException(IamGroup.class, UniqueIdentifier.id,
                    StringUtils.isNotBlank(groupId) ? groupId : defaultGroupName);
        }
        return group;
    }
}