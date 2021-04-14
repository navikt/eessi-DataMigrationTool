package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.applicationProfile;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieListener;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscriber;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscription;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToNieSubscriptionMapper extends AbstractMapToEntityMapper<MapHolder, NieSubscription> {

    @Override
    public void mapAtoB(final MapHolder a, final NieSubscription b, final MappingContext context) {
        b.setIsCase(a.bool(NieFields.CASE, Boolean.FALSE) || a.bool(NieFields.IS_CASE, Boolean.FALSE));
        b.setId(a.string(NieFields.ID));
        b.setSubscriptionName(a.string(NieFields.SUBSCRIPTION_NAME));

        mapChildren(a.listToMapHolder(NieFields.SUBSCRIBERS), NieSubscriber.class, b::addNieSubscriber);
        mapChildren(a.listToMapHolder(NieFields.NOTIFICATIONS), NieListener.class, b::addNieListener);
        mapChildren(a.listToMapHolder(NieFields.LISTENERS), NieListener.class, b::addNieListener);
    }

    private <T extends Persistable> void mapChildren(
            final List<MapHolder> children,
            final Class<T> childClass,
            final Consumer<T> childConsumer) {

        if (CollectionUtils.isNotEmpty(children)) {
            children.stream()
                    .map(child -> mapperFacade.map(child, childClass))
                    .forEach(childConsumer);
        }
    }

}
