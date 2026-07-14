package br.angarion.dev.api.communication;

public class TargetScope {
    public final boolean broadcast;
    public final TargetModifier modifier;

    public TargetScope(boolean broadcast) {
        this.broadcast = broadcast;
        this.modifier = null;
    }

    public TargetScope(TargetModifier modifier) {
        this.broadcast = false;
        this.modifier = modifier;
    }
}
