package com.unbounded.input.core.component;

import com.unbounded.input.core.layout.KeyModel;
import java.util.List;

public interface ComponentFactory {
    List<KeyModel> instantiate(ComponentContext context);
}
