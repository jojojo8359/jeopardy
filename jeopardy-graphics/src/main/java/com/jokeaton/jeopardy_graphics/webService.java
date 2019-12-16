package main.java.com.jokeaton.jeopardy_graphics;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an API wrapper for the JService.io API. (http://jservice.io/)
 * Every endpoint of the API has its respective method with the proper parameters for each call.
 * @author Joel Keaton
 * @version 1.0.1
 */
public class webService {
    public webService() {}

//    private static Logger logger = LogManager.getLogger(webService.class);

    /**
     * Represents the /api/clues endpoint of the API.
     * Fetches a list of clues based on certain search criteria.
     * @param value the value of the clue in dollars
     * @param category the id of the category you want to return
     * @param offset offsets the returned clues. Useful in pagination
     * @return JSON data fetched from endpoint
     * @throws IOException caused by get method
     */
    public static JSONArray clues(int value, int category, int offset) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        if(value > 0) {
            parameters.put("value", Integer.toString(value));
        }
        if(category > 0) {
            parameters.put("category", Integer.toString(category));
        }
        if(offset > 0) {
            parameters.put("offset", Integer.toString(offset));
        }
        return new JSONArray(get("http://jservice.io/api/clues", getParamsString(parameters)));
    }

    /**
     * Represents the /api/random endpoint of the API.
     * Generates COUNT number of clues.
     * @param count amount of clues to return, limited to 100 at a time
     * @return JSON data fetched from endpoint
     * @throws IOException caused by get method
     */
    public static JSONArray random(int count) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        if(count > 0) {
            parameters.put("count", Integer.toString(count));
        }
        return new JSONArray(get("http://jservice.io/api/random", getParamsString(parameters)));
    }

    /**
     * Represents the /api/categories endpoint of the API.
     * Fetches a list of clue categories based on search criteria.
     * @param count amount of categories to return, limited to 100 at a time
     * @param offset offsets the starting id of categories returned. Useful in pagination.
     * @return JSON data fetched from endpoint
     * @throws IOException caused by get method
     */
    public static JSONArray categories(int count, int offset) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        if(count > 0) {
            parameters.put("count", Integer.toString(count));
        }
        if(offset > 0) {
            parameters.put("offset", Integer.toString(offset));
        }
        return new JSONArray(get("http://jservice.io/api/categories", getParamsString(parameters)));
    }

    /**
     * Represents the /api/category endpoint of the API.
     * Fetches a category's data given a specific id.
     * @param id the ID of the category to return
     * @return JSON data fetched from endpoint
     * @throws IOException caused by get method
     */
    public static JSONObject category(int id) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        if(id > 0) {
            parameters.put("id", Integer.toString(id));
        }
        return new JSONObject(get("http://jservice.io/api/category", getParamsString(parameters)));
    }

    /**
     * Basic HTTP GET method.
     * @param desturl URL to send request to
     * @param query parameters to add to the end of the URL (not headers)
     * @return data returned from request
     * @throws IOException caused by url.openConnection()
     */
    public static String get(String desturl, String query) throws IOException {
        URL url = new URL(desturl + "?" + query);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
//        logger.debug("   GET " + url + " " + status);
        Reader streamReader = null;
        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        }
        else {
            streamReader = new InputStreamReader(con.getInputStream());
        }

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuffer content = new StringBuffer();
        while((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    /**
     * Formats the parameters string for the URL.
     * @param params a map of parameters to values
     * @return ?param=value&param=value...
     * @throws UnsupportedEncodingException caused by encode()
     */
    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
