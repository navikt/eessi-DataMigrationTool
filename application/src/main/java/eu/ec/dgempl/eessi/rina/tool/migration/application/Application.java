package eu.ec.dgempl.eessi.rina.tool.migration.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.util.CollectionUtils;

import eu.ec.dgempl.eessi.rina.tool.migration.application.full.FullApp;
import eu.ec.dgempl.eessi.rina.tool.migration.application.validation.ValidationApp;

public class Application {

    private final static String ARG_VALIDATE_ALL = "-validate-all";
    private final static String ARG_VALIDATE_CASE = "-validate-case";
    private final static String ARG_VALIDATE_AND_IMPORT = "-validate-import";
    private final static String ARG_IMPORT_ALL = "-import-all";
    private final static String ARG_IMPORT_CASE = "-import-case";
    private final static String ARG_IMPORT_CASES_BULK = "-import-cases-bulk";
    private final static String ARG_HELP_LONG = "-help";
    private final static String ARG_HELP_SHORT = "-h";

    public static void main(String[] args) throws FileNotFoundException {
        // Accepted args:
        //
        // -validate-all (default)
        // -validate-case case
        // -validate-import [importer_name]
        // -import-all [importer_name]
        // -import-case case
        // -import-cases-bulk filename
        // -help, -h

        List<String> arguments = Arrays.stream(args).collect(Collectors.toList());

        boolean proceed = true;

        switch (args.length) {
            case 0: {
                showNoArgsInfo();
                break;
            }
            case 1: {
                if ((arguments.contains(ARG_HELP_LONG) || arguments.contains(ARG_HELP_SHORT)) || (!arguments.contains(ARG_VALIDATE_ALL)
                        && !arguments.contains(ARG_VALIDATE_AND_IMPORT) && !arguments.contains(ARG_IMPORT_ALL))) {
                    showHelp();
                    proceed = false;
                }
                break;
            }
            case 2: {
                String importOption = arguments.get(0);
                if (!importOption.equals(ARG_VALIDATE_CASE) &&
                        !importOption.equals(ARG_IMPORT_CASE) &&
                        !importOption.equals(ARG_VALIDATE_AND_IMPORT) &&
                        !importOption.equals(ARG_IMPORT_ALL) &&
                        !importOption.equals(ARG_IMPORT_CASES_BULK)) {
                    showHelp();
                    proceed = false;
                }
                break;
            }
            default:
                showHelp();
                proceed = false;
                break;
        }

        if (proceed) {
            redirectSystemErr();

            if (arguments.contains(ARG_VALIDATE_ALL) || arguments.contains(ARG_VALIDATE_CASE) || CollectionUtils.isEmpty(arguments)) {
                SpringApplication.run(ValidationApp.class, args);
            } else {
                SpringApplication.run(FullApp.class, args);
            }
        }
    }

    private static void showHelp() {
        String help = String.join(System.lineSeparator(), "", "---Data Migration Help---", "", "Usage: java -jar <jar_name> [options]", "",
                "Options:", "", " " + ARG_VALIDATE_ALL + "                      Default execution. Validates the data from the source",
                "                                    indicated in the config/applications.properties", "",
                " " + ARG_VALIDATE_CASE + " value               Validates the specified caseId indicated",
                "                                    in the 'value' parameter", "",
                " " + ARG_VALIDATE_AND_IMPORT + "                   First validates the data and then imports it", "",
                " " + ARG_IMPORT_ALL + "                        Imports the data without previous validation", "",
                " " + ARG_IMPORT_CASE + " value                 Imports the specified caseId indicated",
                "                                    in the 'value' parameter", "",
                " " + ARG_IMPORT_CASES_BULK + " filename        Imports the caseIds specified in the file, one caseId per line", "",
                " " + ARG_HELP_LONG + ", " + ARG_HELP_SHORT + "                          Shows this help", "",
                " " + "Please review the User Manual for more information and examples", "");
        System.out.println(help);
    }

    private static void showNoArgsInfo() {
        String help = System.lineSeparator() + "Starting application with DEFAULT execution: only validation." + System.lineSeparator()
                + "Please execute 'java -jar <jar_name> " + ARG_HELP_LONG + "' to see different execution options" + System.lineSeparator();
        System.out.println(help);
    }

    private static void redirectSystemErr() throws FileNotFoundException {
        File logs = new File("./logs");

        if (!logs.exists()) {
            logs.mkdir();
        }

        File file = new File("./logs/err.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);
    }

}
