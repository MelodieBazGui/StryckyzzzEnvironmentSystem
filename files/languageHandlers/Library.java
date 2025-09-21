package languageHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Library {
    private HashMap<String, Shelf> library;
    private String lang;

    public Library(String lang) {
        library = new HashMap<>();
        this.lang = lang;
    }

	public String getLang() {
    	return lang;
    }
    
    public void put(String key, Shelf shelf) {
        library.put(key, shelf);
    }

    public Shelf getShelf(String key) {
        return library.get(key);
    }

    public List<Shelf> getAllShelves() {
        return new ArrayList<>(library.values());
    }

    public List<TextSpace> getAllDictionaries() {
        List<TextSpace> result = new ArrayList<>();
        for (Shelf shelf : library.values()) {
            result.addAll(shelf.getAllTextSpace());
        }
        return result;
    }

    public void clear() {
        for (Shelf shelf : library.values()) {
            shelf.clear();
        }
        library.clear();
    }
}