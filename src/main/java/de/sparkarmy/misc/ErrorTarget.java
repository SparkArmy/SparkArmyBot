package de.sparkarmy.misc;

public enum ErrorTarget {
    General(0),
    DATABASE(1000),

    FILEHANDLER(2000),
    ;

    private final int id;

    ErrorTarget(int id) {

        this.id = id;
    }

    public int getId() {
        return id;
    }
}

