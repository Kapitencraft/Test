package net.kapitencraft.lang.exception.runtime;

public class MissingVarException extends RuntimeException {
    private final String name;

    public MissingVarException(String name) {
        this.name = name;
    }
}
