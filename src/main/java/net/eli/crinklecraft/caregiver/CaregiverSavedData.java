package net.eli.crinklecraft.caregiver;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Per-world data: caregiver consent (who can change diaper/onesie) and lock state (diaper/paci locked by caregiver).
 */
public class CaregiverSavedData extends SavedData {
    private static final String DATA_NAME = CrinkleCraft.MOD_ID + "_caregiver";
    /** Player UUID -> their consent/lock data */
    private final Map<UUID, CaregiverData> data = new HashMap<>();

    public CaregiverSavedData() {}

    public static SavedData.Factory<CaregiverSavedData> factory() {
        return new SavedData.Factory<>(CaregiverSavedData::new, CaregiverSavedData::load, DataFixTypes.LEVEL);
    }

    public static CaregiverSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CaregiverSavedData d = new CaregiverSavedData();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            UUID uuid = e.getUUID("uuid");
            CaregiverData cd = new CaregiverData();
            ListTag uuids = e.getList("caregivers_uuids", Tag.TAG_COMPOUND);
            for (int j = 0; j < uuids.size(); j++) {
                CompoundTag u = uuids.getCompound(j);
                if (u.hasUUID("id")) cd.caregivers.add(u.getUUID("id"));
            }
            if (e.contains("diaper_locked")) cd.diaperLocked = e.getBoolean("diaper_locked");
            if (e.contains("paci_locked")) cd.paciLocked = e.getBoolean("paci_locked");
            if (e.contains("locked_by")) cd.lockedByCaregiver = e.getUUID("locked_by");
            d.data.put(uuid, cd);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, CaregiverData> e : data.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            ListTag caregivers = new ListTag();
            for (UUID u : e.getValue().caregivers) {
                CompoundTag uTag = new CompoundTag();
                uTag.putUUID("id", u);
                caregivers.add(uTag);
            }
            entry.put("caregivers_uuids", caregivers);
            if (e.getValue().diaperLocked) entry.putBoolean("diaper_locked", true);
            if (e.getValue().paciLocked) entry.putBoolean("paci_locked", true);
            if (e.getValue().lockedByCaregiver != null)
                entry.putUUID("locked_by", e.getValue().lockedByCaregiver);
            list.add(entry);
        }
        tag.put("players", list);
        return tag;
    }

    public CaregiverData getOrCreate(UUID playerId) {
        return data.computeIfAbsent(playerId, k -> new CaregiverData());
    }

    public void markDirty() {
        setDirty();
    }

    public static CaregiverSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static class CaregiverData {
        public final Set<UUID> caregivers = new HashSet<>();
        public boolean diaperLocked;
        public boolean paciLocked;
        /** UUID of caregiver who locked (if any) */
        public UUID lockedByCaregiver;
    }
}
