package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.assignment;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignmentFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.commons.es.LocalUuidUtil;
import eu.ec.dgempl.eessi.rina.model.common.AssignmentPolicyConstants;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EPolicyType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESector;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserOrGroupType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyRule;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyRuleCountry;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.repo.SectorRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignmentPolicyFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAssignmentPolicyRuleMapper extends AbstractMapToEntityMapper<MapHolder, AssignmentPolicyRule> {

    private final IamUserRepo iamUserRepo;
    private final IamGroupRepo iamGroupRepo;
    private final ProcessDefRepo processDefRepo;
    private final RoleRepo roleRepo;
    private final SectorRepo sectorRepo;

    @Autowired
    private OrganisationService organisationService;

    public MapToAssignmentPolicyRuleMapper(
            final IamUserRepo iamUserRepo,
            final IamGroupRepo iamGroupRepo,
            final ProcessDefRepo processDefRepo,
            final RoleRepo roleRepo, final SectorRepo sectorRepo) {
        this.iamUserRepo = iamUserRepo;
        this.iamGroupRepo = iamGroupRepo;
        this.processDefRepo = processDefRepo;
        this.roleRepo = roleRepo;
        this.sectorRepo = sectorRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final AssignmentPolicyRule b, final MappingContext context) {
        mapId(a, b);
        mapRoles(a, b);
        mapUserGroups(a, b);

        MapHolder condition = a.getMapHolder(AssignmentPolicyFields.CONDITION);
        b.setSubjectAddress(condition.string(AssignmentPolicyFields.SUBJECT_ADDRESS));
        mapAppRole(condition, b);
        mapOwnerCountry(condition, b);
        mapCreatorGroups(condition, b);
        mapSector(condition, b);
        mapProcess(condition, b);
        mapOwnerOrganization(condition, b);
        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final AssignmentPolicyRule b) {
        setDefaultAudit(b::setAudit);
    }

    private void mapId(final MapHolder a, final AssignmentPolicyRule b) {
        String id = a.string(AssignmentPolicyFields.RULE_ID);
        if (StringUtils.isNotBlank(id)) {
            b.setId(id);
        } else {
            b.setId(LocalUuidUtil.generateUuid());
        }
    }

    private void mapOwnerOrganization(final MapHolder a, final AssignmentPolicyRule b) {
        List<MapHolder> organisations = a.listToMapHolder(AssignmentPolicyFields.OWNER_ORGANIZATION);
        if (CollectionUtils.isNotEmpty(organisations)) {
            organisations.stream()
                    .map(organisation -> organisation.string(AssignmentPolicyFields.ORGANISATION_ID))
                    .map(organisationService::getOrSaveOrganisation)
                    .forEach(b::addOrganisation);
        }
    }

    private void mapProcess(final MapHolder a, final AssignmentPolicyRule b) {
        List<String> processes = a.list(AssignmentPolicyFields.PROCESS, String.class, false);
        if (CollectionUtils.isNotEmpty(processes)) {
            processes.stream()
                    .map(process -> RepositoryUtils.findById(process, processDefRepo::findById, ProcessDef.class))
                    .forEach(b::addProcessDef);
        }
    }

    private void mapSector(final MapHolder a, final AssignmentPolicyRule b) {
        List<String> sectors = a.list(AssignmentPolicyFields.SECTOR, String.class, false);
        if (CollectionUtils.isNotEmpty(sectors)) {
            sectors.stream()
                    .map(this::getSector)
                    .map(sectorRepo::findByName)
                    .filter(Objects::nonNull)
                    .forEach(b::addSector);
        }
    }

    private void mapCreatorGroups(final MapHolder a, final AssignmentPolicyRule b) {
        List<MapHolder> creatorGroups = a.listToMapHolder(AssignmentPolicyFields.CREATOR_GROUP);

        mapUsers(creatorGroups, b::addCreatorIamUser);
        mapGroups(creatorGroups, b::addCreatorIamGroup);
    }

    private void mapUserGroups(final MapHolder a, final AssignmentPolicyRule b) {
        List<MapHolder> userGroups = a.listToMapHolder(AssignmentPolicyFields.RULE_USER_GROUPS);

        processCreatorUserOrGroup(
                userGroups,
                userGroup ->
                        userGroup.string(AssignmentPolicyFields.USER_GROUP_ID).equalsIgnoreCase(AssignmentPolicyConstants.CREATORS_USER_ID),
                userGroup -> b.setAssignToCreatorUser(true));

        processCreatorUserOrGroup(
                userGroups,
                userGroup -> userGroup.string(AssignmentPolicyFields.USER_GROUP_ID)
                        .equalsIgnoreCase(AssignmentPolicyConstants.CREATORS_GROUP_ID),
                userGroup -> b.setAssignToCreatorBranch(true));

        mapUsers(userGroups, b::addIamUser);
        mapGroups(userGroups, b::addIamGroup);
    }

    private void processCreatorUserOrGroup(final List<MapHolder> userGroups, final Predicate<MapHolder> isCreatorUserPredicate,
            final Consumer<MapHolder> mapHolderConsumer) {
        if (CollectionUtils.isNotEmpty(userGroups)) {
            userGroups.stream()
                    .filter(isCreatorUserPredicate)
                    .findFirst()
                    .ifPresent(mapHolderConsumer);

            userGroups.removeIf(isCreatorUserPredicate);
        }
    }

    private void mapOwnerCountry(final MapHolder a, final AssignmentPolicyRule b) {
        List<String> countries = a.list(AssignmentPolicyFields.OWNER_COUNTRY, String.class, false);
        if (CollectionUtils.isNotEmpty(countries)) {
            countries.stream()
                    .map(countryCode -> ECountryCode.lookup(countryCode).orElseThrow(
                            () -> new DmtEnumNotFoundException(
                                    ECountryCode.class,
                                    a.addPath(AssignmentPolicyFields.OWNER_COUNTRY),
                                    countryCode))
                    )
                    .map(country -> new AssignmentPolicyRuleCountry(b, country))
                    .forEach(b::addRuleCountry);
        }
    }

    private void mapAppRole(final MapHolder a, final AssignmentPolicyRule b) {
        String appRole = a.string(AssignmentPolicyFields.RULE_APP_ROLE);
        if (StringUtils.isNotBlank(appRole)) {
            EApplicationRole eApplicationRole = EApplicationRole.lookup(appRole).orElseThrow(
                    () -> new DmtEnumNotFoundException(EApplicationRole.class, a.addPath(AssignmentPolicyFields.RULE_APP_ROLE), appRole)
            );
            b.setApplicationRole(eApplicationRole);
        }
    }

    private void mapRoles(final MapHolder a, final AssignmentPolicyRule b) {
        List<String> actors = a.list(AssignmentPolicyFields.ACTORS, String.class, false);
        if (CollectionUtils.isNotEmpty(actors)) {
            actors.stream()
                    .filter(actor -> EPolicyType.CREATOR_ROLE.equalsIgnoreCase(actor) == false)
                    .map(actor -> ERole.lookup(actor).orElseThrow(
                            () -> new DmtEnumNotFoundException(ERole.class, a.addPath(AssignmentPolicyFields.ACTORS), actor)
                    ))
                    .map(roleRepo::findByName)
                    .filter(Objects::nonNull)
                    .forEach(b::addRole);
        }
    }

    private void mapUsers(final List<MapHolder> actors, Consumer<IamUser> iamUserConsumer) {
        mapIam(actors, EUserOrGroupType.USER, this::findIamUserById, iamUserConsumer);
    }

    private void mapGroups(final List<MapHolder> actors, Consumer<IamGroup> iamGroupConsumer) {
        mapIam(actors, EUserOrGroupType.GROUP, this::findIamGroupById, iamGroupConsumer);
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
                        .orElseThrow(() -> new DmtEnumNotFoundException(EUserOrGroupType.class, actor.addPath(TYPE), actor.string(TYPE))))
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

    private ESector getSector(String sectorId) {
        return Arrays.stream(ESector.values())
                .filter(sector -> sector.getCode().equalsIgnoreCase(sectorId)
                        || sector.getDisplayName().equalsIgnoreCase(sectorId)
                        || sector.name().equalsIgnoreCase(sectorId))
                .findFirst()
                .orElseThrow(() -> new DmtEnumNotFoundException(ESector.class, AssignmentPolicyFields.SECTOR, sectorId));
    }
}
