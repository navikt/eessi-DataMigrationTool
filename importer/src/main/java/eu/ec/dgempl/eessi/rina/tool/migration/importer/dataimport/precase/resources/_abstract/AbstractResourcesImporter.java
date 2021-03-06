package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.resources._abstract;

import org.springframework.beans.factory.annotation.Autowired;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EResourceType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Resource;
import eu.ec.dgempl.eessi.rina.repo.ResourceRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public abstract class AbstractResourcesImporter extends AbstractDataImporter implements PreCaseImporter {

    private ResourceRepo resourceRepo;

    @Override
    public DocumentsReport importData() {
        return run(this::processResourcesData);
    }

    private void processResourcesData(final MapHolder doc) {
        Resource resource = beanMapper.map(doc, Resource.class);
        resource.setType(type());
        resourceRepo.saveAndFlush(resource);
    }

    @Autowired
    public void setResourceRepo(final ResourceRepo resourceRepo) {
        this.resourceRepo = resourceRepo;
    }

    protected abstract EResourceType type();
}
