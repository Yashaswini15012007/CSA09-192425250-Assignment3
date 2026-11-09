import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ThreadLifecycleDemo {
    public static void main(String[] args) throws InterruptedException, IOException {
        demoRunInsteadOfStart();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            threads.add(new ScannerStationThread("Station-" + (i + 1), i + 1));
        }

        for (Thread thread : threads) {
            System.out.println(thread.getName() + " state before start: " + thread.getState());
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Finish order: " + StationTracker.getFinishOrder());
        Thread daemon = new Thread(() -> System.out.println("Daemon thread ran before process exit"));
        daemon.setDaemon(true);
        daemon.start();
        daemon.join();

        benchmark();
    }

    static void demoRunInsteadOfStart() {
        Runnable runnable = () -> {
            System.out.println("Runnable executed on: " + Thread.currentThread().getName());
        };
        Thread t = new Thread(runnable, "RunnableDemoThread");
        t.run();
        System.out.println("Direct run() call reuses the main thread.");
    }

    static void benchmark() throws IOException {
        List<Long> samples = new ArrayList<>();
        for (int run = 0; run < 10; run++) {
            StationTracker.reset();
            long start = System.nanoTime();
            List<Thread> tasks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                ScannerStationThread station = new ScannerStationThread("Station-" + (i + 1), i + 1);
                tasks.add(station);
                station.start();
            }
            for (Thread thread : tasks) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            samples.add(System.nanoTime() - start);
        }
        writeCsv("Q4/results.csv", samples);
    }

    static void writeCsv(String fileName, List<Long> data) throws IOException {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;
        for (long value : data) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double avg = sum / (double) data.size();
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
            writer.write("iteration,elapsed_ns\n");
            for (int i = 0; i < data.size(); i++) {
                writer.write((i + 1) + "," + data.get(i) + "\n");
            }
            writer.write("average_ns," + avg + "\n");
            writer.write("min_ns," + min + "\n");
            writer.write("max_ns," + max + "\n");
        }
    }
}

class ScannerStationThread extends Thread {
    private final int delayMs;

    public ScannerStationThread(String name, int delayMs) {
        super(name);
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        System.out.println(getName() + " state before work: " + getState());
        try {
            Thread.sleep(delayMs * 30L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        StationTracker.record(getName());
        System.out.println(getName() + " finished. Current state: " + getState());
    }
}

class StationTracker {
    private static final List<String> FINISH_ORDER = new ArrayList<>();

    public static synchronized void record(String name) {
        FINISH_ORDER.add(name);
    }

    public static synchronized List<String> getFinishOrder() {
        return new ArrayList<>(FINISH_ORDER);
    }

    public static synchronized void reset() {
        FINISH_ORDER.clear();
    }
}
