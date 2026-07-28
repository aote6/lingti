package com.unbounded.input.core.component;

public class ComponentDescriptor {
    private final String id;
    private final ComponentCategory category;
    private final String label;

    public ComponentDescriptor(String id, ComponentCategory category, String label) {
        this.id = id;
        this.category = category != null ? category : ComponentCategory.BASE;
        this.label = label != null ? label : id;
    }

    public String getId() { return id; }
    public ComponentCategory getCategory() { return category; }
    public String getLabel() { return label; }
}
