package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NieFields.*;

import java.util.HashMap;
import java.util.Map;

public class NieSubscriptionDto {

    private final String id;

    private final String name;

    private final boolean isCase;

    private final Map<String, MapHolder> subscribers = new HashMap<>();

    private final Map<String, MapHolder> listeners = new HashMap<>();

    public NieSubscriptionDto(String id, String name, boolean isCase) {
        this.id = id;
        this.name = name;
        this.isCase = isCase;
    }

    public void addSubscriber(MapHolder subscriber) {
        subscribers.put(subscriber.string(ID), subscriber);
    }

    public void removeSubscriber(String id) {
        subscribers.remove(id);
    }

    public void addListener(MapHolder listener) {
        listeners.put(listener.string(ID), listener);
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

    public Map<String, MapHolder> getSubscribers() {
        return subscribers;
    }

    public Map<String, MapHolder> getListeners() {
        return listeners;
    }

}
