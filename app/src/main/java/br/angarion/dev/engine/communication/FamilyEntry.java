package br.angarion.dev.engine.communication;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public record FamilyEntry(int id, Object2IntMap<String> types) {}
