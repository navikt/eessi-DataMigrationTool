package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignedBuc;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.repo.AssignedBucRepo;
import eu.ec.dgempl.eessi.rina.repo.OrgContactMethodRepo;
import eu.ec.dgempl.eessi.rina.repo.OrganisationRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.OrganisationFields;

@Component
@ElasticTypeImporter(type = EElasticType.ORGANISATION)
public class OrganisationImporter extends AbstractDataImporter implements PreCaseImporter {

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
    public DocumentsReport importData() {
        return run(this::processOrganisationData);
    }

    private void processOrganisationData(MapHolder doc) {
        Organisation organisation = beanMapper.map(doc, Organisation.class);
        organisationRepo.saveAndFlush(organisation);

        List<AssignedBuc> originalAssignedBucs = organisation.getAssignedBucs();

        Map<Pair<ProcessDef, EApplicationRole>, List<AssignedBuc>> assignedBucsGrouped = originalAssignedBucs.stream()
                .collect(Collectors.groupingBy(assignedBuc -> Pair.of(assignedBuc.getProcessDef(), assignedBuc.getApplicationRole())));

        List<AssignedBuc> resultBucs = assignedBucsGrouped.values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .map(assignedBucs -> {
                    AssignedBuc result;
                    if (assignedBucs.size() == 1) {
                        result = assignedBucs.get(0);
                    } else {
                        result = assignedBucs
                                .stream()
                                .min(Comparator.nullsLast(Comparator.comparing(AssignedBuc::getValidityStartDate)))
                                .orElseThrow();
                        result.setValidityEndDate(getMaxValidityEndDate(assignedBucs));
                    }
                    return result;
                }).collect(Collectors.toList());

        organisation.setAssignedBucs(resultBucs);

        saveInBulk(() -> resultBucs, () -> assignedBucRepo);
        saveInBulk(organisation::getOrgContactMethods, () -> orgContactMethodRepo);
    }

    @Override
    protected String getId(MapHolder doc) {
        return doc.string(OrganisationFields.ID);
    }

    private ZonedDateTime getMaxValidityEndDate(List<AssignedBuc> assignedBucs) {
        return assignedBucs.stream()
                .map(AssignedBuc::getValidityEndDate)
                .max(Comparator.nullsLast(ZonedDateTime::compareTo)).orElse(null);
    }

}
