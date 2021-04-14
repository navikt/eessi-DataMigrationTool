package eu.ec.dgempl.eessi.rina.repo;

import java.util.List;

import org.springframework.stereotype.Repository;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.repo._abstraction.RinaJpaRepoForLogableWithSidAndVersion;

@Repository
public interface IamUserRepoExtended extends RinaJpaRepoForLogableWithSidAndVersion<IamUser> {

    IamUser findByUsernameAndIsDeletedIsFalse(String username);

    List<IamUser> findByUsernameAndIsDeletedIsTrue(String username);

}
