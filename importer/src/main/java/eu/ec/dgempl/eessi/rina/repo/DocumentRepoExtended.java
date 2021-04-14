package eu.ec.dgempl.eessi.rina.repo;

import java.util.List;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.repo._abstraction.RinaJpaRepoForAuditableWithSidAndVersion;

@Component
public interface DocumentRepoExtended extends RinaJpaRepoForAuditableWithSidAndVersion<Document> {

    List<Document> findByRinaCaseId(String caseId);
}
