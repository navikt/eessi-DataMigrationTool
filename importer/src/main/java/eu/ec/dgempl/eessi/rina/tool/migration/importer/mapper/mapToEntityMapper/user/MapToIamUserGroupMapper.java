package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.user;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUserGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Role;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserGroupFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToIamUserGroupMapper extends AbstractMapToEntityMapper<MapHolder, IamUserGroup> {

    private final IamGroupRepo iamGroupRepo;
    private final RoleRepo roleRepo;

    public MapToIamUserGroupMapper(
            final IamGroupRepo iamGroupRepo,
            final RoleRepo roleRepo) {
        this.iamGroupRepo = iamGroupRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, IamUserGroup b, MappingContext context) {

        String roleName = a.string(UserGroupFields.GROUP_ROLE);
        Role role = roleRepo.findByName(ERole.valueOf(roleName));
        if (role == null) {
            throw new EntityNotFoundEessiRuntimeException(Role.class, UniqueIdentifier.name, roleName);
        }
        b.setRole(role);

        String groupId = a.string(UserGroupFields.GROUP_ID);
        IamGroup group = iamGroupRepo.findById(groupId);
        if (group == null) {
            throw new EntityNotFoundEessiRuntimeException(IamGroup.class, UniqueIdentifier.id, groupId);
        }
        b.setIamGroup(group);
    }
}
