package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.dtoToEntityMapper._abstract;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.AbstractLocalMapper;

@Transactional(propagation = Propagation.MANDATORY)
public class AbstractDtoToEntityMapper<A, B extends PersistableWithSid> extends AbstractLocalMapper<A, B>
        implements DtoToEntityMapper<A, B> {
}
