package main.java.com.jokeaton.jeopardy;

import org.json.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Provides a data structure for a Jeopardy clue.
 * @author Joel Keaton
 */
public class Clue extends Space {
    private String answer;
    private String question;
    private String airdate;
    private int invalid_count;
    private int id;
    private Category category;
    private int value;
    private boolean answered;

    /**
     * Constructor for a clue, given a valid category structure the clue belongs to.
     * @param answer answer to the clue's question
     * @param question clue's question
     * @param airdate the airdate of the clue, in format YYYY-MM-DDTHH:MMZ (according to ISO 8601)
     * @param invalid_count how many times the clue has been marked as invalid (missing data)
     * @param id jservice's clue id
     * @param category category structure provided
     * @param value dollar value of the clue
     */
    public Clue(String answer, String question, String airdate, int invalid_count, int id, Category category, int value) {
        this.answer = answer;
        this.question = question;
        this.airdate = airdate;
        this.invalid_count = invalid_count;
        this.id = id;
        this.category = category;
        this.value = value;
        this.answered = false;
    }

    /**
     * Constructor for a clue, NOT given a valid category structure (mainly for when using the /api/category endpoint,
     * since this endpoint gives the category data separate from the clues).
     * @param answer answer to the clue's question
     * @param question clue's question
     * @param airdate the airdate of the clue, in format YYYY-MM-DDTHH:MMZ (according to ISO 8601)
     * @param invalid_count how many times the clue has been marked as invalid (missing data)
     * @param id jservice's clue id
     * @param value dollar value of the clue
     */
    public Clue(String answer, String question, String airdate, int invalid_count, int id, int value) {
        this.answer = answer;
        this.question = question;
        this.airdate = airdate;
        this.invalid_count = invalid_count;
        this.id = id;
        this.value = value;
        this.answered = false;
    }

    /**
     * Getter method for the clue's answer
     * @return answer to the clue's question
     */
    public String getAnswer() {
        return this.answer;
    }

    /**
     * Getter method for the clue's question
     * @return clue's question
     */
    public String getQuestion() {
        return this.question;
    }

    /**
     * Getter method for the airdate of the clue
     * @return airdate of the clue, in format YYYY-MM-DDTHH:MMZ (according to ISO 8601)
     */
    public String getAirdate() {
        return this.airdate;
    }

    /**
     * Getter method for invalid count of the clue
     * @return how many times the clue has been marked as invalid (missing data)
     */
    public int getInvalidCount() {
        return this.invalid_count;
    }

    /**
     * Getter method for the clue's id
     * @return jservice's clue id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Getter method for the clue's category info
     * @return category structure provided
     */
    public Category getCategory() {
        return this.category;
    }

    /**
     * Getter method for the clue's money value
     * @return dollar value of the clue
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Getter method for whether the clue has been answered or not
     * @return if the clue is answered
     */
    public boolean isAnswered() {
        return this.answered;
    }

    /**
     * Setter method for the clue's answered state
     * @param state the state of the clue to update to
     */
    public void setAnswered(boolean state) {
        this.answered = state;
    }

    /**
     * Creates a clue object from a JSONObject.
     * @param clue the JSONObject containing the clue data
     * @return clue object
     */
    public static Clue clueFromObject(JSONObject clue) {
        String answer = clue.getString("answer");
        String question = clue.getString("question");
        String airdate = clue.getString("airdate");
        int invalid_count;
        try {
            invalid_count = clue.getInt("invalid_count");
        } catch (org.json.JSONException e) {
            invalid_count = 0;
        }
        int id = clue.getInt("id");
        Category category = Category.categoryFromObject(clue.getJSONObject("category"));
        int value = clue.getInt("value");
        return new Clue(answer, question, airdate, invalid_count, id, category, value);
    }

    /**
     * Creates a clue object from a JSONObject. Meant to be used with the /api/category endpoint, since no category
     * data is given inside the clue.
     * @param clue the JSONObject containing the clue data (with no category data)
     * @return clue object
     */
    public static Clue clueFromCategoryObject(JSONObject clue) {
        String answer = clue.getString("answer");
        String question = clue.getString("question");
        String airdate = clue.getString("airdate");
        int invalid_count;
        try {
            invalid_count = clue.getInt("invalid_count");
        } catch (org.json.JSONException e) {
            invalid_count = 0;
        }
        int id = clue.getInt("id");
        int value = clue.getInt("value");
        return new Clue(answer, question, airdate, invalid_count, id, value);
    }

    /**
     * Creates an ArrayList of clue objects from a JSONArray, utilizing the clueFromObject method.
     * @param clues the JSONArray containing multiple JSONObjects with clue data
     * @return multiple clue objects
     */
    public static ArrayList<Clue> cluesFromArray(JSONArray clues) {
        ArrayList<Clue> cluesList = new ArrayList<>();
        for(int i = 0; i < clues.length(); i++) {
            cluesList.add(clueFromObject(clues.getJSONObject(i)));
        }
        return cluesList;
    }

    /**
     * Creates an ArrayList of clue objects (without category data) from a JSONArray, utilizing the
     * clueFromCategoryObject method. (Meant to be used with the /api/category endpoint)
     * @param clues the JSONArray containing multiple JSONObjects with clue data and no category data
     * @return multiple clue objects
     */
    public static ArrayList<Clue> cluesFromArrayNoCat(JSONArray clues) {
        ArrayList<Clue> cluesList = new ArrayList<>();
        for(int i = 0; i < clues.length(); i++) {
            cluesList.add(clueFromCategoryObject(clues.getJSONObject(i)));
        }
        return cluesList;
    }

    /**
     * Gets and parses data from the /api/clues endpoint.
     * @param value the value of the clue in dollars
     * @param category the id of the category you want to return
     * @param offset offsets the returned clues. Useful in pagination
     * @return multiple clue objects fetched from the endpoint
     * @throws IOException
     */
    public static ArrayList<Clue> getClues(int value, int category, int offset) throws IOException {
        return cluesFromArray(webService.clues(value, category, offset));
    }

    /**
     * Gets and parses data from the /api/random endpoint.
     * @param count amount of clues to return, limited to 100 at a time
     * @return multiple clue objects fetched from the endpoint
     * @throws IOException
     */
    public static ArrayList<Clue> getRandom(int count) throws IOException {
        return cluesFromArray(webService.random(count));
    }

    public String toString() {
        return "Q. " + this.getQuestion() + " A. " + this.getAnswer() + " $" + this.getValue();
    }
}
