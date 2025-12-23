package io.slingr.service.docsconv;

import io.slingr.services.services.AppLogs;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

public class DocumentConverterService {
    private AppLogs appLogs;

    private LocalOfficeManager officeManager;
    private DocumentConverter converter;

    // The "Worker" that processes one file at a time
    private ExecutorService conversionQueue;

    DocumentConverterService(AppLogs appLogs) {
        this.appLogs = appLogs;
    }

    /**
     * initializes the service, installs dependencies, and starts the worker queue.
     */
    public void init() throws Exception {
        // 1. Runtime Install (As requested previously)
        installLibreOfficeDependencies();

        // 2. Configure Office Manager
        officeManager = LocalOfficeManager.builder()
                .install()
                .portNumbers(2002)
                .taskExecutionTimeout(120_000L) // 2 mins max per doc
                .officeHome("/usr/lib/libreoffice")
                .build();

        // 3. Start LibreOffice
        this.appLogs.info("Starting LibreOffice...");
        officeManager.start();

        // 4. Build the Converter
        converter = LocalConverter.builder()
                .officeManager(officeManager)
                .build();

        // 5. Initialize the Single Thread Worker Queue
        // This ensures strictly sequential processing
        this.conversionQueue = Executors.newSingleThreadExecutor();

        this.appLogs.info("Service Ready. Conversion Queue initialized.");
    }

    /**
     * Places a conversion task in the queue and waits for it to finish.
     * Thread-safe: Can be called by multiple threads simultaneously.
     * * @param inputFile The source file
     * @param outputFile The target file
     * @return The converted outputFile
     * @throws Exception If conversion fails or is interrupted
     */
    public File placeConversionTask(File inputFile, File outputFile) throws Exception {
        // Validation
        if (!inputFile.exists()) {
            throw new IOException("Input file not found: " + inputFile.getAbsolutePath());
        }

        // Create the task (Callable)
        Callable<File> conversionTask = () -> {
            this.appLogs.info(String.format("[Worker] Starting conversion: %s -> %s%n",
                    inputFile.getName(), outputFile.getName()));

            converter.convert(inputFile)
                    .to(outputFile)
                    .execute();

            this.appLogs.info(String.format("[Worker] Finished: %s%n", outputFile.getName()));
            return outputFile;
        };

        // Submit to queue
        this.appLogs.info(String.format("[Main] Queuing task for %s. Waiting for worker...%n", inputFile.getName()));
        Future<File> futureResult = conversionQueue.submit(conversionTask);

        try {
            // .get() BLOCKS here until the worker finishes this specific task
            return futureResult.get();
        } catch (ExecutionException e) {
            // Unwrap the actual exception (e.g., OfficeException) from the wrapper
            Throwable cause = e.getCause();
            throw (cause instanceof Exception) ? (Exception) cause : e;
        } catch (InterruptedException e) {
            // Handle thread interruption
            Thread.currentThread().interrupt();
            throw new IOException("Conversion waiting was interrupted", e);
        }
    }

    /**
     * Clean shutdown of the queue and LibreOffice.
     */
    public void stop() {
        // 1. Stop accepting new tasks
        if (conversionQueue != null) {
            conversionQueue.shutdown();
            try {
                // Wait a bit for pending tasks to finish
                if (!conversionQueue.awaitTermination(5, TimeUnit.SECONDS)) {
                    conversionQueue.shutdownNow();
                }
            } catch (InterruptedException e) {
                conversionQueue.shutdownNow();
            }
        }

        // 2. Stop LibreOffice
        if (officeManager != null && officeManager.isRunning()) {
            try {
                officeManager.stop();
            } catch (OfficeException e) {
                this.appLogs.error("Error stopping OfficeManager: " + e.getMessage());
            }
        }
    }

    // --- Runtime Installation Logic (Same as before) ---
    private void installLibreOfficeDependencies() throws IOException, InterruptedException {
        File officeBin = new File("/usr/bin/soffice");
        if (officeBin.exists()) return;

        this.appLogs.info("Installing LibreOffice dependencies...");
        String[] command = {
                "/bin/sh", "-c",
                "apt-get update && apt-get install -y --no-install-recommends " +
                        "libreoffice libreoffice-java-common default-jre fonts-liberation"
        };

        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) this.appLogs.info("[INSTALL]: " + line);
        }
        if (p.waitFor() != 0) throw new RuntimeException("Install failed");
    }
}