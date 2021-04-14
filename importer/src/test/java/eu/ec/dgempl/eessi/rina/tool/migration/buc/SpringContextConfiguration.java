package eu.ec.dgempl.eessi.rina.tool.migration.buc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.jpa.listener.LogListener;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.auditor.AuditorAwareImpl;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.BonitaProcessDefMappingsService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.BonitaProcessDefMappingsServiceTest;

// @formatter:off
@Configuration
@ComponentScan(
        basePackages = {
                "eu.ec.dgempl.eessi.rina.tool.migration.buc",
                "eu.ec.dgempl.eessi.rina.tool.migration.importer",
                "eu.ec.dgempl.eessi.rina.repo",
                "eu.ec.dgempl.eessi.rina.tool.migration.common.service"
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.service\\.DatabaseCleanupService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.service\\.BonitaProcessDefMappingsService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.service\\.FieldMappingService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.service\\.AssignmentPolicyService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.service\\.SequenceUpdateService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.common\\.service\\.EsClientService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.dataimport.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu\\.ec\\.dgempl\\.eessi\\.rina\\.tool\\.migration\\.importer\\.dataprocessor.*")
        }
        )
// @formatter:on
public class SpringContextConfiguration {

    @Bean
    public ActionService actionService() {
        return new ActionService();
    }

    @Bean
    public BucDefinitionImporterFactory bucDefinitionImporterFactory() throws JAXBException, URISyntaxException {

        // get the BUCS dir
        File file = new File(getClass().getClassLoader().getResource("bucs").toURI());
        String bucsDir = file.getAbsolutePath();

        return new BucDefinitionImporterFactory(bucsDir);
    }

    @Bean
    public AuditorAwareImpl auditorAware() {
        return new AuditorAwareImpl();
    }

    @Bean
    public LogListener logListener() {
        return new LogListener();
    }

    @Bean
    public BonitaProcessDefMappingsService assignmentPolicyService() throws IOException {

        String path = BonitaProcessDefMappingsServiceTest.class.getClassLoader().getResource("BonitaProcessDefinitionActors.json").getPath();
        return new BonitaProcessDefMappingsService(new RinaJsonMapper(new ObjectMapper()), path);

    }

}
