[![JetBrains official project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin experimental stability](https://img.shields.io/badge/project-experimental-kotlin.svg?colorA=555555&colorB=AC29EC&label=&logo=kotlin&logoColor=ffffff&logoWidth=10)](https://kotlinlang.org/docs/components-stability.html)

# Kotlin Notebook Widgets Integration

This integration provides a collection of interactive widgets for Kotlin Notebooks, such as sliders, labels, and more. It allows you to create a richer, more interactive experience within your notebooks.

## Usage

Use this API through the `%use widgets` magic command in a Kotlin Notebook.

```kotlin
%use widgets

val slider = intSliderWidget().apply {
    min = 0
    max = 100
    value = 50
    description = "Select a value:"
}

val label = labelWidget().apply {
    value = "Current value: ${slider.value}"
}

// Display the slider
slider
```

## Module structure

This project consists of the following modules:

- `widgets-api`: Contains the core widget implementations, protocols, and model definitions.
- `widgets-jupyter`: Provides the integration logic and useful helpers for Kotlin Jupyter notebooks.
- `widgets-generator`: Generates widget models from `schema.json`.
- `widgets-tests`: Contains integration tests for widgets.

## Development

Most widgets are automatically generated from a schema.
To regenerate widgets after changing `schema.json` or the generator itself, run:

```bash
./gradlew :integrations:widgets:widgets-generator:generateWidgets
```
