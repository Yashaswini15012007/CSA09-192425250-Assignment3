# Q6: Deadlock and inter-thread communication

## Shared

This question combines wallet-transfer deadlock analysis and the producer-consumer buffer. The producer-consumer design uses `wait()` and `notifyAll()` inside synchronized methods to protect the buffer state.

## Student A

### Why `wait()` is in a loop
The condition can change unexpectedly because of spurious wakeups or because another thread changed the shared state. A `while` loop keeps checking the condition before acting.

### `notify()` vs `notifyAll()`
`notifyAll()` wakes all waiting threads and is safer when multiple consumers or producers may be waiting on a shared resource. `notify()` may wake only one thread and can leave others asleep even though work remains.

### `sleep()` vs `wait()`
`sleep()` is just a time-based pause and does not release the monitor. `wait()` releases the lock and waits for a notification, making it appropriate for inter-thread coordination.

## Student B

### Deadlock demonstration
A deadlock occurs when thread A holds account X and waits for account Y while thread B holds account Y and waits for account X. The four Coffman conditions are mutual exclusion, hold and wait, no preemption, and circular wait.

### Prevention with consistent ordering
Lock the accounts in a fixed order (for example, lower account ID before higher ID). This prevents circular wait and breaks the deadlock scenario.

## Joint

Deadlock and producer-consumer both depend on shared state. Deadlock arises when coordination is incorrect; producer-consumer depends on correct coordination and notification to keep progress moving without violation of capacity or empty-buffer rules.

## Required output and if-version difference
The correct version prints:
```text
Sum of tokens collected: 210 (expected 210)
```
Using `if` instead of `while` can cause missed wake-ups and stale-condition bugs. A consumer may proceed when the buffer is still empty or a producer may overwrite a state that another thread still expects. These bugs may show up as wrong sums, hangs, or exceptions.

## Alternative analysis
Using `ArrayBlockingQueue` is safer and simpler because the queue handles guard conditions internally. `tryLock` with timeout can also prevent deadlocks in transfer logic but is more complex to reason about than a consistent lock ordering strategy.
