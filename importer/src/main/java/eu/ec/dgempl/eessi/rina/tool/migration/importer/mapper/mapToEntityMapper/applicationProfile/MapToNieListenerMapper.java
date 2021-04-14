package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieListener;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToNieListenerMapper extends AbstractMapToEntityMapper<MapHolder, NieListener> {

    @Override
    public void mapAtoB(final MapHolder a, final NieListener b, final MappingContext context) {
        b.setId(a.string(NieFields.ID));
        b.setLabel(a.string(NieFields.LABEL));

        mapUrl(a, b);
    }

    private void mapUrl(final MapHolder a, final NieListener b) {
        String listener = a.string(NieFields.LISTENER);
        String url = a.string(NieFields.URL);

        String listenerUrl = "";
        if (StringUtils.isNotBlank(listener)) {
            listenerUrl = listener;
        }
        if (StringUtils.isNotBlank(url)) {
            listenerUrl = url;
        }

        b.setUrl(listenerUrl);
    }
}
