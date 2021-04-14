package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Signature;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseSignatureFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSignatureMapper extends AbstractMapToEntityMapper<MapHolder, Signature> {

    private final DocumentRepo documentRepo;

    @Autowired
    private RinaJsonMapper rinaJsonMapper;

    public MapToSignatureMapper(final DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Signature b, final MappingContext context) {
        b.setId(a.string(CaseSignatureFields.MESSAGE_ID));

        mapUserMessage(a, b);
        mapSedSignature(a, b);
    }

    private void mapSedSignature(final MapHolder a, final Signature b) {
        try {
            Map<String, Object> sedSignature = a.getMap(CaseSignatureFields.SED_SIGNATURE);
            b.setSedSignature(rinaJsonMapper.mapToJson(sedSignature));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not extract sedSignature", e);
        }
    }

    private void mapUserMessage(final MapHolder a, final Signature b) {
        String messageId = a.string(CaseSignatureFields.MESSAGE_ID);
        String targetSedId = a.string(CaseSignatureFields.TARGET_SED_ID);
        String caseId = a.string(CaseSignatureFields.CASE_ID);

        Document document = documentRepo.findByIdAndRinaCaseId(targetSedId, caseId);
        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(
                    Document.class,
                    UniqueIdentifier.id, targetSedId,
                    UniqueIdentifier.caseId, caseId);
        }

        UserMessage userMessage = document.getDocumentConversations().stream()
                .map(DocumentConversation::getUserMessages)
                .flatMap(List::stream)
                .filter(message -> message.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundEessiRuntimeException(UserMessage.class, UniqueIdentifier.id, messageId));

        b.setUserMessage(userMessage);
    }
}
