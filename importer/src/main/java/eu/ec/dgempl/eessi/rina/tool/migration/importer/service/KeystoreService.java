package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.commons.keystore.KeyStoreOperationException;
import eu.ec.dgempl.eessi.rina.commons.keystore.KeyStoreOperations;
import eu.ec.dgempl.eessi.rina.commons.keystore.KeystoresPathResolver;
import eu.ec.dgempl.eessi.rina.commons.string.PasswordUtil;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.keys.KeystoreEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.BusinessKey;
import eu.ec.dgempl.eessi.rina.repo.BusinessKeyRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.GlobalParamHelper;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class KeystoreService {

    @Autowired
    private BusinessKeyRepo businessKeyRepo;

    @Autowired
    private GlobalParamHelper globalParamHelper;

    public void syncJksBusinessKeysWithDb() {

        String _businessKeystorePassword = globalParamHelper.getGlobalParam(
                EGlobalParam.MESSAGING_SECURITY_BUSINESS_PRIVATE_KEYSTORE_PASSWORD);
        if (_businessKeystorePassword == null) {
            throw new RuntimeException("Could not find the password for the business keystore.");
        }

        String businessKeystorePassword = PasswordUtil.decodePassword(_businessKeystorePassword);

        String msPath = globalParamHelper.getGlobalParam(EGlobalParam.MESSAGING_MSH_PATH);

        List<String> businessKeyAliases = businessKeyRepo.findAllAliases();

        List<String> jksKeys = listBusinessKeystoreAliases(msPath, businessKeystorePassword);

        for (String businessKeyAlias : businessKeyAliases) {
            if (!jksKeys.contains(businessKeyAlias)) {
                businessKeyRepo.deleteByAlias(businessKeyAlias);
            }
        }

        // add new keys from JSK file in DB
        for (String thisKey : jksKeys) {
            if (!businessKeyAliases.contains(thisKey)) {
                final BusinessKey businessKey = new BusinessKey(thisKey, "");
                businessKeyRepo.saveAndFlush(businessKey);
            }
        }

    }

    /**
     * Returns a list of item aliases from the business keystore
     *
     * @param mshPath  Location directory of the keystores
     * @param password Keystore password
     * @return List of alias names
     */
    private List<String> listBusinessKeystoreAliases(final String mshPath, final String password) {
        try {
            return KeyStoreOperations.listAliases(KeystoresPathResolver.getBusinessKeystorePath(mshPath), password);
        } catch (KeyStoreOperationException e) {
            throw new KeystoreEessiRuntimeException(e);
        }
    }

}
