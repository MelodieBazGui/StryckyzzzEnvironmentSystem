package parametres;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import utils.Logger;

public abstract class Parametres {
    private static final Logger logger = new Logger(Parametres.class);
    private static boolean gsonAvailable;

    static {
        try {
            Class.forName("com.google.gson.Gson");
            gsonAvailable = true;
        } catch (ClassNotFoundException e) {
            gsonAvailable = false;
        }
    }

    public abstract String getName();

    public void load() {
        Path path = Paths.get("data/parametres/" + getName() + ".json");
        if (Files.notExists(path)) {
            logger.info("No config found, creating default: " + path);
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String json = reader.lines().reduce("", (a, b) -> a + b);

            if (gsonAvailable) {
                try {
                    Object gson = Class.forName("com.google.gson.Gson").getDeclaredConstructor().newInstance();
                    Object loaded = gson.getClass()
                            .getMethod("fromJson", String.class, Class.class)
                            .invoke(gson, json, this.getClass());
                    if (loaded != null) copyFrom((Parametres) loaded);
                    logger.info("Loaded parameters using Gson for " + getName());
                    return;
                } catch (Exception e) {
                    logger.warn("Gson available but failed to parse JSON, falling back.");
                }
            }

            // Fallback: minimal JSON parser (flat fields only)
            json = json.trim().replaceAll("[{}\"]", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                if (!pair.contains(":")) continue;
                String[] kv = pair.split(":");
                String key = kv[0].trim();
                String value = kv[1].trim();
                try {
                    Field field = this.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    if (type == int.class) field.setInt(this, Integer.parseInt(value));
                    else if (type == boolean.class) field.setBoolean(this, Boolean.parseBoolean(value));
                    else if (type == double.class) field.setDouble(this, Double.parseDouble(value));
                    else field.set(this, value);
                } catch (Exception ignored) {}
            }
            logger.info("Loaded parameters using fallback parser for " + getName());
        } catch (IOException e) {
            logger.error("Error reading parameters file: " + path, e);
        }
    }

    public void save() {
        Path dir = Paths.get("data/parametres/");
        Path file = dir.resolve(getName() + ".json");

        try {
            Files.createDirectories(dir);
            if (gsonAvailable) {
                try {
                    Object gson = Class.forName("com.google.gson.GsonBuilder")
                            .getDeclaredConstructor().newInstance();
                    gson = gson.getClass().getMethod("setPrettyPrinting").invoke(gson);
                    Object gsonObj = gson.getClass().getMethod("create").invoke(gson);
                    String json = (String) gsonObj.getClass().getMethod("toJson", Object.class).invoke(gsonObj, this);

                    try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                        writer.write(json);
                    }
                    logger.info("Saved parameters using Gson to " + file);
                    return;
                } catch (Exception e) {
                    logger.warn("Gson save failed, falling back.");
                }
            }

            // Fallback save (flat JSON only)
            Map<String, Object> fields = new LinkedHashMap<>();
            for (Field f : this.getClass().getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    fields.put(f.getName(), f.get(this));
                }
            }

            StringBuilder json = new StringBuilder("{\n");
            for (var entry : fields.entrySet()) {
                json.append("  \"").append(entry.getKey()).append("\": ");
                Object v = entry.getValue();
                if (v instanceof Number || v instanceof Boolean)
                    json.append(v);
                else
                    json.append("\"").append(v).append("\"");
                json.append(",\n");
            }
            if (json.lastIndexOf(",") > 0)
                json.deleteCharAt(json.lastIndexOf(","));
            json.append("}\n");

            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write(json.toString());
            }
            logger.info("Saved parameters using fallback writer to " + file);

        } catch (Exception e) {
            logger.error("Error saving parameters: " + getName(), e);
        }
    }

    protected void copyFrom(Parametres other) {
        try {
            for (Field f : this.getClass().getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    f.set(this, f.get(other));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to copy parameters for " + getName());
        }
    }
}
