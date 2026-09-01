package br.angarion.dev;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class App {
    public static final Reference2ObjectOpenHashMap<Object, Object> objects = new Reference2ObjectOpenHashMap<>();

    static {
        objects.put(Integer.class, InternalError.class);
    }

    void main() {

    }
}
