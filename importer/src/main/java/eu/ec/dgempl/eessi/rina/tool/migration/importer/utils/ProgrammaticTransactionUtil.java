package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.ec.dgempl.eessi.rina.commons.test.TimeBasedUUIDGenerator;

public class ProgrammaticTransactionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticTransactionUtil.class);

    @FunctionalInterface
    public interface Procedure0 {
        void accept() throws Exception;
    }

    /**
     * @param transactionManager
     * @param proc
     */
    public static void processSuccessfulTransaction(final PlatformTransactionManager transactionManager, final Procedure0 proc)
            throws Exception {
        processSuccessfulTransaction(transactionManager, TransactionDefinition.PROPAGATION_REQUIRES_NEW, proc);
    }

    /**
     * @param transactionManager
     * @param propagationBehavior
     * @param proc
     */
    public static void processSuccessfulTransaction(final PlatformTransactionManager transactionManager, final int propagationBehavior,
            final Procedure0 proc) throws Exception {
        processSuccessfulTransaction(transactionManager, propagationBehavior, TimeBasedUUIDGenerator.generateId().toString(), proc);
    }

    /**
     * @param transactionManager
     * @param propagationBehavior
     * @param txName
     * @param proc
     */
    public static void processSuccessfulTransaction(final PlatformTransactionManager transactionManager, final int propagationBehavior,
            final String txName, final Procedure0 proc) throws Exception {

        final DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setName(txName);
        txDef.setPropagationBehavior(propagationBehavior);
        final TransactionStatus status = transactionManager.getTransaction(txDef);

        try {
            logger.debug("Transaction [{}] started!", txName);

            proc.accept();

            transactionManager.commit(status);
            logger.debug("Transaction [{}] succeeded!", txName);

        } catch (Exception ex) {

            if (!status.isCompleted())
                transactionManager.rollback(status);
            throw ex;
        }

    }

}
