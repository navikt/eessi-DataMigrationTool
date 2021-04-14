package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields.ID;

import java.util.HashMap;
import java.util.Map;

public class NieSubscriptionDto {

    private final String id;

    private final String name;

    private final boolean isCase;

    private final Map<String, Map<String, String>> subscribers = new HashMap<>();

    private final Map<String, Map<String, String>> listeners = new HashMap<>();

    public NieSubscriptionDto(String id, String name, boolean isCase) {
        this.id = id;
        this.name = name;
        this.isCase = isCase;
    }

    public void addSubscriber(Map<String, String> subscriber) {
        subscribers.put(subscriber.get(ID), subscriber);
    }

    public void addListener(Map<String, String> listener) {
        listeners.put(listener.get(ID), listener);
    }

    public void removeListener(String id) {
        listeners.remove(id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCase() {
        return isCase;
    }

    public Map<String, Map<String, String>> getSubscribers() {
        return subscribers;
    }

    public Map<String, Map<String, String>> getListeners() {
        return listeners;
    }

}
