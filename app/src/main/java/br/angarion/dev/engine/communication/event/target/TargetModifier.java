package br.angarion.dev.engine.communication.event.target;

public interface TargetModifier {
    int[] toArray();
    
    default TargetModifier exclude(int id) {
        return () -> java.util.Arrays.stream(toArray())
            .filter(val -> val != id)
            .toArray();
    }
}
