package eu.ec.dgempl.eessi.rina.tool.migration.importer.helper;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EValidationXSDTypes;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.generic.GenericEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.GlobalParam;
import eu.ec.dgempl.eessi.rina.repo.GlobalParamRepo;

@Component
public class GlobalParamHelper {

    Logger logger = LoggerFactory.getLogger(GlobalParamHelper.class);

    private final GlobalParamRepo globalParamRepo;

    public GlobalParamHelper(final GlobalParamRepo globalParamRepo) {
        this.globalParamRepo = globalParamRepo;
    }

    public String getXSDPath(final String sedName, final String sedVersion, EValidationXSDTypes type) {
        if (sedName == null || sedName.isEmpty()) {
            throw new GenericEessiRuntimeException("sedName is null");
        }
        if (sedVersion == null || sedVersion.isEmpty()) {
            throw new GenericEessiRuntimeException("sedVersion is null");
        }

        String xsdRepositoryPath = getGlobalParam(EGlobalParam.MESSAGING_REPOSITORY_XSD_PATH);

        if (xsdRepositoryPath == null || xsdRepositoryPath.equals("")) {
            throw new GenericEessiRuntimeException("The path to XSD files is not set!");
        }

        // append separator if needed
        if (!xsdRepositoryPath.endsWith("\\") && !xsdRepositoryPath.endsWith("/"))
            xsdRepositoryPath += File.separatorChar;

        // go to sed sub-folder
        xsdRepositoryPath += "sed";
        // filter disk files by name
        File folder = new File(xsdRepositoryPath);
        if (!folder.exists()) {
            throw new GenericEessiRuntimeException("The path to xsd repository is probably wrong (" + xsdRepositoryPath + ").");
        }

        final String pattern;
        // determine pattern based on type
        // TODO: remove this when we have a consistent naming pattern for
        // xsd files
        final boolean useNewPattern = !sedVersion.equals("1.0") && !sedVersion.equals("3.2");

        if (useNewPattern) { // U015-DRAFT-4.0.0-20160622T094015.xsd or
            // U015-4.0.0-20160622T094015.xsd OR
            // DA001-DRAFT-4.0.xsd or DA001-4.0.xsd
            if (type == EValidationXSDTypes.Full)
                pattern = sedName + "\\-" + (sedVersion.replace(".", "\\.")) + "(\\..*)?\\.xsd";
            else
                pattern = sedName + "\\-DRAFT\\-" + (sedVersion.replace(".", "\\.")) + "(\\..*)?\\.xsd";
        } else { // Pension-P3000_EE-CountrySpecificInfo_EE-3.2.xsd
            // No DRAFT here for the moment, just one XSD
            pattern = ".*\\-" + sedName + "\\-[\\w|\\d]+\\-" + (sedVersion.replace(".", "\\.")) + "\\." + "((?!DRAFT).)*";

            // if(type == ValidationXSDTypes.Full)
            // pattern = ".*\\-" + sedName + "\\-[\\w|\\d]+\\-" +
            // (sedVersion.replace(".", "\\.")) + "\\." + "((?!DRAFT).)*";
            // else
            // pattern = ".*\\-" + sedName + "\\-[\\w|\\d]+\\-" +
            // (sedVersion.replace(".", "\\.")) + "[\\.|\\-]" + ".*" +
            // "DRAFT\\..*";
        }

        final File[] schemaFiles = folder.listFiles((dir, filename) -> filename.matches(pattern));

        if (schemaFiles == null) {
            throw new GenericEessiRuntimeException("The path to xsd repository is probably wrong.");
        }

        if (schemaFiles.length == 0) {
            logger.debug("Could not find " + type + " xsd for " + sedName + " v" + sedVersion + " in " + xsdRepositoryPath);
            return null;
        }

        return schemaFiles[0].getAbsolutePath();
    }

    public String getGlobalParam(final EGlobalParam key) {
        String value;
        value = getGlobalParamNoCache(key);

        return value;
    }

    private String getGlobalParamNoCache(final EGlobalParam key) {
        final GlobalParam gap = globalParamRepo.findByKey(key);
        return gap.getValue();
    }

}
