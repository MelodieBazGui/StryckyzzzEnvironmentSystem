package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggressive Logger with class-based tagging and colored console output.
 * Each log line can include the specific object instance for readability.
 * Supports timing utilities to measure method/algorithm execution.
 */
public class Logger {
    private final String classTag;
    private final File logFile;

    // ANSI Colors for console
    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";

    // Timers storage
    private final Map<String, Long> timers = new HashMap<>();

    public Logger(Class<?> c) {
        this.classTag = c.getSimpleName();
        this.logFile = createLogFile(c);
    }

    private File createLogFile(Class<?> c) {
        File baseDir = new File(System.getProperty("user.dir"), "logs");
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            System.err.println("Logger: could not create logs directory");
        }
        File file = new File(baseDir, c.getSimpleName() + "_log.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
        return file;
    }

    // --- Timing Methods ---
    /** Start a named timer */
    public void startTimer(String name) {
        timers.put(name, System.nanoTime());
        info("Timer started: " + name);
    }

    /** Stop a named timer and log elapsed time in ms */
    public void endTimer(String name) {
        Long start = timers.remove(name);
        if (start == null) {
            warn("Timer '" + name + "' was never started");
            return;
        }
        long elapsedNs = System.nanoTime() - start;
        double elapsedMs = elapsedNs / 1_000_000.0;
        info(String.format("Timer '%s' finished: %.3f ms", name, elapsedMs));
    }

    // --- Logging API ---
    public void info(String msg) { log(LogLevel.INFO, msg, null, null); }
    public void info(String msg, Object obj) { log(LogLevel.INFO, msg, obj, null); }
    public void warn(String msg) { log(LogLevel.WARN, msg, null, null); }
    public void warn(String msg, Object obj) { log(LogLevel.WARN, msg, obj, null); }
    public void error(String msg, Throwable t) { log(LogLevel.ERROR, msg, null, t); }
    public void error(String msg, Object obj, Throwable t) { log(LogLevel.ERROR, msg, obj, t); }

    private synchronized void log(LogLevel level, String msg, Object obj, Throwable t) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String objTag = "";
        if (obj != null) {
            objTag = String.format("[%s@%08x]", obj.getClass().getSimpleName(), System.identityHashCode(obj));
        }
        String entry = String.format("[%s] [%s] %s %s", timestamp, classTag, level, msg);
        String consoleLine = String.format("[%s] [%s] %s %s %s",
                timestamp, classTag, level, msg, objTag);

        // Console colorized
        switch (level) {
            case INFO  -> System.out.println(GREEN  + consoleLine + RESET);
            case WARN  -> System.out.println(YELLOW + consoleLine + RESET);
            case ERROR -> {
                System.out.println(RED + consoleLine + RESET);
                if (t != null) {
                    t.printStackTrace(System.out);
                }
            }
            default -> System.out.println(consoleLine);
        }

        // File (no colors, keep objTag)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(entry + " " + objTag);
            writer.newLine();
            if (t != null) {
                writer.write("Exception: " + t.toString());
                writer.newLine();
                for (StackTraceElement el : t.getStackTrace()) {
                    writer.write("    at " + el.toString());
                    writer.newLine();
                }
            }
        } catch (IOException io) {
            System.err.println("Logger file write failed: " + io.getMessage());
        }
    }
}