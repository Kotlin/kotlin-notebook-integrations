# Contributing to kotlin-notebook-integrations

This is a single, common contributing guide for all integrations in this repository
(database, http-util, intellij-platform, etc.).

## Local development flow (for any integration)

1. Create a local JSON descriptor file (example name: `Local.json`).
   Put into it only what you plan to use (see the dependencies note below).
   A minimal template looks like this:

   ```json
   {
     "description": "Local snapshot of a Kotlin Notebook integration",
     "properties": [
       { "name": "v", "value": "0.1.0-%VERSION%-SNAPSHOT" }
     ],
     "repositories": [
       "*mavenLocal"
     ],
     "dependencies": [
       "org.jetbrains.kotlinx:ARTIFACT_ID:$v"
     ]
   }
   ```

   Replace `ARTIFACT_ID` with the integration you work on, for example:
   - `kotlin-jupyter-database`
   - `kotlin-jupyter-serialization`
   - `kotlin-jupyter-ktor-client`
   - `kotlin-jupyter-intellij-platform`

2. In the repository root, open `gradle.properties` and increase `devAddition` by 1 (e.g., `1 → 2`).

3. In your JSON file, replace `%VERSION%` with the same number from the previous step, so the property `v`
   becomes something like `0.1.0-2-SNAPSHOT`.

4. Publish the module you are working on to your local Maven repository:
   - Database API: `./gradlew :integrations:database:database-api:publishToMavenLocal`
   - HTTP util – Serialization: `./gradlew :integrations:http-util:serialization:publishToMavenLocal`
   - HTTP util – Ktor Client: `./gradlew :integrations:http-util:ktor-client:publishToMavenLocal`
   - IntelliJ Platform: `./gradlew :integrations:intellij-platform:publishToMavenLocal`

   Alternatively, just run `./gradlew publishToMavenLocal` from the repository root to publish all modules.

5. In a Kotlin Notebook, load your local snapshot using the absolute path to your JSON file
   (instead of a `%use <integration>` alias):

   ```
   %use /absolute/path/to/Local.json
   ```

6. Reload the Kotlin Notebook Kernel if needed.

To publish a newer snapshot, repeat steps 2–6. Bumping the version is required to invalidate the dependency cache.

## Important: the "dependencies" field in JSON

In the `dependencies` array, list exactly the artifacts you want to load in the notebook — no more, no less.
There is no implicit or “magical” expansion.

Examples:

```json
{
  "dependencies": [
    "org.jetbrains.kotlinx:kotlin-jupyter-database:$v"
  ]
}
```

```json
{
  "dependencies": [
    "org.jetbrains.kotlinx:kotlin-jupyter-serialization:$v",
    "org.jetbrains.kotlinx:kotlin-jupyter-ktor-client:$v"
  ]
}
```
