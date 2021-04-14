package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.applicationProfile;

import static eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ApplicationProfileFields.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ETenantParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ArchivingPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ArchivingRepositoryPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ArchivingVolume;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseRetentionPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieListener;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscriber;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscription;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TenantParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TenantParamGroup;
import eu.ec.dgempl.eessi.rina.repo.ArchivingPolicyRepo;
import eu.ec.dgempl.eessi.rina.repo.ArchivingRepositoryPolicyRepo;
import eu.ec.dgempl.eessi.rina.repo.ArchivingVolumeRepo;
import eu.ec.dgempl.eessi.rina.repo.CaseRetentionPolicyRepo;
import eu.ec.dgempl.eessi.rina.repo.NieListenerRepo;
import eu.ec.dgempl.eessi.rina.repo.NieSubscriberRepo;
import eu.ec.dgempl.eessi.rina.repo.NieSubscriptionRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantParamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantParamRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.applicationProfile._abstract.AbstractApplicationProfileImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.SubscriptionsHolder;

@Component
@ElasticTypeImporter(type = EElasticType.APPLICATION_PROFILE)
public class ApplicationProfileImporter extends AbstractApplicationProfileImporter {

    private final ArchivingPolicyRepo archivingPolicyRepo;
    private final ArchivingRepositoryPolicyRepo archivingRepositoryPolicyRepo;
    private final ArchivingVolumeRepo archivingVolumeRepo;
    private final CaseRetentionPolicyRepo caseRetentionPolicyRepo;
    private final NieSubscriptionRepo nieSubscriptionRepo;
    private final NieListenerRepo nieListenerRepo;
    private final NieSubscriberRepo nieSubscriberRepo;
    private final TenantRepo tenantRepo;
    private final TenantParamRepo tenantParamRepo;
    private final TenantParamGroupRepo tenantParamGroupRepo;

    private final List<String> excludedFields = Lists.newArrayList("tenant.message.settings.privateKeyStore",
            "tenant.message.settings.organisation.acronym",
            "tenant.message.settings.organisation.activeSince",
            "tenant.message.settings.organisation.countryCode",
            "tenant.message.settings.organisation.id",
            "tenant.message.settings.organisation.name");

