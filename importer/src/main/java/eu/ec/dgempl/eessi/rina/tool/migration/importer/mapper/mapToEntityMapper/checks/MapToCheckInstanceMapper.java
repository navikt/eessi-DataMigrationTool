package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.checks;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CheckInstanceFields.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckBucket;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckDefinition;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckInstance;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.CheckBucketRepo;
import eu.ec.dgempl.eessi.rina.repo.CheckDefinitionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToCheckInstanceMapper extends AbstractMapToEntityMapper<MapHolder, CheckInstance> {

    private final CheckBucketRepo checkBucketRepo;
    private final CheckDefinitionRepo checkDefinitionRepo;

    public MapToCheckInstanceMapper(
            final CheckBucketRepo checkBucketRepo,
            final CheckDefinitionRepo checkDefinitionRepo) {
        this.checkBucketRepo = checkBucketRepo;
        this.checkDefinitionRepo = checkDefinitionRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final CheckInstance b, final MappingContext context) {
        b.setId(a.string(ID));
        b.setName(a.string(NAME));
        b.setMessage(a.string(MESSAGE));
        b.setStatus(a.string(STATUS));

        String checkDefinitionId = a.string(CHECK_DEFINITION_ID);
        CheckDefinition checkDefinition = checkDefinitionRepo.findById(checkDefinitionId);
        if (checkDefinition == null) {
            throw new EntityNotFoundEessiRuntimeException(CheckDefinition.class, UniqueIdentifier.id, checkDefinitionId);
        }
        b.setCheckDefinition(checkDefinition);

        mapDate(a, START_DATE, b::setStartDate);
        mapDate(a, END_DATE, b::setEndDate);

        String parentCheckBucketId = a.string(PARENT_CHECK_BUCKET_ID);
        CheckBucket checkBucket = checkBucketRepo.findById(parentCheckBucketId);
        if (checkBucket == null) {
            throw new EntityNotFoundEessiRuntimeException(CheckBucket.class, UniqueIdentifier.id, parentCheckBucketId);
        }
        b.setParentCheckBucket(checkBucket);
    }
}