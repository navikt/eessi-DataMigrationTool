package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.searchDefinition;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.SearchDefinitionFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.findById;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserOrGroupType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.*;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSearchDefinitionMapper extends AbstractMapToEntityMapper<MapHolder, SearchDefinition> {

    private static final Logger logger = LoggerFactory.getLogger(MapToSearchDefinitionMapper.class);

    private final IamGroupRepo iamGroupRepo;
    private final IamUserRepoExtended iamUserRepo;
    private final ProcessDefRepo processDefRepo;
    private final DefaultValuesService defaultsService;

    @Autowired
    private OrganisationService organisationService;

    public MapToSearchDefinitionMapper(final IamUserRepoExtended iamUserRepo, final IamGroupRepo iamGroupRepo,
            final ProcessDefRepo processDefRepo, final DefaultValuesService defaultsService) {
        this.iamUserRepo = iamUserRepo;
        this.iamGroupRepo = iamGroupRepo;
        this.processDefRepo = processDefRepo;
        this.defaultsService = defaultsService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final SearchDefinition b, final MappingContext context) {
        b.setId(a.string(ID));

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

        mapName(a, b);
        mapIamUserGroup(a, b);
        mapOrganisation(a, b);
        mapProcessDef(a, b);

    }

    private void mapIamUserGroup(final MapHolder a, final SearchDefinition b) {
        List<MapHolder> userGroups = a.listToMapHolder(USER_GROUPS);
        if (CollectionUtils.isNotEmpty(userGroups)) {

            List<IamGroup> iamGroups = findByIdAndFilter(() -> userGroups,
                    (doc) -> EUserOrGroupType.GROUP == EUserOrGroupType.lookup(doc.string(TYPE)).orElse(null), (doc) -> doc.string(ID),
                    this::findIamGroupById);
            b.setIamGroups(iamGroups);

            List<IamUser> iamUsers = findByIdAndFilter(() -> userGroups,
                    (doc) -> EUserOrGroupType.USER == EUserOrGroupType.lookup(doc.string(TYPE)).orElse(null), (doc) -> doc.string(ID),
                    this::findIamUserById);
            b.setIamUsers(iamUsers);
        }

    }

    private void mapOrganisation(final MapHolder a, final SearchDefinition b) {
        List<MapHolder> participants = a.listToMapHolder(PARTICIPANTS);
        if (CollectionUtils.isNotEmpty(participants)) {
            List<Organisation> organisations = findByIdAndFilter(() -> participants, (doc) -> true, (doc) -> doc.string(ID),
                    this::findOrganisationById);
            b.setOrganisations(organisations);
        }
    }

    private void mapProcessDef(final MapHolder a, final SearchDefinition b) {
        List<MapHolder> processDefs = a.listToMapHolder(PROCESS_DEFINITIONS);
        if (CollectionUtils.isNotEmpty(processDefs)) {
            List<ProcessDef> processDefinitions = findByIdAndFilter(() -> processDefs, (doc) -> true, (doc) -> doc.string(ID),
                    this::findProcessDefById);
            b.setProcessDefinitions(processDefinitions);
        }
    }

    private void mapName(final MapHolder a, final SearchDefinition b) {
        String name = a.string(NAME);
        if (Strings.isNotBlank(name)) {
            b.setName(name);
        } else {
            String defaultName = defaultsService.getDefaultValue("configuration_searchDefinition.name");
            logger.info(String.format("Searchdefinition name value not found. Setting default value %s", defaultName));
            b.setName(defaultName);
        }
    }

    private <T extends Persistable> List<T> findByIdAndFilter(final Supplier<List<MapHolder>> docsSupplier,
            final Predicate<MapHolder> docPredicate, final Function<MapHolder, String> fieldSupplier,
            final Function<String, T> fieldMapper) {
        List<MapHolder> mapHolders = docsSupplier.get();

        return mapHolders.stream().filter(docPredicate).map(fieldSupplier).map(fieldMapper).collect(Collectors.toList());
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
        return organisationService.getOrSaveOrganisation(id);
    }

    private ProcessDef findProcessDefById(String id) {
        return findById(id, processDefRepo::findBySimpleNaturalId, ProcessDef.class);
    }
}
