package net.eli.crinklecraft.contract;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Per-world saved data: set of player UUIDs who have signed the contract.
 * Stored in the overworld level data; loaded on world load. Players must sign per world.
 */
public class ContractSavedData extends SavedData {
    private static final String DATA_NAME = CrinkleCraft.MOD_ID + "_contract";
    private final Set<UUID> signedPlayers = new HashSet<>();

    public ContractSavedData() {}

    /** Factory for Minecraft's SavedData system. */
    public static SavedData.Factory<ContractSavedData> factory() {
        return new SavedData.Factory<>(ContractSavedData::new, ContractSavedData::load, DataFixTypes.LEVEL);
    }

    /** Loads from NBT (level.dat). */
    public static ContractSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ContractSavedData data = new ContractSavedData();
        ListTag list = tag.getList("signed", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("uuid")) {
                data.signedPlayers.add(entry.getUUID("uuid"));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (UUID uuid : signedPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            list.add(entry);
        }
        tag.put("signed", list);
        return tag;
    }

    /** Returns true if the player has signed the contract for this world. */
    public boolean hasSigned(UUID playerId) {
        return signedPlayers.contains(playerId);
    }

    /** Marks the player as signed. Call setDirty() via this method. */
    public void setSigned(UUID playerId) {
        signedPlayers.add(playerId);
        setDirty();
    }

    /** Gets or creates the contract data for the overworld. Use from any dimension. */
    public static ContractSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }
}
