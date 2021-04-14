package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.AssignedBucUtils.*;
import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignedBuc;

public class AssignedBucUtilsTest {

    @Test
    public void testComparator() {
        List<AssignedBuc> assignedBucs = new ArrayList<>();

        AssignedBuc assignedBuc = new AssignedBuc();
        ZonedDateTime firstStartDate = ZonedDateTime.now();
        assignedBuc.setValidityStartDate(firstStartDate);
        assignedBuc.setValidityEndDate(ZonedDateTime.now());
        assignedBucs.add(assignedBuc);

        AssignedBuc assignedBuc1 = new AssignedBuc();
        ZonedDateTime secondStartDate = ZonedDateTime.now();
        assignedBuc1.setValidityStartDate(secondStartDate);
        assignedBuc1.setValidityEndDate(ZonedDateTime.now());
        assignedBucs.add(assignedBuc1);

        AssignedBuc assignedBuc3 = new AssignedBuc();
        assignedBucs.add(assignedBuc3);

        assignedBucs.sort(compareByValidityStartDateNullsLast());

        assertEquals(firstStartDate, assignedBucs.get(0).getValidityStartDate());
    }

    @Test
    public void testGetMaxValidityStartDate() {
        List<AssignedBuc> assignedBucs = new ArrayList<>();

        AssignedBuc assignedBuc = new AssignedBuc();
        ZonedDateTime firstStartDate = ZonedDateTime.now();
        assignedBuc.setValidityStartDate(firstStartDate);
        assignedBuc.setValidityEndDate(ZonedDateTime.now());
        assignedBucs.add(assignedBuc);

        AssignedBuc assignedBuc1 = new AssignedBuc();
        ZonedDateTime secondStartDate = ZonedDateTime.now();
        assignedBuc1.setValidityStartDate(secondStartDate);
        assignedBuc1.setValidityEndDate(ZonedDateTime.now());
        assignedBucs.add(assignedBuc1);

        AssignedBuc assignedBuc3 = new AssignedBuc();
        ZonedDateTime firstEndDate = ZonedDateTime.now();
        assignedBuc3.setValidityStartDate(secondStartDate);
        assignedBuc3.setValidityEndDate(firstEndDate);
        assignedBucs.add(assignedBuc3);

        ZonedDateTime zonedDateTime = getMaxValidityEndDate(assignedBucs);

        assertEquals(firstEndDate, zonedDateTime);
    }

   @Test
    public void testGetMaxValidityStartDateIsNull() {
        List<AssignedBuc> assignedBucs = new ArrayList<>();

        AssignedBuc assignedBuc = new AssignedBuc();
        ZonedDateTime firstStartDate = ZonedDateTime.now();
        assignedBuc.setValidityStartDate(firstStartDate);
        assignedBuc.setValidityEndDate(ZonedDateTime.now());
        assignedBucs.add(assignedBuc);

        AssignedBuc assignedBuc1 = new AssignedBuc();
        ZonedDateTime secondStartDate = ZonedDateTime.now();
        assignedBuc1.setValidityStartDate(secondStartDate);
        assignedBuc1.setValidityEndDate(ZonedDateTime.now());
        assignedBucs.add(assignedBuc1);

        AssignedBuc assignedBuc3 = new AssignedBuc();
        assignedBucs.add(assignedBuc3);

        ZonedDateTime zonedDateTime = getMaxValidityEndDate(assignedBucs);

        assertNull(zonedDateTime);
    }

}
