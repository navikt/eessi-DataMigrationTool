package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.NieSubscriptionDto;

public class SubscriptionsHolder {

    public static final String NOTIFICATION_KEY = "Notification";

    private final Map<String, NieListener> listenersConfiguration = new HashMap<>();
    private final Map<String, NieSubscriber> subscribersConfiguration = new HashMap<>();

    private final Map<String, String> subscriptionNames = new HashMap<>();

    private final Map<String, NieSubscriptionDto> subscriptions = new HashMap<>();

    private MapHolder subscription;

    private MapHolder subscriber;

    private static class NieListener {

        String subscriptionId;

        MapHolder listener;

        public NieListener(String subscriptionId, MapHolder listener) {
            this.subscriptionId = subscriptionId;
            this.listener = listener;
        }
    }
    private static class NieSubscriber {

        String subscriptionId;

        MapHolder subscriber;

        public NieSubscriber(String subscriptionId, MapHolder subscriber) {
            this.subscriptionId = subscriptionId;
            this.subscriber = subscriber;
        }
    }

    public void setSubscription(MapHolder subscription) {

        this.subscription = subscription;
        if (subscription == null) {
            return;
        }

        String subscriptionId = subscription.string(ID);
        String name = subscription.string(SUBSCRIPTION_NAME);

        while (subscriptionNames.containsKey(name)) {
            if (subscriptionNames.get(name).equals(subscriptionId)) {
                break;
            }
            name += "_";
        }

        subscription.put(SUBSCRIPTION_NAME, name);
        subscriptionNames.put(name, subscription.string(ID));

        if (!subscriptions.containsKey(name)) {
            subscriptions.put(subscriptionId,
                    new NieSubscriptionDto(subscriptionId, name, subscription.bool(IS_CASE)));
        }
    }

    public void setSubscriber(MapHolder subscriber) {

        this.subscriber = subscriber;

        if (subscription != null && subscriber != null) {

            String subscriptionId = subscription.string(ID);
            String subscriberKey = getSubscriberKey();
            List<MapHolder> listenersToBeAdded = new ArrayList<>();

            if (subscribersConfiguration.containsKey(subscriberKey)) {
                NieSubscriber nieSubscriber = subscribersConfiguration.get(subscriberKey);
                if (!nieSubscriber.subscriptionId.equals(subscriptionId)) {

                    NieSubscriptionDto nieSubscriptionDto = subscriptions.get(nieSubscriber.subscriptionId);
                    if (nieSubscriptionDto != null) {
                        Map<String, MapHolder> listeners = nieSubscriptionDto.getListeners();
                        listenersToBeAdded.addAll(listeners.values());
                        nieSubscriptionDto.removeSubscriber(subscriberKey);
                    }
                }
            }

            NieSubscriptionDto nieSubscriptionDto = subscriptions.get(subscriptionId);
            nieSubscriptionDto.addSubscriber(subscriberKey, subscriber);
            listenersToBeAdded.forEach(nieSubscriptionDto::addListener);

            subscribersConfiguration.put(subscriberKey, new NieSubscriber(subscriptionId, subscriber));
        }
    }

    private String getSubscriberKey() {

        if (subscription != null && subscriber != null) {
            String id = subscriber.string(ID);
            String version = subscriber.string(VERSION);
            if (subscription.bool(IS_CASE, Boolean.FALSE) || subscription.bool(CASE, Boolean.FALSE)) {
                return id + "_" + version;
            } else {
                return id;
            }
        }

        return null;
    }

    public void addListener(MapHolder listener) {

        String subscriberKey = getSubscriberKey();
        if (subscriberKey == null) {
            return;
        }

        processListener(listener.string(ID) + "_" + subscriberKey, listener);
    }

    public void addNotificationListener(MapHolder notification) {

        processListener(NOTIFICATION_KEY, notification);
    }

    private void processListener(String listenerKey, MapHolder listener) {
        if (subscription == null) {
            return;
        }

        String subscriptionId = subscription.string(ID);

        if (listenersConfiguration.containsKey(listenerKey)) {
            NieListener nieListener = listenersConfiguration.get(listenerKey);
            if (!nieListener.subscriptionId.equals(subscriptionId)) {

                NieSubscriptionDto nieSubscriptionDto = subscriptions.get(nieListener.subscriptionId);
                if (nieSubscriptionDto != null) {
                    nieSubscriptionDto.removeListener(nieListener.listener.string(ID));
                }
            }
        }

        NieSubscriptionDto nieSubscriptionDto = subscriptions.get(subscriptionId);
        nieSubscriptionDto.addListener(listener);

        listenersConfiguration.put(listenerKey, new NieListener(subscriptionId, listener));
    }

    public List<NieSubscriptionDto> getSubscriptions() {

        return subscriptions.values().stream()
                .filter((subscription) -> {
                    Map<String, MapHolder> subscribers = subscription.getSubscribers();
                    Map<String, MapHolder> listeners = subscription.getListeners();

                    return (!subscribers.isEmpty() && !listeners.isEmpty()) ||
                            (subscribers.isEmpty() && !listeners.isEmpty() && listeners.containsKey(NOTIFICATION_KEY));
                })
                .collect(Collectors.toList());
    }
}
