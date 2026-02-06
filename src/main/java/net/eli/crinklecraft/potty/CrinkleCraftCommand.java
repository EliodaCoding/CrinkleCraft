package net.eli.crinklecraft.potty;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.core.Holder;
import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.contract.ContractSavedData;
import net.eli.crinklecraft.effect.ModEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * CrinkleCraft commands: /crinklecraft contract sign, messing enable/disable, gauge, holdit.
 * Contract sign validates secret word and records player in ContractSavedData.
 */
public class CrinkleCraftCommand {

    /** Secret word required to sign (from contract in chat). Case-insensitive. */
    public static final String CONTRACT_SECRET_WORD = "stuffie";

    /** Registers all /crinklecraft subcommands. Called from PottyEvents.onRegisterCommands. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("crinklecraft")
                        .then(Commands.literal("contract")
                                .then(Commands.literal("sign")
                                        .then(Commands.argument("secret", StringArgumentType.word())
                                                .executes(ctx -> signContract(ctx.getSource(), StringArgumentType.getString(ctx, "secret"))))))
                        .then(Commands.literal("messing")
                                .then(Commands.literal("enable")
                                        .executes(ctx -> setMessing(ctx.getSource(), true)))
                                .then(Commands.literal("disable")
                                        .executes(ctx -> setMessing(ctx.getSource(), false))))
                        .then(Commands.literal("gauge")
                                .executes(ctx -> showGauge(ctx.getSource())))
                        .then(Commands.literal("holdit")
                                .executes(ctx -> holdIt(ctx.getSource())))
                        .then(Commands.literal("diaper")
                                .executes(ctx -> choseDiaper(ctx.getSource())))
                        .then(Commands.literal("chart")
                                .executes(ctx -> showChart(ctx.getSource())))
                        .then(Commands.literal("cleanup")
                                .executes(ctx -> cleanup(ctx.getSource())))
        );
    }

    /** Enables or disables mess mechanic for the executing player. */
    private static int setMessing(CommandSourceStack source, boolean enable) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottySavedData data = PottySavedData.get(player.serverLevel());
        PottyPlayerData playerData = data.getOrCreate(player.getUUID());
        playerData.setMessingEnabled(enable);
        data.markDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.messing." + (enable ? "enabled" : "disabled")), true);
        return 1;
    }

    /** Prints pee, mess, continence, and messing status for the executing player. */
    private static int showGauge(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottySavedData data = PottySavedData.get(player.serverLevel());
        PottyPlayerData playerData = data.getOrCreate(player.getUUID());
        source.sendSuccess(() -> Component.literal(String.format("Pee: %.1f / 100 | Mess: %.1f / 100 | Continence: %.1f / 100 | Messing: %s",
                playerData.getPeeLevel(), playerData.getMessLevel(), playerData.getContinence(), playerData.isMessingEnabled() ? "on" : "off")), false);
        return 1;
    }

    /** Pass the current potty check (QTE). Used by Hold It keybinding via command or directly. */
    private static int holdIt(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottySavedData data = PottySavedData.get(player.serverLevel());
        PottyPlayerData playerData = data.getOrCreate(player.getUUID());
        if (!playerData.isInPottyCheck()) {
            source.sendFailure(Component.translatable("command.crinklecraft.holdit.not_in_check"));
            return 0;
        }
        if (playerData.isPottyCheckPee()) {
            playerData.setPeeLevel(Math.max(0, playerData.getPottyCheckThreshold() - 1));
            playerData.setLastPeeThreshold(playerData.getPottyCheckThreshold());
        } else {
            playerData.setMessLevel(Math.max(0, playerData.getPottyCheckThreshold() - 1));
            playerData.setLastMessThreshold(playerData.getPottyCheckThreshold());
        }
        playerData.onPottyCheckSuccess();
        playerData.setInPottyCheck(false);
        data.markDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.holdit.success"), true);
        return 1;
    }

    /** Use diaper deliberately when pee or mess gauge >= 25 (or during potty check). Consumes diaper use, lowers continence. */
    private static int choseDiaper(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottySavedData data = PottySavedData.get(player.serverLevel());
        PottyPlayerData playerData = data.getOrCreate(player.getUUID());

        float minGauge = 25f;
        boolean gaugeOk = playerData.getPeeLevel() >= minGauge || (playerData.isMessingEnabled() && playerData.getMessLevel() >= minGauge);
        if (!playerData.isInPottyCheck() && !gaugeOk) {
            source.sendFailure(Component.translatable("command.crinklecraft.diaper.gauge_too_low", (int) minGauge));
            return 0;
        }

        ItemStack diaper = playerData.getEquippedDiaper();
        if (diaper.isEmpty() || !(diaper.getItem() instanceof net.eli.crinklecraft.item.custom.DiaperItem di)) {
            source.sendFailure(Component.translatable("command.crinklecraft.diaper.no_diaper"));
            return 0;
        }
        if (di.isFullyUsed(diaper)) {
            source.sendFailure(Component.translatable("command.crinklecraft.diaper.already_used"));
            return 0;
        }
        di.useOne(diaper);
        if (di.isFullyUsed(diaper)) {
            playerData.setEquippedDiaper(ItemStack.EMPTY);
            player.getInventory().armor.set(1, playerData.getStoredLeggings().copy());
            playerData.setStoredLeggings(ItemStack.EMPTY);
            if (!player.getInventory().add(diaper)) {
                player.drop(diaper, false);
            }
        } else {
            playerData.setEquippedDiaper(diaper);
        }

        if (playerData.isInPottyCheck()) {
            if (playerData.isPottyCheckPee()) {
                playerData.setPeeLevel(Math.max(0, playerData.getPottyCheckThreshold() - 1));
                playerData.setLastPeeThreshold(playerData.getPottyCheckThreshold());
            } else {
                playerData.setMessLevel(Math.max(0, playerData.getPottyCheckThreshold() - 1));
                playerData.setLastMessThreshold(playerData.getPottyCheckThreshold());
            }
            playerData.setInPottyCheck(false);
        } else {
            playerData.setPeeLevel(0);
            playerData.setMessLevel(0);
            playerData.setLastPeeThreshold(0);
            playerData.setLastMessThreshold(0);
        }
        playerData.onChoseToUseDiaperDuringPottyCheck();
        data.markDirty();

        PottyEvents.broadcastAccidentMessage(player.serverLevel(), player, 20, false);

        source.sendSuccess(() -> Component.translatable("command.crinklecraft.diaper.success"), true);
        return 1;
    }

    /** Removes Wet effect (clean up after leak). */
    private static int cleanup(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        if (player.removeEffect(Holder.direct(ModEffects.WET_EFFECT.get()))) {
            source.sendSuccess(() -> Component.translatable("command.crinklecraft.cleanup.success"), true);
            return 1;
        }
        source.sendFailure(Component.translatable("command.crinklecraft.cleanup.not_wet"));
        return 0;
    }

    /** Prints potty chart (successes, accidents, stars) for the executing player. */
    private static int showChart(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottySavedData data = PottySavedData.get(player.serverLevel());
        PottyPlayerData playerData = data.getOrCreate(player.getUUID());
        int successes = playerData.getSuccessCount();
        int accidents = playerData.getAccidentCount();
        int stars = Math.max(0, successes - accidents);
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.chart.header"), false);
        source.sendSuccess(() -> Component.literal("  ★ Stars: " + stars + " | ✓ Held it: " + successes + " | ✗ Accidents: " + accidents), false);
        source.sendSuccess(() -> Component.literal("  Continence: " + String.format("%.1f", playerData.getContinence()) + " / 100"), false);
        return 1;
    }

    /** Validates secret word, marks player signed in ContractSavedData, sets scoreboard to 1. */
    private static int signContract(CommandSourceStack source, String secret) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        if (!CONTRACT_SECRET_WORD.equalsIgnoreCase(secret)) {
            source.sendFailure(Component.translatable("command.crinklecraft.contract.wrong_secret"));
            return 0;
        }
        ContractSavedData data = ContractSavedData.get(player.serverLevel());
        data.setSigned(player.getUUID());
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.contract.signed"), true);
        return 1;
    }
}
