package io.slingr.service.docsconv;

import io.slingr.services.Service;
import io.slingr.services.framework.annotations.*;
import io.slingr.services.services.AppLogs;
import io.slingr.services.utils.Json;
import io.slingr.services.ws.exchange.FunctionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SlingrService(name = "docsconv")
public class Docsconv extends Service {
    private static final String SERVICE_NAME = "docsconv";
    private final Logger logger = LoggerFactory.getLogger(Docsconv.class);

    @ApplicationLogger
    protected AppLogs appLogs;

    @ServiceConfiguration
    private Json properties;

    private DocumentConverterService converterService;


    public void serviceStarted() {
        logger.info(String.format("Initializing service [%s]", SERVICE_NAME));
        appLogs.info(String.format("Initializing service [%s]", SERVICE_NAME));

        // TODO init libre office service
        converterService = new DocumentConverterService(appLogs);
        try {
            converterService.init();
        } catch (Exception e) {
            logger.error(String.format("Error initializing converter for service [%s]", SERVICE_NAME), e);
        }

        logger.debug(String.format("Properties [%s] for service [%s]", properties.toPrettyString(), SERVICE_NAME));
        logger.info(String.format("Configured service [%s]", SERVICE_NAME));
    }

    @ServiceFunction(name = "convertDocument")
    public Json convertDocument(FunctionRequest request) {
        logger.info("Creating pdf from template");
        Json data = request.getJsonParams();
        //converterService.placeConversionTask(input, output);
        Json resp = Json.map();
        return resp;
    }
}
