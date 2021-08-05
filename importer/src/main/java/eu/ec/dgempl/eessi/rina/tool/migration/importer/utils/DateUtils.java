package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.ec.dgempl.eessi.rina.commons.date.ZonedDateTimePeriod;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Auditable;

public class DateUtils {

    public static int getIntervalIndex(Map<ZonedDateTimePeriod, Integer> intervals, ZonedDateTime creationDate) {
        Optional<Integer> first = intervals.entrySet()
                .stream()
                .filter(pairIntegerEntry -> {
                    ZonedDateTimePeriod interval = pairIntegerEntry.getKey();
                    return creationDate.isEqual(interval.getStart()) ||
                            (creationDate.isAfter(interval.getStart()) && creationDate.isBefore(interval.getEnd()));
                })
                .map(Map.Entry::getValue)
                .findFirst();

        return first.orElse(-1);
    }

    public static <T extends Auditable> Map<ZonedDateTimePeriod, Integer> getIntervalsMap(final List<T> auditables) {
        Map<ZonedDateTimePeriod, Integer> intervals = new HashMap<>();

        auditables.sort(Comparator.comparing(t -> t.getAudit().getCreatedAt()));

        for (int i = 0; i < auditables.size(); i++) {
            T first = auditables.get(i);

            ZonedDateTimePeriod interval;

            if (i == auditables.size() - 1) {
                interval = new ZonedDateTimePeriod(first.getAudit().getCreatedAt(), ZonedDateTime.now());
            } else {
                T second = auditables.get(i + 1);
                interval = new ZonedDateTimePeriod(first.getAudit().getCreatedAt(), second.getAudit().getCreatedAt());
            }
            intervals.put(interval, i);
        }
        return intervals;
    }
}
