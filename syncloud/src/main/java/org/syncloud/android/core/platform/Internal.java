package org.syncloud.android.core.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.syncloud.android.core.common.Result;
import org.syncloud.android.core.common.SyncloudException;
import org.syncloud.android.core.common.WebService;
import org.syncloud.android.core.platform.model.Identification;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.String.format;
import static org.syncloud.android.network.Helpers.checkUrl;

public class Internal {

    private static Logger logger = Logger.getLogger(Internal.class);
    public static final ObjectMapper JSON = new ObjectMapper();

    private String getRestUrl(String host) {
        //TODO: This is needed only for compatibility with releases prior 16.06. New rest URL should be used always.
        String newRestUrl = format("http://%s:81/rest", host);
        if (checkUrl(newRestUrl + "/id", 200))
            return newRestUrl;
        return format("http://%s:81/server/rest", host);
    }

    private WebService getRestWebService(String host) {
        return new WebService(getRestUrl(host));
    }

    public Optional<Identification> getId(String host) {
        WebService webService = getRestWebService(host);
        String json;
        try {
            json = webService.execute("GET", "/id");
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

    public Optional<Identification> activate(
            String host,
            String mainDomain,
            String redirectEmail,
            String redirectPassword,
            String userDomain,
            String deviceUsername,
            String devicePassword) {

        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("main_domain", mainDomain));
        parameters.add(new BasicNameValuePair("redirect_email", redirectEmail));
        parameters.add(new BasicNameValuePair("redirect_password", redirectPassword));
        parameters.add(new BasicNameValuePair("user_domain", userDomain));
        parameters.add(new BasicNameValuePair("device_username", deviceUsername));
        parameters.add(new BasicNameValuePair("device_password", devicePassword));

        WebService webService = getRestWebService(host);
        String json = webService.execute("POST", "/activate", parameters);

        try {
            Result<Identification> result = JSON.readValue(json, new TypeReference<Result<Identification>>() {});
            return Optional.of(result.data);
        } catch (IOException e) {
            String message = "Unable to parse activate response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }
}
