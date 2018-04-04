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

    private static Map<Integer, LogLevel> lookUpMap = new HashMap<>();
    static{
        int index = 0;
        for (LogLevel logLevel : EnumSet.allOf(LogLevel.class)) {
            lookUpMap.put(index++, logLevel);
        }
    }

    public static LogLevel getLogLevel(int key) {
        return lookUpMap.get(key);
    }
}
