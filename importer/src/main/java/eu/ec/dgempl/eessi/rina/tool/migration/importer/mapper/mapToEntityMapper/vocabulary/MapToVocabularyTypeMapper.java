package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.vocabulary;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.VocabularyFields.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EVocabularyType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.VocabularyType;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToVocabularyTypeMapper extends AbstractMapToEntityMapper<MapHolder, VocabularyType> {

    @Override
    public void mapAtoB(final MapHolder a, final VocabularyType b, final MappingContext context) {
        String vocabularyType = a.string(VOCABULARY);
        EVocabularyType eVocabularyType = EVocabularyType.lookupIgnoreCase(vocabularyType);

        if (eVocabularyType == null) {
            throw new EntityNotFoundEessiRuntimeException(VocabularyType.class, UniqueIdentifier.type, vocabularyType);
        }

        b.setType(eVocabularyType);
    }
}
