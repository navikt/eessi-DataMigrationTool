package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.NieSubscriptionDto;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.nie.NieSubscriptionUtils;

public class SubscriptionsHolderTest {

    @Test
    public void testSubscription() {

        final String SUBSCRIPTION1_ID = "subscription1";
        final String SUBSCRIPTION1_NAME = "name1";

        final String SUBSCRIBER1_ID = "subscriber1";
        final String SUBSCRIBER1_VERSION = "version1";

        final String LISTENER1_ID = "listener1";
        final String LISTENER1_LABEL = "listener 1";
        final String LISTENER1_LISTENER = "http://listener1";

        final List<Map<String, Object>> s1subscribers = new ArrayList<>();
        s1subscribers.add(Map.of(ID, SUBSCRIBER1_ID, VERSION, SUBSCRIBER1_VERSION));

        final List<Map<String, Object>> s1listeners = new ArrayList<>();
        s1listeners.add(Map.of(ID, LISTENER1_ID, LABEL, LISTENER1_LABEL, LISTENER, LISTENER1_LISTENER));

        final MapHolder subscription1 = createSubscription(
                SUBSCRIPTION1_ID,
                SUBSCRIPTION1_NAME,
                true,
                s1subscribers,
                s1listeners);

        List<NieSubscriptionDto> subscriptions = NieSubscriptionUtils.getNieSubscriptions(List.of(subscription1));

        assertEquals(1, subscriptions.size());

        NieSubscriptionDto subscriptionDO = subscriptions.get(0);

        assertEquals(SUBSCRIPTION1_ID, subscriptionDO.getId());
        assertEquals(SUBSCRIPTION1_NAME, subscriptionDO.getName());
        assertTrue(subscriptionDO.isCase());

        Map<String, MapHolder> subscribers = subscriptionDO.getSubscribers();

        assertEquals(1, subscribers.size());
        assertTrue(subscribers.containsKey(SUBSCRIBER1_ID));

        MapHolder subscriber = subscribers.get(SUBSCRIBER1_ID);

        assertEquals(SUBSCRIBER1_VERSION, subscriber.string(VERSION));

        Map<String, MapHolder> listeners = subscriptionDO.getListeners();

        assertEquals(1, listeners.size());
        assertTrue(listeners.containsKey(LISTENER1_ID));

        MapHolder listener = listeners.get(LISTENER1_ID);

        assertEquals(LISTENER1_LABEL, listener.string(LABEL));
        assertEquals(LISTENER1_LISTENER, listener.string(LISTENER));

    }

