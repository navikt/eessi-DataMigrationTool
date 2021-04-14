package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.dtoToEntityMapper._abstract;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.LocalMapper;

public interface DtoToEntityMapper<A, B extends PersistableWithSid> extends LocalMapper<A, B> {
}
