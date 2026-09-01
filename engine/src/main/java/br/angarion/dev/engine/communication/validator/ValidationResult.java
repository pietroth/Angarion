package br.angarion.dev.engine.communication.validator;

public sealed interface ValidationResult
    permits Approved, Denied, PartiallyApproved, Failure {}
