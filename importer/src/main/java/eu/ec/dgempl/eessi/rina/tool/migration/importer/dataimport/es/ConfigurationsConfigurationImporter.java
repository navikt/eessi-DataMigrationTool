package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ConfigurationsConfigurationFields.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ETenantConfigurationType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TenantParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TenantParamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.TenantParamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantParamRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.AbstractLocalMapper;

@Component
@ElasticTypeImporter(type = EElasticType.CONFIGURATIONS_CONFIGURATION)
public class ConfigurationsConfigurationImporter extends AbstractDataImporter {

    private final TenantRepo tenantRepo;
    private final TenantParamGroupRepo tenantParamGroupRepo;
    private final TenantParamRepo tenantParamRepo;

    public ConfigurationsConfigurationImporter(
            final TenantRepo tenantRepo,
            final TenantParamGroupRepo tenantParamGroupRepo,
            final TenantParamRepo tenantParamRepo) {
        this.tenantRepo = tenantRepo;
        this.tenantParamGroupRepo = tenantParamGroupRepo;
        this.tenantParamRepo = tenantParamRepo;
    }

    @Override
    public void importData() {
        run(this::processTenantParamGroupsData);
    }

    private void processTenantParamGroupsData(MapHolder doc) {
        TenantParamGroup tenantParamGroup = processTenantParamGroup(doc);

        List<TenantParam> tenantParams = getTenantParamsMap(tenantParamGroup.getName())
                .entrySet()
                .stream()
                .filter(entry -> doc.containsKey(entry.getKey()))
                .map(entry -> new TenantParam(entry.getValue(), tenantParamGroup, String.valueOf(doc.getDeep(entry.getKey()))))
                .collect(Collectors.toList());
        tenantParamRepo.saveAll(tenantParams);
        tenantParamRepo.flush();
    }

    private TenantParamGroup processTenantParamGroup(MapHolder doc) {
        String tenantId = doc.string(INSTITUTION_ID);

        Tenant tenant = tenantRepo.findById(tenantId);
        if (tenant == null) {
            throw new EntityNotFoundEessiRuntimeException(Tenant.class, UniqueIdentifier.id, tenantId);
        }

        String type = doc.string(TYPE);
        ETenantConfigurationType configurationType = ETenantConfigurationType.valueOf(type);

        TenantParamGroup tenantParamGroup = new TenantParamGroup(tenant, configurationType);
        AbstractLocalMapper.setDefaultAudit(tenantParamGroup::setAudit);

        return tenantParamGroupRepo.saveAndFlush(tenantParamGroup);
    }
}
