package br.angarion.dev.engine.communication.validator;

public record Denied(int reasonCode) implements ValidationResult {}
