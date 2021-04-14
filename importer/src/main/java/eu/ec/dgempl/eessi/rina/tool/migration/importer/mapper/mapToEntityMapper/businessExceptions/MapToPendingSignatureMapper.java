package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.businessExceptions;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserMessageDirection;
import eu.ec.dgempl.eessi.rina.model.jackson.JsonMapperQualifiers;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingSignature;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.PendingSignatureFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToPendingSignatureMapper extends AbstractMapToEntityMapper<MapHolder, PendingSignature> {

    @Autowired
    @Qualifier(JsonMapperQualifiers.RINA_JSON_MAPPER_SERVICES)
    private RinaJsonMapper rinaJsonMapper;

    @Override
    public void mapAtoB(final MapHolder a, final PendingSignature b, final MappingContext context) {

        b.setId(a.string(PendingSignatureFields.MESSAGE_ID));

        // pending signature are only for IN direction
        b.setDirection(EUserMessageDirection.IN);

        b.setTargetSedId(a.string(PendingSignatureFields.TARGET_SED_ID));

        try {
            Map<String, Object> sedSignature = a.getMap(PendingSignatureFields.SED_SIGNATURE);
            b.setSedSignature(rinaJsonMapper.mapToJson(sedSignature));
        } catch (Exception e) {
            throw new RuntimeException("Could not extract sedSignature", e);
        }

        b.setLastUpdate(ZonedDateTime.now(ZoneId.systemDefault()));

    }
}
