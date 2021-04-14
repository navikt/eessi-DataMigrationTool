package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.repo.AssignedBucRepo;
import eu.ec.dgempl.eessi.rina.repo.OrgContactMethodRepo;
import eu.ec.dgempl.eessi.rina.repo.OrganisationRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.OrganisationFields;

@Component
@ElasticTypeImporter(type = EElasticType.ORGANISATION)
public class OrganisationImporter extends AbstractDataImporter {

    private final AssignedBucRepo assignedBucRepo;
    private final OrganisationRepo organisationRepo;
    private final OrgContactMethodRepo orgContactMethodRepo;

    public OrganisationImporter(
            final AssignedBucRepo assignedBucRepo,
            final OrganisationRepo organisationRepo,
            final OrgContactMethodRepo orgContactMethodRepo) {
        this.assignedBucRepo = assignedBucRepo;
        this.organisationRepo = organisationRepo;
        this.orgContactMethodRepo = orgContactMethodRepo;
    }

    @Override
    public void importData() {
        run(this::processOrganisationData);
    }

    private void processOrganisationData(MapHolder doc) {
        Organisation organisation = beanMapper.map(doc, Organisation.class);
        organisationRepo.saveAndFlush(organisation);

        saveInBulk(organisation::getAssignedBucs, () -> assignedBucRepo);
        saveInBulk(organisation::getOrgContactMethods, () -> orgContactMethodRepo);
    }

    @Override
    protected String getId(MapHolder doc) {
        return doc.string(OrganisationFields.ID);
    }
}
