package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Log;
import eu.ec.dgempl.eessi.rina.repo.OrganisationRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.OrganisationCsv;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.OrganisationLoaderService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class OrganisationService {

    Logger logger = LoggerFactory.getLogger(OrganisationService.class);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final OrganisationRepo organisationRepo;

    @Autowired
    private OrganisationLoaderService organisationLoaderService;

    public OrganisationService(final OrganisationRepo organisationRepo) {
        this.organisationRepo = organisationRepo;
    }

    public Organisation getOrSaveOrganisation(String id) {
        if (StringUtils.isBlank(id)) {
            throw new RuntimeException("Could not load or save organisation without id");
        }

        Organisation organisation = getOrSaveOrganisationWithDefault(id, null);

        if (organisation == null) {
            throw new RuntimeException(String.format("Could not load and save organisation with id %s", id));
        }

        return organisation;
    }

    public Organisation getOrSaveOrganisationWithDefault(String id, Organisation defaultValue) {
        if (StringUtils.isBlank(id)) {
            return defaultValue;
        }

        Organisation organisation = RepositoryUtils.findById(id, organisationRepo::findById);

        if (organisation == null) {
            organisation = mapOrganisation(organisationLoaderService.getOrganisationFromCsvById(id));

            if (organisation != null) {
                organisationRepo.saveAndFlush(organisation);
            } else {
                organisation = defaultValue;
            }
        }

        return organisation;
    }

    private Organisation mapOrganisation(OrganisationCsv organisationCsv) {
        if (organisationCsv == null) {
            return null;
        }
        Organisation organisation = new Organisation();
        organisation.setId(organisationCsv.id);
        organisation.setAcronym(organisationCsv.CLDId);
        organisation.setCountryCode(ECountryCode.fromString(organisationCsv.country));
        organisation.setAddressCountry(organisationCsv.country);
        if (!"NULL".equals(organisationCsv.activationDate)) {
            organisation.setActiveSince(ZonedDateTime.parse(organisationCsv.activationDate, formatter.withZone(ZoneOffset.UTC)));
        }
        organisation.setIsEnabled(false);
        organisation.setActiveUntil(ZonedDateTime.now().minusHours(1));

        Log log = new Log();
        log.setCreatedAt(ZonedDateTime.now());
        log.setUpdatedAt(ZonedDateTime.now());
        organisation.setLog(log);

        return organisation;
    }

}
