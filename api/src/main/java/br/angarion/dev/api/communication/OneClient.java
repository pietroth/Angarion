package br.angarion.dev.api.communication;

public class OneClient implements TargetModifier {

    public final int clientId;

    public OneClient(Integer clientId) {
        this.clientId = clientId;
    }

    @Override
    public int[] toArray() {
        return new int[] { clientId };
    }
}