    public ApplicationProfileImporter(
            final ArchivingPolicyRepo archivingPolicyRepo,
            final ArchivingRepositoryPolicyRepo archivingRepositoryPolicyRepo,
            final ArchivingVolumeRepo archivingVolumeRepo,
            final CaseRetentionPolicyRepo caseRetentionPolicyRepo,
            final NieSubscriptionRepo nieSubscriptionRepo,
            final NieListenerRepo nieListenerRepo,
            final NieSubscriberRepo nieSubscriberRepo,
            final TenantRepo tenantRepo,
            final TenantParamRepo tenantParamRepo,
            final TenantParamGroupRepo tenantParamGroupRepo) {
        this.archivingPolicyRepo = archivingPolicyRepo;
        this.archivingRepositoryPolicyRepo = archivingRepositoryPolicyRepo;
        this.archivingVolumeRepo = archivingVolumeRepo;
        this.caseRetentionPolicyRepo = caseRetentionPolicyRepo;
        this.nieSubscriptionRepo = nieSubscriptionRepo;
        this.nieListenerRepo = nieListenerRepo;
        this.nieSubscriberRepo = nieSubscriberRepo;
        this.tenantRepo = tenantRepo;
        this.tenantParamRepo = tenantParamRepo;
        this.tenantParamGroupRepo = tenantParamGroupRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processApplicationProfile, false);
    }

    private void processApplicationProfile(MapHolder doc) {
        processGlobalData(doc);
        processDefaultUserProfileData(doc);
        processCaseRetentionPoliciesData(doc);
        processMessageRetentionPoliciesData(doc);
        processArchivingRepositoryPoliciesData(doc);
        processArchivingRepositoriesData(doc);
        processArchivingPoliciesData(doc);
        processNieSubscriptions(doc);
        processTenantsData(doc);
    }

    private void processGlobalData(MapHolder doc) {
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> processGlobalParam(() -> doc, () -> ApplicationProfile.GLOBAL_PARAM_MAP)
            );
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import Global Data from application profile",
                    e);
        }
    }

    private void processDefaultUserProfileData(MapHolder doc) {
        MapHolder defaultUserProfileHolder = doc.getMapHolder(DEFAULT_USER_PROFILE);
        Map<EGlobalParam, String> DEFAULT_VALUES = Map.of(
                USER_PROFILE_ALARMSETTING_AUTOSET_SEND_FLAG, "false",
                USER_PROFILE_ALARMSETTING_AUTOSET_DAYS, "30"
        );

        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> processGlobalParam(
                            () -> defaultUserProfileHolder,
                            () -> DefaultUserProfile.DEFAULT_USER_PROFILE_MAP,
                            (globalParamEnum, value) -> value != null ? value : DEFAULT_VALUES.get(globalParamEnum)));

        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import DefaultUserProfile from application profile",
                    e);
        }
    }

    private void processCaseRetentionPoliciesData(MapHolder doc) {
        MapHolder caseRetentionPoliciesHolder = doc.getMapHolder(CASE_RETENTION_POLICIES);

        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> {
                        processGlobalParam(() -> caseRetentionPoliciesHolder, () -> CaseRetentionPolicies.CASE_RETENTION_POLICIES_MAP);

                        processListData(
                                () -> doc.listToMapHolder(CASE_RETENTION_POLICIES_TABLE),
                                () -> caseRetentionPolicyRepo,
                                () -> CaseRetentionPolicy.class
                        );
                    }
            );
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import CaseRetentionPolicies from application profile",
                    e);
        }
    }

    private void processMessageRetentionPoliciesData(MapHolder doc) {
        MapHolder messageRetentionPolicies = doc.getMapHolder(MESSAGE_RETENTION_POLICIES);
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> processGlobalParam(() -> messageRetentionPolicies, () -> MessageRetentionPolicies.MESSAGE_RETENTION_POLICIES_MAP)
            );
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import MessageRetentionPolicies from application profile",
                    e);
        }
    }

    private void processArchivingRepositoryPoliciesData(MapHolder doc) {
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> processListData(
                            () -> doc.listToMapHolder(ARCHIVING_REPOSITORY_POLICIES),
                            () -> archivingRepositoryPolicyRepo,
                            () -> ArchivingRepositoryPolicy.class
                    ));
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import ArchivingRepositoryPolicies from application profile",
                    e);
        }

    }

    private void processArchivingRepositoriesData(MapHolder doc) {
        MapHolder archivingRepositoriesHolder = doc.getMapHolder(ARCHIVING_REPOSITORIES);

        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> {
                        processGlobalParam(() -> archivingRepositoriesHolder, () -> ArchivingRepositories.ARCHIVING_REPOSITORIES_MAP);

                        processListData(
                                () -> archivingRepositoriesHolder.listToMapHolder(ARCHIVING_REPOSITORIES_LIST),
                                () -> archivingVolumeRepo,
                                () -> ArchivingVolume.class
                        );
                    });
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import ArchivingRepositories from application profile",
                    e);
        }
    }

    private void processArchivingPoliciesData(MapHolder doc) {
        MapHolder archivingPoliciesHolder = doc.getMapHolder(ARCHIVING_POLICIES);

        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> {
                        processGlobalParam(() -> archivingPoliciesHolder, () -> ArchivingPolicies.ARCHIVING_POLICIES_MAP);

                        processListData(
                                () -> archivingPoliciesHolder.listToMapHolder(ARCHIVING_POLICIES_TABLE),
                                () -> archivingPolicyRepo,
                                () -> ArchivingPolicy.class
                        );
                    });
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import ArchivingPolicies from application profile",
                    e);
        }
    }

    private void processNieSubscriptions(MapHolder doc) {
        MapHolder nieSubscriptionsHolder = doc.getMapHolder(NIE_SUBSCRIPTIONS);

        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(
                    transactionManager,
                    () -> {
                        processGlobalParam(() -> nieSubscriptionsHolder, () -> NieSubscriptions.NIE_SUBSCRIPTIONS_MAP);

                        List<NieSubscription> subscriptions = processSubscriptionsData(
                                () -> nieSubscriptionsHolder.listToMapHolder(EVENTS_SUBSCRIPTIONS));

                        subscriptions.forEach(nieSubscriptionRepo::saveAndFlush);

                        List<NieListener> nieListeners = subscriptions.stream()
                                .map(NieSubscription::getNieListeners)
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                        nieListeners.forEach(nieListenerRepo::saveAndFlush);

                        List<NieSubscriber> nieSubscribers = subscriptions.stream()
                                .map(NieSubscription::getNieSubscribers)
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                        nieSubscribers.forEach(nieSubscriberRepo::saveAndFlush);

                    });
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import NieSubscriptions from application profile",
                    e);
        }
    }

    private List<NieSubscription> processSubscriptionsData(final Supplier<List<MapHolder>> subscriptionsSupplier) {

        final String SUBSCRIBERS = "subscribers";
        final String LISTENERS = "listeners";
        final String NOTIFICATIONS = "notifications";

        List<MapHolder> subscriptionsMap = subscriptionsSupplier.get();
        if (CollectionUtils.isEmpty(subscriptionsMap)) {
            return Collections.emptyList();
        }

        SubscriptionsHolder subscriptionsHolder = new SubscriptionsHolder();

        subscriptionsMap.forEach((subscriptionHolder) -> {

            Map<String, Object> subscription = subscriptionHolder.getHolding();
            subscriptionsHolder.setSubscription(subscription);

            List<Map<String, String>> subscribers = getListFromObject(subscription.get(SUBSCRIBERS));
            if (!subscribers.isEmpty()) {

                List<Map<String, String>> listeners = getListFromObject(subscription.get(LISTENERS));

                subscribers.forEach((subscriber) -> {
                    subscriptionsHolder.setSubscriber(subscriber);
                    listeners.forEach(subscriptionsHolder::addListener);
                });
            } else {
                subscriptionsHolder.setSubscriber(null);
                getListFromObject(subscription.get(NOTIFICATIONS)).forEach(subscriptionsHolder::addNotificationListener);
            }

        });

        return beanMapper.mapAsList(subscriptionsHolder.getSubscriptions(), NieSubscription.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getListFromObject(Object value) {
        if (value instanceof List) {
            return (List<Map<String, String>>) value;
        }

        return new ArrayList<>();
    }

    private void processTenantsData(MapHolder doc) {
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {
                List<MapHolder> tenants = doc.listToMapHolder(TENANTS);

                List<MapHolder> institutionAliasMappings = doc.listToMapHolder(MESSAGING_SETTINGS_INSTITUTION_ALIAS_MAPPINGS, true);

                processTenants(tenants);
                processTenantGroups(institutionAliasMappings);
            });
        } catch (Exception e) {
            dataReporter.reportProblem(
                    this.inferElasticType(),
                    this.getId(doc),
                    "Could not import tenants from application profile",
                    e);
        }
    }

    private void processTenants(List<MapHolder> tenantDocs) {
        processListData(
                () -> tenantDocs,
                () -> tenantRepo,
                () -> Tenant.class);
    }

    private void processTenantGroups(List<MapHolder> institutionAliasMappings) {
        if (!CollectionUtils.isEmpty(institutionAliasMappings)) {
            for (MapHolder institutionAliasMapping : institutionAliasMappings) {
                TenantParamGroup tenantParamGroup = beanMapper.map(institutionAliasMapping, TenantParamGroup.class);

                tenantParamGroupRepo.saveAndFlush(tenantParamGroup);

                processTenantParams(
                        institutionAliasMapping,
                        "tenant.message.settings",
                        tenantParamGroup);
            }
        }
    }

    private void processTenantParams(
            MapHolder institutionAliasMapping,
            String paramKey,
            TenantParamGroup tenantParamGroup) {

        for (String key : institutionAliasMapping.fields()) {

            Object value = institutionAliasMapping.get(key, false);

            String paramKeyValue = paramKey + "." + key;

            if (value instanceof Map) {

                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                processTenantParams(
                        new MapHolder(map, institutionAliasMapping.getVisitedFields(), institutionAliasMapping.addPath(key)),
                        paramKeyValue,
                        tenantParamGroup);

            } else if (value instanceof List) {
                // TODO this has to be defined ... how to handle it
            } else {

                if (excludeFields(paramKeyValue))
                    continue;

                TenantParam tenantParam = new TenantParam();
                tenantParam.setTenantParamGroup(tenantParamGroup);
                tenantParam.setTenant(tenantParamGroup.getTenant());
                tenantParam.setKey(ETenantParam.lookup(paramKeyValue).orElseThrow(
                        () -> new DmtEnumNotFoundException(ETenantParam.class, paramKeyValue)
                ));
                tenantParam.setValue(value != null ? String.valueOf(value) : null);

                tenantParamRepo.save(tenantParam);
            }
        }
        tenantParamRepo.flush();
    }

    private boolean excludeFields(String field) {
        return excludedFields.contains(field);
    }
}
