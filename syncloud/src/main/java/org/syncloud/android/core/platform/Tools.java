package org.syncloud.android.core.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.android.core.common.SyncloudException;
import org.syncloud.android.core.platform.model.Identification;
import org.syncloud.android.core.common.Result;
import org.syncloud.android.core.common.WebService;

import java.io.IOException;

import static java.lang.String.format;

public class Tools {

    private static Logger logger = Logger.getLogger(Tools.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    private WebService webService;

    public Tools(WebService webService) {
        this.webService = webService;
    }

    public Optional<Identification> getId(String host) {
        String json;
        try {
            json = webService.execute("GET", format("http://%s:81/server/rest/id", host));
        } catch (SyncloudException e) {
            String message = "Unable to get identification response";
            logger.error(message, e);
            return Optional.absent();
        }

        try {
            Result<Identification> result = JSON.readValue(json, new TypeReference<Result<Identification>>() {});
            return Optional.of(result.data);
        } catch (IOException e) {
            String message = "Unable to parse identification response";
            logger.error(message+" "+json, e);
            return Optional.absent();
        }
    }
}
