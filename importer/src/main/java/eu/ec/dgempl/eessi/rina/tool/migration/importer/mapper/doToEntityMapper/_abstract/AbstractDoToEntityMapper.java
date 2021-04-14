package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.doToEntityMapper._abstract;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.buc.api.model.DataObject;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.AbstractLocalMapper;

@Transactional(propagation = Propagation.MANDATORY)
public abstract class AbstractDoToEntityMapper<A extends DataObject, B extends PersistableWithSid> extends AbstractLocalMapper<A, B>
        implements DoToEntityMapper<A, B> {
}
