# Notebook Manipulator Protocol (v1)

This document describes the protocol for notebook manipulation via Jupyter Comms.

## Overview

The Notebook Manipulator protocol enables programmatic manipulation of Jupyter notebooks from kernel code. It uses the Jupyter Comms infrastructure to send requests from the kernel to the frontend and receive responses.

**Target Name**: `jupyter.notebook.manipulator.v1`

**Protocol Version**: 1

## Message Structure

All messages follow a standard structure:

```json
{
  "method": "<method-name>",
  "request_id": "<unique-request-id>",
  "params": {
    // method-specific parameters
  }
}
```

All responses follow a standard structure:

```json
{
  "request_id": "<same-as-request>",
  "status": "ok" | "error",
  "result": {
    // method-specific result (only present when status is "ok")
  },
  "error": {
    "message": "<error-message>",
    "code": "<error-code>"
  } // only present when status is "error"
}
```

## Methods

### 1. Get Cell Count

Returns the total number of cells in the current notebook.

**Request:**
```json
{
  "method": "get_cell_count",
  "request_id": "req-001"
}
```

**Response (success):**
```json
{
  "request_id": "req-001",
  "status": "ok",
  "result": {
    "count": 42
  }
}
```

### 2. Get Notebook Metadata

Returns the metadata of the current notebook.

**Request:**
```json
{
  "method": "get_notebook_metadata",
  "request_id": "req-002"
}
```

**Response (success):**
```json
{
  "request_id": "req-002",
  "status": "ok",
  "result": {
    "metadata": {
      "kernelspec": {
        "name": "kotlin",
        "display_name": "Kotlin"
      },
      "language_info": {
        "name": "kotlin",
        "version": "1.9.0"
      }
      // ... other metadata fields
    }
  }
}
```

### 3. Get Cell Range

Returns information about a range of cells in the notebook.

**Request:**
```json
{
  "method": "get_cell_range",
  "request_id": "req-003",
  "params": {
    "start": 0,  // inclusive, 0-based
    "end": 5     // exclusive, 0-based
  }
}
```

**Response (success):**
```json
{
  "request_id": "req-003",
  "status": "ok",
  "result": {
    "cells": [
      {
        "cell_type": "code" | "markdown" | "raw",
        "source": "print(\"Hello, World!\")",  // string or array of strings
        "metadata": {
          // cell-specific metadata
        },
        "execution_count": 1,  // only for code cells, may be null
        "outputs": [           // only for code cells
          {
            "output_type": "stream" | "execute_result" | "display_data" | "error",
            // ... output-specific fields
          }
        ]
      }
      // ... more cells
    ]
  }
}
```

**Response (error - invalid range):**
```json
{
  "request_id": "req-003",
  "status": "error",
  "error": {
    "message": "Invalid cell range: start=10, end=5",
    "code": "INVALID_RANGE"
  }
}
```

**Response (error - out of bounds):**
```json
{
  "request_id": "req-003",
  "status": "error",
  "error": {
    "message": "Cell range out of bounds: end=100 exceeds cell count of 42",
    "code": "OUT_OF_BOUNDS"
  }
}
```

### 4. Splice Cell Range

Modifies a range of cells in the notebook (delete, insert, or replace).

**Request:**
```json
{
  "method": "splice_cell_range",
  "request_id": "req-004",
  "params": {
    "start": 2,      // inclusive, 0-based index where to start modification
    "delete_count": 1,  // number of cells to delete starting from 'start'
    "cells": [       // cells to insert at 'start' position (after deletion)
      {
        "cell_type": "code",
        "source": "val x = 42",
        "metadata": {}
      },
      {
        "cell_type": "markdown",
        "source": "# New Section",
        "metadata": {}
      }
    ]
  }
}
```

**Semantics:**
- Delete `delete_count` cells starting at index `start`
- Insert all cells from the `cells` array at index `start`
- If `delete_count` is 0, this is a pure insertion
- If `cells` is empty, this is a pure deletion
- If both are non-zero, this is a replacement operation

**Examples:**
- Delete 2 cells starting at index 3: `{"start": 3, "delete_count": 2, "cells": []}`
- Insert 1 cell at index 0: `{"start": 0, "delete_count": 0, "cells": [...]}`
- Replace cell at index 5: `{"start": 5, "delete_count": 1, "cells": [...]}`

**Response (success):**
```json
{
  "request_id": "req-004",
  "status": "ok",
  "result": {
    "affected_range": {
      "start": 2,
      "end": 4  // new end position after splice
    }
  }
}
```

**Response (error):**
```json
{
  "request_id": "req-004",
  "status": "error",
  "error": {
    "message": "Invalid splice parameters: start=10 is out of bounds",
    "code": "INVALID_SPLICE_PARAMS"
  }
}
```

