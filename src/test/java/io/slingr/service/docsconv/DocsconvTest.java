package io.slingr.service.docsconv;

import io.slingr.services.services.exchange.Parameter;
import io.slingr.services.utils.Json;
import io.slingr.services.utils.tests.ServiceTests;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class DocsconvTest {

    private static final Logger logger = LoggerFactory.getLogger(DocsconvTest.class);

    private static ServiceTests test;

    @BeforeClass
    public static void init() throws Exception {
//        test = ServiceTests.start(new io.slingr.service.docsconv.Runner(), "test.properties");
    }

    @Test
    @Ignore
    public void testConvertWord() {
        final Json req = Json.map();
        Json res = test.executeFunction("convertDocument", req);
        // TODO build request to convert document
        assertNotNull(res);
        logger.info(res.toString());
        assertFalse(res.is(Parameter.EXCEPTION_FLAG));
        assertNotNull(res.string("status"));
        assertEquals("ok", res.string("status"));
    }
}
