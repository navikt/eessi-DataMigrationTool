package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.searchDefinition;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.SearchDefinitionFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserOrGroupType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SearchDefinition;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.OrganisationRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSearchDefinitionMapper extends AbstractMapToEntityMapper<MapHolder, SearchDefinition> {

    private final IamGroupRepo iamGroupRepo;
    private final IamUserRepoExtended iamUserRepo;
    private final ProcessDefRepo processDefRepo;
    private final OrganisationRepo organisationRepo;

    public MapToSearchDefinitionMapper(
            final IamUserRepoExtended iamUserRepo,
            final IamGroupRepo iamGroupRepo,
            final ProcessDefRepo processDefRepo,
            final OrganisationRepo organisationRepo) {
        this.iamUserRepo = iamUserRepo;
        this.iamGroupRepo = iamGroupRepo;
        this.processDefRepo = processDefRepo;
        this.organisationRepo = organisationRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final SearchDefinition b, final MappingContext context) {
        b.setId(a.string(ID));
        b.setName(a.string(NAME));

        String userName = a.string(USER_NAME);
        IamUser ownerUser = RepositoryUtils.getIamUser(() -> userName, () -> iamUserRepo);
        b.setOwnerUser(ownerUser);

        b.setColor(a.string(COLOR));
        b.setCriticalities(String.join(",", a.list(CRITICALITIES, String.class, true)));
        b.setImportances(String.join(",", a.list(IMPORTANCES, String.class, true)));
        b.setStatuses(String.join(",", a.list(STATUSES, String.class, true)));

        b.setTimeIntervalType(a.string(TIME_INTERVAL_TYPE));

        mapDate(a, START_DATE, b::setStartDate);
        mapDate(a, END_DATE, b::setEndDate);

        mapIamUserGroup(a, b);
        mapOrganisation(a, b);
        mapProcessDef(a, b);

    }

    private void mapIamUserGroup(final MapHolder a, final SearchDefinition b) {
        List<MapHolder> userGroups = a.listToMapHolder(USER_GROUPS);
        if (CollectionUtils.isNotEmpty(userGroups)) {

            List<IamGroup> iamGroups = findByIdAndFilter(
                    () -> userGroups,
                    (doc) -> EUserOrGroupType.GROUP == EUserOrGroupType.lookup(doc.string(TYPE)).orElse(null),
                    (doc) -> doc.string(ID),
                    this::findIamGroupById);
            b.setIamGroups(iamGroups);

            List<IamUser> iamUsers = findByIdAndFilter(
                    () -> userGroups,
                    (doc) -> EUserOrGroupType.USER == EUserOrGroupType.lookup(doc.string(TYPE)).orElse(null),
                    (doc) -> doc.string(ID),
                    this::findIamUserById);
            b.setIamUsers(iamUsers);
        }

    }

    private void mapOrganisation(final MapHolder a, final SearchDefinition b) {
        List<MapHolder> participants = a.listToMapHolder(PARTICIPANTS);
        if (CollectionUtils.isNotEmpty(participants)) {
            List<Organisation> organisations = findByIdAndFilter(
                    () -> participants,
                    (doc) -> true,
                    (doc) -> doc.string(ID),
                    this::findOrganisationById
            );
            b.setOrganisations(organisations);
        }
    }

    private void mapProcessDef(final MapHolder a, final SearchDefinition b) {
        List<MapHolder> processDefs = a.listToMapHolder(PROCESS_DEFINITIONS);
        if (CollectionUtils.isNotEmpty(processDefs)) {
            List<ProcessDef> processDefinitions = findByIdAndFilter(
                    () -> processDefs,
                    (doc) -> true,
                    (doc) -> doc.string(ID),
                    this::findProcessDefById
            );
            b.setProcessDefinitions(processDefinitions);
        }
    }

    private <T extends Persistable> List<T> findByIdAndFilter(
            final Supplier<List<MapHolder>> docsSupplier,
            final Predicate<MapHolder> docPredicate,
            final Function<MapHolder, String> fieldSupplier,
            final Function<String, T> fieldMapper) {
        List<MapHolder> mapHolders = docsSupplier.get();

        return mapHolders
                .stream()
                .filter(docPredicate)
                .map(fieldSupplier)
                .map(fieldMapper)
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

    @NotNull
    private Organisation findOrganisationById(String id) {
        return findById(id, organisationRepo::findBySimpleNaturalId, Organisation.class);
    }

    private ProcessDef findProcessDefById(String id) {
        return findById(id, processDefRepo::findBySimpleNaturalId, ProcessDef.class);
    }
}