### 5. Set Notebook Metadata

Updates the metadata of the current notebook.

**Request:**
```json
{
  "method": "set_notebook_metadata",
  "request_id": "req-005",
  "params": {
    "metadata": {
      "custom_field": "custom_value",
      "kernelspec": {
        "name": "kotlin"
      }
      // ... metadata fields to set/update
    },
    "merge": true  // if true, merge with existing metadata; if false, replace entirely
  }
}
```

**Response (success):**
```json
{
  "request_id": "req-005",
  "status": "ok",
  "result": {}
}
```

**Response (error):**
```json
{
  "request_id": "req-005",
  "status": "error",
  "error": {
    "message": "Failed to update notebook metadata: invalid JSON structure",
    "code": "INVALID_METADATA"
  }
}
```

### 6. Execute Cell Range

Executes a range of cells in the notebook.

**Request:**
```json
{
  "method": "execute_cell_range",
  "request_id": "req-006",
  "params": {
    "start": 0,  // inclusive, 0-based
    "end": 5     // exclusive, 0-based
  }
}
```

**Response (success):**
```json
{
  "request_id": "req-006",
  "status": "ok",
  "result": {}
}
```

**Response (error - invalid range):**
```json
{
  "request_id": "req-006",
  "status": "error",
  "error": {
    "message": "Invalid cell range: start=10, end=5",
    "code": "INVALID_RANGE"
  }
}
```

**Response (error - out of bounds):**
```json
{
  "request_id": "req-006",
  "status": "error",
  "error": {
    "message": "Cell range out of bounds: end=100 exceeds cell count of 42",
    "code": "OUT_OF_BOUNDS"
  }
}
```

**Response (error - execution failed):**
```json
{
  "request_id": "req-006",
  "status": "error",
  "error": {
    "message": "Cell execution failed at index 3: SyntaxError",
    "code": "EXECUTION_FAILED"
  }
}
```

## Error Codes

| Code | Description |
|------|-------------|
| `INVALID_RANGE` | The specified cell range is invalid (e.g., start > end) |
| `OUT_OF_BOUNDS` | The specified cell range exceeds the notebook boundaries |
| `INVALID_SPLICE_PARAMS` | The splice parameters are invalid |
| `INVALID_METADATA` | The provided metadata is invalid or malformed |
| `INVALID_CELL_DATA` | The provided cell data is invalid or malformed |
| `EXECUTION_FAILED` | Cell execution failed |
| `UNKNOWN_METHOD` | The requested method is not recognized |
| `INTERNAL_ERROR` | An internal error occurred on the frontend |
| `NO_ACTIVE_NOTEBOOK` | No active notebook is available |

## Communication Flow

1. **Initialization:**
   - The kernel integration opens a comm with target `jupyter.notebook.manipulator.v1`
   - The frontend listens for this target and accepts the comm connection

2. **Request/Response:**
   - The kernel sends a request message via the comm
   - The frontend processes the request and sends a response via the same comm
   - Each request includes a unique `request_id` that is echoed in the response
   - Requests should timeout after a reasonable period (e.g., 30 seconds)

3. **Error Handling:**
   - If the frontend cannot process a request, it responds with `status: "error"`
   - If a request times out, the kernel API should throw an exception
   - The comm remains open even after errors for subsequent requests

## Cell Format

Cells follow the Jupyter Notebook format specification (nbformat). Each cell has:

- `cell_type`: One of `"code"`, `"markdown"`, or `"raw"`
- `source`: Cell content as a string or array of strings
- `metadata`: Cell-specific metadata (object)
- `execution_count`: (code cells only) Execution counter, may be `null`
- `outputs`: (code cells only) Array of output objects

## Notebook Metadata Format

Notebook metadata follows the Jupyter Notebook format specification. Common fields include:

- `kernelspec`: Kernel specification (name, display_name, language)
- `language_info`: Language information (name, version, file_extension, etc.)
- Custom fields specific to the notebook or extensions

## Implementation Notes

1. **Thread Safety:** The frontend implementation should handle concurrent requests safely or serialize them.

2. **Cell Validation:** The frontend should validate cell data before applying changes to ensure notebook integrity.

3. **Undo/Redo:** Cell modifications may affect the notebook's undo/redo stack. Implementation is frontend-specific.

4. **Persistence:** Changes are applied to the in-memory notebook model. Saving to disk is the user's responsibility.

5. **Notifications:** Cell modifications may trigger change notifications to other notebook components (e.g., UI updates).

6. **Request IDs:** Must be unique within a comm session. Recommended format: UUID or sequential counter with prefix.

## Future Extensions

Possible future additions to this protocol:

- Cell output manipulation
- Kernel restart/interrupt
- Notebook-level operations (save, rename, etc.)
- Bulk metadata operations
- Cell metadata updates without full cell replacement
