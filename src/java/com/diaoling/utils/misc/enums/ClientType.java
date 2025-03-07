package com.diaoling.utils.misc.enums;



/**
 * @author DiaoLing
 * @since 4/5/2024
 */
public enum ClientType {
    EMPTY("Empty"),
    LAVENDER("Lavender"),
    Kura("Kura"),
    Rebirth("Rebirth"),
    Artist("Artist"),
    NEVER("Never");
    private final String name;

    ClientType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
