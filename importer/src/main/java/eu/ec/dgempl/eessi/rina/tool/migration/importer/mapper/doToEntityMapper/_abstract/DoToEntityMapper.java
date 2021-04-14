package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.doToEntityMapper._abstract;

import eu.ec.dgempl.eessi.rina.buc.api.model.DataObject;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.LocalMapper;

public interface DoToEntityMapper<A extends DataObject, B extends PersistableWithSid> extends LocalMapper<A, B> {
}
