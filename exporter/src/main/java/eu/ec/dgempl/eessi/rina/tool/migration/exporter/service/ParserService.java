package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.aggregator.Aggregator;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.SchemaEntry;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.JsonPathHelper;

/**
 * Service for parsing elasticsearch documents and executing actions on the document fields
 */
@Service
public class ParserService {
    private static final Logger logger = LoggerFactory.getLogger(ParserService.class);
    private final SchemaProviderService schemaProvider;

    @Autowired
    public ParserService(SchemaProviderService schemaProvider) {
        this.schemaProvider = schemaProvider;
    }

    /**
     * Method for parsing elasticsearch documents. The parser calls {@link Aggregator#process(String, Object, ValidationContext)} on every
     * leaf of the document and uses {@link Aggregator#accumulate(Object, Object)} for aggregating and propagating the results to the
     * document level
     *
     * @param document
     *            the elasticsearch document
     * @param parent
     *            the parent of {@code document}
     * @param aggregator
     *            {@link Aggregator} instance that provides methods for processing fields and aggregating results
     * @param <T>
     *            generic type of objects handled by {@code aggregator}
     * @return
     */
    public <T> T parse(EsDocument document, EsDocument parent, Aggregator<T> aggregator) {
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(parent, "parent");
        PreconditionsHelper.notNull(aggregator, "aggregator");

        // get the proper schema
        Schema schema = schemaProvider.getSchema(document.getIndex(), document.getType());

        // initialize the parsing context
        ValidationContext context = new ValidationContext(document, parent, schema);

        // recursively parse the json
        T result = walk("", document.getObject(), aggregator, context);

        return result;
    }

    /**
     * Recursive method that parses the contents of {@code obj} and calls {@link Aggregator#process(String, Object, ValidationContext)} on
     * each leaf of the object. The algorithm is as follows:
     * <ul>
     * <li>if the current key in the json points to an object, update the json path and call
     * {@link #walk(String, Object, Aggregator, ValidationContext)} on all object keys</li>
     * <li>if the current key points to an array, call {@link #walk(String, Object, Aggregator, ValidationContext)} on all items in the
     * array</li>
     * <li>else call {@link Aggregator#process(String, Object, ValidationContext)} on the current field (leaf field)</li>
     * </ul>
     * The results of {@link Aggregator#process(String, Object, ValidationContext)} are propagated towards the root call through
     * {@link Aggregator#accumulate(Object, Object)}. The result of the processing can be anything (e.g. boolean for simple validation, or
     * aggregated stats for data analysis)
     *
     * @param esFieldPath
     *            current path in the json object
     * @param obj
     *            the json object
     * @param aggregator
     *            {@link Aggregator} instance that provides methods for processing fields and aggregating results
     * @param context
     *            the validation context
     * @return
     */
    private <T> T walk(String esFieldPath, @Nullable Object obj, Aggregator<T> aggregator, ValidationContext context) {
        PreconditionsHelper.notNull(esFieldPath, "esFieldPath"); // can be ""
        PreconditionsHelper.notNull(aggregator, "aggregator");
        PreconditionsHelper.notNull(context, "context");

        String normalisedPath = JsonPathHelper.normalisePath(esFieldPath);
        SchemaEntry schemaEntry = context.getSchema().getSchemaEntry(normalisedPath);

        if (schemaEntry != null && schemaEntry.isIgnored()) {
            logger.debug("Field validation SKIP: ignored path [path={}]", esFieldPath);
            return aggregator.identity();
        }

        if (obj instanceof Map) {
            String updatedPath = "".equals(esFieldPath) ? esFieldPath : esFieldPath + ".";

            // @formatter:off
            T results = ((Map<String, Object>) obj).entrySet().stream()
                    .map(entry -> walk(updatedPath + entry.getKey(), entry.getValue(), aggregator, context))
                    .reduce(aggregator.identity(), aggregator::accumulate);
            // @formatter:on

            List<String> requiredFields = context.getSchema().getRequiredFields(normalisedPath);

            for (String requiredField : requiredFields) {
                if (((Map<String, Object>) obj).containsKey(requiredField) == false) {
                    // force the processing of required fields, even if they don't exist in Elasticsearch
                    results = aggregator.accumulate(results, aggregator.process(updatedPath + requiredField, null, context));
                }
            }

            return results;
        } else if (obj instanceof ArrayList) {
            ArrayList<Map<String, Object>> array = (ArrayList<Map<String, Object>>) obj;

            if (array.size() == 0) {
                return aggregator.identity();
            }

            // @formatter:off
            return IntStream.range(0, array.size())
                    .mapToObj(i -> walk(esFieldPath + "[" + i + "]", array.get(i), aggregator, context))
                    .reduce(aggregator.identity(), aggregator::accumulate);
        } else {
            return aggregator.process(esFieldPath, obj, context);
        }
    }
}
