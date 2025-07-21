package languageHandlers;

import java.io.File;

public class LanguageManager {

    public static File getAvailableLanguageCodes() {
        File file = new File(System.getProperty("user.dir") + File.separator + "languageHandling" + File.separator + "lang" + File.separator);
		return file;
    }
}



