package de.sparkarmy.misc;

public enum ErrorGroup {

    EMPTY(-1),
    GENERAL(0),
    CONFIG(100);

    private final int id;

    ErrorGroup(int id) {

        this.id = id;
    }

    public int getId() {
        return id;
    }
}