    @Test
    public void testDuplicateListeners() {

        final String SUBSCRIPTION1_ID = "subscription1";
        final String SUBSCRIPTION1_NAME = "name1";

        final String SUBSCRIBER1_ID = "subscriber1";
        final String SUBSCRIBER1_VERSION = "version1";

        final String LISTENER1_ID = "listener1";
        final String LISTENER1_LABEL = "listener 1";
        final String LISTENER1_LISTENER = "http://listener1";

        final String SUBSCRIPTION2_ID = "subscription2";
        final String SUBSCRIPTION2_NAME = "name2";

        final String LISTENER2_LISTENER = "http://listener2";

        final List<Map<String, Object>> s1subscribers = new ArrayList<>();
        s1subscribers.add(Map.of(ID, SUBSCRIBER1_ID, VERSION, SUBSCRIBER1_VERSION));

        final List<Map<String, Object>> s1listeners = new ArrayList<>();
        s1listeners.add(Map.of(ID, LISTENER1_ID, LABEL, LISTENER1_LABEL, LISTENER, LISTENER1_LISTENER));

        final MapHolder subscription1 = createSubscription(
                SUBSCRIPTION1_ID,
                SUBSCRIPTION1_NAME,
                true,
                s1subscribers,
                s1listeners);

        final List<Map<String, Object>> s2subscribers = new ArrayList<>();
        s2subscribers.add(Map.of(ID, SUBSCRIBER1_ID, VERSION, SUBSCRIBER1_VERSION));

        final List<Map<String, Object>> s2listeners = new ArrayList<>();
        s2listeners.add(Map.of(ID, LISTENER1_ID, LABEL, LISTENER1_LABEL, LISTENER, LISTENER2_LISTENER));

        final MapHolder subscription2 = createSubscription(
                SUBSCRIPTION2_ID,
                SUBSCRIPTION2_NAME,
                true,
                s2subscribers,
                s2listeners);

        List<NieSubscriptionDto> subscriptions = NieSubscriptionUtils.getNieSubscriptions(List.of(subscription1, subscription2));

        assertEquals(1, subscriptions.size());

        NieSubscriptionDto subscriptionDO = subscriptions.get(0);

        assertEquals(SUBSCRIPTION2_ID, subscriptionDO.getId());
        assertEquals(SUBSCRIPTION2_NAME, subscriptionDO.getName());
        assertTrue(subscriptionDO.isCase());

        Map<String, MapHolder> subscribers = subscriptionDO.getSubscribers();

        assertEquals(1, subscribers.size());
        assertTrue(subscribers.containsKey(SUBSCRIBER1_ID));

        MapHolder subscriber = subscribers.get(SUBSCRIBER1_ID);

        assertEquals(SUBSCRIBER1_VERSION, subscriber.string(VERSION));

        Map<String, MapHolder> listeners = subscriptionDO.getListeners();

        assertEquals(1, listeners.size());
        assertTrue(listeners.containsKey(LISTENER1_ID));

        MapHolder listener = listeners.get(LISTENER1_ID);

        assertEquals(LISTENER1_LABEL, listener.string(LABEL));
        assertEquals(LISTENER2_LISTENER, listener.string(LISTENER));

    }

    @Test
    public void testDuplicateSubscriptionName() {

        final String SUBSCRIPTION1_ID = "subscription1";
        final String SUBSCRIPTION1_NAME = "name";

        final String SUBSCRIBER1_ID = "subscriber1";
        final String SUBSCRIBER1_VERSION = "version1";

        final String LISTENER1_ID = "listener1";
        final String LISTENER1_LABEL = "listener 1";
        final String LISTENER1_LISTENER = "http://listener1";

        final String SUBSCRIPTION2_ID = "subscription2";

        final String SUBSCRIBER2_ID = "subscriber2";
        final String SUBSCRIBER2_VERSION = "version2";

        final String LISTENER2_ID = "listener2";
        final String LISTENER2_LABEL = "listener 2";
        final String LISTENER2_LISTENER = "http://listener2";

        final List<Map<String, Object>> s1subscribers = new ArrayList<>();
        s1subscribers.add(Map.of(ID, SUBSCRIBER1_ID, VERSION, SUBSCRIBER1_VERSION));

        final List<Map<String, Object>> s1listeners = new ArrayList<>();
        s1listeners.add(Map.of(ID, LISTENER1_ID, LABEL, LISTENER1_LABEL, LISTENER, LISTENER1_LISTENER));

        final MapHolder subscription1 = createSubscription(
                SUBSCRIPTION1_ID,
                SUBSCRIPTION1_NAME,
                false,
                s1subscribers,
                s1listeners);

        final List<Map<String, Object>> s2subscribers = new ArrayList<>();
        s2subscribers.add(Map.of(ID, SUBSCRIBER2_ID, VERSION, SUBSCRIBER2_VERSION));

        final List<Map<String, Object>> s2listeners = new ArrayList<>();
        s2listeners.add(Map.of(ID, LISTENER2_ID, LABEL, LISTENER2_LABEL, LISTENER, LISTENER2_LISTENER));

        final MapHolder subscription2 = createSubscription(
                SUBSCRIPTION2_ID,
                SUBSCRIPTION1_NAME,
                false,
                s2subscribers,
                s2listeners);

        List<NieSubscriptionDto> subscriptions = NieSubscriptionUtils.getNieSubscriptions(List.of(subscription1, subscription2));

        assertEquals(2, subscriptions.size());

        NieSubscriptionDto subscriptionDO1 = subscriptions.get(0);

        assertEquals(SUBSCRIPTION1_ID, subscriptionDO1.getId());
        assertEquals(SUBSCRIPTION1_NAME, subscriptionDO1.getName());
        assertFalse(subscriptionDO1.isCase());

        NieSubscriptionDto subscriptionDO2 = subscriptions.get(1);

        assertEquals(SUBSCRIPTION2_ID, subscriptionDO2.getId());
        assertEquals(SUBSCRIPTION1_NAME + "_", subscriptionDO2.getName());
        assertFalse(subscriptionDO2.isCase());
    }

