package com.unbounded.input.core.theme;

public class ThemeManager {
    private ThemeProfile current = ThemeProfile.DEFAULT;

    public void setTheme(ThemeProfile theme) {
        if (theme != null) this.current = theme;
    }

    public ThemeProfile getTheme() { return current; }

    public void setByName(String name) {
        if ("Amber".equals(name)) setTheme(ThemeProfile.AMBER);
        else if ("IBM".equals(name)) setTheme(ThemeProfile.IBM);
        else setTheme(ThemeProfile.DEFAULT);
    }
}
