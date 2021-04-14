package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto;

import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;

public class DmtEnumNotFoundException extends EnumNotFoundEessiRuntimeException {

    public DmtEnumNotFoundException(Class<? extends Enum> enumType, String value) {
        super(String.format("Enum of type [%s] and value [%s] Not Found!", enumType.getSimpleName(), value));
    }

    public DmtEnumNotFoundException(Class<? extends Enum> enumType, String identifier, String value) {
        super(String.format("Enum of type [%s] and identifier [%s=%s] Not Found!", enumType.getSimpleName(), identifier, value));
    }
}
