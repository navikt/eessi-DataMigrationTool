package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache.Cache;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache.CacheEntry;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.IdHelper;

/**
 * Service for caching Elasticsearch documents
 */
@Service
public class CacheService {
    private final Cache cache;

    public CacheService() {
        cache = new Cache();
    }

    /**
     * Method that checks if there is a cached entry of a previous Elasticsearch result. If the entry exists, it means the document was
     * already searched in Elasticsearch. The value of the cache entry will contain information whether the document actually existed at the
     * time of the query or not. If there is no entry, the document was not searched in Elasticsearch or removed from the cache
     *
     * @param index
     *            the elasticsearch index
     * @param type
     *            the elasticsearch type
     * @param documentId
     *            the document id
     * @return
     */
    public CacheEntry get(String index, String type, String documentId) {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(documentId, "documentId");

        return cache.get(IdHelper.getDocumentReference(index, type, documentId));
    }

    /**
     * Method for caching the result of an elasticsearch exists query
     *
     * @param value
     *            the entry to be cached
     */
    public void add(CacheEntry value) {
        PreconditionsHelper.notNull(value, "value");

        cache.put(IdHelper.getDocumentReference(value.getIndex(), value.getType(), value.getDocumentId()), value);
    }

    /**
     * Method for removing cache entries based on the {@code context}. The method does not delete cached entries that have a different index
     * from the context. For example, if in the context of a case, one caches a user, or an organisation, these will not be considered
     * resources bound to the case context and will not be deleted from the cache
     * 
     * @param context
     *            the cache entry context (see {@link CacheEntry#getContext()})
     */
    public void removeEntriesByContext(String context) {
        PreconditionsHelper.notEmpty(context, "context");

        // create the delete condition
        Predicate<CacheEntry> predicate = entry -> entry.getContext().equals(context) && entry.getIndex().equals(context.split("_")[0]);

        // delete entries
        cache.deleteEntries(predicate);
    }
}
