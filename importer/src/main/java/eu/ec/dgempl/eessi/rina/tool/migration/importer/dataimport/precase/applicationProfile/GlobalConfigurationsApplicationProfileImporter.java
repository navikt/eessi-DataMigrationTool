package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.applicationProfile;

import static eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.GlobalConfigurationApplicationProfileFields.*;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.ec.dgempl.eessi.rina.commons.string.PasswordUtil;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.BusinessKey;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ClusterNode;
import eu.ec.dgempl.eessi.rina.repo.BusinessKeyRepo;
import eu.ec.dgempl.eessi.rina.repo.ClusterNodeRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.applicationProfile._abstract.AbstractApplicationProfileImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.GlobalConfigurationApplicationProfileFields;

@Component
@ElasticTypeImporter(type = EElasticType.GLOBAL_CONFIGURATIONS_APPLICATION_PROFILE)
public class GlobalConfigurationsApplicationProfileImporter extends AbstractApplicationProfileImporter {

    private final BusinessKeyRepo businessKeyRepo;
    private final ClusterNodeRepo clusterNodeRepo;

    private final List<EGlobalParam> FIELDS_TO_BE_HASHED = Lists.newArrayList(
            MESSAGING_SECURITY_EBMS_PRIVATE_KEYSTORE_PASSWORD,
            MESSAGING_SECURITY_EBMS_PUBLIC_KEYSTORE_PASSWORD,
            MESSAGING_SECURITY_TLS_PRIVATE_KEYSTORE_PASSWORD,
            MESSAGING_SECURITY_TLS_PUBLIC_KEYSTORE_PASSWORD,
            MESSAGING_SECURITY_BUSINESS_PRIVATE_KEYSTORE_PASSWORD
    );

    public GlobalConfigurationsApplicationProfileImporter(
            final BusinessKeyRepo businessKeyRepo,
            final ClusterNodeRepo clusterNodeRepo) {
        this.businessKeyRepo = businessKeyRepo;
        this.clusterNodeRepo = clusterNodeRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processGlobalConfigurationsApplicationProfile);
    }

    private void processGlobalConfigurationsApplicationProfile(MapHolder doc) {
        MapHolder globalSettingsHolder = doc.getMapHolder(MESSAGING_SETTINGS);

        processGlobalParam(
                () -> globalSettingsHolder,
                () -> GlobalConfigurationApplicationProfileFields.GLOBAL_PARAM_MAP,
                (key, fieldValue) -> {
                    if (fieldValue != null && FIELDS_TO_BE_HASHED.contains(key)) {
                        return PasswordUtil.encodePassword(fieldValue);
                    }
                    return fieldValue;
                });

        processListData(
                () -> globalSettingsHolder.listToMapHolder(BUSINESS_KEY_ALIAS_PASSWORD_LIST),
                () -> businessKeyRepo,
                () -> BusinessKey.class
        );

        processListData(
                () -> globalSettingsHolder.listToMapHolder(CLUSTER_NODES),
                () -> clusterNodeRepo,
                () -> ClusterNode.class
        );
    }
}
