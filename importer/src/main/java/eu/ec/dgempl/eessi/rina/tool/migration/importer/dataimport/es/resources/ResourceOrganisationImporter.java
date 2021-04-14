package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.resources;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EResourceType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.resources._abstract.AbstractResourcesImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

@Component
@ElasticTypeImporter(type = EElasticType.RESOURCES_ORGANISATION)
public class ResourceOrganisationImporter extends AbstractResourcesImporter {

    @Override
    protected EResourceType type() {
        return EResourceType.organisation;
    }
}
