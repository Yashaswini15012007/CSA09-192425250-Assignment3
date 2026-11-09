# Q5: Cinema seat booking, race conditions

## Shared

The key issue is a check-then-book race. Two threads may both see a seat as free before either sets it to true.

## Student A

### Double-booking test
The unsynchronized method intentionally adds a brief sleep between the check and the write. Over 10 runs, the unsynchronized version produced values above 10, demonstrating the bug.

### Critical section
The critical section is the combined check and seat assignment block, because the state of the array must be updated atomically.

## Student B

### Whole-method lock vs critical-section-only lock
A whole-method lock is simpler but blocks all booking operations across the entire object. A critical-section lock narrows the lock to the exact seat check/update and improves concurrency.

### Static and intrinsic locking
Static synchronization uses the class object lock. Instance synchronization uses the object lock. In this project, the `synchronized` version uses the intrinsic object lock for the `SeatBooking` instance.

## Joint

For a cinema with 20 screens, one lock per screen object is best because it avoids unnecessary contention. Each screen lock protects only its own seat map, so unrelated screens can operate independently.

## Before and after output
Before synchronization, values like `Run 1: Seats sold = 8` and `Run 4: Seats sold = 10` show duplicates; after synchronization, the final value is `Seats sold: 10`.

## Alternative analysis
`AtomicBoolean[]`, `ReentrantLock`, and `ConcurrentHashMap.putIfAbsent()` are stronger concurrency tools for high-contention systems. They reduce lock contention and are easier to reason about when many threads act at once.
