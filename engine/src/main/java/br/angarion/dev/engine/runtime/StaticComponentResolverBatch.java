package br.angarion.dev.engine.runtime;

public final class StaticComponentResolverBatch {
    private static final Object[] COMPONENT_TABLE = new Object[256];

    static {
        COMPONENT_TABLE[0] = new FakeComponent("alpha", 0, 0);
        COMPONENT_TABLE[1] = new FakeComponent("beta", 0, 1);
        COMPONENT_TABLE[2] = new FakeComponent("gamma", 0, 2);
    }

    private StaticComponentResolverBatch() {
        throw new UnsupportedOperationException("Classe utilitária");
    }

    public static Object[] resolveBatch(final int[] ids, final Object[] out) {
        // 1. Local Vars: Copiar a referência do array global para uma variável local 
        // reduz o custo de acessar o campo estático repetidamente no escopo do JIT.
        final Object[] table = COMPONENT_TABLE;
        final int length = ids.length;

        // 2. Bound Check Elimination Hint:
        // Explicitar o tamanho para o JIT eliminar checagens de borda implícitas dentro do loop.
        if (out.length < length) {
            throw new IllegalArgumentException("Output array is too small");
        }

        // 3. Loop Reajustado para Unrolling Sem Dependência Cruzada direta na mesma linha
        int i = 0;
        final int limit = length - 4;
        
        for (; i <= limit; i += 4) {
            // Carregamos os índices primeiro (A CPU puxa esses ints em sequência muito rápido)
            final int id0 = ids[i];
            final int id1 = ids[i + 1];
            final int id2 = ids[i + 2];
            final int id3 = ids[i + 3];

            // Resolvemos e escrevemos (Isolando o acesso à tabela)
            out[i]     = table[id0];
            out[i + 1] = table[id1];
            out[i + 2] = table[id2];
            out[i + 3] = table[id3];
        }

        // Limpa o resto do array (caso o tamanho não seja múltiplo de 4)
        for (; i < length; i++) {
            out[i] = table[ids[i]];
        }

        return out;
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