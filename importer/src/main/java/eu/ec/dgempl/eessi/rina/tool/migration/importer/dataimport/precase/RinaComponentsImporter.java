package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.RinaComponentsFields.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.GlobalParam;
import eu.ec.dgempl.eessi.rina.repo.GlobalParamRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.RINA_COMPONENTS)
public class RinaComponentsImporter extends AbstractDataImporter implements PreCaseImporter {

    private final GlobalParamRepo globalParamRepo;

    public RinaComponentsImporter(GlobalParamRepo globalParamRepo) {
        this.globalParamRepo = globalParamRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processRinaComponentsData);
    }

    private void processRinaComponentsData(MapHolder doc) {
        List<MapHolder> components = doc.listToMapHolder(COMPONENTS);

        if (CollectionUtils.isNotEmpty(components)) {
            Optional<String> portalVersionOptional = components.stream()
                    .filter(component -> PORTAL_NAME.equals(component.string(NAME)))
                    .map(component -> component.string(VERSION))
                    .findFirst();

            if (portalVersionOptional.isPresent()) {
                GlobalParam portalDate = new GlobalParam(
                        EGlobalParam.APP_PROFILE_INSTALLATION_COMPONENT_SERVER_DATE,
                        doc.string(INSTALLATION_DATE));

                GlobalParam portalVersion = new GlobalParam(
                        EGlobalParam.APP_PROFILE_INSTALLATION_COMPONENT_SERVER_VERSION,
                        portalVersionOptional.get());

                globalParamRepo.saveAll(Arrays.asList(portalDate, portalVersion));
                globalParamRepo.flush();
            }
        }
    }
}