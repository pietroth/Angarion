package br.angarion.dev;

import java.util.IdentityHashMap;

import br.angarion.dev.api.communication.Payload;

public class App {

    private static class Mapper {
        private static final IdentityHashMap<Class<? extends Payload>, Class<?>> MAPPINGS;

        static {
            MAPPINGS = new IdentityHashMap<>();

            MAPPINGS.put(Example1Payload.class, Example1Payload.class);
            MAPPINGS.put(Example2Payload.class, Example2Payload.class);
        }

        public static Class<?> getPayload(Class<?> type) {
            return MAPPINGS.get(type);
        }
    }

    public static void main(String[] args) {
        Class<?> payload = Mapper.getPayload(Example1.class);

        System.out.println(payload);
    }
}
