package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import eu.ec.dgempl.apclient.sbdh.model.CaseActionType;
import eu.ec.dgempl.apclient.sbdh.model.ContactTypeIdentifier;
import eu.ec.dgempl.apclient.sbdh.model.InternetMediaType;

public class SbdhUtils {

    private static final Map<String, Class<? extends Enum<?>>> ENUM_MAPPINGS = Map.of(
            "sender.contactTypeIdentifier", ContactTypeIdentifier.class,
            "receivers.contactTypeIdentifier", ContactTypeIdentifier.class,
            "documentIdentification.action", CaseActionType.class,
            "attachments.internetMediaTypeCode", InternetMediaType.class
    );

    public static void fixSbdhEnumValues(Map<String, Object> sbdh, final String path) {
        for (Map.Entry<String, Object> entry : sbdh.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof List) {
                for (Object child : (List<Map<String, Object>>) value) {
                    fixSbdhEnumValues((Map<String, Object>) child, addPath(path, key));
                }
            } else if (value instanceof Map) {
                fixSbdhEnumValues((Map<String, Object>) value, addPath(path, key));
            } else if (value instanceof String) {
                String currentPath = addPath(path, key);
                if (ENUM_MAPPINGS.containsKey(currentPath)) {
                    Enum<?>[] enums = ENUM_MAPPINGS.get(currentPath).getEnumConstants();

                    Optional<Enum<?>> enumOptional = Stream.of(enums)
                            .filter(enumeration -> enumeration.toString().equals(value))
                            .findFirst();

                    enumOptional.ifPresent(enumValue -> entry.setValue(enumValue.name()));
                }
            }
        }
    }

    private static String addPath(String path, String newPath) {
        return StringUtils.isNotBlank(path) ? path + "." + newPath : newPath;
    }
}
