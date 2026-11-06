# Q3: Daily sales report, resource safety and debugging

## Shared

The daily report reads several CSV files and totals the sales values. The key resource is each file handle. The correct safe pattern is to close each reader as soon as the calculation is complete.

Resource checklist:
- input files: must be closed.
- output writer: must be flushed and closed.
- logging handler: should be closed to avoid file leak.

The manual version leaks because a file reader may stay open when a later file fails before the close call is reached.

## Student A

### Debugger workflow
Use breakpoints on the file loop and on the `close()` calls.
1. Set a breakpoint before opening the next file.
2. Step into `Files.newBufferedReader()`.
3. Inspect the reader object and confirm the open counter increments.
4. Step through the failure path and verify that the close call is skipped.
5. Repeat with try-with-resources to compare the final state.

Screenshot placeholders:
- Breakpoint on the file loop.
- Watch panel showing `OPEN_COUNT` and `CLOSE_COUNT`.
- Final comparison screen showing `manual leak = 2`, `try-with-resources = 0`.

### Observed resource counts
The manual version leaks 2 open handles when the missing file fails mid-loop. The try-with-resources version closes every reader immediately and stays at 0.

## Student B

### Why the manual version leaks
When the loop hits `missing.csv`, the program throws before the reader closes. If a previous file reader was still active, it remains open until process shutdown. That is a resource leak.

### Why try-with-resources is safe
The compiler transforms `try (BufferedReader r = ...)` into a hidden `finally` that guarantees `close()` is called. The close order is reverse declaration order, matching the resource stack semantics.

### Suppressed exception example
A suppressed exception is the secondary error from closing a resource after a primary failure. It appears in the stack trace as `Suppressed: ...` and can be accessed via `Throwable.getSuppressed()`.

## Joint

Try-with-resources alone is not always enough when:
- the resource does not implement `AutoCloseable`;
- partial output is left behind after a write failure;
- a retry or atomic rename is required for recovery;
- multiple threads share the same resource and require a higher-level lock.

## Alternative analysis
The `close()`-in-`finally` pattern works but is verbose and easy to get wrong. A no-close version leaks file handles and eventually causes `Too many open files` behavior. The manual version also has O(f) open resources during a failure burst, while try-with-resources keeps the maximum open resource count bounded by the current scope.
