### Kotlin Jupyter Widgets Project Requirements & Guidelines

This document summarizes the key architectural decisions, requirements, and technical details discovered and implemented during the widget development session. This information is intended for other agents working on this project.

#### 1. Core Architecture
- **Modules & Project Structure**:
    - `widgets-api`: Core logic, protocol implementation, and base classes. Contains the base classes and generated widget models used by both the kernel and potential clients.
    - `widgets-generator`: Automates code generation from `schema.json`. Contains the generator logic and `schema.json`.
    - `widgets-jupyter`: Integration with Kotlin Jupyter Notebook. Contains the Jupyter-specific integration code and helpers.
    - `widgets-tests`: Integration and REPL tests.
- **Comm Targets**:
    - `jupyter.widget`: Main target for widget instance synchronization.
    - `jupyter.widget.control`: Used for global control messages like `request_states`.
- **State Synchronization**: Bi-directional sync via Jupyter Comms. Properties use delegation to track changes and sync with the frontend.
    - `echoUpdateEnabled`: (Default: `false`) Controls whether frontend-initiated updates are echoed back. Useful for multi-frontend sync or backend overrides.
- **Binary Data**: Handled via binary buffers, not JSON-encoded. `ByteArray` properties are serialized as `null` in JSON and transferred via binary buffers, with paths specified in `buffer_paths`.

#### 2. Code Generation Guidelines
- **Schema-Driven**: The widget generator uses `schema.json` to define properties and widgets.
- **Naming Conventions**:
    - Use `toPascalCase()` for class and file names.
    - Use `toCamelCase()` for property and factory method names.
    - Use full nouns for variables and properties; avoid shorthands like `itfTrait` (use `interfaceTrait` instead).
    - Widgets should always end with the `Widget` suffix (e.g., `IntSliderWidget`). Use `String.toWidgetClassName()` from `StringUtil.kt`.
    - Handle abbreviations correctly (e.g., `HtmlWidget`, `VBoxWidget`).
    - **Splitting Logic**: `StringUtil.splitIntoParts()` handles underscores, hyphens, spaces, and CASE transitions. It specifically allows single-letter parts followed by lowercase (e.g., `V` + `Box` -> `VBox`).
- **Implementation Details**:
    - Generates Kotlin classes for widgets with property delegation for state synchronization.
    - Handles enums and factory registries.
    - Produces Jupyter integration helpers for easy widget creation in notebooks.
- **Base Widgets**:
    - Some widgets (e.g., `OutputWidget`) are generated as `abstract Base` classes (e.g., `OutputWidgetBase`) to allow manual extensions.
    - Base widgets have `internal` visibility for their `WidgetSpec`.
    - No factory or manager extension methods are generated for base widgets.
- **Trait-based Inheritance**:
    - Use `traits` in `WidgetGenerator.kt` to match widgets by property names and types.
    - This allows common logic for selection widgets to be moved to specialized base classes like `SingleNullableSelectionWidgetBase`.
    - **Multiple Traits**: The generator supports matching multiple traits per widget.
        - Only **one** trait can be a base class trait (`isInterface = false`).
        - Multiple traits can be interface traits (`isInterface = true`).
    - **Explicit Decisions**: The generator explicitly decides whether to generate, override, or skip a property based on flags in `TraitInfo`:
        - `isInterface`: If `true`, the widget class implements the specified `baseClassName` as an interface.
        - `shouldOverride`: If `true`, properties belonging to the trait are generated with the `override` keyword.
        - `skipGeneration`: If `true`, properties belonging to the trait are NOT generated in the widget class (usually because they are inherited from a base class).
    - These flags are independent: for example, an interface trait might require overrides (`shouldOverride = true`) but still need property generation (`skipGeneration = false`), while a base class trait might skip generation entirely (`skipGeneration = true`).
    - Do not rely on implicit signals like the presence of an import to determine inheritance.
- **Generator Execution**:
    - **Path Handling**: It's better to pass absolute paths to generators via command-line arguments instead of relying on the working directory, especially in multi-module Gradle projects where the working directory might vary.

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
- **Closing Widgets**:
    - `WidgetManager.closeWidget(widget)` sends a `comm_close` message and removes the widget from the manager's internal maps.

#### 6. Testing Requirements
- **Assertions**: EXCLUSIVELY use Kotest `should*` notation (e.g., `result shouldBe 42`). Do not use `assert*`.
- **JSON creation**: EXCLUSIVELY use `buildJsonObject` and `buildJsonArray` for creating JSON objects and arrays. Do not use `JsonObject(mapOf(...))` or `JsonArray(listOf(...))` directly.
- **Naming**: Test method names MUST NOT be in camelCase. Use descriptive names in backticks (e.g., ``fun `should have 42`()`` or ``fun `check that 42 is returned`()``).
- **REPL Tests**: Inherit from `AbstractWidgetReplTest`. Use `shouldHaveNextOpenEvent`, `shouldHaveNextUpdateEvent`, etc., to verify the sequence of Comm events.
- **Type Tests**: `TypesTest.kt` covers all property serialization/deserialization. Use `TestWidgetManager.INSTANCE` for tests that don't need real manager logic.

#### 7. Build, Verification & Style
- **Gradle Tasks**:
    - `:generateWidgets`: Regenerates all widgets from `schema.json`. To make a task incremental, define `inputs` and `outputs`. This prevents unnecessary execution when no source files or schemas have changed.
    - `:compileKotlin`: Verifies that manual and generated code are compatible.
    - `:ktlintFormat`: Automatically formats the code according to the project's style guide.
    - `:check`: Runs all verification tasks, including tests, ktlint checks, and `:checkWidgetsRegenerated`.
    - `:checkWidgetsRegenerated`: (Part of `:check`) Ensures that the generated widgets are up-to-date with the schema and generator.
- **Build Configuration**:
    - **Centralized Dependencies**: In `integrations/widgets/build.gradle.kts`, a `subprojects` block ensures that all `KotlinCompile` and `ktlint` tasks in `widgets-api` and `widgets-jupyter` depend on the `:generateWidgets` task from `widgets-generator`. This prevents code duplication in individual module build scripts.
    - **Type-Safe Project Accessors**: Use `projects.path.to.module` instead of `project(":path:to:module")` when `TYPESAFE_PROJECT_ACCESSORS` is enabled in `settings.gradle.kts`. This provides better IDE support and compile-time safety for module references.
    - **Verification Logic**: When verifying generated code, it's more robust to generate into a temporary directory and compare content rather than relying on `git status`. This makes the build independent of the VCS state.
    - **Custom Executions**: Use `javaexec` (via `project.javaexec` or `ExecOperations.javaexec`) for running internal tools like generators. This is more portable than manual `java` path resolution and `providers.exec`.
- **KDocs**: Always use multiline format:
    ```kotlin
    /**
     * Description here.
     */
    ```
- **Thread Safety**: Property updates are generally safe as long as the underlying messaging protocol is thread-safe, but explicit concurrency guarantees are not provided.
- **Generated Code**: NEVER manually edit files in `src/generated`. Update the `WidgetGenerator` instead.
- **Schema Modification**: Please NEVER modify `schema.json`. Instead, add "override" mechanics such as we added for traits, or use `propertyOverrides` in `PropertyOverrides.kt` for property customizations, within the generator itself.
