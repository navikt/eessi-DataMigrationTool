package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.group;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamOriginRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.GroupFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToIamGroupMapper extends AbstractMapToEntityMapper<MapHolder, IamGroup> {

    private final IamGroupRepo iamGroupRepo;
    private final IamOriginRepo iamOriginRepo;
    private final TenantRepo tenantRepo;

    public MapToIamGroupMapper(final IamGroupRepo iamGroupRepo, final IamOriginRepo iamOriginRepo,
            final TenantRepo tenantRepo) {
        this.iamGroupRepo = iamGroupRepo;
        this.iamOriginRepo = iamOriginRepo;
        this.tenantRepo = tenantRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, IamGroup b, MappingContext context) {

        mapTenant(a, b);

        b.setId(a.string(GroupFields.ID));
        b.setName(a.string(GroupFields.NAME));
        b.setDisplayName(a.string(GroupFields.DISPLAY_NAME));
        b.setParentPath(a.string(GroupFields.PARENT_PATH));
        b.setDescription(a.string(GroupFields.DESCRIPTION));
        b.setDeleted(a.bool(GroupFields.IS_DELETED));
        b.setOrganisationUnit(a.bool(GroupFields.IS_ORG_UNIT, Boolean.FALSE));

        String iamOrigin = a.string(GroupFields.ORIGIN);
        if (StringUtils.isNotBlank(iamOrigin)) {
            b.setIamOrigin(iamOriginRepo.findByName(iamOrigin));
        }

        mapParentGroup(a, b);
        mapAudit(a, b);

    }

    private void mapParentGroup(final MapHolder a, final IamGroup b) {
        String parentGroupId = a.string("parentGroupId");
        if (StringUtils.isNotBlank(parentGroupId)) {
            IamGroup iamGroup = RepositoryUtils.findById(parentGroupId, iamGroupRepo::findById, IamGroup.class);
            b.setParentGroup(iamGroup);
        }
    }

    private void mapAudit(final MapHolder a, final IamGroup b) {
        setDefaultAudit(b::setAudit);

        mapDate(a, "creationDate", b.getAudit()::setCreatedAt);
        mapDate(a, "lastUpdate", b.getAudit()::setUpdatedAt);
    }

    private void mapTenant(final MapHolder a, final IamGroup b) {
        String institutionId = a.string(GroupFields.INSTITUTION_ID);
        Tenant tenant = tenantRepo.findById(institutionId);
        if (tenant == null) {
            throw new EntityNotFoundEessiRuntimeException(Tenant.class, UniqueIdentifier.id, institutionId);
        }
        b.setTenant(tenant);
    }
}
