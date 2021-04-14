package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ListSortUtilsTests {

    @Test
    public void testListIsOrdered() {
        List<Document> documents = List.of(
                new Document("d1", ""),
                new Document("d2", ""),
                new Document("d3", "d1"),
                new Document("d4", "d2"),
                new Document("d5", "d3"),
                new Document("d6", "randomDocId")
        );

        List<Document> sortedDocuments = ListSortUtils.depthFirstTreeSort(
                documents,
                Comparator.comparing(Document::getParentId),
                Document::getParentId,
                Document::getId
        );

        List<String> sortedDocumentsIds = List.of("d1", "d3", "d5", "d2", "d4", "d6");

        Assert.assertArrayEquals(sortedDocumentsIds.toArray(), sortedDocuments.stream().map(Document::getId).toArray());
    }

    private static class Document {
        String id;
        String parentId;

        public Document(String id, String parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return parentId;
        }
    }

}
