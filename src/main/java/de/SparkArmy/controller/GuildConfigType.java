package de.SparkArmy.controller;

@SuppressWarnings("unused")
public enum GuildConfigType {
    MAIN(1,"config.json","The main guild config"),
    KEYWORDS(2,"highlighted-keywords.json","Keywords from guild"),
    RULES(3,"rules.json","Rules from guild");


    private final String name;
    private final Integer id;
    private final String description;

    GuildConfigType(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
