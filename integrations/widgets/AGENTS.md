### Kotlin Jupyter Widgets Project Requirements & Guidelines

This document summarizes the key architectural decisions, requirements, and technical details discovered and implemented during the widget development session. This information is intended for other agents working on this project.

#### 1. Core Architecture
- **Modules**:
    - `widgets-api`: Core logic, protocol implementation, and base classes.
    - `widgets-generator`: Automates code generation from `schema.json`.
    - `widgets-jupyter`: Integration with Kotlin Jupyter Notebook.
    - `widgets-tests`: Integration and REPL tests.
- **Comm Targets**:
    - `jupyter.widget`: Main target for widget instance synchronization.
    - `jupyter.widget.control`: Used for global control messages like `request_states`.
- **State Synchronization**: Bi-directional sync via Jupyter Comms. Properties use delegation to track changes and sync with the frontend.
    - `echoUpdateEnabled`: (Default: `false`) Controls whether frontend-initiated updates are echoed back. Useful for multi-frontend sync or backend overrides.
- **Binary Data**: Handled via binary buffers, not JSON-encoded. `ByteArray` properties are serialized as `null` in JSON and transferred via binary buffers, with paths specified in `buffer_paths`.

#### 2. Code Generation Guidelines
- **Naming Conventions**:
    - Use `toPascalCase()` for class and file names.
    - Use `toCamelCase()` for property and factory method names.
    - Widgets should always end with the `Widget` suffix (e.g., `IntSliderWidget`). Use `String.toWidgetClassName()` from `StringUtil.kt`.
    - Handle abbreviations correctly (e.g., `HtmlWidget`, `VBoxWidget`).
    - **Splitting Logic**: `StringUtil.splitIntoParts()` handles underscores, hyphens, spaces, and CASE transitions. It specifically allows single-letter parts followed by lowercase (e.g., `V` + `Box` -> `VBox`).
- **Base Widgets**:
    - Some widgets (e.g., `OutputWidget`) are generated as `abstract Base` classes (e.g., `OutputWidgetBase`) to allow manual extensions.
    - Base widgets have `internal` visibility for their `WidgetSpec`.
    - No factory or manager extension methods are generated for base widgets.
- **Trait-based Inheritance**:
    - Use `traits` in `WidgetGenerator.kt` to match widgets by property names and types.
    - This allows common logic for selection widgets to be moved to specialized base classes like `SingleNullableSelectionWidgetBase`.

#### 3. Selection Widget System
- **Specialized Bases**:
    - `OptionWidgetBase`: Core logic for mapping labels (frontend) to values (backend).
    - `SingleSelectionWidgetBase`: Non-nullable single selection (`index: Int`).
    - `SingleNullableSelectionWidgetBase`: Nullable single selection (`index: Int?`).
    - `MultipleSelectionWidgetBase`: Multiple selection (`index: List<Int>`).
    - `SelectionRangeWidgetBase`: Range selection (`index: IntRange?`).
- **Key Properties**:
    - `options`: `List<Pair<String, ValueT>>`.
    - `simpleOptions`: `List<String>` (labels used as values).
    - `value`: High-level selection value(s) (e.g., `Any?` or `Pair<Any?, Any?>?`).
    - `label` / `labels`: Frontend labels for selection.

#### 4. Type System & Serialization
- **Custom Types**:
    - `IntRange`, `ClosedRange<Double>`: Serialized as 2-element lists `[start, end]`.
    - `Pair<A, B>`: Serialized as 2-element lists.
    - `WidgetModel`: Serialized as strings with `IPY_MODEL_` prefix.
- **Type Overrides**:
    - Use `assignedPropertyTypes` in `PropertyType.kt` to override schema-defined types (e.g., changing an `array` type to `IntRange` for `IntRangeSliderWidget.value`).
- **Dates & Times**:
    - `Instant`, `LocalDate`, `LocalTime`: Serialized as ISO-8601 strings.
- **Union Types**:
    - Generated as sealed interfaces with inline value classes for different types.
    - Handled by `UnionType` with multiple deserializers attempted in order.

#### 5. Messaging & Lifecycle
- **Custom Messages**:
    - Use `WidgetModel.sendCustomMessage(content, metadata, buffers)` for actions not covered by property sync.
    - Register listeners via `WidgetModel.addCustomMessageListener { content, metadata, buffers -> ... }`.
    - Example: `OutputWidget.clearOutput()` uses a custom message with `method = "clear_output"`.
- **Closing Widgets**:
    - `WidgetManager.closeWidget(widget)` sends a `comm_close` message and removes the widget from the manager's internal maps.

#### 6. Testing Requirements
- **Assertions**: EXCLUSIVELY use Kotest `should*` notation (e.g., `result shouldBe 42`). Do not use `assert*`.
- **Naming**: Test method names MUST NOT be in camelCase. Use descriptive names in backticks (e.g., ``fun `should have 42`()`` or ``fun `check that 42 is returned`()``).
- **REPL Tests**: Inherit from `AbstractWidgetReplTest`. Use `shouldHaveNextOpenEvent`, `shouldHaveNextUpdateEvent`, etc., to verify the sequence of Comm events.
- **Type Tests**: `TypesTest.kt` covers all property serialization/deserialization. Use `TestWidgetManager.INSTANCE` for tests that don't need real manager logic.
- **Gradle Tasks**:
    - `:generateWidgets`: Regenerates all widgets from `schema.json`.
    - `:compileKotlin`: Verifies that manual and generated code are compatible.

#### 7. Documentation & Style
- **KDocs**: Always use multiline format:
    ```kotlin
    /**
     * Description here.
     */
    ```
- **Thread Safety**: Property updates are generally safe as long as the underlying messaging protocol is thread-safe, but explicit concurrency guarantees are not provided.
- **Generated Code**: NEVER manually edit files in `src/generated`. Update the `WidgetGenerator` instead.
