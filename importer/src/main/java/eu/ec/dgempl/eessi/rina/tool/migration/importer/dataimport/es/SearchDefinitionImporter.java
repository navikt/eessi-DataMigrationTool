package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.SearchDefinition;
import eu.ec.dgempl.eessi.rina.repo.SearchDefinitionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CONFIGURATIONS_SEARCHDEFINITION)
public class SearchDefinitionImporter extends AbstractDataImporter {

    private final SearchDefinitionRepo searchDefinitionRepo;

    public SearchDefinitionImporter(final SearchDefinitionRepo searchDefinitionRepo) {
        this.searchDefinitionRepo = searchDefinitionRepo;
    }

    @Override
    public void importData() {
        run(this::processSearchDefinitions);
    }

    private void processSearchDefinitions(MapHolder a) {
        SearchDefinition searchDefinition = beanMapper.map(a, SearchDefinition.class);
        searchDefinitionRepo.saveAndFlush(searchDefinition);
    }
}
