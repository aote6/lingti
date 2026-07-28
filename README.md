Lingti - Programmable Android Input Panel Generator

Not an input method. A tool for building your own keyboard panels.

What it does

- Drag and drop buttons anywhere in edit mode to design your layout
- Each button can insert text, send key events, paste clipboard, or trigger key chords (Ctrl+C etc.)
- 3 independent layout slots, each saved separately and switchable at runtime
- Component library with 20+ groups: QWERTY full set, digits, arrow keys, brackets, punctuation, operators, special symbols, Tab, Esc, Delete, Insert, Home, End, PageUp, PageDown, text editing operations (select all, copy, cut, paste), and 11 Ctrl key chords (C, D, L, R, A, E, U, K, Z, W, Y)
- Save writes to local JSON file, restore resets to factory layout

Architecture

Touch -> GestureRecognizer -> KeyboardGestureController -> Command -> InputEngine -> InputConnection

Build

Phone only (Huawei P20 + Termux), no PC, no IDE, no Gradle.

    bash build_simple.sh
    APK auto-copied to ~/storage/downloads/unbounded-mvp.apk

Toolchain: ecj -> d8 -> aapt -> apksigner

Project structure

    app/src/main/java/com/unbounded/input/             main source
    app/src/main/java/com/unbounded/input/core/         layout, component, command subsystems
    app/src/main/assets/default.json                    factory layout
    build_simple.sh                                     build script

License

GPLv3
