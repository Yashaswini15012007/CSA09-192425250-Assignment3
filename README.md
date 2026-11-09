# CSA09-192425250-Assignment3

Student: Yashaswini
Register No: 192425250
Student A / Student B: Student A = Yashaswini; Student B = Partner (not specified in the assignment brief)

## Java version
```text
java version "27" 2026-09-15
Java(TM) SE Runtime Environment (build 27+35-2325)
Java HotSpot(TM) 64-Bit Server VM (build 27+35-2325, mixed mode, sharing)
```

## Run steps
```bash
cd "C:\Users\yasha\Downloads\Java Assignment 3"

# Q1
javac Q1\AtmDemo.java
java -cp Q1 AtmDemo > Q1\output.txt

# Q2
javac Q2\ExamPortalDemo.java Q2\StorageDemo.java Q2\SubmissionStorageException.java
java -cp Q2 StorageDemo > Q2\output.txt

# Q3
javac Q3\SalesReportDemo.java
java -cp Q3 SalesReportDemo > Q3\output.txt

# Q4
javac Q4\ThreadLifecycleDemo.java
java -cp Q4 ThreadLifecycleDemo > Q4\output.txt

# Q5
javac Q5\SeatBookingDemo.java
java -cp Q5 SeatBookingDemo > Q5\output.txt

# Q6
javac Q6\TokenCounterDemo.java
java -cp Q6 TokenCounterDemo > Q6\output.txt

# Q7
javac Q7\ResultProcessorDemo.java
java -cp Q7 ResultProcessorDemo > Q7\output.txt
```

## Result notes
- Q1: valid withdrawals and all invalid paths covered; output saved in Q1/output.txt and benchmark CSV saved in Q1/results.csv.
- Q2: storage demo prints the required `Cause: Disk quota exceeded`; benchmark CSV saved in Q2/results.csv.
- Q3: manual and try-with-resources reports compared; logs saved in Q3/sales.log; results in Q3/results.csv.
- Q4: thread lifecycle and join demo verified; results in Q4/results.csv.
- Q5: unsynchronized races reproduced; synchronized run ends with `Seats sold: 10`; results in Q5/results.csv.
- Q6: correct token buffer output is `Sum of tokens collected: 210 (expected 210)`; results in Q6/results.csv.
- Q7: 1, 2, 4, and 8 thread benchmark executed; results in Q7/results.csv.

## Completed artifacts
- Q1-Q7 folders with Java source, notes.md, output.txt, and results.csv where applicable.
- Root report.md and contribution_record.md.
- Git commits created in sequence for each question.
