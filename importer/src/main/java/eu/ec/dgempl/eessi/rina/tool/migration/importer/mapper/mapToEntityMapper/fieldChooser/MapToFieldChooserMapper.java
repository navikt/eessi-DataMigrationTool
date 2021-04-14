package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.fieldChooser;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Field;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.FieldChooser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.FieldsChooserFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToFieldChooserMapper extends AbstractMapToEntityMapper<MapHolder, FieldChooser> {

    private final IamUserRepoExtended iamUserRepo;
    private final ProcessDefRepo processDefRepo;

    public MapToFieldChooserMapper(IamUserRepoExtended iamUserRepo, ProcessDefRepo processDefRepo) {
        this.iamUserRepo = iamUserRepo;
        this.processDefRepo = processDefRepo;
    }

    @Override
    public void mapAtoB(MapHolder a, FieldChooser b, MappingContext context) {
        String userName = a.string(FieldsChooserFields.USER_NAME);
        b.setIamUser(getIamUser(() -> userName, () -> iamUserRepo));

        String processDefId = a.string(FieldsChooserFields.PROCESS_DEF_ID);
        ProcessDef processDef = processDefRepo.findById(processDefId);

        if (processDef != null) {
            b.setProcessDef(processDef);
        } else {
            throw new EntityNotFoundEessiRuntimeException(ProcessDef.class, UniqueIdentifier.id, processDefId);
        }

        List<MapHolder> fieldChooserFields = a.listToMapHolder(FieldsChooserFields.FIELDS);

        if (!CollectionUtils.isEmpty(fieldChooserFields)) {
            List<Field> fields = fieldChooserFields.stream()
                    .map(mapHolder -> mapperFacade.map(mapHolder, Field.class))
                    .peek(field -> field.setFieldChooser(b))
                    .collect(Collectors.toList());
            b.setFields(fields);
        }
    }
}
