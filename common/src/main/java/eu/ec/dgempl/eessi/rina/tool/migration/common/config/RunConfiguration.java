package eu.ec.dgempl.eessi.rina.tool.migration.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RunConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RunConfiguration.class);

    private Integer threadsNumber;

    public Integer getThreadsNumber() {
        int value = 1;
        if (threadsNumber != null
                && threadsNumber > 0) {
            value = threadsNumber;
        }
        return value;
    }

    public void setThreadsNumber(Integer threadsNumber) {
        this.threadsNumber = threadsNumber;
        logger.info("Multi-threading using " + threadsNumber + " threads");
    }
}
