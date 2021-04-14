package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscriber;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDefVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefVersionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToNieSubscriberMapper extends AbstractMapToEntityMapper<MapHolder, NieSubscriber> {

    private final ProcessDefVersionRepo processDefVersionRepo;
    private final DocumentTypeRepo documentTypeRepo;

    public MapToNieSubscriberMapper(final ProcessDefVersionRepo processDefVersionRepo,
            final DocumentTypeRepo documentTypeRepo) {
        this.processDefVersionRepo = processDefVersionRepo;
        this.documentTypeRepo = documentTypeRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final NieSubscriber b, final MappingContext context) {
        String type = a.string(NieFields.ID);
        String version = a.string(NieFields.VERSION);

        ProcessDefVersion processDefVersion = processDefVersionRepo.findByProcessDefIdAndBusinessVersion(type, version);
        DocumentType documentType = documentTypeRepo.findByType(type);

        if (processDefVersion == null && documentType == null) {
            throw new EntityNotFoundEessiRuntimeException(
                    ProcessDefVersion.class,
                    UniqueIdentifier.type, type,
                    UniqueIdentifier.version, version);
        }

        b.setId(type);
        b.setProcessDefVersion(processDefVersion);
        b.setDocumentType(documentType);
    }
}
