package main.java.com.jokeaton.jeopardy;

import org.json.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Provides a data structure for a Jeopardy category.
 * @author Joel Keaton
 */
public class Category extends Space {
    private int clues_count;
    private int id;
    private String title;
    private ArrayList<Clue> clues;

    /**
     * Constructor for a category.
     * @param clues_count how many clues are in the category
     * @param id jservice's category id
     * @param title title of the category
     */
    public Category(int clues_count, int id, String title) {
        this.clues_count = clues_count;
        this.id = id;
        this.title = title;
    }

    /**
     * Constructor for a category, given a clue list for the category. (Meant to be used with the /api/category
     * endpoint)
     * @param clues_count how many clues are in the category
     * @param id jservice's category id
     * @param title title of the category
     * @param clues clues in the category
     */
    public Category(int clues_count, int id, String title, ArrayList<Clue> clues) {
        this.clues_count = clues_count;
        this.id = id;
        this.title = title;
        this.clues = clues;
    }

    /**
     * Getter method for the number of clues in the category
     * @return how many clues are in the category
     */
    public int getCluesCount() {
        return this.clues_count;
    }

    /**
     * Getter method for the category's id
     * @return jservice's category id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Getter method for the category's title
     * @return title of the category
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Getter method for the clues in the category
     * @return clues in the category
     */
    public ArrayList<Clue> getClues() {
        return this.clues;
    }

    /**
     * Creates a category object from a JSONObject.
     * @param category the JSONObject containing the category data
     * @return category object
     */
    public static Category categoryFromObject(JSONObject category) {
        int clues_count = category.getInt("clues_count");
        int id = category.getInt("id");
        String title = category.getString("title");
        return new Category(clues_count, id, title);
    }

    /**
     * Creates a category object from a JSONObject that contains clues in the category. (Meant to be used with the
     * /api/category endpoint)
     * @param category the JSONObject containing the category data, including clues
     * @return category object
     */
    public static Category categoryFromObjectClues(JSONObject category) {
        int clues_count = category.getInt("clues_count");
        int id = category.getInt("id");
        String title = category.getString("title");
        ArrayList<Clue> clues = Clue.cluesFromArrayNoCat(category.getJSONArray("clues"));
        return new Category(clues_count, id, title, clues);
    }

    /**
     * Creates an ArrayList of category objects from a JSONArray, utilizing the categoryFromObject method.
     * @param categories the JSONArray containing multiple JSONObjects with category data
     * @return multiple category objects
     */
    public static ArrayList<Category> categoriesFromArray(JSONArray categories) {
        ArrayList<Category> categoriesList = new ArrayList<>();
        for(int i = 0; i < categories.length(); i++) {
            categoriesList.add(categoryFromObject(categories.getJSONObject(i)));
        }
        return categoriesList;
    }

    /**
     * Gets and parses data from the /api/categories endpoint.
     * @param count amount of categories to return, limited to 100 at a time
     * @param offset offsets the starting id of categories returned. Useful in pagination.
     * @return multiple category objects fetched from the endpoint
     * @throws IOException
     */
    public static ArrayList<Category> getCategories(int count, int offset) throws IOException {
        return categoriesFromArray(webService.categories(count, offset));
    }

    /**
     * Gets and parses data from the /api/category endpoint.
     * @param id the ID of the category to return
     * @return multiple category objects fetched from the endpoint
     * @throws IOException
     */
    public static Category getCategory(int id) throws IOException {
        return categoryFromObjectClues(webService.category(id));
    }

    public String toString() {
        return this.getTitle().toUpperCase() + " (" + this.getId() + ")";
    }
}
