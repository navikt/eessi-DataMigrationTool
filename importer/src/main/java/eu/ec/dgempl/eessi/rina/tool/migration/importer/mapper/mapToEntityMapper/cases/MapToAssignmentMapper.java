package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignmentFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserOrGroupType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Assignment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Role;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAssignmentMapper extends AbstractMapToEntityMapper<MapHolder, Assignment> {

    private final IamGroupRepo iamGroupRepo;
    private final IamUserRepo iamUserRepo;
    private final RoleRepo roleRepo;

    public MapToAssignmentMapper(
            final IamGroupRepo iamGroupRepo,
            final IamUserRepo iamUserRepo,
            final RoleRepo roleRepo) {
        this.iamGroupRepo = iamGroupRepo;
        this.iamUserRepo = iamUserRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Assignment b, final MappingContext context) {
        mapId(a, b, context);

        mapRole(a, b);

        List<MapHolder> actors = a.listToMapHolder(USER_GROUPS);
        if (CollectionUtils.isNotEmpty(actors)) {
            mapUsers(actors, b);
            mapGroups(actors, b);
        }
    }

    private void mapId(final MapHolder a, final Assignment b, final MappingContext context) {
        String caseId = (String) context.getProperty("caseId");
        String id = a.string(ID);

        b.setId(String.join("_", caseId, id));
    }

    private void mapUsers(final List<MapHolder> actors, final Assignment b) {
        mapIam(actors, EUserOrGroupType.USER, this::findIamUserById, b::addIamUser);
    }

    private void mapGroups(final List<MapHolder> actors, final Assignment b) {
        mapIam(actors, EUserOrGroupType.GROUP, this::findIamGroupById, b::addIamGroup);
    }

    private void mapRole(final MapHolder a, final Assignment b) {
        String roleName = a.string(NAME);
        ERole eRole = ERole.lookup(roleName).orElseThrow(() -> new DmtEnumNotFoundException(ERole.class, a.addPath(NAME), roleName));

        Role role = roleRepo.findByName(eRole);
        if (role == null) {
            throw new EntityNotFoundEessiRuntimeException(Role.class, UniqueIdentifier.name, roleName);
        }

        b.setRole(role);
    }

    private <T> void mapIam(
            final List<MapHolder> actors,
            final EUserOrGroupType eUserOrGroupType,
            final Function<String, T> mapper,
            final Consumer<T> collector) {

        List<String> userIds = getUserGroupIdsByType(actors, eUserOrGroupType);
        if (CollectionUtils.isNotEmpty(userIds)) {
            userIds.stream()
                    .map(mapper)
                    .forEach(collector);
        }
    }

    private List<String> getUserGroupIdsByType(List<MapHolder> actors, EUserOrGroupType eUserOrGroupType) {
        return actors.stream()
                .filter(actor -> eUserOrGroupType == EUserOrGroupType.lookup(actor.string(TYPE))
                        .orElseThrow(
                                () -> new DmtEnumNotFoundException(EUserOrGroupType.class, actor.addPath(TYPE), actor.string(TYPE))
                        ))
                .map(actor -> actor.string(ID))
                .collect(Collectors.toList());
    }

    @NotNull
    private IamGroup findIamGroupById(final String id) {
        return findById(id, iamGroupRepo::findBySimpleNaturalId, IamGroup.class);
    }

    @NotNull
    private IamUser findIamUserById(final String id) {
        return findById(id, iamUserRepo::findBySimpleNaturalId, IamUser.class);
    }

}
