package languageHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Shelf {
    private final HashMap<String, TextSpace> shelf;
    private String name;

    public Shelf(String name) {
    	this.name = name;
        this.shelf = new HashMap<>();
    }

    public String getName() {
    	return name;
    }

    public void put(String key, TextSpace info) {
        shelf.put(key, info);
    }

    public TextSpace getDictionary(String key) {
        return shelf.get(key);
    }

    public boolean containsKey(String key) {
        return shelf.containsKey(key);
    }

    public List<TextSpace> getAllTextSpace() {
        return new ArrayList<>(shelf.values());
    }

    public void clear() {
        shelf.clear(); // This clears the actual map
    }

    public List<TextSpace> values() {
        return new ArrayList<>(shelf.values()); // Same as getAllTextSpace(); you can remove one
    }
}