[![JetBrains official project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin experimental stability](https://img.shields.io/badge/project-experimental-kotlin.svg?colorA=555555&colorB=AC29EC&label=&logo=kotlin&logoColor=ffffff&logoWidth=10)](https://kotlinlang.org/docs/components-stability.html)

# Kotlin Notebook Widgets Integration

This integration provides a collection of interactive widgets for Kotlin Notebooks, such as sliders, labels, and more. It allows you to create a richer, more interactive experience within your notebooks.

## Usage

Use this API through the `%use widgets` magic command in a Kotlin Notebook.

### Basic Widgets

```kotlin
%use widgets

val slider = intSliderWidget {
    min = 0
    max = 100
    value = 50
    description = "Select a value:"
}

val label = labelWidget {
    value = "Current value: ${slider.value}"
}

// Display the slider
slider
```

### Layouts and Containers

Arrange widgets using `hBox`, `vBox`, `accordion`, or `tab`:

```kotlin
val slider = intSliderWidget { description = "Slider" }
val text = textWidget { description = "Text" }

val accordion = accordionWidget {
    children = listOf(slider, text)
    titles = listOf("Controls", "Input")
}

accordion
```

### Linking Widgets

You can link properties of different widgets together:

```kotlin
val play = playWidget {
    min = 0
    max = 100
    step = 1
    interval = 500
}

val slider = intSliderWidget()

linkWidget {
    source = play to "value"
    target = slider to "value"
}

hBoxWidget { children = listOf(play, slider) }
```

### Output Widget

The `OutputWidget` can capture and display standard output and rich results:

```kotlin
val out = outputWidget()
out // Display the widget

out.withScope {
    println("This will be printed inside the output widget")
    DISPLAY("Rich output works too")
}
```

### Selection Widgets

Various selection widgets like `dropdown`, `selectMultiple`, and `radioButtons` are available:

```kotlin
val dropdown = dropdownWidget {
    options = listOf(
        "Option 1" to 1,
        "Option 2" to 2,
        "Option 3" to 3
    )
    value = 2
    description = "Choose:"
}
```

### Event Listeners

You can react to widget property changes or custom messages from the frontend:

```kotlin
val slider = intSliderWidget { description = "Slider" }
val label = labelWidget { value = "Value is 0" }

// Listen to all property changes
slider.addChangeListener { patch, fromFrontend ->
    if ("value" in patch) {
        label.value = "Value is ${slider.value}"
    }
}

// Listen to a specific property change
slider.getProperty("value")?.addChangeListener { newValue, fromFrontend ->
    println("Value changed to $newValue")
}

vBoxWidget { children = listOf(slider, label) }
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
