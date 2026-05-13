package br.angarion.dev.api.communication;

public enum ValidatorType {
    SUCCESS(0),
    ERROR(1),
    PARTIAL(2)

    private final int id;

    public ValidatorType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}