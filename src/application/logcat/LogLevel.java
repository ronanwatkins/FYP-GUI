package application.logcat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ASSERT,
    ERROR,
    NONE;

    private static Map<Integer, LogLevel> getLogLevelMap = new HashMap<>();
    private static Map<String, Integer> getOrdinalMap = new HashMap<>();

    static {
        int index = 0;
        for (LogLevel logLevel : EnumSet.allOf(LogLevel.class)) {
            System.out.println(logLevel + " " + index);

            getOrdinalMap.put(logLevel.toString(), index);
            getLogLevelMap.put(index++, logLevel);
        }
    }

    public static LogLevel getLogLevel(int key) {
        return getLogLevelMap.get(key);
    }

    public static int getOrdinal(String key) {
        System.out.println("getOrdinal>> key: " + key + ", " + getOrdinalMap.get(key));
        return getOrdinalMap.get(key);
    }
}
