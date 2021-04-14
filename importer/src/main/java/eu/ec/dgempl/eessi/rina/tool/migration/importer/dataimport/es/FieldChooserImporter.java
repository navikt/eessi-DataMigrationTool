package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.FieldChooser;
import eu.ec.dgempl.eessi.rina.repo.FieldChooserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CONFIGURATIONS_FIELDCHOOSER)
public class FieldChooserImporter extends AbstractDataImporter {

    private final FieldChooserRepo fieldChooserRepo;

    public FieldChooserImporter(final FieldChooserRepo fieldChooserRepo) {
        this.fieldChooserRepo = fieldChooserRepo;
    }

    @Override
    public void importData() {
        run(this::processFieldChooserData);
    }

    private void processFieldChooserData(final MapHolder doc) {
        FieldChooser fieldChooser = beanMapper.map(doc, FieldChooser.class);
        fieldChooserRepo.saveAndFlush(fieldChooser);
    }
}
