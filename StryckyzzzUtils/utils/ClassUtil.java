package utils;

import java.io.File;
import java.net.URISyntaxException;

public class ClassUtil {

    public static String getAbsolutePathOfClass(Class<?> cls) {
        try {
            File file = new File(cls.getProtectionDomain()
                                     .getCodeSource()
                                     .getLocation()
                                     .toURI());
            return file.getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<? extends Object> getClassName() {
        try {
            String className = Thread.currentThread()
                                     .getStackTrace()[2]
                                     .getClassName();
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}