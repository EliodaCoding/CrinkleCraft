package net.eli.crinklecraft.potty;

import com.mojang.brigadier.CommandDispatcher;
import net.eli.crinklecraft.caregiver.CaregiverSavedData;
import net.eli.crinklecraft.contract.ContractSavedData;
import net.eli.crinklecraft.effect.ModEffects;
import net.eli.crinklecraft.util.AdvancementHelper;
import net.eli.crinklecraft.util.FlavorTextHelper;
import net.eli.crinklecraft.menu.CrinkleCraftSlotsMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * CrinkleCraft commands: /crinklecraft contract sign, messing enable/disable, gauge, holdit.
 * Contract sign validates secret word and records player in ContractSavedData.
 */
public class CrinkleCraftCommand {

    /** Secret word required to sign (from contract in chat). Case-insensitive. */
    public static final String CONTRACT_SECRET_WORD = "stuffie";

    /** Runs a command as the player with suppressed output. */
    public static void runCommandAsPlayer(ServerPlayer player, String command) {
        if (player.getServer() != null) {
            player.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput(), command);
        }
    }

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
                        .then(Commands.literal("slots")
                                .executes(ctx -> openCrinkleCraftSlots(ctx.getSource())))
                        .then(Commands.literal("chart")
                                .executes(ctx -> showChart(ctx.getSource())))
                        .then(Commands.literal("cleanup")
                                .executes(ctx -> cleanup(ctx.getSource())))
                        .then(Commands.literal("caregiver")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                                .executes(ctx -> caregiverAdd(ctx.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player")))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                                .executes(ctx -> caregiverRemove(ctx.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player")))))
                                .then(Commands.literal("list")
                                        .executes(ctx -> caregiverList(ctx.getSource())))
                                .then(Commands.literal("lock")
                                        .then(Commands.literal("diaper")
                                                .then(Commands.argument("locked", BoolArgumentType.bool())
                                                        .executes(ctx -> caregiverLockDiaper(ctx.getSource(), BoolArgumentType.getBool(ctx, "locked")))))
                                        .then(Commands.literal("paci")
                                                .then(Commands.argument("locked", BoolArgumentType.bool())
                                                        .executes(ctx -> caregiverLockPaci(ctx.getSource(), BoolArgumentType.getBool(ctx, "locked")))))))
        );
    }

    /** Enables or disables mess mechanic for the executing player. */
    private static int setMessing(CommandSourceStack source, boolean enable) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottyPlayerData playerData = PottySavedData.getPlayerData(player);
        playerData.setMessingEnabled(enable);
        PottySavedData.get(player.serverLevel()).markDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.messing." + (enable ? "enabled" : "disabled")), true);
        return 1;
    }

    /** Prints pee, mess, continence, and messing status for the executing player. */
    private static int showGauge(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottyPlayerData playerData = PottySavedData.getPlayerData(player);
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
        PottyPlayerData playerData = PottySavedData.getPlayerData(player);
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
        PottySavedData.get(player.serverLevel()).markDirty();
        AdvancementHelper.grant(player, AdvancementHelper.FIRST_SUCCESS);
        int stars = playerData.getSuccessCount() - playerData.getAccidentCount();
        if (stars >= 3) AdvancementHelper.grant(player, AdvancementHelper.DRY_STREAK);
        String key = FlavorTextHelper.pick(player.getRandom(), FlavorTextHelper.HOLDIT_SUCCESS);
        source.sendSuccess(() -> Component.translatable(key), true);
        return 1;
    }

    /** Use diaper deliberately when pee or mess gauge >= 25 (or during potty check). Consumes diaper use, lowers continence. */
    private static int choseDiaper(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottyPlayerData playerData = PottySavedData.getPlayerData(player);

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
        playerData.setEquippedDiaper(diaper); // keep diaper in slot even when fully used

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
        PottySavedData.get(player.serverLevel()).markDirty();

        PottyEvents.broadcastAccidentMessage(player.serverLevel(), player, 20, false);

        source.sendSuccess(() -> Component.translatable("command.crinklecraft.diaper.success"), true);
        return 1;
    }

    /** Removes Wet and Rash effects (clean up after leak). */
    private static int cleanup(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        boolean removedWet = ModEffects.removeWet(player);
        boolean removedRash = ModEffects.removeRash(player);
        if (removedWet || removedRash) {
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
        PottyPlayerData playerData = PottySavedData.getPlayerData(player);
        int successes = playerData.getSuccessCount();
        int accidents = playerData.getAccidentCount();
        int stars = Math.max(0, successes - accidents);
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.chart.header"), false);
        source.sendSuccess(() -> Component.literal("  ★ Stars: " + stars + " | ✓ Held it: " + successes + " | ✗ Accidents: " + accidents), false);
        source.sendSuccess(() -> Component.literal("  Continence: " + String.format("%.1f", playerData.getContinence()) + " / 100"), false);
        return 1;
    }

    /** Opens the CrinkleCraft slots GUI (diaper, pacifier, mittens) for the executing player. */
    private static int openCrinkleCraftSlots(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        PottyPlayerData playerData = PottySavedData.getPlayerData(player);
        ItemStackHandler diaperHandler = new ItemStackHandler(1);
        diaperHandler.setStackInSlot(0, playerData.getEquippedDiaper().copy());
        ItemStackHandler pacifierHandler = new ItemStackHandler(1);
        pacifierHandler.setStackInSlot(0, playerData.getEquippedPacifier().copy());
        ItemStackHandler mittensHandler = new ItemStackHandler(1);
        mittensHandler.setStackInSlot(0, playerData.getEquippedMittens().copy());
        ItemStackHandler onesieHandler = new ItemStackHandler(1);
        onesieHandler.setStackInSlot(0, playerData.getEquippedOnesie().copy());

        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.crinklecraft.crinklecraft_slots");
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                return new CrinkleCraftSlotsMenu(containerId, playerInv, diaperHandler, pacifierHandler, mittensHandler,
                        onesieHandler, ContainerLevelAccess.create(player.level(), player.blockPosition()), player);
            }
        };
        player.openMenu(provider);
        return 1;
    }

    private static int caregiverAdd(CommandSourceStack source, ServerPlayer target) {
        if (!(source.getEntity() instanceof ServerPlayer actor)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        if (actor.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.translatable("command.crinklecraft.caregiver.cannot_self"));
            return 0;
        }
        CaregiverSavedData.CaregiverData cd = CaregiverSavedData.get(actor.serverLevel()).getOrCreate(target.getUUID());
        cd.caregivers.add(actor.getUUID());
        CaregiverSavedData.get(actor.serverLevel()).markDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.caregiver.added", target.getName().getString()), true);
        return 1;
    }

    private static int caregiverRemove(CommandSourceStack source, ServerPlayer target) {
        if (!(source.getEntity() instanceof ServerPlayer actor)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        CaregiverSavedData.CaregiverData cd = CaregiverSavedData.get(actor.serverLevel()).getOrCreate(target.getUUID());
        cd.caregivers.remove(actor.getUUID());
        CaregiverSavedData.get(actor.serverLevel()).markDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.caregiver.removed", target.getName().getString()), true);
        return 1;
    }

    private static int caregiverList(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        CaregiverSavedData.CaregiverData cd = CaregiverSavedData.get(player.serverLevel()).getOrCreate(player.getUUID());
        if (cd.caregivers.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.crinklecraft.caregiver.list_empty"), false);
            return 1;
        }
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.caregiver.list_header"), false);
        for (java.util.UUID u : cd.caregivers) {
            ServerPlayer p = player.getServer().getPlayerList().getPlayer(u);
            source.sendSuccess(() -> Component.literal("  - " + (p != null ? p.getName().getString() : u.toString())), false);
        }
        return 1;
    }

    private static int caregiverLockDiaper(CommandSourceStack source, boolean locked) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        CaregiverSavedData.CaregiverData cd = CaregiverSavedData.get(player.serverLevel()).getOrCreate(player.getUUID());
        cd.diaperLocked = locked;
        cd.lockedByCaregiver = locked ? player.getUUID() : null; // Placeholder - in real use a caregiver would run this on target
        CaregiverSavedData.get(player.serverLevel()).setDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.caregiver.lock_diaper", locked ? "locked" : "unlocked"), true);
        return 1;
    }

    private static int caregiverLockPaci(CommandSourceStack source, boolean locked) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        CaregiverSavedData.CaregiverData cd = CaregiverSavedData.get(player.serverLevel()).getOrCreate(player.getUUID());
        cd.paciLocked = locked;
        if (!locked) cd.lockedByCaregiver = null;
        else if (cd.lockedByCaregiver == null) cd.lockedByCaregiver = player.getUUID();
        CaregiverSavedData.get(player.serverLevel()).setDirty();
        source.sendSuccess(() -> Component.translatable("command.crinklecraft.caregiver.lock_paci", locked ? "locked" : "unlocked"), true);
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
