package io.slingr.service.docsconv;

import io.slingr.services.utils.tests.ServiceTests;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DocsconvTest {

    private static final Logger logger = LoggerFactory.getLogger(DocsconvTest.class);

    private static ServiceTests test;

    @Test
    @Ignore
    public void testConvertWord() throws Exception {
        DocumentConverterService converterService = new DocumentConverterService(null);
        converterService.init();
        File input = new File("/home/dgaviola/slingr/temp/word1.docx");
        File output = new File("/home/dgaviola/slingr/temp/word1.pdf");
        converterService.convert(input, output);
        System.out.println("CONVERTED!");
    }

    private File streamToFile(InputStream inputStream, String fileName) throws Exception {
        File tempFile = File.createTempFile("upload-", fileName);
        tempFile.deleteOnExit();
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }
}
