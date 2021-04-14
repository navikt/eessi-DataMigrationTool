package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo._abstraction.RinaJpaRepo;

public class RepositoryUtils {

    public static <T extends PersistableWithSid> T findById(String id, Function<String, T> mapper) {
        return findById(id, mapper, null);
    }

    public static <T extends PersistableWithSid> T findById(String id, Function<String, T> mapper, Class<T> clazz) {

        if (clazz != null && clazz.isAssignableFrom(IamUser.class)) {
            id = id.equalsIgnoreCase("system") ? "0" : id;
        }

        T t = mapper.apply(id);
        if (t == null && clazz != null) {
            throw new EntityNotFoundEessiRuntimeException(clazz, UniqueIdentifier.id, id);
        }
        return t;
    }

    public static <T extends Persistable> void saveInBulk(
            Supplier<List<T>> entitiesSupplier,
            Supplier<RinaJpaRepo<T, Long>> repoSupplier) {
        List<T> entities = entitiesSupplier.get();
        if (CollectionUtils.isNotEmpty(entities)) {
            RinaJpaRepo<T, Long> rinaJpaRepo = repoSupplier.get();
            rinaJpaRepo.saveAll(entities);
            rinaJpaRepo.flush();
        }
    }

    public static IamUser getIamUser(final Supplier<String> userNameSupplier, final Supplier<IamUserRepoExtended> iamUserRepoSupplier) {
        final String userName = userNameSupplier.get();
        final IamUserRepoExtended iamUserRepo = iamUserRepoSupplier.get();

        IamUser iamUser = iamUserRepo.findByUsernameAndIsDeletedIsFalse(userName);

        if (iamUser == null) {
            List<IamUser> deletedUsers = iamUserRepo.findByUsernameAndIsDeletedIsTrue(userName);

            if (org.apache.commons.collections.CollectionUtils.isEmpty(deletedUsers)) {
                throw new EntityNotFoundEessiRuntimeException(IamUser.class, UniqueIdentifier.username, userName);
            }

            return deletedUsers.get(0);
        }

        return iamUser;
    }
}
