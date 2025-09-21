package languageHandlers;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import appWindow.Application;
import basicHandler.DataHandler;
import utils.LogLevel;
import utils.Logger;

/**
 * Language loader Class, provides language handling from localization files
 * <p>
 * Localization files are grouped by their corresponding class
 * @author Stryckoeurzzz "Emejay Bazeries Guilbault"
 */
public class LanguageLoader extends DataHandler {
    private final Library translations;
    private final List<TextSpace> txtspaces;
    private final Logger logger = new Logger(this.getClass());
    private final String langDirectory = 
    		super.DATALOC +
    		File.separator+"lang"+
    		File.separator; 
    private List<String> langs = new ArrayList<String>();
    public File langDir = new File(langDirectory);
    
    public LanguageLoader() {
        this.translations = new Library(Application.defaultLang);
        this.txtspaces = new ArrayList<>();
        logger.log(LogLevel.INFO, "Initializing LanguageLoader...", null);
        loadLanguage();
        listLangs(langDir);
    }
    
    public void loadLanguage() {
    	if(translations.getLang() != Application.getDefaultLang()) {
            translations.clear();
    	}
        logger.logInfo("Attempting to load language: " + Application.getDefaultLang());
        
        logger.logInfo("Exploring : " + langDir.getAbsolutePath());
        //listFiles(langDir);
        //i have no idea when i added that into a commentary and what i wanted to do with it
        if(langDir.exists() && langDir.isDirectory()) {
        	exploreLang(langDir);
        } else {
        	logger.logError("Could not find lang Directory", LangException.MissingDirectoryFile.get());
        }
        logger.logInfo("Finished exploring lang files and loading them");
        reloadAllLanguageMethods();
    }
    
    private void listLangs(File dir) {
    	for(File f : dir.listFiles()) {
    		logger.logInfo("Exploring directory : " + dir.getName());
    		if(f.isDirectory()) {
    			logger.logInfo("Recursive, found directory : " + f.getName());
    			listLangs(f);
    		} else if (!langs.contains(f.getName())){
    			logger.logInfo("Adding " + f.getName() + " as possible language option");
    			langs.add(f.getName().substring(0, 5));
    		}
    	}
    };

    private void exploreLang(File f) {
    	if (f.isDirectory()){
			for (File f1 : f.listFiles()) {
				logger.logInfo("Exploring lang directory : " + f.getName());
				exploreLang(f1);
			}
		} else if (f.isFile() && f.getName().equals(Application.getDefaultLang())) {
    		loadLanguageFile(f);
    	}
    }
    
    private void loadLanguageFile(File file) {
        logger.logInfo("Loading language file: " + file.getName());
        if(file.isDirectory()) {
        	for( File f : file.listFiles()) {
        		loadLanguageFile(f);
        	}
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int typesep = line.indexOf('.');
                int textsep = line.indexOf('=');

                if (typesep != -1 && textsep != -1 && typesep < textsep) {
                    String type = line.substring(0, typesep).trim();
                    String key = line.substring(typesep + 1, textsep).trim();
                    String value = line.substring(textsep + 1).trim();

                    Shelf shelf = translations.getShelf(type);
                    if (shelf == null) {
                        shelf = new Shelf(type);
                        translations.put(type, shelf);
                        logger.logInfo("Loaded shelf: " + shelf.getName());
                    }

                    if (!shelf.containsKey(key)) {
                        TextSpace txtspace = new TextSpace(value);
                        txtspaces.add(txtspace);
                        shelf.put(key, txtspace);
                        logger.logInfo("Loaded entry: " + type + "." + key + " = " + value);
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            logger.logError("IOException while reading language file: " + file.getAbsolutePath(), e);
        }
    }


    private void reloadAllLanguageMethods() {
        logger.logInfo("Reloading language across all STAs...");
        Application.reloadLanguage();
        logger.logDuration(logger.getFileName());
    }
    
    public String getSingle(String compositeKey) {
        int dotIndex = compositeKey.indexOf('.');
        if (dotIndex == -1) {
            logger.logError("Invalid key format: " + compositeKey + ". Expected format: type.key", LangException.InvalidFormatKey.get());
            return null;
        }

        String type = compositeKey.substring(0, dotIndex);
        String key = compositeKey.substring(dotIndex + 1);
        
        Shelf shelf = translations.getShelf(type);
        if (shelf == null) {
            logger.logError("No shelf found for type: " + type, LangException.InvalidShelf.get());
            return null;
        }

        TextSpace textSpace = shelf.getDictionary(key);
        if (textSpace == null) {
            logger.logError("No dictionary entry for key: " + key + " in type: " + type, LangException.InvalidTextSpace.get());
            return new String(type+"."+key);
        }
        logger.logInfo("Could not find key " + key);
        return textSpace.getTextSpace();
    }

	public List<String> getLangs() {
		Set<String> seen = new HashSet<>();
		List<String> result = langs.stream()
		    .filter(seen::add)
		    .collect(Collectors.toList());
		return result;
	}
    
}