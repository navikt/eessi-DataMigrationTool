package eu.ec.dgempl.eessi.rina.tool.migration.common.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.bean.CsvToBeanBuilder;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.OrganisationCsv;

/**
 * Service that loads organisations from CSV file
 */
@Service
public class OrganisationLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(OrganisationLoaderService.class);

    @Value("${organisations.file}")
    private String organisationsFile;

    private HashMap<String, OrganisationCsv> organisationMap;

    /**
     * Method executed on application start that iterates through the lines of a CSV and loads an {@link OrganisationCsv} list to be later
     * consulted.
     */
    @PostConstruct
    public void loadOrganisationsFromFile() {

        try {
            File csvFile = new File(organisationsFile);
            if (csvFile.exists()) {

                List<OrganisationCsv> organisationsCsv = new CsvToBeanBuilder(new FileReader(csvFile)).withSeparator(';')
                        .withType(OrganisationCsv.class).build().parse();

                if (!organisationsCsv.isEmpty()) {
                    organisationMap = new HashMap<>();
                    organisationsCsv.forEach(it -> organisationMap.put(it.id, it));
                    logger.info("Loaded " + organisationMap.size() + " organisations from CSV file");
                } else {
                    logger.info("Organisations CSV file exists but has no organisations");
                }
            } else {
                logger.info("Organisations CSV file not found. Not loading any organisation from CSV.");
            }

        } catch (IOException e) {
            logger.error("Error processing Organisations CSV file. Error: " + e.getMessage());
        }
    }

    public OrganisationCsv getOrganisationFromCsvById(String id) {

        if (organisationMap == null || organisationMap.isEmpty() || !organisationMap.containsKey(id))
            return null;

        return organisationMap.get(id);
    }
}
