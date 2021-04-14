package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EnumWrapper;

/**
 * Service for loading enums and transforming them for easy lookup
 */
@Service
public class EnumService {
    private final Map<String, EnumWrapper> enums;

    // @formatter:off
    private static final String[] KNOWN_ENUM_PACKAGES = new String[] {
            "eu.ec.dgempl.eessi.rina.model.enumtypes.",
            "eu.ec.dgempl.eessi.rina.model.enumtypes.audit.",
            "eu.ec.dgempl.eessi.rina.model.enumtypes.portal."
    };
    // @formatter:on

    public EnumService() {
        enums = new HashMap<>();
    }

    /**
     * Method that checks if a value can be found in an enum
     * 
     * @param enumName
     *            the enum class name
     * @param value
     *            the value that is searched in the enum
     * @return
     * @throws IllegalAccessException
     * @throws NoClassDefFoundError
     */
    public boolean exists(String enumName, String value) throws IllegalAccessException, NoClassDefFoundError {
        PreconditionsHelper.notEmpty(enumName, "enumName");
        PreconditionsHelper.notNull(value, "value");

        EnumWrapper enumWrapper = enums.get(enumName);

        if (enumWrapper == null) {
            Class clazz = null;

            // try to find the enum in any of the known packages
            for (String pkg : KNOWN_ENUM_PACKAGES) {
                try {
                    clazz = EnumService.class.getClassLoader().loadClass(pkg + enumName);
                    // break if enum is found in current package
                    break;
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // ignore and try another package
                }
            }

            // check if enum was found
            if (clazz == null) {
                throw new NoClassDefFoundError("Enum " + enumName + " could not be found.");
            }

            // create the enum wrapper
            enumWrapper = new EnumWrapper(clazz);

            // update the enums map
            enums.put(enumName, enumWrapper);
        }

        return enumWrapper.lookup(value);
    }
}
