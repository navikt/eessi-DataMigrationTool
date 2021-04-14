package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.user;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserFields.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToIamUserMapper extends AbstractMapToEntityMapper<MapHolder, IamUser> {

    private final IamOriginRepo iamOriginRepo;
    private final TenantRepo tenantRepo;

    public MapToIamUserMapper(IamOriginRepo iamOriginRepo, TenantRepo tenantRepo) {
        this.iamOriginRepo = iamOriginRepo;
        this.tenantRepo = tenantRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, IamUser b, MappingContext context) {

        b.setId(a.string(UserFields.ID));

        mapTenant(a, b);
        mapLog(a, b);

        String originId = a.string(UserFields.IAM_ORIGIN);
        if (StringUtils.isNotBlank(originId)) {
            IamOrigin origin = iamOriginRepo.findByName(originId);
            b.setIamOrigin(origin);
        }

        b.setUsername(a.string(UserFields.USERNAME));
        b.setFirstName(a.string(UserFields.FIRS_NAME));
        b.setLastName(a.string(UserFields.LAST_NAME));
        b.setEmail(a.string(UserFields.EMAIL));
        b.setPassword(a.string(UserFields.PASSWORD));
        b.setSalt(a.string(UserFields.SALT));
        b.setIsSystem(false);
        b.setIsEnabled(a.bool(UserFields.IS_ENABLED));
        b.setIsDeleted(a.bool(UserFields.IS_DELETED));
        b.setIsAdmin(a.bool(UserFields.IS_ADMIN));
        b.setPhoneNumber(a.string(PHONE_NUMBER));
        b.setKeystoreAlias(a.string(KEYSTORE_ALIAS));

        List<MapHolder> groups = a.listToMapHolder(UserFields.GROUPS);
        if (!CollectionUtils.isEmpty(groups)) {
            groups.stream()
                    .map(holder -> mapperFacade.map(holder, IamUserGroup.class, context))
                    .forEach(b::addIamUserGroup);
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
}
