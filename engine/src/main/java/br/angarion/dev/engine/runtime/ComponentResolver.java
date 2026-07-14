package br.angarion.dev.engine.runtime;

public interface ComponentResolver {
    InnerProcessor<?> lookup(int type);
}
