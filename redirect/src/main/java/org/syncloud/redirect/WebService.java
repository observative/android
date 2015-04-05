package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.syncloud.redirect.model.RedirectApiException;
import org.syncloud.redirect.model.RedirectException;
import org.syncloud.redirect.model.RestResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static java.net.URLEncoder.encode;
import static org.syncloud.redirect.jackson.Jackson.createObjectMapper;

public class WebService {

    private static Logger logger = Logger.getLogger(RedirectService.class);

    private String apiUrl;

    public WebService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    private static ObjectMapper mapper = createObjectMapper();

    public String execute(String type, String url, List<NameValuePair> parameters) {
        HttpUriRequest request = request(type, apiUrl + url, parameters);
        Response response = getResponse(request);
        RestResult restResponse = null;
        try {
            restResponse = mapper.readValue(response.output, RestResult.class);
        } catch (IOException e) {
            String message = "Failed to deserialize json";
            logger.error(message+" "+response.output, e);
            throw new RedirectException(message);
        }
        restResponse.statusCode = response.statusCode;
        checkStatusCode(restResponse);
        return response.output;
    }

    private void checkStatusCode(RestResult rest) {
        if (rest.statusCode != 200) {
            String message = "Response has bad status code: "+rest.statusCode;
            logger.error(message);
            throw new RedirectApiException(message, rest);
        }
    }

    private class Response {
        public String output;
        public int statusCode;

        public Response(int statusCode, String output) {
            this.statusCode = statusCode;
            this.output = output;
        }
    }

    private Response getResponse(HttpUriRequest request) {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient http = HttpClients.createDefault();
            response = http.execute(request);
            InputStream jsonResponse = response.getEntity().getContent();
            String textJsonResponse = readText(jsonResponse);
            int statusCode = response.getStatusLine().getStatusCode();
            return new Response(statusCode, textJsonResponse);
        } catch (IOException e) {
            String message = "Failed to get response";
            logger.error("Failed to get response", e);
            throw new RedirectException(message);
        } finally {
            if (response != null)
                try { response.close(); } catch (IOException e) {}
        }
    }

    private static HttpUriRequest request(String type, String url, List<NameValuePair> parameters) {
        try {
            if (type.toUpperCase().equals("POST")) {
                HttpPost post = new HttpPost(url);
                post.setEntity(new UrlEncodedFormEntity(parameters));
                return post;
            }

            if (type.toUpperCase().equals("GET")) {
                String urlFull = url;
                for(NameValuePair pair: parameters) {
                    boolean first = pair == parameters.get(0);
                    if (first)
                        urlFull += "?";
                    else
                        urlFull += "&";
                    urlFull += pair.getName();
                    urlFull += "=";
                    urlFull += encode(pair.getValue(), "utf-8");
                }
                HttpGet get = new HttpGet(urlFull);
                return get;
            }
        } catch (UnsupportedEncodingException e) {
            String message = "Failed to form request";
            logger.error(message, e);
            throw new RedirectException(message);
        }
        String message = "Unknown request type "+type;
        logger.error(message);
        throw new RedirectException(message);
    }

    private static String readText(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }
}