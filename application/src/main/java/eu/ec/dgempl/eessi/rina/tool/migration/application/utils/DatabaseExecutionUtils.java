package eu.ec.dgempl.eessi.rina.tool.migration.application.utils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.ec.dgempl.eessi.rina.commons.file.LocalPropertiesUtil;

public final class DatabaseExecutionUtils {

    public static void createTempTables() {
        Properties properties = getProperties();
        DataSource dataSource = dataSource(properties);

        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.setSqlScriptEncoding(StandardCharsets.UTF_8.name());

        rdp.addScript(resourceLoader.getResource(
                "file:" + properties.getProperty("sql.scripts.path") + "/pre-migrate/createTempTables.sql"));
        DatabasePopulatorUtils.execute(rdp, dataSource);
    }

    public static DataSource dataSource(Properties properties) {
        Map dbProperties = LocalPropertiesUtil.getMapFromAbsolutePath(
                Paths.get(properties.getProperty("rina.configuration.datasource"), "jpa", "db.properties").toString(),
                "spring.postgresql.rina.datasource.");

        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.databaseName", (String) dbProperties.get("databaseName"));
        props.setProperty("dataSource.user", (String) dbProperties.get("user"));
        props.setProperty("dataSource.password", (String) dbProperties.get("password"));
        props.setProperty("dataSource.serverName", (String) dbProperties.get("serverName"));
        props.setProperty("dataSource.portNumber", StringUtils.isNotBlank((CharSequence) dbProperties.get("portNumber")) ?
                (String) dbProperties.get("portNumber") :
                Integer.toString(5432));
        HikariConfig hikariConfig = new HikariConfig(props);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("createTempTablesHikariCP");
        return new HikariDataSource(hikariConfig);
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("./config/application.properties")) {
            properties.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Error reading the defaults file", e);
        }
        return properties;
    }

}
