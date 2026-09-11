package br.angarion.dev.engine.communication;

public final class UnsignedShort {
    public static boolean checkRange(long value) {
        return value >= 0 && value <= 0xFFFF;
    }
}
