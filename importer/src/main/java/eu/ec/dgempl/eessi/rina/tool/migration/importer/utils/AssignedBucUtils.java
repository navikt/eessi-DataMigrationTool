package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignedBuc;

public class AssignedBucUtils {

    public static Comparator<AssignedBuc> compareByValidityStartDateNullsLast() {
        return (a, b) -> {
            ZonedDateTime first = a.getValidityStartDate();
            ZonedDateTime second = b.getValidityStartDate();

            return Comparator.nullsLast(ZonedDateTime::compareTo).compare(first, second);
        };
    }

    public static ZonedDateTime getMaxValidityEndDate(List<AssignedBuc> assignedBucs) {
        List<ZonedDateTime> validityEndDates = assignedBucs.stream()
                .map(AssignedBuc::getValidityEndDate)
                .sorted(Comparator.nullsLast(ZonedDateTime::compareTo).reversed())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validityEndDates)) {
            return null;
        }

        return validityEndDates.get(0);
    }

}
