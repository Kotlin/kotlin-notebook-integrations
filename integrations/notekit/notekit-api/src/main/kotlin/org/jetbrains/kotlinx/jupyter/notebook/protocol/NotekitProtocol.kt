package org.jetbrains.kotlinx.jupyter.notebook.protocol

public const val NOTEKIT_PROTOCOL_TARGET: String = "jupyter.notekit.v1"

// Response fields
public const val FIELD_REQUEST_ID: String = "request_id"
public const val FIELD_STATUS: String = "status"
public const val FIELD_RESULT: String = "result"
public const val FIELD_ERROR: String = "error"
public const val FIELD_METHOD: String = "method"

// Response status values
public const val STATUS_OK: String = "ok"
public const val STATUS_ERROR: String = "error"

// Result fields
public const val FIELD_COUNT: String = "count"
public const val FIELD_METADATA: String = "metadata"
public const val FIELD_CELLS: String = "cells"
public const val FIELD_START: String = "start"
public const val FIELD_END: String = "end"
public const val FIELD_DELETE_COUNT: String = "delete_count"
public const val FIELD_MERGE: String = "merge"
public const val FIELD_NBFORMAT: String = "nbformat"
public const val FIELD_NBFORMAT_MINOR: String = "nbformat_minor"

// Method names
public const val METHOD_GET_CELL_COUNT: String = "get_cell_count"
public const val METHOD_GET_NOTEBOOK_METADATA: String = "get_notebook_metadata"
public const val METHOD_GET_CELL_RANGE: String = "get_cell_range"
public const val METHOD_SPLICE_CELL_RANGE: String = "splice_cell_range"
public const val METHOD_SET_NOTEBOOK_METADATA: String = "set_notebook_metadata"
public const val METHOD_EXECUTE_CELL_RANGE: String = "execute_cell_range"
public const val METHOD_GET_NBFORMAT_VERSION: String = "get_nbformat_version"
