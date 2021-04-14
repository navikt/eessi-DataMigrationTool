package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;

public class ListSortUtils {

    public static final String ROOT_PARENT_ID = "";

    public static <T> List<T> depthFirstTreeSort(
            final List<T> arr,
            final Comparator<T> comparator,
            final Function<T, String> parentIdSupplier,
            final Function<T, String> childIdSupplier) {

        if (CollectionUtils.isEmpty(arr) || arr.size() == 1) {
            return arr;
        }

        List<T> result = new ArrayList<>();
        Map<String, List<T>> tree = makeTree(arr, parentIdSupplier);

        depthFirstTraversal(tree, ROOT_PARENT_ID, comparator, result::add, childIdSupplier);

        tree.forEach((id, children) -> {
                    if (CollectionUtils.isNotEmpty(children)) {
                        depthFirstTraversal(tree, id, comparator, result::add, childIdSupplier);
                    }
                }
        );

        return result;
    }

    private static <T> Map<String, List<T>> makeTree(final List<T> arr, final Function<T, String> parentIdSupplier) {
        Map<String, List<T>> tree = new HashMap<>();

        for (T obj : arr) {
            String parentId = parentIdSupplier.apply(obj);

            if (!tree.containsKey(parentId)) {
                tree.put(parentId, new ArrayList<>());
            }

            tree.get(parentId).add(obj);
        }

        return tree;
    }

    private static <T> void depthFirstTraversal(
            final Map<String, List<T>> tree,
            final String id,
            final Comparator<T> comparator,
            final Consumer<T> consumer,
            final Function<T, String> childIdSupplier) {

        List<T> children = tree.get(id);

        if (children != null && children.size() > 0) {
            children.sort(comparator);
            for (T child : children) {
                consumer.accept(child);
                depthFirstTraversal(tree, childIdSupplier.apply(child), comparator, consumer, childIdSupplier);
            }
            children.clear();
        }

    }

}
