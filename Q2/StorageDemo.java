import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StorageDemo {
    public static void main(String[] args) {
        benchmark();
        try {
            saveSubmission("answer01.pdf");
            System.out.println("Saved answer01.pdf");
        } catch (SubmissionStorageException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
        }

        try {
            saveSubmission("answer02.pdf");
            System.out.println("Saved answer02.pdf");
        } catch (SubmissionStorageException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
        }

        try {
            SubmissionStorageException e = new SubmissionStorageException("No cause was passed");
            System.out.println("Cause line would be: " + e.getCause().getMessage());
        } catch (NullPointerException npe) {
            System.out.println("NullPointerException: e.getCause() was null, so getMessage() crashed.");
        }
    }

    public static void benchmark() {
        List<Long> samples = new ArrayList<>();
        for (int round = 0; round < 10; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < 500; i++) {
                try {
                    saveSubmission("answer01.pdf");
                } catch (SubmissionStorageException ignored) {
                }
                try {
                    saveSubmission("answer02.pdf");
                } catch (SubmissionStorageException ignored) {
                }
            }
            samples.add(System.nanoTime() - start);
        }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;
        for (long value : samples) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double avg = sum / (double) samples.size();
        writeCsv("Q2/results.csv", samples);
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

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("iteration,elapsed_ns\n");
            for (int i = 0; i < data.size(); i++) {
                sb.append((i + 1)).append(',').append(data.get(i)).append('\n');
            }
            sb.append("average_ns,").append(avg).append('\n');
            sb.append("min_ns,").append(min).append('\n');
            sb.append("max_ns,").append(max).append('\n');
            Files.writeString(Path.of(fileName), sb.toString());
        } catch (IOException e) {
            System.out.println("CSV write failed: " + e.getMessage());
        }
    }

    private static void saveSubmission(String fileName) throws SubmissionStorageException {
        if (fileName.equals("answer01.pdf")) {
            return;
        }
        if (fileName.equals("answer02.pdf")) {
            try {
                throw new IOException("Disk quota exceeded");
            } catch (IOException e) {
                throw new SubmissionStorageException("Could not store " + fileName, e);
            }
        }
        throw new SubmissionStorageException("Could not store " + fileName);
    }
}

