package net.eli.crinklecraft.potty;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.util.datafix.DataFixTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-world saved data: map of player UUID -> PottyPlayerData (pee, mess, continence, diaper, etc.).
 * Stored in overworld level data. Use get(level) to access from any dimension.
 */
public class PottySavedData extends SavedData {
    private static final String DATA_NAME = CrinkleCraft.MOD_ID + "_potty";
    private final Map<UUID, PottyPlayerData> playerData = new HashMap<>();

    public PottySavedData() {}

    /** Factory for Minecraft's SavedData system. */
    public static SavedData.Factory<PottySavedData> factory() {
        return new SavedData.Factory<>(PottySavedData::new, PottySavedData::load, DataFixTypes.LEVEL);
    }

    /** Loads from NBT (level.dat). */
    public static PottySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PottySavedData data = new PottySavedData();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID("uuid");
            float pee = entry.getFloat("pee");
            float mess = entry.getFloat("mess");
            float continence = entry.contains("continence") ? entry.getFloat("continence") : PottyPlayerData.DEFAULT_CONTINENCE;
            boolean messing = entry.getBoolean("messing");
            PottyPlayerData pd = new PottyPlayerData(pee, mess, continence, messing);
            pd.setLastPeeThreshold(entry.getFloat("last_pee_threshold"));
            pd.setLastMessThreshold(entry.getFloat("last_mess_threshold"));
            pd.setSuccessCount(entry.contains("success_count") ? entry.getInt("success_count") : 0);
            pd.setAccidentCount(entry.contains("accident_count") ? entry.getInt("accident_count") : 0);
            if (entry.contains("diaper"))
                pd.setEquippedDiaper(ItemStack.parse(registries, entry.getCompound("diaper")).orElse(ItemStack.EMPTY));
            if (entry.contains("pacifier"))
                pd.setEquippedPacifier(ItemStack.parse(registries, entry.getCompound("pacifier")).orElse(ItemStack.EMPTY));
            if (entry.contains("mittens"))
                pd.setEquippedMittens(ItemStack.parse(registries, entry.getCompound("mittens")).orElse(ItemStack.EMPTY));
            if (entry.contains("onesie"))
                pd.setEquippedOnesie(ItemStack.parse(registries, entry.getCompound("onesie")).orElse(ItemStack.EMPTY));
            if (entry.contains("magic_regen_tick"))
                pd.setLastMagicDiaperRegenTick(entry.getLong("magic_regen_tick"));
            data.playerData.put(uuid, pd);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PottyPlayerData> e : playerData.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.putFloat("pee", e.getValue().getPeeLevel());
            entry.putFloat("mess", e.getValue().getMessLevel());
            entry.putFloat("continence", e.getValue().getContinence());
            entry.putBoolean("messing", e.getValue().isMessingEnabled());
            entry.putFloat("last_pee_threshold", e.getValue().getLastPeeThreshold());
            entry.putFloat("last_mess_threshold", e.getValue().getLastMessThreshold());
            entry.putInt("success_count", e.getValue().getSuccessCount());
            entry.putInt("accident_count", e.getValue().getAccidentCount());
            if (!e.getValue().getEquippedDiaper().isEmpty())
                entry.put("diaper", e.getValue().getEquippedDiaper().save(registries));
            if (!e.getValue().getEquippedPacifier().isEmpty())
                entry.put("pacifier", e.getValue().getEquippedPacifier().save(registries));
            if (!e.getValue().getEquippedMittens().isEmpty())
                entry.put("mittens", e.getValue().getEquippedMittens().save(registries));
            if (!e.getValue().getEquippedOnesie().isEmpty())
                entry.put("onesie", e.getValue().getEquippedOnesie().save(registries));
            if (e.getValue().getLastMagicDiaperRegenTick() != 0)
                entry.putLong("magic_regen_tick", e.getValue().getLastMagicDiaperRegenTick());
            list.add(entry);
        }
        tag.put("players", list);
        return tag;
    }

    /** Gets existing player data or creates default. Call markDirty() after modifying. */
    public PottyPlayerData getOrCreate(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PottyPlayerData());
    }

    /** Call after modifying player data so it persists. */
    public void markDirty() {
        setDirty();
    }

    /** Get PottySavedData from the overworld (use for global player data). */
    public static PottySavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    /** Convenience: get player's potty data. */
    public static PottyPlayerData getPlayerData(ServerPlayer player) {
        return get(player.serverLevel()).getOrCreate(player.getUUID());
    }
}
