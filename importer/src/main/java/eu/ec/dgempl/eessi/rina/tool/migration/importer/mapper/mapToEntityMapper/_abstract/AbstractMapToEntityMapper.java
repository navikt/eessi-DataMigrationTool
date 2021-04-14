package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.AbstractLocalMapper;

@Transactional(propagation = Propagation.MANDATORY)
public abstract class AbstractMapToEntityMapper<A extends MapHolder, B extends Persistable> extends AbstractLocalMapper<A, B>
        implements MapToEntityMapper<A, B> {

}
