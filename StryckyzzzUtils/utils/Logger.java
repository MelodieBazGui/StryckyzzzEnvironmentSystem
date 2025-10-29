package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Structured Logger with automatic cleanup and formatted output.
 * 
 * Folder structure:
 * logs/<package>/<subpackage>/ClassName_log.txt
 * 
 * Example:
 * logs/ecs/systems/EntityManager_log.txt
 */
public class Logger {
    private final String classTag;
    private final File logFile;

    // ANSI Colors for console
    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";

    // Timer tracking
    private final Map<String, Long> timers = new HashMap<>();

    // --- CONFIGURATION ---
    private static final boolean CLEAR_ON_START = true;     // clean log file each run
    private static final boolean ROTATE_ON_START = false;   // rename old log to timestamped file instead of deleting

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public Logger(Class<?> clazz) {
        this.classTag = clazz.getSimpleName();
        this.logFile = createStructuredLogFile(clazz);
        prepareLogFile();
        writeSessionHeader();
    }

    // -------------------------------------------------------------------------
    // File setup
    // -------------------------------------------------------------------------
    private File createStructuredLogFile(Class<?> clazz) {
        // Root "logs" folder
        File baseDir = new File(System.getProperty("user.dir"), "logs");

        // Build folder hierarchy from package name
        String pkg = clazz.getPackageName();
        File pkgDir = new File(baseDir, pkg.replace('.', File.separatorChar));

        if (!pkgDir.exists() && !pkgDir.mkdirs()) {
            System.err.println("Logger: could not create directory " + pkgDir.getAbsolutePath());
        }

        // Log file
        return new File(pkgDir, clazz.getSimpleName() + "_log.txt");
    }

    /** Optionally clears or rotates log file. */
    private void prepareLogFile() {
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Logger: failed to create log file: " + e.getMessage());
            }
            return;
        }

        if (ROTATE_ON_START) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File rotated = new File(logFile.getParent(), logFile.getName().replace(".txt", "_" + timestamp + ".txt"));
            if (logFile.renameTo(rotated)) {
                System.out.println("[Logger] Rotated old log: " + rotated.getName());
            }
        } else if (CLEAR_ON_START) {
            try (PrintWriter writer = new PrintWriter(logFile)) {
                writer.print(""); // clear file
            } catch (FileNotFoundException e) {
                System.err.println("Logger: failed to clear log file: " + e.getMessage());
            }
        }
    }

    /** Adds a session header at the top of the log for clarity. */
    private void writeSessionHeader() {
        String header = "\n" +
                "============================================================\n" +
                "  LOG SESSION START  (" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ")\n" +
                "  CLASS: " + classTag + "\n" +
                "============================================================\n";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(header);
        } catch (IOException ignored) {}
    }

    // -------------------------------------------------------------------------
    // Timer API
    // -------------------------------------------------------------------------
    public void startTimer(String name) {
        timers.put(name, System.nanoTime());
        info("⏱ Timer started: " + name);
    }

    public void endTimer(String name) {
        Long start = timers.remove(name);
        if (start == null) {
            warn("Timer '" + name + "' was never started");
            return;
        }
        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
        info(String.format("⏱ Timer '%s' finished in %.3f ms", name, elapsedMs));
    }

    // -------------------------------------------------------------------------
    // Public Logging API
    // -------------------------------------------------------------------------
    public void info(String msg) { log(LogLevel.INFO, msg, null, null); }
    public void info(String msg, Object obj) { log(LogLevel.INFO, msg, obj, null); }
    public void warn(String msg) { log(LogLevel.WARN, msg, null, null); }
    public void warn(String msg, Object obj) { log(LogLevel.WARN, msg, obj, null); }
    public void error(String msg, Throwable t) { log(LogLevel.ERROR, msg, null, t); }
    public void error(String msg, Object obj, Throwable t) { log(LogLevel.ERROR, msg, obj, t); }

    // -------------------------------------------------------------------------
    // Core Logging Logic
    // -------------------------------------------------------------------------
    private synchronized void log(LogLevel level, String msg, Object obj, Throwable t) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String objTag = (obj != null)
                ? String.format("[%s@%08x]", obj.getClass().getSimpleName(), System.identityHashCode(obj))
                : "";

        String entry = String.format("%s | %-5s | %-15s | %s %s",
                timestamp, level, classTag, msg, objTag);

        // --- Console Output (colorized) ---
        switch (level) {
            case INFO  -> System.out.println(GREEN  + entry + RESET);
            case WARN  -> System.out.println(YELLOW + entry + RESET);
            case ERROR -> {
                System.out.println(RED + entry + RESET);
                if (t != null) t.printStackTrace(System.out);
            }
            default -> System.out.println(entry);
        }

        // --- Write to file (plain, formatted) ---
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(entry);
            writer.newLine();

            if (t != null) {
                writer.write("    Exception: " + t);
                writer.newLine();
                for (StackTraceElement el : t.getStackTrace()) {
                    writer.write("        at " + el);
                    writer.newLine();
                }
            }
        } catch (IOException io) {
            System.err.println("Logger file write failed: " + io.getMessage());
        }
    }
}
