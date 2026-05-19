package br.angarion.dev.engine.communication;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

/*
    This is used for registration of types' identifications.
*/
public record FamilyEntry(int id, Object2IntMap<String> types) {}
