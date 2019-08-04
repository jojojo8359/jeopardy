package main.java.com.jokeaton.jeopardy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class timeParser {
    public timeParser() {}

    public static String convert(String raw) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = format.parse(raw);
        DateFormat newformat = new SimpleDateFormat("LLLL dd, yyyy");
        return newformat.format(date);
    }
}
