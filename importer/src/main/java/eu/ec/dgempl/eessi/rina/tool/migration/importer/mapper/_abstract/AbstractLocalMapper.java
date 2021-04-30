package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Log;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.DateResolver;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

import ma.glasnost.orika.CustomMapper;

@Transactional(propagation = Propagation.MANDATORY)
public abstract class AbstractLocalMapper<A, B> extends CustomMapper<A, B> implements LocalMapper<A, B> {

    protected void mapDate(final MapHolder a, String key, Consumer<ZonedDateTime> dateConsumer) {
        String date = a.string(key);
        if (StringUtils.isNotBlank(date)) {
            dateConsumer.accept(DateResolver.parse(date));
        }
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
