package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.nie;

import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.NieSubscriptionDto;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.SubscriptionsHolder;

public class NieSubscriptionUtils {

    public static List<NieSubscriptionDto> getNieSubscriptions(List<MapHolder> subscriptionsMapHolders) {
        final String SUBSCRIBERS = "subscribers";
        final String LISTENERS = "listeners";
        final String NOTIFICATIONS = "notifications";

        if (CollectionUtils.isEmpty(subscriptionsMapHolders)) {
            return Collections.emptyList();
        }

        SubscriptionsHolder subscriptionsHolder = new SubscriptionsHolder();

        subscriptionsMapHolders.forEach((subscriptionHolder) -> {

            subscriptionsHolder.setSubscription(subscriptionHolder);

            List<MapHolder> subscribers = subscriptionHolder.listToMapHolder(SUBSCRIBERS);
            if (!subscribers.isEmpty()) {

                List<MapHolder> listeners = subscriptionHolder.listToMapHolder(LISTENERS);

                subscribers.forEach((subscriber) -> {
                    subscriptionsHolder.setSubscriber(subscriber);
                    listeners.forEach(subscriptionsHolder::addListener);
                });
            } else {
                subscriptionsHolder.setSubscriber(null);
                List<MapHolder> notifications = subscriptionHolder.listToMapHolder(NOTIFICATIONS);
                notifications.forEach(subscriptionsHolder::addNotificationListener);
            }

        });

        return subscriptionsHolder.getSubscriptions();
    }
}