package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class SequenceUpdateService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Value("${sql.scripts.path}")
    private String scriptsPath;

    public SequenceUpdateService(final DataSource dataSource, final JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateSequences() {

        jdbcTemplate.execute("SET search_path TO rina;");

        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.setSqlScriptEncoding(StandardCharsets.UTF_8.name());

        rdp.addScript(resourceLoader.getResource("file:" + scriptsPath + "/post-merge/updateSequences.sql"));

        DatabasePopulatorUtils.execute(rdp, dataSource);

        jdbcTemplate.execute("SET search_path TO rina;");
    }
}
