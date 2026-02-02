package org.jetbrains.kotlinx.jupyter.notebook

import org.jetbrains.kotlinx.jupyter.api.libraries.CommManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a new instance of [NotebookManipulator].
 *
 * @param commManager The comm manager to use for communication with the frontend
 * @param requestTimeout The timeout for requests
 * @return A new NotebookManipulator instance
 */
public fun createNotebookManipulator(
    commManager: CommManager,
    requestTimeout: Duration = 30.seconds,
): NotebookManipulator = NotebookManipulatorImpl(commManager, requestTimeout)
