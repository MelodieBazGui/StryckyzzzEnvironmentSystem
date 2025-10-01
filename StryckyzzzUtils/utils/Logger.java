package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Aggressive Logger with class-based tagging and colored console output.
 * Each log line can include the specific object instance for readability.
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

    // Convenience overloads so callers can attach the concrete object
    public void info(String msg) {
        log(LogLevel.INFO, msg, null, null);
    }

    public void info(String msg, Object obj) {
        log(LogLevel.INFO, msg, obj, null);
    }

    public void warn(String msg) {
        log(LogLevel.WARN, msg, null, null);
    }

    public void warn(String msg, Object obj) {
        log(LogLevel.WARN, msg, obj, null);
    }

    public void error(String msg, Throwable t) {
        log(LogLevel.ERROR, msg, null, t);
    }

    public void error(String msg, Object obj, Throwable t) {
        log(LogLevel.ERROR, msg, obj, t);
    }

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
                if (t != null) t.printStackTrace(System.out);
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
