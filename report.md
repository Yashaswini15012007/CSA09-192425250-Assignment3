# Assignment 3 Report - Java Exception Handling and Multithreading

## Student information
- Student: Yashaswini
- Register no: 192425250
- Student A: Yashaswini
- Student B: Partner (assignment brief does not specify a name)

## Q1: ATM withdrawal failure handling
### Shared
- Failure points include numeric parsing, negative amount validation, insufficient balance, missing card file, and an `Error` example (`OutOfMemoryError`).
- The solution uses `try`, `catch`, and `finally` to log transaction status even when exceptions occur.

### Student A
- Throwable hierarchy and catch-order reasoning are documented in [Q1/notes.md](Q1/notes.md).
- The `finally` block guarantees the transaction attempt is always logged.

### Student B
- The demo includes valid and invalid inputs; the invalid cases produce distinct catch blocks.
- A broader catch before a narrower one would not compile.

### Joint
- A single `catch (Exception e)` is possible but loses precise recovery logic and error classification.

### Output evidence
```text
Withdrawal successful: 250.0 | Remaining balance: 250.0
Input error: abc -> NumberFormatException: For input string: "abc"
Bad amount: -50 -> IllegalArgumentException: Amount cannot be negative.
Insufficient funds: 800 -> Insufficient balance for withdrawal of 800.0.
```

### Benchmark summary
| iteration | elapsed_ns |
|---|---:|
| 1 | recorded in [Q1/results.csv](Q1/results.csv) |
| 2 | recorded in [Q1/results.csv](Q1/results.csv) |
| 3 | recorded in [Q1/results.csv](Q1/results.csv) |

## Q2: Online exam portal, user-defined exceptions and propagation
### Shared
- `ExamException` is the checked base class.
- Storage wraps `IOException` in `SubmissionStorageException` with the original cause.

### Student A
- Class diagram and reasoning are in [Q2/notes.md](Q2/notes.md).
- The propagation path is controller -> service -> storage -> root cause.

### Student B
- The `getCause()` demo prints `Disk quota exceeded`.
- A null-cause demo shows the `NullPointerException` if the cause is not passed.

### Joint
- `SubmissionStorageException` keeps the business message and the technical cause together.

### Required output
```text
Saved answer01.pdf
Error: Could not store answer02.pdf
Cause: Disk quota exceeded
```

### Benchmark summary
| iteration | elapsed_ns |
|---|---:|
| 1 | recorded in [Q2/results.csv](Q2/results.csv) |
| 2 | recorded in [Q2/results.csv](Q2/results.csv) |
| 3 | recorded in [Q2/results.csv](Q2/results.csv) |

## Q3: Daily sales report, resource safety and debugging
### Shared
- Manual and try-with-resources versions both read sales values from CSV files.
- The root issue is resource leak handling and proper closure.

### Student A
- Breakpoints, watch variables, and leak diagnostics are described in [Q3/notes.md](Q3/notes.md).

### Student B
- The manual version leaks handles when a missing file is hit before the previous reader is closed.
- try-with-resources guarantees closure in reverse declaration order.

### Joint
- try-with-resources is not enough when the resource is not `AutoCloseable` or when an atomic write/retry pattern is needed.

### Observed results
```text
=== Manual report ===
Store A: 600
Store B: 750
Store C: 0
Total: 1350

=== Try-with-resources report ===
Store A: 600
Store B: 750
Store C: 0
Total: 1350
Reports identical: true
Open handles after manual fail: 2
Open handles after try-with-resources fail: 0
```

## Q4: Thread creation and life cycle
### Shared
- Four scanner stations are started and joined.
- The lifecycle states are traced through `getState()`.

### Student A
- `Thread` vs `Runnable`, lifecycle diagram, and `join()` reasoning are in [Q4/notes.md](Q4/notes.md).

### Student B
- Finish order varies by scheduler; priorities are only hints.
- Daemon threads end with the JVM.

### Joint
- Output order is not stable, so programs must not depend on it without explicit coordination.

## Q5: Cinema seat booking, race conditions
### Shared
- The unsynchronized version exposes a check-then-book race.
- The synchronized version protects the critical section.

### Student A
- The critical section is the check and assignment of a seat state.
- The unsynchronized test reproduces multiple bookings for the same seat.

### Student B
- The synchronized run keeps the count stable.
- The final value is `Seats sold: 10`.

### Joint
- A single lock per screen is a good design for a multi-screen cinema.

### Before and after output
```text
Before synchronization:
Run 1: Seats sold = 8
Run 2: Seats sold = 6
...
Run 10: Seats sold = 8

After synchronization:
Seats sold: 10
```

## Q6: Deadlock and inter-thread communication
### Shared
- The buffer uses `wait()` and `notifyAll()` inside synchronized methods.
- The correct version prints the required token sum.

### Student A
- `while` guards are necessary for spurious wakeups and stale conditions.
- `notifyAll()` is safer than `notify()` when multiple threads may be waiting.

### Student B
- Deadlock is prevented by consistent lock ordering.
- The four Coffman conditions are explained in [Q6/notes.md](Q6/notes.md).

### Joint
- Deadlock and producer-consumer both rely on correct shared-state coordination.

### Required output
```text
Sum of tokens collected: 210 (expected 210)
```

## Q7: Concurrent fault-tolerant result processor
### Shared
- Tasks are executed via `ExecutorService` and each file is validated independently.
- Failures are captured per file and do not stop the run.

### Student A
- The design uses `Callable`, `Future`, and `LongAdder` counters; see [Q7/notes.md](Q7/notes.md).

### Student B
- A validation run demonstrates valid and failed counts reconcile with the total files processed.
- Thread counts of 1, 2, 4, and 8 were benchmarked.

### Joint
- More threads do not always help because the workload is I/O-heavy and context switching overhead becomes significant.

### Benchmark summary
| threads | elapsed_ns |
|---|---:|
| 1 | 148795100 |
| 2 | 171828800 |
| 4 | 91222800 |
| 8 | 39964300 |

## File evidence
- Q1: [Q1/output.txt](Q1/output.txt), [Q1/results.csv](Q1/results.csv), [Q1/notes.md](Q1/notes.md)
- Q2: [Q2/output.txt](Q2/output.txt), [Q2/results.csv](Q2/results.csv), [Q2/notes.md](Q2/notes.md)
- Q3: [Q3/output.txt](Q3/output.txt), [Q3/results.csv](Q3/results.csv), [Q3/notes.md](Q3/notes.md)
- Q4: [Q4/output.txt](Q4/output.txt), [Q4/results.csv](Q4/results.csv), [Q4/notes.md](Q4/notes.md)
- Q5: [Q5/output.txt](Q5/output.txt), [Q5/results.csv](Q5/results.csv), [Q5/notes.md](Q5/notes.md)
- Q6: [Q6/output.txt](Q6/output.txt), [Q6/results.csv](Q6/results.csv), [Q6/notes.md](Q6/notes.md)
- Q7: [Q7/output.txt](Q7/output.txt), [Q7/results.csv](Q7/results.csv), [Q7/notes.md](Q7/notes.md)

## Completion summary
All seven questions were implemented, compiled, executed, and measured. The repository includes runnable Java sources, evidence logs in `output.txt`, and real timing data in the corresponding CSV files.
