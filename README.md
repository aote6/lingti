Lingti - Programmable Android Input Panel Generator

Not an input method. A tool for building your own keyboard panels.

What it does

- Drag and drop buttons anywhere in edit mode to design your layout
- Each button can insert text, send key events, paste clipboard, or trigger key chords (Ctrl+C etc.)
- 3 independent layout slots, each saved separately and switchable at runtime
- Component library: QWERTY full set, digits, arrow keys, Tab, Esc, Delete, Insert, Home, End, PageUp, PageDown, brackets, punctuation, operators, special symbols, and 11 Ctrl key chords
- Save writes to local JSON file, restore resets to factory layout

Architecture

Touch -> GestureRecognizer -> KeyboardGestureController -> Command -> InputEngine -> InputConnection

Build

Single developer, phone only (Huawei P20 + Termux), no PC, no IDE, no Gradle.

    bash build_simple.sh
    APK auto-copied to ~/storage/downloads/unbounded-mvp.apk

Toolchain: ecj (Eclipse Compiler for Java) -> d8 (dex) -> aapt (resources) -> apksigner

Project structure

    app/src/main/java/com/unbounded/input/          main source
    app/src/main/java/com/unbounded/input/core/      layout, component, command subsystems
    app/src/main/assets/default.json                 factory layout
    build_simple.sh                                  build script

License

GPLv3
