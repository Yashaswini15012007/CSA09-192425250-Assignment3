import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SeatBookingDemo {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("=== Unsynchronized runs ===");
        List<Integer> unsyncResults = new ArrayList<>();
        for (int run = 1; run <= 10; run++) {
            SeatBooking unsync = new SeatBooking(10);
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final int seat = i % 10;
                Thread t = new Thread(() -> unsync.book(seat));
                threads.add(t);
                t.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            unsyncResults.add(unsync.getBookingsMade());
            System.out.println("Run " + run + ": Seats sold = " + unsync.getBookingsMade());
        }

        SeatBooking sync = new SeatBooking(10);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int seat = i % 10;
            Thread t = new Thread(() -> sync.bookSynchronized(seat));
            threads.add(t);
            t.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Seats sold: " + sync.getBookingsMade());

        benchmark();
    }

    static void benchmark() throws IOException {
        List<Long> unsyncData = new ArrayList<>();
        List<Long> syncData = new ArrayList<>();
        for (int run = 0; run < 10; run++) {
            long start = System.nanoTime();
            SeatBooking unsync = new SeatBooking(10);
            List<Thread> list = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                final int seat = i % 10;
                Thread t = new Thread(() -> unsync.book(seat));
                list.add(t);
                t.start();
            }
            for (Thread t : list) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            unsyncData.add(System.nanoTime() - start);

            long syncStart = System.nanoTime();
            SeatBooking sync = new SeatBooking(10);
            List<Thread> syncThreads = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                final int seat = i % 10;
                Thread t = new Thread(() -> sync.bookSynchronized(seat));
                syncThreads.add(t);
                t.start();
            }
            for (Thread t : syncThreads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            syncData.add(System.nanoTime() - syncStart);
        }
        writeCsv("Q5/results.csv", unsyncData, syncData);
    }

    static void writeCsv(String fileName, List<Long> unsync, List<Long> sync) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("Q5", "results.csv"))) {
            writer.write("run,unsync_ns,synchronized_ns\n");
            int size = Math.max(unsync.size(), sync.size());
            for (int i = 0; i < size; i++) {
                writer.write((i + 1) + "," + unsync.get(i) + "," + sync.get(i) + "\n");
            }
        }
    }
}

class SeatBooking {
    private final boolean[] seats;
    private int bookingsMade;

    public SeatBooking(int seatCount) {
        this.seats = new boolean[seatCount];
    }

    public void book(int seat) {
        if (seat < 0 || seat >= seats.length) {
            return;
        }
        if (!seats[seat]) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            seats[seat] = true;
            bookingsMade++;
        }
    }

    public synchronized void bookSynchronized(int seat) {
        if (seat < 0 || seat >= seats.length) {
            return;
        }
        if (!seats[seat]) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            seats[seat] = true;
            bookingsMade++;
        }
    }

    public int getBookingsMade() {
        return bookingsMade;
    }
}
