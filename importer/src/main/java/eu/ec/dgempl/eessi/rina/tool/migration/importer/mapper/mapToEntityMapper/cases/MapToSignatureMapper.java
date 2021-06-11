package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Signature;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseSignatureFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSignatureMapper extends AbstractMapToEntityMapper<MapHolder, Signature> {

    @Autowired
    private RinaJsonMapper rinaJsonMapper;

    @Override
    public void mapAtoB(final MapHolder a, final Signature b, final MappingContext context) {
        b.setId(a.string(CaseSignatureFields.MESSAGE_ID));

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
}
