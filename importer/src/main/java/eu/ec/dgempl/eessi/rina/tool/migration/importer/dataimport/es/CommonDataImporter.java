package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.JdbcUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

@Component
@ElasticTypeImporter(type = EElasticType.NONE)
public class CommonDataImporter extends AbstractDataImporter {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Value("${sql.scripts.path}")
    private String scriptsPath;

    public CommonDataImporter(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void importData() {
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {
                this.cleanupCommonData();
                this.populateCommonData();
            });
        } catch (Exception e) {
            dataReporter.reportProblem(this.inferElasticType(), "common_data", e.getMessage(), e);
        }
    }

    private void cleanupCommonData() {
        JdbcUtils.deleteFromTables(jdbcTemplate, "supported_language");
        JdbcUtils.deleteFromTables(jdbcTemplate, "transposition");
        JdbcUtils.deleteFromTables(jdbcTemplate, "admin_notification_type");

        JdbcUtils.deleteFromTables(jdbcTemplate, "vocabulary");
        JdbcUtils.deleteFromTables(jdbcTemplate, "vocabulary_type");

        JdbcUtils.deleteFromTables(jdbcTemplate, "document_type_version");
        JdbcUtils.deleteFromTables(jdbcTemplate, "document_type");

        JdbcUtils.deleteFromTables(jdbcTemplate, "process_def_version");
        JdbcUtils.deleteFromTables(jdbcTemplate, "process_def");

        JdbcUtils.deleteFromTables(jdbcTemplate, "sector");
        JdbcUtils.deleteFromTables(jdbcTemplate, "role");
        JdbcUtils.deleteFromTables(jdbcTemplate, "iam_user");
        JdbcUtils.deleteFromTables(jdbcTemplate, "iam_origin");
    }

    private void populateCommonData() {

        jdbcTemplate.execute("SET search_path TO rina;");

        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.setSqlScriptEncoding(StandardCharsets.UTF_8.name());

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/sector.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/iam_origin.sql"));
        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/iam_system_user.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/role.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/process_definition.sql"));
        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/process_definition_version.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/vocabulary_type.sql"));
        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/vocabulary.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/document_type.sql"));
        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/document_type_version.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/transposition.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/admin_notification_type.sql"));

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/supported_language.sql"));

        DatabasePopulatorUtils.execute(rdp, dataSource);

        jdbcTemplate.execute("SET search_path TO rina;");

    }

    @Override
    protected String getId(MapHolder doc) {
        return "common_data";
    }
}
