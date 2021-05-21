package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final IamUserRepo iamUserRepo;
    private final IamUserRepoExtended iamUserRepoExtended;
    private final DefaultValuesService defaultValuesService;

    public UserService(final IamUserRepo iamUserRepo,
            final IamUserRepoExtended iamUserRepoExtended,
            final DefaultValuesService defaultValuesService) {
        this.iamUserRepo = iamUserRepo;
        this.iamUserRepoExtended = iamUserRepoExtended;
        this.defaultValuesService = defaultValuesService;
    }

    public IamUser resolveUser(final String userId, final String username, final RinaCase rinaCase, final boolean nullIfNotFound) {

        // resolve by userId
        if (StringUtils.isNotBlank(userId)) {
            try {
                return RepositoryUtils.findById(userId, iamUserRepo::findById, IamUser.class);
            } catch (EntityNotFoundEessiRuntimeException ex) {
                if (nullIfNotFound) {
                    return null;
                }
                throw ex;
            }
        }

        // resolve by username
        if (StringUtils.isNotBlank(username)) {
            try {
                return RepositoryUtils.getIamUser(() -> username, () -> iamUserRepoExtended);
            } catch (EntityNotFoundEessiRuntimeException e) {
                logger.info(String.format("Could not resolve user by userId=%s and username=%s", userId, username));
            }
        }

        // resolve by tenantId
        String tenantId = getTenantId(rinaCase);
        if (StringUtils.isNotBlank(tenantId)) {
            String defaultUsername = defaultValuesService.getDefaultUsernameByTenantId(tenantId);
            try {
                IamUser iamUser = RepositoryUtils.getIamUser(() -> defaultUsername, () -> iamUserRepoExtended);
                logger.info(String.format("Resolved the user based on the default username [username=%s, tenant=%s]", defaultUsername,
                        tenantId));
                return iamUser;
            } catch (EntityNotFoundEessiRuntimeException ex) {
                logger.info(
                        String.format("Could not resolve user based on the default username [username=%s, tenant=%s]", defaultUsername,
                                tenantId));
            }
        }

        // use the default value
        if (nullIfNotFound) {
            return null;
        }

        throw new RuntimeException(
                String.format("Could not resolve user for [userId=%s, username=%s, tenantId=%s]", userId, username, tenantId));
    }

    public IamUser resolveUser(final String userId, final String username, final RinaCase rinaCase) {
        return resolveUser(userId, username, rinaCase, false);
    }

    private String getTenantId(final RinaCase rinaCase) {
        String tenantId = null;
        if (rinaCase != null && rinaCase.getTenant() != null) {
            tenantId = rinaCase.getTenant().getId();
        }
        return tenantId;
    }
}