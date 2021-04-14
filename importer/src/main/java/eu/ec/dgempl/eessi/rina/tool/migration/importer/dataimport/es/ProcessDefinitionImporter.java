package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.FieldChooser;
import eu.ec.dgempl.eessi.rina.repo.FieldChooserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ProcessDefFields;

@Component
@ElasticTypeImporter(type = EElasticType.PROCESSDEFINITION)
public class ProcessDefinitionImporter extends AbstractDataImporter {

    private final FieldChooserRepo fieldChooserRepo;

    public ProcessDefinitionImporter(final FieldChooserRepo fieldChooserRepo) {
        this.fieldChooserRepo = fieldChooserRepo;
    }

    @Override
    public void importData() {
        run(this::processProcessDef);
    }

    public void processProcessDef(MapHolder doc) {
        MapHolder fieldChooserMap = doc.getMapHolder(ProcessDefFields.DEFAULT_FIELD_CHOOSER);
        FieldChooser fieldChooser = beanMapper.map(fieldChooserMap, FieldChooser.class);
        fieldChooserRepo.saveAndFlush(fieldChooser);
    }
}
