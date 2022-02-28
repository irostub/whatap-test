package com.irostub.filedb;

public enum Mode {
    READ("r"), WRITE("rw");

    private final String property;

    Mode(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
