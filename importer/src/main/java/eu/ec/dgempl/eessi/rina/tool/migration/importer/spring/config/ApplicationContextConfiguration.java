package eu.ec.dgempl.eessi.rina.tool.migration.importer.spring.config;

import java.util.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.commons.transformation.EessiObjectMapperFactory;
import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.dto.util.DtoModelMapperModule;
import eu.ec.dgempl.eessi.rina.repo.config.JpaConfiguration;

@Configuration
@Import(JpaConfiguration.class)
public class ApplicationContextConfiguration {

    /**
     * Creates a JSON object mapper for the services layer.
     *
     * @return
     */
    @Bean
    public ObjectMapper servicesObjectMapper() {
        Collection<Module> modules = Arrays.asList(new Module[] { new DtoModelMapperModule() });
        Map<DeserializationFeature, Boolean> deserializationFeatures = new HashMap<>();
        deserializationFeatures.put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return EessiObjectMapperFactory.createNewMapper(modules, Collections.emptyMap(), deserializationFeatures);
    }

    /**
     * Creates the object mapper wrapper with the JSON Object Mapper for services.
     *
     * @param servicesObjectMapper
     * @return
     */
    @Bean
    public RinaJsonMapper servicesRinaJsonMapper(ObjectMapper servicesObjectMapper) {
        return new RinaJsonMapper(servicesObjectMapper);
    }

}
