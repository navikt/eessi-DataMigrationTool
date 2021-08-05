package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.doToEntityMapper._abstract.DoToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.dtoToEntityMapper._abstract.DtoToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.MapToEntityMapper;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.ClassMapBuilder;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BeanMapper extends ConfigurableMapper implements ApplicationContextAware {

    private MapperFactory factory;
    private ApplicationContext applicationContext;

    public BeanMapper() {
        super(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure(final MapperFactory factory) {
        this.factory = factory;
        addAllSpringBeans(applicationContext);
    }

    /**
     * Scans the application context and registers all Mappers and Converters found in it.
     *
     * @param applicationContext
     */
    private void addAllSpringBeans(final ApplicationContext applicationContext) {
        @SuppressWarnings("rawtypes")
        Map<String, MapToEntityMapper> mapMappers = applicationContext.getBeansOfType(MapToEntityMapper.class);
        for (Mapper<?, ?> mapper : mapMappers.values()) {
            addMapper(mapper);
        }

        @SuppressWarnings("rawtypes")
        Map<String, DoToEntityMapper> doMappers = applicationContext.getBeansOfType(DoToEntityMapper.class);
        for (Mapper<?, ?> mapper : doMappers.values()) {
            addMapper(mapper);
        }

        @SuppressWarnings("rawtypes")
        Map<String, DtoToEntityMapper> dtoMappers = applicationContext.getBeansOfType(DtoToEntityMapper.class);
        for (Mapper<?, ?> mapper : dtoMappers.values()) {
            addMapper(mapper);
        }
    }

    /**
     * Constructs and registers a {@link ClassMapBuilder} into the {@link MapperFactory} using a {@link Mapper}.
     *
     * @param mapper
     */
    public <A, B> void addMapper(final Mapper<A, B> mapper) {
        factory.classMap(mapper.getAType(), mapper.getBType()).customize(mapper).register();
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        init();
    }

}
