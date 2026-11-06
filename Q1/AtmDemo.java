import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AtmDemo {
    private static double balance = 500.0;

    public static void main(String[] args) {
        benchmark();
        System.out.println("=== Valid and invalid withdrawal tests ===");
        runCase("250");
        runCase("abc");
        runCase("-20");
        runCase("600");
        try {
            loadCardFile("missing-card.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Caught FileNotFoundException: " + e.getMessage());
        }
        try {
            triggerError();
        } catch (OutOfMemoryError e) {
            System.out.println("Caught OutOfMemoryError: " + e.getClass().getSimpleName());
        }
    }

    public static void runCase(String input) {
        try {
            double amount = parseAmount(input);
            withdraw(amount);
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Input error: " + input + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Bad amount: " + input + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Insufficient funds: " + input + " -> " + e.getMessage());
        } finally {
            System.out.println("Finally block: transaction attempt finished.");
        }
    }

    public static double parseAmount(String input) {
        double amount = Double.parseDouble(input);
        return amount;
    }

    public static void withdraw(double amount) throws InsufficientFundsException {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal of " + amount + ".");
        }
        balance -= amount;
        System.out.println("Withdrawal successful: " + amount + " | Remaining balance: " + balance);
    }

    public static void loadCardFile(String fileName) throws FileNotFoundException {
        Path path = Path.of(fileName);
        try {
            Files.readString(path);
        } catch (IOException e) {
            throw new FileNotFoundException("Card file not found: " + fileName);
        }
    }

    public static void triggerError() {
        throw new OutOfMemoryError("Simulated Error: JVM memory exhausted");
    }

    public static void benchmark() {
        List<Long> sample = new ArrayList<>();
        for (int round = 0; round < 10; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                try {
                    runCase("250");
                    runCase("abc");
                    runCase("-50");
                    runCase("800");
                } catch (Exception ignored) {
                }
            }
            long elapsed = System.nanoTime() - start;
            sample.add(elapsed);
        }
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;
        for (long value : sample) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double avg = sum / (double) sample.size();
        System.out.println("=== ATM benchmark (ns, 10 runs) ===");
        System.out.println("avg=" + avg + " ns min=" + min + " ns max=" + max + " ns");
        System.out.println("runData=" + sample);
        writeCsv("Q1/results.csv", sample);
    }

    public static void writeCsv(String fileName, List<Long> data) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;
        for (long value : data) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double avg = data.isEmpty() ? 0 : sum / (double) data.size();

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
            writer.write("iteration,elapsed_ns\n");
            for (int i = 0; i < data.size(); i++) {
                writer.write((i + 1) + "," + data.get(i) + "\n");
            }
            writer.write("average_ns," + avg + "\n");
            writer.write("min_ns," + min + "\n");
            writer.write("max_ns," + max + "\n");
        } catch (IOException e) {
            System.out.println("CSV write failed: " + e.getMessage());
        }
    }
}

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

class InputMismatchException extends RuntimeException {
    public InputMismatchException(String message) {
        super(message);
    }
}