    @Test
    public void testDuplicateSubscriberDocType() {

        final String SUBSCRIPTION1_ID = "subscription1";
        final String SUBSCRIPTION1_NAME = "name";

        final String SUBSCRIBER_ID = "subscriber1";
        final String SUBSCRIBER_VERSION = "version1";

        final String LISTENER1_ID = "listener1";
        final String LISTENER1_LABEL = "listener 1";
        final String LISTENER1_LISTENER = "http://listener1";

        final String SUBSCRIPTION2_ID = "subscription2";
        final String SUBSCRIPTION2_NAME = "name_";

        final String LISTENER2_ID = "listener2";
        final String LISTENER2_LABEL = "listener 2";
        final String LISTENER2_LISTENER = "http://listener2";

        final List<Map<String, Object>> s1subscribers = new ArrayList<>();
        s1subscribers.add(Map.of(ID, SUBSCRIBER_ID, VERSION, SUBSCRIBER_VERSION));

        final List<Map<String, Object>> s1listeners = new ArrayList<>();
        s1listeners.add(Map.of(ID, LISTENER1_ID, LABEL, LISTENER1_LABEL, LISTENER, LISTENER1_LISTENER));

        final MapHolder subscription1 = createSubscription(
                SUBSCRIPTION1_ID,
                SUBSCRIPTION1_NAME,
                false,
                s1subscribers,
                s1listeners);

        final List<Map<String, Object>> s2subscribers = new ArrayList<>();
        s2subscribers.add(Map.of(ID, SUBSCRIBER_ID, VERSION, SUBSCRIBER_VERSION));

        final List<Map<String, Object>> s2listeners = new ArrayList<>();
        s2listeners.add(Map.of(ID, LISTENER2_ID, LABEL, LISTENER2_LABEL, LISTENER, LISTENER2_LISTENER));

        final MapHolder subscription2 = createSubscription(
                SUBSCRIPTION2_ID,
                SUBSCRIPTION1_NAME,
                false,
                s2subscribers,
                s2listeners);

        List<NieSubscriptionDto> subscriptions = NieSubscriptionUtils.getNieSubscriptions(List.of(subscription1, subscription2));

        assertEquals(1, subscriptions.size());

        NieSubscriptionDto subscriptionDO1 = subscriptions.get(0);

        assertEquals(SUBSCRIPTION2_ID, subscriptionDO1.getId());
        assertEquals(SUBSCRIPTION2_NAME, subscriptionDO1.getName());
        assertFalse(subscriptionDO1.isCase());
    }

    private MapHolder createSubscription(
            final String id,
            final String name,
            final boolean isCase,
            final List<Map<String, Object>> subscribers,
            final List<Map<String, Object>> listeners) {

        final Map<String, Object> subscription = new HashMap<>();
        subscription.put(ID, id);
        subscription.put(SUBSCRIPTION_NAME, name);
        subscription.put(IS_CASE, isCase);
        subscription.put(SUBSCRIBERS, subscribers);
        subscription.put(LISTENERS, listeners);

        return new MapHolder(subscription, new ConcurrentHashMap<>(), "");
    }

}
