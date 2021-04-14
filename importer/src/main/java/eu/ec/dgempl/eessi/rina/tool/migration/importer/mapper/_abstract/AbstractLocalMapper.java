package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Log;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

import ma.glasnost.orika.CustomMapper;

@Transactional(propagation = Propagation.MANDATORY)
public abstract class AbstractLocalMapper<A, B> extends CustomMapper<A, B> implements LocalMapper<A, B> {

    // @formatter:off
    private static final DateTimeFormatter[] SUPPORTED_DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    };
    // @formatter:on

    protected void mapDate(final MapHolder a, String key, Consumer<ZonedDateTime> dateConsumer) {
        String date = a.string(key);
        if (StringUtils.isNotBlank(date)) {
            dateConsumer.accept(parseDate(date));
        }
    }

    public static ZonedDateTime parseDate(String date) {
        for (DateTimeFormatter format : SUPPORTED_DATE_FORMATS) {
            try {
                return ZonedDateTime.parse(date, format);
            } catch (DateTimeParseException dtpe) {
                // ignore and try another format
            }
        }
        throw new RuntimeException("Could not parse ZonedDateTime: " + date);
    }

    public static void setDefaultLog(Consumer<Log> logConsumer) {
        Log log = new Log();

        ZonedDateTime startOfTime = Instant.EPOCH.atZone(ZoneId.systemDefault());
        log.setCreatedAt(startOfTime);
        log.setUpdatedAt(startOfTime);

        logConsumer.accept(log);
    }

    public static void setDefaultAudit(Consumer<Audit> auditConsumer) {
        Audit audit = new Audit();

        ZonedDateTime startOfTime = Instant.EPOCH.atZone(ZoneId.systemDefault());
        audit.setCreatedAt(startOfTime);
        audit.setUpdatedAt(startOfTime);

        audit.setCreatedBy("0");
        audit.setUpdatedBy("0");

        auditConsumer.accept(audit);
    }

}
