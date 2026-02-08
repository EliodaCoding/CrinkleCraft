package net.eli.crinklecraft.util;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Grants CrinkleCraft advancements from code. Advancements use tick trigger with impossible conditions. */
public final class AdvancementHelper {

    public static final ResourceLocation FIRST_SUCCESS = ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "first_success");
    public static final ResourceLocation DRY_STREAK = ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "dry_streak");
    public static final ResourceLocation WELL_PREPARED = ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "well_prepared");
    public static final ResourceLocation COMFY = ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "comfy");

    public static void grant(ServerPlayer player, ResourceLocation advancementId) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        var advancement = server.getAdvancements().get(advancementId);
        if (advancement == null) return;
        var progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
            break;
        }
    }
}
