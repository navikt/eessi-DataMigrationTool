package eu.ec.dgempl.eessi.rina.tool.migration.importer.auditor;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.model.jpa.listener.AuditListener;
import eu.ec.dgempl.eessi.rina.model.jpa.listener.LocalAuditorAware;

@Component("auditorAware")
@Primary
public class AuditorAwareImpl implements LocalAuditorAware<String> {

    private static final Object MUTEX = new Object();

    @PostConstruct
    public void setup() {
        synchronized (MUTEX) {
            AuditListener.setAuditorAware(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public String getCurrentAuditor() {
        return "0";
    }

}