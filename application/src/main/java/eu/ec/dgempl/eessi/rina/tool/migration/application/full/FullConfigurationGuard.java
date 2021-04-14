package eu.ec.dgempl.eessi.rina.tool.migration.application.full;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class FullConfigurationGuard {

    @Value("${elasticsearch.host:#{null}}")
    private String elasticSearchHost;
    @Value("${elasticsearch.port:#{null}}")
    private String elasticSearchPort;
    @Value("${rina.configuration.datasource:#{null}}")
    private String rinaConfigurationDatasource;
    @Value("${sql.scripts.path:#{null}}")
    private String sqlScriptsPath;
    @Value("${schema.path:#{null}}")
    private String schemaPath;
    @Value("${buc.definitions.path:#{null}}")
    private String bucDefinitionsPath;
    @Value("${policy.assignments.export.file:#{null}}")
    private String policyAssignmentsExportFile;
    @Value("${logging.config:#{null}}")
    private String loggingConfig;
    @Value("${reporting.folder:#{null}}")
    private String reportingFolder;

    @PostConstruct
    public void check() {
        ArrayList<String> folders = new ArrayList<>(
                Arrays.asList(schemaPath, sqlScriptsPath, rinaConfigurationDatasource, bucDefinitionsPath));
        ArrayList<String> properties = new ArrayList<>(folders);
        properties.addAll(Arrays.asList(reportingFolder, elasticSearchHost, elasticSearchPort, loggingConfig, policyAssignmentsExportFile));

        // Check properties values not null or empty
        ArrayList<String> notFound = (ArrayList<String>) properties.stream().filter(Strings::isNullOrEmpty).collect(Collectors.toList());
        if (!notFound.isEmpty()) {
            System.out.println();
            System.out.println("ERROR - Please review the following values needed from application.properties:");
            System.out.println();
            System.out.println("Value for elasticsearch.host: " + elasticSearchHost);
            System.out.println("Value for elasticsearch.port: " + elasticSearchPort);
            System.out.println("Value for rina.configuration.datasource: " + rinaConfigurationDatasource);
            System.out.println("Value for sql.scripts.path: " + sqlScriptsPath);
            System.out.println("Value for schema.path: " + schemaPath);
            System.out.println("Value for buc.definitions.path: " + bucDefinitionsPath);
            System.out.println("Value for policy.assignments.export.file: " + policyAssignmentsExportFile);
            System.out.println("Value for logging.config: " + loggingConfig);
            System.out.println("Value for reporting.folder: " + reportingFolder);
            System.out.println();
            System.out.println("Some resource is null or empty. Please fix this and try again.");

            System.exit(1);
        }

        // Check folders exist and are not empty
        ArrayList<String> emptyFolders = (ArrayList<String>) folders.stream().filter(not(this::checkFolder)).collect(Collectors.toList());
        if (emptyFolders.size() > 0) {
            System.out.println();
            System.out.println("ERROR - Please review the following empty folders:");
            System.out.println();
            emptyFolders.forEach(System.out::println);
            System.out.println();
            System.out.println("Please correct the path or add needed files and try again.");

            System.exit(1);
        }

        // Check file exist
        File f = new File(policyAssignmentsExportFile);
        if (!f.exists() || f.isDirectory()) {
            System.out.println();
            System.out.println("ERROR - Please add the BonitaProcessDefinitionActors.json in the specified path.");
            System.out.println("To download the BonitaProcessDefinitionActors.json, please check the User Manual.");
            System.out.println();

            System.exit(1);
        }
    }

    private boolean checkFolder(String path) {
        File directory = new File(path);
        return directory.isDirectory() && directory.list() != null && directory.list().length > 0;
    }

    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }
}
