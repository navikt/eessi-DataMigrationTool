package eu.ec.dgempl.eessi.rina.tool.migration.buc;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.buc.precondition.PreconditionsHelper;

@Component
public class BucDefinitionImporterFactory {

    private static final Logger logger = LoggerFactory.getLogger(BucDefinitionImporterFactory.class);

    private String bucDefinitionsPath;

    private Map<String, Case> bucDefinitionsMap;

    /**
     * Java package containing the model generated from the BUC XSD definition.
     */
    public static final String BUC_MODEL_PACKAGE = "eu.ec.dgempl.eessi.rina.buc.core.model";

    /**
     * JAXB context holder
     */
    private final JAXBContext jaxbContext;

    public BucDefinitionImporterFactory(@Value("${buc.definitions.path}") final String bucDefinitionsPath) throws JAXBException {

        logger.info("Initializing the BucDefinitionFactory");

        this.bucDefinitionsPath = bucDefinitionsPath;
        this.jaxbContext = JAXBContext.newInstance(BUC_MODEL_PACKAGE);
        this.bucDefinitionsMap = new ConcurrentHashMap<>();
        logger.trace("Created the JAXB context");
    }

    /**
     * Unmarshalizes the configuration XML file located as a class path resource.
     *
     * @param bucType
     * @param caseRole
     * @param modelVersion
     * @return
     * @throws Exception
     */
    public Case loadBucConfiguration(final String bucType, final ECaseRole caseRole, final String modelVersion) throws Exception {

        PreconditionsHelper.notBlank(bucType, "bucType");
        PreconditionsHelper.notNull(caseRole, "caseRole");
        PreconditionsHelper.notBlank(modelVersion, "modelVersion");

        String bucKey = bucType + "_" + caseRole + "_" + modelVersion;

        if (bucDefinitionsMap.containsKey(bucKey)) {

            return bucDefinitionsMap.get(bucKey);

        } else {
            // load the BUC definition file
            String definitionFilePath = getBucDefinitionAbsolutePath(bucType, caseRole, modelVersion);
            logger.debug("Loading the BUC definition [{}]", definitionFilePath);
            try (InputStream is = new FileInputStream(definitionFilePath)) {

                Case extractedBucCase = createFromInputStream(is, Case.class);
                bucDefinitionsMap.put(bucKey, extractedBucCase);

                return extractedBucCase;
            }
        }
    }

    /**
     * Get the BUC definition absolute path.
     *
     * @param bucType
     * @param caseRole
     * @param modelVersion
     * @return
     */
    protected String getBucDefinitionAbsolutePath(final String bucType, final ECaseRole caseRole, final String modelVersion) {

        Path bucXmlPath = Paths.get(bucDefinitionsPath,
                modelVersion, caseRole.value().toLowerCase() + "_" + bucType.toLowerCase() + ".xml");
        return bucXmlPath.toAbsolutePath().toString();

    }

    /**
     * Unmarshalizes the given input stream into the object of type {@code clazz}.
     *
     * @param xmlInputStream
     * @param clazz
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T createFromInputStream(final InputStream xmlInputStream, Class<T> clazz) throws Exception {

        PreconditionsHelper.notNull(xmlInputStream, "xmlInputStream");
        PreconditionsHelper.notNull(clazz, "clazz");

        try {

            logger.trace("Creating unmarshaller for the BUC definition.");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            T object = (T) jaxbUnmarshaller.unmarshal(xmlInputStream);
            logger.trace("Successfully unmarshalled the BUC definition XML.");

            return object;

        } catch (JAXBException e) {
            logger.error("Could not unmarshal the XML Input Stream into an object: {}", e.getCause());
            throw new Exception(String.format("Could not unmarshal the XML Input Stream into an object"), e);
        }
    }

    public void setBucDefinitionsPath(String bucDefinitionsPath) {
        this.bucDefinitionsPath = bucDefinitionsPath;
    }
}
