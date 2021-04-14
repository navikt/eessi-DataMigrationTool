package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

@Service
public class DatabaseCleanupService {

    private final Logger logger = LoggerFactory.getLogger(DatabaseCleanupService.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;
    private final String scriptsPath;

    private final List<String> DELETE_CASE_RESOURCES_STATEMENTS;

    public DatabaseCleanupService(
            final DataSource dataSource,
            final JdbcTemplate jdbcTemplate,
            final PlatformTransactionManager transactionManager,
            final @Value("${sql.scripts.path}") String scriptsPath) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionManager = transactionManager;
        this.scriptsPath = scriptsPath;

        DELETE_CASE_RESOURCES_STATEMENTS = new ArrayList<>();

        try {
            DELETE_CASE_RESOURCES_STATEMENTS.addAll(
                    Files.readAllLines(Path.of(scriptsPath, "pre-migrate", "cleanupCaseResources.sql"), Charset.defaultCharset()));
        } catch (IOException e) {
            logger.error("Could not load cleanupCaseResources script", e);
            throw new RuntimeException("Could not load cleanupCaseResources script", e);
        }
    }

    public void cleanupTables() throws Exception {
        ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {

            logger.info("Cleaning up database!");

            jdbcTemplate.execute("SET search_path TO rina;");

            final ResourceLoader resourceLoader = new DefaultResourceLoader();
            final ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
            rdp.setSqlScriptEncoding(StandardCharsets.UTF_8.name());

            rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/pre-migrate/cleanup.sql"));
            DatabasePopulatorUtils.execute(rdp, dataSource);

            jdbcTemplate.execute("SET search_path TO rina;");
        });
    }

    public void cleanupPostCaseResources() throws Exception {
        ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {

            logger.info("Cleaning up database!");

            jdbcTemplate.execute("SET search_path TO rina;");

            final ResourceLoader resourceLoader = new DefaultResourceLoader();
            final ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
            rdp.setSqlScriptEncoding(StandardCharsets.UTF_8.name());

            rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/pre-migrate/cleanupPostCaseResources.sql"));
            DatabasePopulatorUtils.execute(rdp, dataSource);

            jdbcTemplate.execute("SET search_path TO rina;");
        });
    }

    public void cleanupCaseResources(String caseId) throws Exception {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        List<PreparedStatement> deleteStatements = null;

        if (CollectionUtils.isNotEmpty(DELETE_CASE_RESOURCES_STATEMENTS)) {
            deleteStatements = new ArrayList<>();

            for (String statement : DELETE_CASE_RESOURCES_STATEMENTS) {
                statement = statement.replace(":caseId", "?");
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setString(1, caseId);

                deleteStatements.add(preparedStatement);
            }
        }

        if (CollectionUtils.isNotEmpty(deleteStatements)) {
            try {
                for (PreparedStatement deleteStatement : deleteStatements) {
                    deleteStatement.execute();
                }
                connection.commit();
                connection.setAutoCommit(true);
            } catch (Exception e) {
                connection.rollback();
            } finally {
                for (PreparedStatement deleteStatement : deleteStatements) {
                    deleteStatement.close();
                }

                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }
}
