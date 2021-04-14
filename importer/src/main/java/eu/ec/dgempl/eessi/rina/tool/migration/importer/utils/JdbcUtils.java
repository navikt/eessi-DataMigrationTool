package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUtils {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

    public static int deleteFromTables(JdbcTemplate jdbcTemplate, String... tableNames) {
        int totalRowCount = 0;

        for (String tableName : tableNames) {
            int rowCount = jdbcTemplate.update("DELETE FROM " + tableName);
            totalRowCount += rowCount;
            if (logger.isInfoEnabled()) {
                logger.info("Deleted " + rowCount + " rows from table " + tableName);
            }
        }

        return totalRowCount;
    }

}
