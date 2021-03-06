package application.logcat.filter;

import application.utilities.XMLUtil;

public class Filter {
    private String filterName;
    private String applicationName;
    private String PID;
    private String logMessage;
    private String logTag;
    private LogLevel logLevel;
    private String searchText;
    private LogLevel logLevel2;

    public Filter(String searchText, LogLevel logLevel2) {
        this.filterName = "";
        this.applicationName = "";
        this.PID = "";
        this.logMessage = "";
        this.logTag = "";
        this.logLevel = LogLevel.NONE;
        this.searchText = searchText;
        this.logLevel2 = logLevel2;
    }

    public Filter(Filter filter) {
        this(filter.getFilterName(),
                filter.getApplicationName(),
                filter.getPID(),
                filter.getLogMessage(),
                filter.getLogTag(),
                filter.getLogLevelOrdinal());
    }

    public Filter(String filterName, String applicationName, String PID, String logMessage, String logTag, int level) {
        this.filterName = filterName;
        this.applicationName = applicationName;
        this.PID = PID;
        this.logMessage = logMessage;
        this.logTag = logTag;
        this.logLevel = LogLevel.getLogLevel(level);
    }

    public void setLogLevel2(LogLevel logLevel2) {
        this.logLevel2 = logLevel2;
    }

    public void setSearchText(String text) {
        this.searchText = text;
    }

    public void save() {
        new XMLUtil(false).saveFilter(this);
    }

    public static Filter getFilter(String name) {
        return new XMLUtil(false).openFilter(name);
    }

    public String getLogLevel2() {
        return logLevel2 == null ? "" : logLevel2.toString();
    }

    public String getSearchText() {
        return searchText;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public String getLogTag() {
        return logTag;
    }

    public String getLogLevel() {
        return logLevel == null ? "" : logLevel.toString();
    }

    public int getLogLevelOrdinal() {
        return logLevel == null ? 6 : logLevel.ordinal();
    }

    public String getPID() {
        return PID;
    }

    @Override
    public String toString() {
        return "filterName: " + filterName + "\n"
                + "applicationName: " + applicationName  + "\n"
                + "PID " + PID + "\n"
                + "logMessage: " + logMessage + "\n"
                + "logTag: " + logTag + "\n"
                + "logLevel: " + (logLevel == null ? "NA" : logLevel.toString()) + "\n"
                + "searchText: " + searchText + "\n"
                + "logLevel2: " + (logLevel2 == null ? "NA" : logLevel2.toString());
    }
}


