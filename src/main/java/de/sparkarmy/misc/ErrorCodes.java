package de.sparkarmy.misc;

@SuppressWarnings("unused")
public enum ErrorCodes {

    GENERAL_CONFIG_ERROR(-100, ErrorTarget.General, ErrorGroup.CONFIG, "General config error"),
    GENERAL_CONFIG_CANT_CREATE_MAIN_CONFIG(-101, ErrorTarget.General, ErrorGroup.CONFIG, "Cant create a main-config-file"),
    GENERAL_CONFIG_IS_NULL(-102, ErrorTarget.General, ErrorGroup.CONFIG, "The main-config is null"),


    SQL_EXCEPTION(-1000, ErrorTarget.DATABASE, ErrorGroup.GENERAL, "General Database error"),
    SQL_CONFIG_URL_IS_EMPTY(-1110, ErrorTarget.DATABASE, ErrorGroup.CONFIG, "The Database-URL is empty"),
    SQL_CONFIG_USER_IS_EMPTY(-1111, ErrorTarget.DATABASE, ErrorGroup.CONFIG, "The Database-User is empty"),
    SQL_CONFIG_PASSWORD_IS_EMPTY(-1112, ErrorTarget.DATABASE, ErrorGroup.CONFIG, "The Database-Password is empty"),

    SQL_UPDATE_PRECONDITION_FAILED(-1200, ErrorTarget.DATABASE, ErrorGroup.EMPTY, "A precondition from the update failed"),

    SQL_QUERY_PRECONDITION_FAILED(-1310, ErrorTarget.DATABASE, ErrorGroup.EMPTY, "A precondition from the query failed"),
    SQL_QUERY_SELECT_COUNT_NO_ROW(-1311, ErrorTarget.DATABASE, ErrorGroup.EMPTY, "A Select Count() has no result row"),
    SQL_QUERY_SELECT_HAS_NO_ROW(-1312, ErrorTarget.DATABASE, ErrorGroup.EMPTY, "A Select statement has no row"),


    FILEHANDLER_GENERAL_ERROR(2000, ErrorTarget.FILEHANDLER, ErrorGroup.GENERAL, "General FileHandler error"),
    FILEHANDLER_CANT_CREATE_DIRECTORY(2010, ErrorTarget.FILEHANDLER, ErrorGroup.GENERAL, "Cant create a directory"),
    FILEHANDLER_CANT_READ_DIRECTORY(2011, ErrorTarget.FILEHANDLER, ErrorGroup.GENERAL, "Cant read a directory"),
    FILEHANDLER_CANT_CREATE_FILE(2020, ErrorTarget.FILEHANDLER, ErrorGroup.GENERAL, "Cant create a file"),
    FILEHANDLER_CANT_READ_FILE(2021, ErrorTarget.FILEHANDLER, ErrorGroup.GENERAL, "Cant read a file"),

    ;
    private final int id;
    private final ErrorTarget target;
    private final ErrorGroup group;
    private final String description;

    ErrorCodes(int id, ErrorTarget target, ErrorGroup group, String description) {

        this.id = id;
        this.target = target;
        this.group = group;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public ErrorGroup getGroup() {
        return group;
    }

    public ErrorTarget getTarget() {
        return target;
    }

    public int getId() {
        return id;
    }
}
