package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EVocabularyType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Vocabulary;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.VocabularyType;
import eu.ec.dgempl.eessi.rina.repo.VocabulariesRepo;
import eu.ec.dgempl.eessi.rina.repo.VocabularyTypeRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.VocabularyFields;

@Component
@ElasticTypeImporter(type = EElasticType.VOCABULARIES_CONCEPT)
public class VocabulariesConceptImporter extends AbstractDataImporter {

    private final VocabulariesRepo vocabulariesRepo;
    private final VocabularyTypeRepo vocabularyTypeRepo;

    private static final Logger logger = LoggerFactory.getLogger(VocabulariesConceptImporter.class);

    public VocabulariesConceptImporter(final VocabulariesRepo vocabulariesRepo, final VocabularyTypeRepo vocabularyTypeRepo) {
        this.vocabulariesRepo = vocabulariesRepo;
        this.vocabularyTypeRepo = vocabularyTypeRepo;
    }

    @Override
    public void importData() {
        logger.info("Started importing documents [index={}, type={}]", EElasticType.VOCABULARIES_CONCEPT.getIndex(),
                EElasticType.VOCABULARIES_CONCEPT.getType());
        run(this::processVocabulariesData);
    }

    private void processVocabulariesData(final MapHolder doc) {
        String vocabularyTypeString = doc.string(VocabularyFields.VOCABULARY);
        EVocabularyType eVocabularyType = EVocabularyType.lookupIgnoreCase(vocabularyTypeString);
        VocabularyType vocabularyType = vocabularyTypeRepo.findByType(eVocabularyType);

        if (vocabularyType == null) {
            vocabularyType = beanMapper.map(doc, VocabularyType.class);
        } else {
            beanMapper.map(doc, vocabularyType);
        }

        vocabularyTypeRepo.saveAndFlush(vocabularyType);

        String vocabularyCode = doc.string(VocabularyFields.CODE);
        Vocabulary vocabulary = vocabulariesRepo.findByVocabularyTypeAndCode(vocabularyType, vocabularyCode);

        if (vocabulary == null) {
            vocabulary = beanMapper.map(doc, Vocabulary.class);
        } else {
            beanMapper.map(doc, vocabulary);
        }

        vocabulariesRepo.saveAndFlush(vocabulary);
    }
}
