package br.angarion.dev.engine.communication;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public record FamilyEntry(int id, Object2IntOpenHashMap<String> types) {}
