package io.slingr.service.docsconv;

import io.slingr.services.Service;
import io.slingr.services.framework.annotations.*;
import io.slingr.services.services.AppLogs;
import io.slingr.services.services.rest.DownloadedFile;
import io.slingr.services.utils.Json;
import io.slingr.services.ws.exchange.FunctionRequest;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;


@SlingrService(name = "docsconv")
public class Docsconv extends Service {
    private static final String SERVICE_NAME = "docsconv";
    private final Logger logger = LoggerFactory.getLogger(Docsconv.class);

    @ApplicationLogger
    protected AppLogs appLogs;

    @ServiceConfiguration
    private Json properties;

    private DocumentConverterService converterService;


    @Override
    public void serviceStarted() {
        logger.info(String.format("Initializing service [%s]", SERVICE_NAME));
        appLogs.info(String.format("Initializing service [%s]", SERVICE_NAME));

        converterService = new DocumentConverterService(appLogs);
        try {
            converterService.init();
        } catch (Exception e) {
            logger.error(String.format("Error initializing converter for service [%s]", SERVICE_NAME), e);
        }

        logger.debug(String.format("Properties [%s] for service [%s]", properties.toPrettyString(), SERVICE_NAME));
        logger.info(String.format("Configured service [%s]", SERVICE_NAME));
    }

    @Override
    public void serviceStopped(String cause) {
        converterService.stop();
    }

    @ServiceFunction(name = "convertDocument")
    public Json convertDocument(FunctionRequest request) {
        Json data = request.getJsonParams();
        String fileId = data.string("inputFileId");
        String inputMimeType = data.string("inputMimeType");
        String outputMimeType = data.string("outputMimeType");
        DownloadedFile appFile = files().download(fileId);
        String tempName = randomName(10);
        Json resp = Json.map();
        File input = null;
        try {
            input = streamToFile(appFile.file(), tempName+"."+getExtensionForMimeType(inputMimeType));
        } catch (Exception e) {
            logger.error("Error reading file", e);
            resp.set("status", "error");
            resp.set("error", "There was a problem converting the document: "+e.getMessage());
            return resp;
        }
        try {
            File output = File.createTempFile("converted-",tempName+"."+getExtensionForMimeType(outputMimeType));
            converterService.convert(input, output);
            Json uploadedFile = files().upload(tempName+"."+getExtensionForMimeType(outputMimeType), Files.newInputStream(output.toPath()), outputMimeType);
            resp.set("status", "ok");
            resp.set("file", uploadedFile);
            return resp;
        } catch (Exception e) {
            logger.error("Error converting file", e);
            resp.set("status", "error");
            resp.set("error", "There was a problem converting the document: "+e.getMessage());
            return resp;
        }
    }

    private File streamToFile(InputStream inputStream, String fileName) throws Exception {
        // 1. Create a temporary file to hold the data
        // "prefix" must be at least 3 chars long
        File tempFile = File.createTempFile("upload-", fileName);

        // 2. Schedule the file to be deleted when the JVM exits (Optional, but good for cleanup)
        tempFile.deleteOnExit();

        // 3. Copy the stream to the file
        // REPLACE_EXISTING ensures we don't fail if the temp file already exists
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }

    public String getExtensionForMimeType(String mimeType) {
        // 1. Get the format object (returns null if unknown)
        DocumentFormat format = DefaultDocumentFormatRegistry.getInstance()
                .getFormatByMediaType(mimeType);

        if (format != null) {
            // Returns "pdf", "docx", "xlsx", etc.
            return format.getExtension();
        }

        return "bin"; // Default fallback
    }

    // Define the allowed characters
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String randomName(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }
        return sb.toString();
    }
}
