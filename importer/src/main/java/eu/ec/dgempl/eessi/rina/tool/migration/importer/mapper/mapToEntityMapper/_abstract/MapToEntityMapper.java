package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract;

import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.LocalMapper;

public interface MapToEntityMapper<A extends MapHolder, B extends Persistable> extends LocalMapper<A, B> {

}
