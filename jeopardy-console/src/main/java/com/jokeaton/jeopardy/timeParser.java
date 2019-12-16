package main.java.com.jokeaton.jeopardy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Unused class to parse datetime in an ISO 8601 format
 * @author Joel Keaton
 * @version 1.0.1
 */

public class timeParser {
    /**
     * Converts an ISO 8601 time into month dd, yyyy
     * @param raw the raw ISO 8601 datetime string
     * @return the month dd, yyyy format
     * @throws ParseException caused by parse()
     */
    public static String convert(String raw) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = format.parse(raw);
        DateFormat newformat = new SimpleDateFormat("LLLL dd, yyyy");
        return newformat.format(date);
    }
}
