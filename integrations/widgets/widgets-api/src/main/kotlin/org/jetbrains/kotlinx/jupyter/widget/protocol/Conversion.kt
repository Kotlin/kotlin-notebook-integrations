package org.jetbrains.kotlinx.jupyter.widget.protocol

internal fun WidgetStateMessage.toPatch(buffers: List<ByteArray>): Patch = getPatch(WireMessage(state, bufferPaths, buffers))
