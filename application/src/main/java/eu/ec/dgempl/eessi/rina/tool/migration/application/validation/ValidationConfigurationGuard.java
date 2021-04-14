package eu.ec.dgempl.eessi.rina.tool.migration.application.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class ValidationConfigurationGuard {

    @Value("${elasticsearch.host:#{null}}")
    private String elasticSearchHost;
    @Value("${elasticsearch.port:#{null}}")
    private String elasticSearchPort;
    @Value("${schema.path:#{null}}")
    private String schemaPath;
    @Value("${logging.config:#{null}}")
    private String loggingConfig;
    @Value("${reporting.folder:#{null}}")
    private String reportingFolder;

    @PostConstruct
    public void check() {
        List<String> folders = new ArrayList<>(Collections.singletonList(schemaPath));
        List<String> properties = new ArrayList<>(folders);
        properties.addAll(Arrays.asList(reportingFolder, elasticSearchHost, elasticSearchPort, loggingConfig));

        // Check properties values not null or empty
        List<String> notFound = properties.stream().filter(Strings::isNullOrEmpty).collect(Collectors.toList());
        if (!notFound.isEmpty()) {
            System.out.println();
            System.out.println("ERROR - Please review the following values needed from application.properties:");
            System.out.println();
            System.out.println("Value for elasticsearch.host: " + elasticSearchHost);
            System.out.println("Value for elasticsearch.port: " + elasticSearchPort);
            System.out.println("Value for schema.path: " + schemaPath);
            System.out.println("Value for logging.config: " + loggingConfig);
            System.out.println("Value for reporting.folder: " + reportingFolder);
            System.out.println();
            System.out.println("Some resource is null or empty. Please fix this and try again.");

            System.exit(1);
        }

        // Check folders exist and are not empty
        List<String> emptyFolders = folders.stream().filter(not(this::checkFolder)).collect(Collectors.toList());
        if (emptyFolders.size() > 0) {
            System.out.println();
            System.out.println("ERROR - Please review the following empty folders:");
            System.out.println();
            emptyFolders.forEach(System.out::println);
            System.out.println();
            System.out.println("Please correct the path or add needed files and try again.");

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
