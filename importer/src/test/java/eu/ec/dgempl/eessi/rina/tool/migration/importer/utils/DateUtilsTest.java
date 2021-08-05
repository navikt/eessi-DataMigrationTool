package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import eu.ec.dgempl.eessi.rina.commons.date.ZonedDateTimePeriod;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;

public class DateUtilsTest {

    @Test
    public void testIntervalsCreated() {
        List<SubdocumentBversion> activities = new ArrayList<>();

        SubdocumentBversion subdocumentBversion1 = createSubdocumentBversion(
                1,
                ZonedDateTime.of(2020, 11, 5, 12, 23, 44, 0, ZoneId.systemDefault()));
        activities.add(subdocumentBversion1);

        SubdocumentBversion subdocumentBversion2 = createSubdocumentBversion(
                2,
                ZonedDateTime.of(2020, 12, 6, 12, 23, 44, 0, ZoneId.systemDefault()));
        activities.add(subdocumentBversion2);

        SubdocumentBversion subdocumentBversion3 = createSubdocumentBversion(
                3,
                ZonedDateTime.of(2020, 11, 6, 12, 23, 44, 0, ZoneId.systemDefault()));
        activities.add(subdocumentBversion3);

        Map<ZonedDateTimePeriod, Integer> intervalsMap = DateUtils.getIntervalsMap(activities);

        Assert.assertEquals(3, intervalsMap.size());
        Assert.assertEquals(2, activities.get(2).getId());
    }

    private SubdocumentBversion createSubdocumentBversion(int id, ZonedDateTime createdAt) {
        SubdocumentBversion subdocumentBversion = createRandomSubdocumentBversion();
        Audit audit = new Audit();
        audit.setCreatedAt(createdAt);
        subdocumentBversion.setAudit(audit);
        subdocumentBversion.setId(id);

        return subdocumentBversion;
    }

}