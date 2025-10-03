package languageHandlers;

/**
 * Enum provided by Fromage as a refactor to my previous enum
 * <p>
 * Enum works so custom exceptions regarding LanguageHandlers correspond to errors raised by the code in a way to minimize the amount of code needed
 * <p>
 * Check out Fromage and her work
 * @author Fromage (Philou404 on GitHub)
 */
public enum LangException {
    MissingLanguageFile ("Missing Language File"),
    MissingDirectoryFile("No such Directory"),
    EmptyLanguageFile ("Missing language translations"),
    InvalidFormatKey ("Invalid FormatKey"),
    InvalidShelf("Invalid Shelf"),
    InvalidTextSpace("Invalid TextSpace");

    private final String text;

    /**
     * LangException constructor
     * @param t
     * @author Fromage
     */
    LangException(String t){
        text = t;
    }

    /**
     * Getter method to retrieve exception information as a regular Exception
     * <p>
     * Code provided by Fromage from the previous get Method
     * @return Exception
     * @author Stryckoeuzzz, Fromage
     */
    public Exception get() {
        return new Exception(text);
    }
}
