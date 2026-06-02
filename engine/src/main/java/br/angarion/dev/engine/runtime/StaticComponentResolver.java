package br.angarion.dev.engine.runtime;

public final class StaticComponentResolver {
    private static final Object[] COMPONENT_TABLE = new Object[256]; 

    static {
        COMPONENT_TABLE[0] = new FakeComponent("alpha", 0, 0);
        COMPONENT_TABLE[1] = new FakeComponent("beta", 0, 1);
        COMPONENT_TABLE[2] = new FakeComponent("gamma", 0, 2);
    }

    private StaticComponentResolver() {
        throw new UnsupportedOperationException("Classe utilitária");
    }

    public static Object getComponent(int id) {
        if (id < 0 || id >= COMPONENT_TABLE.length) {
            return null;
        }
        return COMPONENT_TABLE[id];
    }

    private static final class FakeComponent {
        private final String name;
        private final int familyId;
        private final int typeId;

        private FakeComponent(String name, int familyId, int typeId) {
            this.name = name;
            this.familyId = familyId;
            this.typeId = typeId;
        }
    }
}
