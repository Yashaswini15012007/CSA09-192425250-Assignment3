import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

public class ResultProcessorDemo {
    public static void main(String[] args) throws Exception {
        generateSampleScores(1000);
        for (int threads : new int[] {1, 2, 4, 8}) {
            Map<String, LongAdder> counters = new ConcurrentHashMap<>();
            counters.put("valid", new LongAdder());
            counters.put("invalid", new LongAdder());
            counters.put("failed", new LongAdder());
            long start = System.nanoTime();
            processFiles("scores", threads, counters);
            long elapsed = System.nanoTime() - start;
            System.out.println("Threads=" + threads + " valid=" + counters.get("valid").sum() + " invalid=" + counters.get("invalid").sum() + " failed=" + counters.get("failed").sum() + " elapsed_ns=" + elapsed);
        }
        benchmark();
    }

    static void generateSampleScores(int totalFiles) throws IOException {
        Path dir = Path.of("scores");
        Files.createDirectories(dir);
        for (int i = 1; i <= totalFiles; i++) {
            Path file = dir.resolve("score-" + i + ".txt");
            if (i % 17 == 0) {
                Files.writeString(file, "bad-value");
            } else if (i % 23 == 0) {
                Files.writeString(file, "99\n88\n");
            } else {
                int value = (i % 100) + 1;
                Files.writeString(file, String.valueOf(value));
            }
        }
    }

    static void processFiles(String folder, int threads, Map<String, LongAdder> counters) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<ScoreResult>> futures = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            final int fileNumber = i;
            futures.add(executor.submit(new Callable<ScoreResult>() {
                @Override
                public ScoreResult call() {
                    try {
                        Path file = Path.of(folder, "score-" + fileNumber + ".txt");
                        if (!Files.exists(file)) {
                            counters.get("failed").increment();
                            return new ScoreResult(0, 0, 1);
                        }
                        String content = Files.readString(file).trim();
                        int score = Integer.parseInt(content);
                        if (score < 0 || score > 100) {
                            counters.get("invalid").increment();
                            return new ScoreResult(0, 1, 0);
                        }
                        counters.get("valid").increment();
                        return new ScoreResult(1, 0, 0);
                    } catch (Exception e) {
                        counters.get("failed").increment();
                        return new ScoreResult(0, 0, 1);
                    }
                }
            }));
        }
        for (Future<ScoreResult> future : futures) {
            future.get();
        }
        executor.shutdown();
    }

    static void benchmark() throws Exception {
        List<String> rows = new ArrayList<>();
        rows.add("threads,elapsed_ns");
        for (int threads : new int[] {1, 2, 4, 8}) {
            long start = System.nanoTime();
            Map<String, LongAdder> counters = new ConcurrentHashMap<>();
            counters.put("valid", new LongAdder());
            counters.put("invalid", new LongAdder());
            counters.put("failed", new LongAdder());
            processFiles("scores", threads, counters);
            long elapsed = System.nanoTime() - start;
            rows.add(threads + "," + elapsed);
        }
        Files.writeString(Path.of("Q7", "results.csv"), String.join(System.lineSeparator(), rows));
    }
}

class ScoreResult {
    int valid;
    int invalid;
    int failed;

    public ScoreResult(int valid, int invalid, int failed) {
        this.valid = valid;
        this.invalid = invalid;
        this.failed = failed;
    }
}
