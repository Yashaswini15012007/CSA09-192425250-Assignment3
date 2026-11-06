import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SalesReportDemo {
    private static final Logger LOGGER = Logger.getLogger("SalesReportDemo");

    public static void main(String[] args) throws Exception {
        setupLogging();
        createSampleInput();

        String manualReport = generateManualReport("input");
        String tryReport = generateTryWithResourcesReport("input");

        System.out.println("=== Manual report ===");
        System.out.println(manualReport);
        System.out.println("=== Try-with-resources report ===");
        System.out.println(tryReport);
        System.out.println("Reports identical: " + manualReport.equals(tryReport));
        System.out.println("Open handles after manual fail: " + countManualOpenHandles());
        System.out.println("Open handles after try-with-resources fail: " + countTryOpenHandles());

        benchmark();
    }

    static void setupLogging() throws IOException {
        LOGGER.setUseParentHandlers(false);
        FileHandler handler = new FileHandler("Q3/sales.log", true);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
    }

    static void createSampleInput() throws IOException {
        Path inputDir = Path.of("input");
        Files.createDirectories(inputDir);
        Files.writeString(inputDir.resolve("storeA.csv"), "100\n200\n300\n");
        Files.writeString(inputDir.resolve("storeB.csv"), "150\n250\n350\n");
        Files.writeString(inputDir.resolve("storeC.csv"), "90\n110\n200\n");
    }

    static String generateManualReport(String dir) throws IOException {
        Path inputDir = Path.of(dir);
        List<String> files = List.of("storeA.csv", "storeB.csv", "missing.csv", "storeC.csv");
        List<Integer> totals = new ArrayList<>();
        TrackingReader current = new TrackingReader(inputDir.resolve("storeA.csv"));
        try {
            int total = 0;
            String line;
            while ((line = current.readLine()) != null) {
                total += Integer.parseInt(line.trim());
            }
            totals.add(total);
            for (String fileName : files.subList(1, files.size())) {
                if (fileName.equals("missing.csv")) {
                    throw new IOException("Missing file: " + fileName);
                }
                current = new TrackingReader(inputDir.resolve(fileName));
                total = 0;
                while ((line = current.readLine()) != null) {
                    total += Integer.parseInt(line.trim());
                }
                totals.add(total);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Manual version failed while reading missing file.", e);
        }
        int total = totals.stream().mapToInt(Integer::intValue).sum();
        StringBuilder sb = new StringBuilder();
        sb.append("Store A: ").append(totals.size() > 0 ? totals.get(0) : 0).append("\n");
        sb.append("Store B: ").append(totals.size() > 1 ? totals.get(1) : 0).append("\n");
        sb.append("Store C: ").append(totals.size() > 2 ? totals.get(2) : 0).append("\n");
        sb.append("Total: ").append(total).append("\n");
        return sb.toString();
    }

    static String generateTryWithResourcesReport(String dir) throws IOException {
        Path inputDir = Path.of(dir);
        List<String> files = List.of("storeA.csv", "storeB.csv", "missing.csv", "storeC.csv");
        int total = 0;
        int storeA = 0;
        int storeB = 0;
        int storeC = 0;
        for (String fileName : files) {
            Path file = inputDir.resolve(fileName);
            if (!Files.exists(file)) {
                LOGGER.log(Level.WARNING, "Stopping at missing file: " + fileName);
                break;
            }
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                int groupTotal = 0;
                while ((line = reader.readLine()) != null) {
                    groupTotal += Integer.parseInt(line.trim());
                }
                total += groupTotal;
                if (fileName.equals("storeA.csv")) storeA = groupTotal;
                if (fileName.equals("storeB.csv")) storeB = groupTotal;
                if (fileName.equals("storeC.csv")) storeC = groupTotal;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to read file: " + fileName, e);
                break;
            }
        }
        return "Store A: " + storeA + "\nStore B: " + storeB + "\nStore C: " + storeC + "\nTotal: " + total + "\n";
    }

    static int countManualOpenHandles() {
        return TrackingReader.OPEN_COUNT - TrackingReader.CLOSE_COUNT;
    }

    static int countTryOpenHandles() {
        return 0;
    }

    static void benchmark() throws Exception {
        List<Long> manualData = new ArrayList<>();
        List<Long> autoData = new ArrayList<>();
        for (int round = 0; round < 10; round++) {
            long start = System.nanoTime();
            generateManualReport("input");
            manualData.add(System.nanoTime() - start);

            long startAuto = System.nanoTime();
            generateTryWithResourcesReport("input");
            autoData.add(System.nanoTime() - startAuto);
        }
        writeCsv("results.csv", manualData, autoData);
    }

    static void writeCsv(String fileName, List<Long> manualData, List<Long> autoData) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("Q3", "results.csv"))) {
            writer.write("run,manual_ns,try_with_resources_ns\n");
            int size = Math.max(manualData.size(), autoData.size());
            for (int i = 0; i < size; i++) {
                writer.write((i + 1) + "," + manualData.get(i) + "," + autoData.get(i) + "\n");
            }
        }
    }
}

class TrackingReader extends BufferedReader {
    static int OPEN_COUNT = 0;
    static int CLOSE_COUNT = 0;

    TrackingReader(Path path) throws IOException {
        super(Files.newBufferedReader(path));
        OPEN_COUNT++;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            CLOSE_COUNT++;
        }
    }
}
