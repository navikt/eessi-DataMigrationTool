package eu.ec.dgempl.eessi.rina.repo;

import org.springframework.stereotype.Repository;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempDocument;
import eu.ec.dgempl.eessi.rina.repo._abstraction.RinaJpaRepoForPersistableWithSid;

@Repository
public interface TempDocumentRepo extends RinaJpaRepoForPersistableWithSid<TempDocument> {
}
