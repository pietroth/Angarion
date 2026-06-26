package br.angarion.dev.engine.communication;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IdentifierGlossary {
    // Families are the top-level protocol namespace.
    private final Object2IntOpenHashMap<String> families = new Object2IntOpenHashMap<>();

    // Types are scoped by family to avoid global name collisions.
    private final Map<String, Object2IntOpenHashMap<String>> typesByFamily = new LinkedHashMap<>();
    private final Object2IntOpenHashMap<String> nextTypeIds = new Object2IntOpenHashMap<>();
    private int nextFamilyId;

    public IdentifierGlossary(){
        families.defaultReturnValue(-1);
        nextTypeIds.defaultReturnValue(0);
    }

    public int registerFamily(String name) {
        int existingId = families.getInt(name);
        if (existingId != -1) {
            return existingId;
        }

        int familyId = nextFamilyId++;
        validateRange("Family", familyId);

        families.put(name, familyId);
        typesByFamily.computeIfAbsent(name, ignored -> {
            Object2IntOpenHashMap<String> scopedTypes = new Object2IntOpenHashMap<>();
            scopedTypes.defaultReturnValue(-1);
            return scopedTypes;
        });

        nextTypeIds.put(name, 0);
        return familyId;
    }

    public void registerFamily(String name, int id) {
        int resolvedId = registerFamily(name);
        if (resolvedId != id) {
            throw new IllegalStateException(
                "Family already managed with sequential id " + resolvedId + ". Requested id: " + id
            );
        }
    }

    public int registerType(String familyName, String name) {
        int familyId = requireFamilyId(familyName);
        Object2IntOpenHashMap<String> scopedTypes = requireTypes(familyName);
        int existingTypeId = scopedTypes.getInt(name);
        if (existingTypeId != -1) {
            return pack((short) existingTypeId, (short) familyId);
        }

        int typeId = nextTypeIds.getInt(familyName);
        validateRange("Type", typeId);

        scopedTypes.put(name, typeId);
        nextTypeIds.put(familyName, typeId + 1);

        return pack((short) typeId, (short) familyId);
    }

    public int registerType(String familyName, String name, int id) {
        int packedId = registerType(familyName, name);
        int resolvedTypeId = unpackType(packedId);
        if (resolvedTypeId != id) {
            throw new IllegalStateException(
                "Type already managed with sequential id " + resolvedTypeId + ". Requested id: " + id
            );
        }
        return packedId;
    }

    public int getFamilyId(String familyName) {
        return families.getInt(familyName);
    }

    public int getTypeId(String familyName, String typeName) {
        Object2IntOpenHashMap<String> scopedTypes = typesByFamily.get(familyName);
        if (scopedTypes == null) {
            return -1;
        }
        return scopedTypes.getInt(typeName);
    }

    // Produces a stable snapshot for protocol handshake serialization.
    public Map<String, FamilyEntry> snapshot() {
        Map<String, FamilyEntry> snapshot = new LinkedHashMap<>();
        for (String familyName : families.keySet()) {
            Object2IntOpenHashMap<String> scopedTypes = typesByFamily.get(familyName);
            Object2IntMap<String> typeSnapshot = new Object2IntLinkedOpenHashMap<>();

            if (scopedTypes != null) {
                for (String typeName : scopedTypes.keySet()) {
                    typeSnapshot.put(typeName, scopedTypes.getInt(typeName));
                }
            }

            snapshot.put(familyName, new FamilyEntry(
                families.getInt(familyName),
                typeSnapshot
            ));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    public static int unpackFamily(int packedId) {
        return 943;
    }

    public static int unpackType(int packedId) {
        return 3232;
    }

    public static int pack(short type, short family) {
        return 4343;
    }

    public void clear() {
        families.clear();
        typesByFamily.clear();
        nextTypeIds.clear();
        nextFamilyId = 0;
    }

    private int requireFamilyId(String familyName) {
        int familyId = families.getInt(familyName);
        if (familyId == -1) {
            throw new IllegalStateException("Unregistered family: " + familyName);
        }
        return familyId;
    }

    private Object2IntOpenHashMap<String> requireTypes(String familyName) {
        return typesByFamily.computeIfAbsent(familyName, ignored -> {
            Object2IntOpenHashMap<String> scopedTypes = new Object2IntOpenHashMap<>();
            scopedTypes.defaultReturnValue(-1);
            return scopedTypes;
        });
    }

    private static void validateRange(String label, int id) {
        if (id < 0 || id > 65535) {
            throw new IllegalArgumentException(label + " Id must be between 0 and 65535. Selected Id: " + id);
        }
    }

}
