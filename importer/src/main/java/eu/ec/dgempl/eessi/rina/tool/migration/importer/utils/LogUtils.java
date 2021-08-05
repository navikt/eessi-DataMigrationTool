package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

public class LogUtils {

    public static void printImportProcess(
            final AtomicLong docsCount,
            final AtomicInteger globalCounter,
            final AtomicLong processedDocs,
            final Instant batchStart,
            final Instant startInclusive,
            final Logger logger) {
        float processTime = Duration.between(batchStart, Instant.now()).toMillis() / 1000F;
        float batchTime = Duration.between(startInclusive, Instant.now()).toMillis() / 1000F;
        String message = "Iteration " + globalCounter.incrementAndGet() +
                ". Total documents: " + docsCount.get() +
                ". Pending: " + (docsCount.get() - processedDocs.get()) +
                ". Total time: " + processTime +
                ". Batch time: " + batchTime;
        System.out.println(message);
        logger.info(message);
    }
}
