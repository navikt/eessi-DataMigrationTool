package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

public class MapHolder {

    private final Map<String, Object> holding;

    private final Map<String, Boolean> visitedFields;
    private final String path;

    public MapHolder(Map<String, Object> holding, final Map<String, Boolean> visitedFields, final String path) {
        this.holding = holding;
        this.visitedFields = visitedFields;
        this.path = path;
    }

    public List<MapHolder> listToMapHolder(String key) {
        return listToMapHolder(key, false);
    }

    public List<MapHolder> listToMapHolder(String key, boolean deep) {
        List<Map<String, Object>> value;
        if (deep) {
            value = (List<Map<String, Object>>) getDeep(key);
        } else {
            value = (List<Map<String, Object>>) holding.get(key);
        }

        visit(key);

        if (CollectionUtils.isNotEmpty(value)) {
            return IntStream.range(0, value.size())
                    .mapToObj(i -> new MapHolder(value.get(i), this.visitedFields, addPath(key + "[" + i + "]")))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public <T> List<T> list(String key, Class<T> clazz, boolean deep) {
        Object result;
        if (deep) {
            result = getDeep(key);
        } else {
            result = holding.get(key);
        }

        visit(key);
        return result != null ? (List<T>) result : Collections.emptyList();

    }

    public String string(String key) {
        String o = string(key, false);
        visit(key);
        return o;
    }

    public String string(String key, boolean deep) {
        Object value;
        if (deep) {
            value = getDeep(key);
        } else {
            value = holding.get(key);
        }

        visit(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return String.valueOf(value);
        }

        if (value instanceof Boolean) {
            return String.valueOf(value);
        }

        return (String) value;
    }

    public Boolean bool(String key) {
        return bool(key, false);
    }

    public Boolean bool(String key, Boolean defaultValue) {
        Boolean bool = bool(key, false);
        if (bool != null) {
            return bool;
        }

        return defaultValue;
    }

    public Boolean bool(String key, boolean deep) {
        Object value;
        if (deep) {
            value = getDeep(key);
        } else {
            value = holding.get(key);
        }

        visit(key);

        if (value == null) {
            return null;
        }

        if ("false".equals(value)) {
            return false;
        }

        if ("true".equals(value)) {
            return true;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        throw new RuntimeException("Could not map boolean " + value + " for key: " + key);
    }

    public Integer integer(String key) {
        return integer(key, false);
    }

    public Integer integer(String key, boolean deep) {
        Integer value;
        if (deep) {
            value = (Integer) getDeep(key);
        } else {

            value = (Integer) holding.get(key);
        }

        visit(key);
        return value;

    }

    public Long asLong(String key) {
        return asLong(key, false);
    }

    public Long asLong(String key, boolean deep) {
        Long value;
        if (deep) {
            value = (Long) getDeep(key);
        } else {

            value = (Long) holding.get(key);
        }

        visit(key);
        return value;

    }

    public Map<String, Object> getMap(String key) {
        Map<String, Object> result = getMap(key, false);

        visit(key);

        return result;
    }

    public Map<String, Object> getMap(String key, boolean deep) {
        if (deep) {
            return (Map<String, Object>) getDeep(key);
        }

        return (Map<String, Object>) holding.get(key);
    }

    public MapHolder getMapHolder(String key) {
        return getMapHolder(key, false);
    }

    public MapHolder getMapHolder(String key, boolean deep) {
        Map<String, Object> value;
        if (deep) {
            value = (Map<String, Object>) getDeep(key);
        } else {
            value = (Map<String, Object>) holding.get(key);
        }

        return new MapHolder(value, visitedFields, addPath(key));

    }

    public Set<String> fields() {
        return this.holding.keySet();
    }

    public Object get(String key, boolean deep) {
        if (deep) {
            return getDeep(key);
        }

        Object o = holding.get(key);

        visit(key, o);

        return o;
    }

    public boolean containsKey(String key) {
        return holding.containsKey(key);
    }

    /**
     * Recursively traverses a document map through the key in a form like: key1.key2.key3
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object getDeep(final String key) {

        Assert.isTrue(StringUtils.isNotBlank(key), "Parameter 'key' cannot be empty.");
        Assert.notNull(holding, "Parameter 'key' cannot be empty.");

        String[] path = StringUtils.split(key, ".");
        Map<String, Object> inner = holding;
        int i = 0;
        do {

            Object value = inner.get(path[i]);
            visit(key, value);

            if (value != null) {

                if (Map.class.isAssignableFrom(value.getClass())) {
                    inner = (Map<String, Object>) inner.get(path[i]);

                    if (i == path.length - 1) {
                        return inner;
                    }

                } else {
                    return value;
                }

            } else {
                return null;
            }

            i++;

        } while (i < path.length);

        return null;
    }

    public Map<String, Boolean> getVisitedFields() {
        return visitedFields;
    }

    public String addPath(String path) {
        return StringUtils.isNotBlank(this.path) ? this.path + "." + path : path;
    }

    private void visit(final String key, final Object o) {
        if (!(o instanceof Map || o instanceof List)) {
            visit(key);
        }
    }

    private void visit(final String key) {
        visitedFields.putIfAbsent(addPath(key), true);
    }

    public Map<String, Object> getHolding() {
        return holding;
    }
}
