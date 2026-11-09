import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TokenCounterDemo {
    public static void main(String[] args) throws Exception {
        TokenBuffer correct = new TokenBuffer(5);
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                correct.put(21);
            }
        });
        Thread consumer = new Thread(() -> {
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += correct.take();
            }
            System.out.println("Sum of tokens collected: " + sum + " (expected 210)");
        });
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        TokenBuffer broken = new TokenBuffer(5);
        Thread badProducer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                broken.putIfVersion(21);
            }
        });
        Thread badConsumer = new Thread(() -> {
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += broken.takeIfVersion();
            }
            System.out.println("If-version sum: " + sum);
        });
        benchmark();
    }

    static void benchmark() throws IOException {
        List<Long> data = new ArrayList<>();
        for (int run = 0; run < 10; run++) {
            long start = System.nanoTime();
            TokenBuffer buffer = new TokenBuffer(5);
            Thread producer = new Thread(() -> {
                for (int i = 0; i < 50; i++) {
                    buffer.put(1);
                }
            });
            Thread consumer = new Thread(() -> {
                int sum = 0;
                for (int i = 0; i < 50; i++) {
                    sum += buffer.take();
                }
            });
            producer.start();
            consumer.start();
            try {
                producer.join();
                consumer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            data.add(System.nanoTime() - start);
        }
        writeCsv("Q6/results.csv", data);
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
        double avg = data.isEmpty() ? 0 : sum / (double) data.size();
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("Q6", "results.csv"))) {
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

class TokenBuffer {
    private final int[] buffer;
    private int count;
    private int in;
    private int out;

    public TokenBuffer(int capacity) {
        this.buffer = new int[capacity];
    }

    public synchronized void put(int token) {
        while (count == buffer.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        buffer[in] = token;
        in = (in + 1) % buffer.length;
        count++;
        notifyAll();
    }

    public synchronized int take() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        int token = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        notifyAll();
        return token;
    }

    public synchronized void putIfVersion(int token) {
        if (count == buffer.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        buffer[in] = token;
        in = (in + 1) % buffer.length;
        count++;
        notifyAll();
    }

    public synchronized int takeIfVersion() {
        if (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        int token = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        notifyAll();
        return token;
    }
}
