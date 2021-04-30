package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.OrganisationLoaderService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.FieldInfo;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.FieldInfoProperty;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator.*;

/**
 * Service that defines methods for retrieving instances of {@link Validator}
 */
@Service
public class ValidatorProviderService {

    @Value("${ignore.invalid.references.policy:#{false}}")
    private Boolean ignoreInvalidReferencesPolicy;

    private final EsClientService elasticsearchService;
    private final CacheService cacheService;
    private final EnumService enumService;
    private final OrganisationLoaderService organisationLoaderService;

    @Autowired
    public ValidatorProviderService(EsClientService elasticsearchService, CacheService cacheService, EnumService enumService,
            OrganisationLoaderService organisationLoaderService) {
        this.elasticsearchService = elasticsearchService;
        this.cacheService = cacheService;
        this.enumService = enumService;
        this.organisationLoaderService = organisationLoaderService;
    }

    /**
     * Method for retrieving all the validators associated to a specific {@code fieldInfo} according to the
     * {@link eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema}
     *
     * @param fieldInfo
     *            the field info object
     * @return the full list of validators
     */
    public List<Validator> getValidators(FieldInfo fieldInfo) {
        List<Validator> validators = new ArrayList<>();

        if (fieldInfo == null) {
            return validators;
        }

        // add syntactic validator
        Validator typeValidator = getTypeValidator(fieldInfo);
        if (typeValidator != null) {
            validators.add(typeValidator);
        }

        // add required validator
        Validator requiredValidator = getRequiredValidator(fieldInfo);
        if (requiredValidator != null) {
            validators.add(requiredValidator);
        }

        // add semantic validator
        Validator valueValidator = getValueValidator(fieldInfo);
        if (valueValidator != null) {
            validators.add(valueValidator);
        }

        return validators;
    }

    /**
     * Method for getting the proper syntactic validator for validating the object type. For example, an {@link IntValidator} will be used
     * for validating if an object is of type {@code INT4}, a {@link StringValidator} will be used for validating if an object is of type
     * {@code VARCHAR(255)}, etc.
     * 
     * @param fieldInfo
     *            the field info object
     * @return the list of syntactic validators
     */
    private Validator getTypeValidator(FieldInfo fieldInfo) {
        PreconditionsHelper.notNull(fieldInfo, "fieldInfo");

        // parse the sql type, extract type and params
        FieldInfoProperty sqlTypeProperty = FieldInfoProperty.fromProperty("sqlType", fieldInfo.getSqlType());

        Validator validator = null;

        // select the type validator
        switch (sqlTypeProperty.getType().toLowerCase()) {
        case "int4":
            validator = new IntValidator();
            break;
        case "int8":
            validator = new LongValidator();
            break;
        case "varchar":
        case "text":
            validator = new StringValidator(sqlTypeProperty.getParams());
            break;
        case "bool":
            validator = new BooleanValidator();
            break;
        case "timestamp":
        case "timestamp with time zone":
            validator = new DateValidator();
            break;
        case "":
            break;
        case "?":
            // TODO must remove this once all mappings are defined
            break;
        default:
            throw new IllegalStateException(
                    String.format("Unknown syntactic validation [path=%s,type=%s]", fieldInfo.getEsPath(), fieldInfo.getSqlType()));
        }

        return validator;
    }

    /**
     * Method that returns a required validator if this is defined in the schema
     * 
     * @param fieldInfo
     *            the field info object
     * @return
     */
    private Validator getRequiredValidator(FieldInfo fieldInfo) {
        PreconditionsHelper.notNull(fieldInfo, "fieldInfo");

        if (fieldInfo.isRequired()) {
            return new NotNullValidator();
        } else {
            return null;
        }
    }

    /**
     * Method for getting the proper semantic validator for validating the object value
     * 
     * @param fieldInfo
     *            the field info object
     * @return
     */
    private Validator getValueValidator(FieldInfo fieldInfo) {
        PreconditionsHelper.notNull(fieldInfo, "fieldInfo");

        if (fieldInfo.getValue() == null) {
            return null;
        }

        // parse the value, extract type and params
        FieldInfoProperty valueProperty = FieldInfoProperty.fromProperty("value", fieldInfo.getValue());

        Validator validator = null;

        // select the type validator
        switch (valueProperty.getType().toLowerCase()) {
        case "reference":
            validator = new ReferenceValidator(elasticsearchService, cacheService, organisationLoaderService, ignoreInvalidReferencesPolicy, valueProperty.getParams());
            break;
        case "duplicate":
            validator = new DuplicateValidator(valueProperty.getParams());
            break;
        case "enum":
            validator = new EnumValidator(enumService, valueProperty.getParams());
            break;
        case "regex":
            validator = new RegexValidator(valueProperty.getParams());
            break;
        case "":
            break;
        default:
            throw new IllegalStateException(
                    String.format("Unknown semantic validation [path=%s,value=%s]", fieldInfo.getEsPath(), fieldInfo.getValue()));
        }

        return validator;
    }
}
