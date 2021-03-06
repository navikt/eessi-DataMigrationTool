package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.user;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserFields.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamOrigin;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUserGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamOriginRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToIamUserMapper extends AbstractMapToEntityMapper<MapHolder, IamUser> {

    private static final Logger logger = LoggerFactory.getLogger(MapToIamUserMapper.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final IamOriginRepo iamOriginRepo;
    private final TenantRepo tenantRepo;
    private final DefaultValuesService defaultsService;

    public MapToIamUserMapper(IamOriginRepo iamOriginRepo, TenantRepo tenantRepo, DefaultValuesService defaultsService) {
        this.iamOriginRepo = iamOriginRepo;
        this.tenantRepo = tenantRepo;
        this.defaultsService = defaultsService;
    }

    @Override
    public void mapAtoB(MapHolder a, IamUser b, MappingContext context) {

        b.setId(a.string(UserFields.ID));

        mapTenant(a, b);
        mapLog(a, b);
        mapOrigin(a, b);
        mapSalt(a, b);
        // mapGroups() to be processed after mapTenant()
        mapGroups(a, b, context);

        b.setUsername(a.string(UserFields.USERNAME));
        b.setEmail(a.string(UserFields.EMAIL));
        b.setPassword(a.string(UserFields.PASSWORD));
        b.setIsSystem(false);
        b.setPhoneNumber(a.string(PHONE_NUMBER));
        b.setKeystoreAlias(a.string(KEYSTORE_ALIAS));

        setValue(a, FIRST_NAME, b::setFirstName);
        setValue(a, LAST_NAME, b::setLastName);

        setValueWithFallback(a, IS_ENABLED, ENABLED, b::setIsEnabled);
        setValueWithFallback(a, IS_DELETED, DELETED, b::setIsDeleted);
        setValueWithFallback(a, IS_ADMIN, ADMIN, b::setIsAdmin);
    }

    private void mapGroups(final MapHolder a, final IamUser b, final MappingContext context) {
        List<MapHolder> groups = a.listToMapHolder(UserFields.GROUPS);
        if (!CollectionUtils.isEmpty(groups)) {
            context.setProperty("tenantId", b.getTenant().getId());
            groups.stream().map(holder -> mapperFacade.map(holder, IamUserGroup.class, context)).forEach(b::addIamUserGroup);
        }
    }

    private void mapOrigin(final MapHolder a, final IamUser b) {
        String originId = a.string(UserFields.IAM_ORIGIN);
        if (StringUtils.isNotBlank(originId)) {
            IamOrigin origin = iamOriginRepo.findByName(originId);
            b.setIamOrigin(origin);
        }
    }

    private void mapSalt(final MapHolder a, final IamUser b) {
        if (StringUtils.isBlank(a.string(UserFields.SALT))) {
            if (StringUtils.isNotBlank(a.string(UserFields.PASSWORD))) {
                throw new RuntimeException("For user " + b.getUsername() + " the salt is null and the password is not null");
            }
            b.setSalt(generatePasswordSalt());
        } else {
            b.setSalt(a.string(UserFields.SALT));
        }
    }

    private void mapLog(final MapHolder a, final IamUser b) {
        setDefaultLog(b::setLog);

        mapDate(a, "creationDate", b.getLog()::setCreatedAt);
        mapDate(a, "lastUpdate", b.getLog()::setUpdatedAt);

        if (b.getLog().getUpdatedAt() == null) {
            b.getLog().setUpdatedAt(b.getLog().getCreatedAt());
        }
    }

    private void mapTenant(final MapHolder a, final IamUser b) {
        String tenantId = a.string(UserFields.TENANT_ID);
        Tenant tenant = tenantRepo.findById(tenantId);
        if (tenant == null) {
            throw new EntityNotFoundEessiRuntimeException(Tenant.class, UniqueIdentifier.id, tenantId);
        }
        b.setTenant(tenant);
    }

    private void setValue(final MapHolder a, final String path, Consumer<String> valueConsumer) {
        String value = a.string(path, true);
        if (Strings.isBlank(value)) {
            value = defaultsService.getDefaultValue(path);
            logger.info(String.format("User %s is empty. Setting default value %s", path, value));
        }
        valueConsumer.accept(value);
    }

    private void setValueWithFallback(MapHolder a, String valuePath, String fallbackValuePath, Consumer<Boolean> valueConsumer) {
        Boolean value = a.bool(valuePath);

        if (value == null) {
            value = a.bool(fallbackValuePath);
        }

        valueConsumer.accept(value);
    }

    /**
     * Generates a base64 encoded salt string for the password
     *
     * @return
     */
    private String generatePasswordSalt() {

        byte[] bytes = new byte[10];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
