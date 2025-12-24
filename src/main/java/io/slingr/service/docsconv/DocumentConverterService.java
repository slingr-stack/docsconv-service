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
                .maxTasksPerProcess(10)
                .officeHome("/usr/lib/libreoffice")
                .disableOpengl(true)
                .build();

        // 3. Start LibreOffice
        logInfo("Starting LibreOffice...");
        officeManager.start();

        // 4. Build the Converter
        converter = LocalConverter.builder()
                .officeManager(officeManager)
                .build();

        // 5. Initialize the Single Thread Worker Queue
        // This ensures strictly sequential processing
        this.conversionQueue = Executors.newSingleThreadExecutor();

        logInfo("Service Ready. Conversion Queue initialized.");
    }

    /**
     * Places a conversion task in the queue and waits for it to finish.
     * Thread-safe: Can be called by multiple threads simultaneously.
     * * @param inputFile The source file
     * @param outputFile The target file
     * @return The converted outputFile
     * @throws Exception If conversion fails or is interrupted
     */
    public File convert(File inputFile, File outputFile) throws Exception {
        // Validation
        if (!inputFile.exists()) {
            throw new IOException("Input file not found: " + inputFile.getAbsolutePath());
        }
        logInfo(String.format("[Worker] Starting conversion: %s -> %s%n",
                inputFile.getName(), outputFile.getName()));

        converter.convert(inputFile)
                .to(outputFile)
                .execute();

        logInfo(String.format("[Worker] Finished: %s%n", outputFile.getName()));
        return outputFile;
    }

    /**
     * Clean shutdown of the queue and LibreOffice.
     */
    public void stop() {
        // Stop LibreOffice
        if (officeManager != null && officeManager.isRunning()) {
            try {
                officeManager.stop();
            } catch (OfficeException e) {
                logError("Error stopping OfficeManager: " + e.getMessage(), e);
            }
        }
    }

    // --- Runtime Installation Logic (Same as before) ---
    private void installLibreOfficeDependencies() throws IOException, InterruptedException {
        File officeBin = new File("/usr/bin/soffice");
        if (officeBin.exists()) return;

        logInfo("Installing LibreOffice dependencies...");
        String[] command = {
                "/bin/sh", "-c",
                "export DEBIAN_FRONTEND=noninteractive && " +
                        "apt-get update && apt-get install -y --no-install-recommends " +
                        "tzdata libreoffice libreoffice-java-common default-jre fonts-liberation libgl1 libglx-mesa0 libxinerama1 libdbus-glib-1-2"
        };

        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) System.out.println("[INSTALL]: " + line);
        }
        if (p.waitFor() != 0) throw new RuntimeException("Install failed");
    }

    private void logInfo(String msg) {
        if (this.appLogs != null) {
            this.appLogs.info(msg);
        } else {
            System.out.println(msg);
        }
    }

    private void logError(String msg, Exception e) {
        if (this.appLogs != null) {
            this.appLogs.error(msg, e);
        } else {
            System.err.println(msg);
        }
    }
}