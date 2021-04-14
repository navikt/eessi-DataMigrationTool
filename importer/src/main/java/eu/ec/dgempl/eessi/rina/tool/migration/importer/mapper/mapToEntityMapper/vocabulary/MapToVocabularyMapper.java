package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.vocabulary;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.VocabularyFields.*;

import java.util.Map;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EVocabularyType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Vocabulary;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.VocabularyType;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.VocabularyTypeRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.VocabularyFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToVocabularyMapper extends AbstractMapToEntityMapper<MapHolder, Vocabulary> {

    private final VocabularyTypeRepo vocabularyTypeRepo;

    public MapToVocabularyMapper(final VocabularyTypeRepo vocabularyTypeRepo) {
        this.vocabularyTypeRepo = vocabularyTypeRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Vocabulary b, final MappingContext context) {
        String vocabularyCode = a.string(VocabularyFields.VOCABULARY);
        EVocabularyType eVocabularyType = EVocabularyType.lookupIgnoreCase(vocabularyCode);

        VocabularyType vocabularyType = vocabularyTypeRepo.findByType(eVocabularyType);

        if (vocabularyType == null) {
            throw new EntityNotFoundEessiRuntimeException(VocabularyType.class, UniqueIdentifier.type, vocabularyCode);
        }

        b.setVocabularyType(vocabularyType);
        b.setCode(a.string(CODE));

        mapValue(a, b);
    }

    @SuppressWarnings("unchecked")
    private void mapValue(final MapHolder a, final Vocabulary b) {
        Object value = a.get(VALUE, false);

        if (value instanceof String) {
            b.setName((String) value);
        }

        if (value instanceof Map) {
            MapHolder valueHolder = new MapHolder((Map<String, Object>) value, a.getVisitedFields(), VALUE);
            b.setName(valueHolder.string(NAME));
            b.setAltColor(valueHolder.string(ALT_COLOR));
            b.setColor(valueHolder.string(COLOR));
            b.setDescription(valueHolder.string(DESCRIPTION));
            b.setIcon(valueHolder.string(ICON));
            b.setSeverity(valueHolder.string(SEVERITY));
            b.setTemplate(valueHolder.string(TEMPLATE));
        }
    }
}
