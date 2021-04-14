package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

/**
 * Annotates a {@link ElasticTypeImporter} class to specify the information about which ES data that importer handles.
 *
 * @author Silviu Stefan
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ElasticTypeImporter {

    /**
     * The type from the enumeration of existing ES types
     *
     * @return
     */
    EElasticType type();

    /**
     * Processing order of this importer
     *
     * @return
     */
    int order() default 0;
}
