package com.unbounded.input.core.component;

import com.unbounded.input.core.layout.KeyModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComponentRegistry {
    private static final ComponentRegistry INSTANCE = new ComponentRegistry();

    private final Map<String, ComponentDescriptor> descriptors = new LinkedHashMap<String, ComponentDescriptor>();
    private final Map<String, ComponentFactory> factories = new LinkedHashMap<String, ComponentFactory>();
    private boolean initialized = false;

    private ComponentRegistry() {}

    public static ComponentRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void register(ComponentDescriptor descriptor, ComponentFactory factory) {
        if (descriptor == null || factory == null) {
            throw new IllegalArgumentException("Descriptor and Factory cannot be null");
        }
        String id = descriptor.getId();
        if (descriptors.containsKey(id)) {
            throw new IllegalStateException("Duplicate component registered: " + id);
        }
        descriptors.put(id, descriptor);
        factories.put(id, factory);
    }

    public synchronized List<KeyModel> instantiate(String id, ComponentContext context) {
        ComponentFactory factory = factories.get(id);
        if (factory == null) {
            return Collections.emptyList();
        }
        List<KeyModel> result = factory.instantiate(context != null ? context : new ComponentContext(System.currentTimeMillis()));
        return result != null ? result : Collections.<KeyModel>emptyList();
    }

    public synchronized List<ComponentDescriptor> getAll() {
        return Collections.unmodifiableList(new ArrayList<ComponentDescriptor>(descriptors.values()));
    }

    public synchronized boolean isInitialized() { return initialized; }
    public synchronized void markInitialized() { this.initialized = true; }
}
