import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExamPortalDemo {
    public static void main(String[] args) {
        runExceptionBenchmark();
        System.out.println("=== Exam portal checks ===");
        try {
            ExamController controller = new ExamController();
            controller.submit("answer01.pdf", 1500);
            controller.submit("answer02.pdf", 50000);
            controller.submit("answer03.pdf", 1200);
            controller.submit("answer04.pdf", 1200);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        System.out.println("=== Cause demo ===");
        try {
            throw new SubmissionStorageException("Could not store answer02.pdf", new IOException("Disk quota exceeded"));
        } catch (SubmissionStorageException e) {
            System.out.println("Cause: " + e.getCause().getMessage());
        }

        System.out.println("=== Null cause demo ===");
        try {
            SubmissionStorageException e = new SubmissionStorageException("No cause was passed");
            System.out.println("Cause field: " + e.getCause());
            System.out.println("Cause message: " + e.getCause().getMessage());
        } catch (NullPointerException npe) {
            System.out.println("NullPointerException: e.getCause() returned null and .getMessage() crashed.");
        }
    }

    public static void runExceptionBenchmark() {
        List<Long> samples = new ArrayList<>();
        for (int round = 0; round < 10; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                try {
                    throw new SubmissionStorageException("Could not store file.txt");
                } catch (SubmissionStorageException ignored) {
                }
            }
            samples.add(System.nanoTime() - start);
        }
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;
        for (long v : samples) {
            sum += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        double avg = sum / (double) samples.size();
        System.out.println("=== Exam exception benchmark (ns, 10 runs) ===");
        System.out.println("avg=" + avg + " ns min=" + min + " ns max=" + max + " ns");
        System.out.println("runData=" + samples);
    }
}

class ExamController {
    private final ExamService service = new ExamService();

    public void submit(String fileName, int fileSize) throws ExamException {
        try {
            service.saveSubmission(fileName, fileSize);
            System.out.println("Saved " + fileName);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }
}

class ExamService {
    private final StorageService storage = new StorageService();

    public void saveSubmission(String fileName, int fileSize) throws ExamException {
        if (fileName.equals("answer02.pdf")) {
            throw new SubmissionStorageException("Could not store " + fileName, new IOException("Disk quota exceeded"));
        }
        if (fileName.equals("answer03.pdf")) {
            throw new FileTooLargeException("File too large: " + fileName + " (" + fileSize + " bytes)");
        }
        if (fileName.equals("answer04.pdf")) {
            throw new NotRegisteredException("Student not registered for the exam.");
        }
        storage.store(fileName, fileSize);
    }
}

class StorageService {
    public void store(String fileName, int fileSize) throws ExamException {
        if (fileSize > 30000) {
            throw new FileTooLargeException("File too large: " + fileName + " (" + fileSize + " bytes)");
        }
        try {
            Files.writeString(Path.of(fileName), "demo content");
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + fileName, e);
        }
    }
}

class ExamException extends Exception {
    public ExamException(String message) {
        super(message);
    }

    public ExamException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ExamWindowClosedException extends ExamException {
    public ExamWindowClosedException(String message) {
        super(message);
    }
}

class NotRegisteredException extends ExamException {
    public NotRegisteredException(String message) {
        super(message);
    }
}

class FileTooLargeException extends ExamException {
    public FileTooLargeException(String message) {
        super(message);
    }
}

