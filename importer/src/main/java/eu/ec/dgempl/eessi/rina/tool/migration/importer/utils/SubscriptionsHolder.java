package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.NieSubscriptionDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields.*;

public class SubscriptionsHolder {

    public static final String NOTIFICATION_KEY = "Notification";

    private final Map<String, NieListener> listenersConfiguration = new HashMap<>();

    private final Map<String, String> subscriptionNames = new HashMap<>();

    private final Map<String, NieSubscriptionDto> subscriptions = new HashMap<>();

    private Map<String, Object> subscription;

    private Map<String, String> subscriber;

    private static class NieListener {

        String subscriptionId;

        Map<String, String> listener;

        public NieListener(String subscriptionId, Map<String, String> listener) {
            this.subscriptionId = subscriptionId;
            this.listener = listener;
        }
    }

    public void setSubscription(Map<String, Object> subscription) {

        this.subscription = subscription;
        if (subscription == null) {
            return;
        }

        String subscriptionId = (String) subscription.get(ID);
        String name = (String) subscription.get(SUBSCRIPTION_NAME);

        while (subscriptionNames.containsKey(name)) {
            if (subscriptionNames.get(name).equals(subscriptionId)) {
                break;
            }
            name += "_";
        }

        subscription.put(SUBSCRIPTION_NAME, name);
        subscriptionNames.put(name, (String) subscription.get(ID));

        if (!subscriptions.containsKey(name)) {
            subscriptions.put(subscriptionId,
                    new NieSubscriptionDto(subscriptionId, name, (boolean) subscription.get(IS_CASE)));
        }
    }

    public void setSubscriber(Map<String, String> subscriber) {

        this.subscriber = subscriber;

        if (subscription != null && subscriber != null) {

            String subscriptionId = (String) subscription.get(ID);
            subscriptions.get(subscriptionId).addSubscriber(subscriber);
        }
    }

    private String getSubscriberKey() {

        if (subscription != null && subscriber != null) {
            if ((boolean) subscription.get(IS_CASE)) {
                return subscriber.get(ID) + "_" + subscriber.get(VERSION);
            } else {
                return subscriber.get(ID);
            }
        }

        return null;
    }

    public void addListener(Map<String, String> listener) {

        String subscriberKey = getSubscriberKey();
        if (subscriberKey == null) {
            return;
        }

        processListener(listener.get(ID) + "_" + subscriberKey, listener);
    }

    public void addNotificationListener(Map<String, String> notification) {

        processListener(NOTIFICATION_KEY, notification);
    }

    private void processListener(String listenerKey, Map<String, String> listener) {
        if (subscription == null) {
            return;
        }

        String subscriptionId = (String) subscription.get(ID);

        if (listenersConfiguration.containsKey(listenerKey)) {
            NieListener nieListener = listenersConfiguration.get(listenerKey);
            if (!nieListener.subscriptionId.equals(subscriptionId)) {

                NieSubscriptionDto nieSubscriptionDto = subscriptions.get(nieListener.subscriptionId);
                if (nieSubscriptionDto != null) {
                    nieSubscriptionDto.removeListener(nieListener.listener.get(ID));
                }
            }
        }

        NieSubscriptionDto nieSubscriptionDto = subscriptions.getOrDefault(subscriptionId,
                new NieSubscriptionDto(subscriptionId, (String) subscription.get(SUBSCRIPTION_NAME),
                        (boolean) subscription.get(IS_CASE)));
        nieSubscriptionDto.addSubscriber(subscriber);
        nieSubscriptionDto.addListener(listener);

        listenersConfiguration.put(listenerKey, new NieListener(subscriptionId, listener));
    }

    public List<NieSubscriptionDto> getSubscriptions() {

        return subscriptions.values().stream()
                .filter((subscription) -> !subscription.getListeners().isEmpty() && !subscription.getSubscribers().isEmpty())
                .collect(Collectors.toList());
    }

}
