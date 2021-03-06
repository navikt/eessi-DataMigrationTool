package eu.ec.dgempl.eessi.rina.repo;

import org.springframework.stereotype.Repository;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAction;
import eu.ec.dgempl.eessi.rina.repo._abstraction.RinaJpaRepoForPersistableWithSid;

@Repository
public interface TempActionRepo extends RinaJpaRepoForPersistableWithSid<TempAction> {
}
