package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.glasnost.orika.MappingContext;

public class MappingContextBuilder {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(MappingContextBuilder.class);

    private final MappingContext context;
    private final Map<Object, Object> properties;

    private MappingContextBuilder() {
        super();
        properties = new HashMap<>();
        context = new MappingContext.Factory().getContext();
    }

    public static MappingContextBuilder instance() {
        return new MappingContextBuilder();
    }

    public MappingContext build() {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            context.setProperty(entry.getKey(), entry.getValue());
        }
        return context;
    }

    /**
     * Provide a Mapping context with a property
     *
     * @param propertyName
     * @param propertyValue
     * @return
     */
    public MappingContextBuilder addProp(final Object propertyName, final Object propertyValue) {
        properties.put(propertyName, propertyValue);
        return this;
    }

}