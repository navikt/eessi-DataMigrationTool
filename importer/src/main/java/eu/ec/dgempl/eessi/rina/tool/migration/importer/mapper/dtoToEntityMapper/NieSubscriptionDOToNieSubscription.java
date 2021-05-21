package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.dtoToEntityMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieListener;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscriber;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.NieSubscription;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefVersionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.NieSubscriptionDto;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.dtoToEntityMapper._abstract.AbstractDtoToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.SubscriptionsHolder;

import ma.glasnost.orika.MappingContext;

@Component
public class NieSubscriptionDOToNieSubscription extends AbstractDtoToEntityMapper<NieSubscriptionDto, NieSubscription> {

    private final DocumentTypeRepo documentTypeRepo;
    private final ProcessDefVersionRepo processDefVersionRepo;

    @Autowired
    public NieSubscriptionDOToNieSubscription(
            final DocumentTypeRepo documentTypeRepo,
            final ProcessDefVersionRepo processDefVersionRepo) {
        this.processDefVersionRepo = processDefVersionRepo;
        this.documentTypeRepo = documentTypeRepo;
    }

    @Override
    public void mapAtoB(NieSubscriptionDto a, NieSubscription b, MappingContext context) {

        b.setId(a.getId());
        b.setSubscriptionName(a.getName());
        b.setIsCase(a.isCase());

        a.getSubscribers().values().forEach((subscriber) -> {
            NieSubscriber nieSubscriber;

            String version = subscriber.string(NieFields.VERSION);
            String subscriberId = subscriber.string(NieFields.ID);
            if (a.isCase()) {
                nieSubscriber = new NieSubscriber(b, processDefVersionRepo.findByProcessDefIdAndBusinessVersion(subscriberId, version));
            } else {
                nieSubscriber = new NieSubscriber(b, documentTypeRepo.findByType(subscriberId));
            }
            nieSubscriber.setId(subscriberId);

            b.getNieSubscribers().add(nieSubscriber);
        });

        a.getListeners().forEach((listenerId, listener) -> {
            NieListener nieListener = new NieListener(b);

            nieListener.setId(listenerId);
            nieListener.setLabel(listener.string(NieFields.LABEL));

            String listenerListener = listener.string(NieFields.LISTENER);
            String listenerUrl = listener.string(NieFields.URL);

            if (listenerId.equals(SubscriptionsHolder.NOTIFICATION_KEY)) {
                nieListener.setUrl(listenerUrl);
            } else {
                nieListener.setUrl(listenerListener);
            }

            b.getNieListeners().add(nieListener);
        });

    }

}
