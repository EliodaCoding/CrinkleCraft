package net.eli.crinklecraft.event;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.item.custom.PacifierItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Chat events: pacifier transforms outgoing chat to baby talk/babbling when held in offhand.
 */
@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatEvents {

    private static final String[] BABBLE_SYLLABLES = { "goo", "ga", "gah", "wah", "ba", "da", "ma", "na", "baba", "dada", "mama", "ooh", "aah" };

    /** If player has pacifier in offhand, transform chat to baby talk or babbling. */
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !(offhand.getItem() instanceof PacifierItem)) return;

        String raw = event.getRawText().trim();
        if (raw.isEmpty()) return;

        // 50% baby talk filter, 50% full babbling
        String transformed = ThreadLocalRandom.current().nextBoolean()
                ? toBabyTalk(raw)
                : toBabbling(raw);
        event.setMessage(Component.literal(transformed));
    }

    /** Applies baby-talk substitutions: R->W, L->W, TH->D, etc. */
    private static String toBabyTalk(String text) {
        StringBuilder sb = new StringBuilder();
        text = text.toLowerCase();
        int i = 0;
        while (i < text.length()) {
            if (i + 2 <= text.length()) {
                String digraph = text.substring(i, i + 2);
                if (digraph.equals("th")) {
                    sb.append("d");
                    i += 2;
                    continue;
                }
                if (digraph.equals("ng") || digraph.equals("nk")) {
                    sb.append(digraph);
                    i += 2;
                    continue;
                }
            }
            char c = text.charAt(i);
            if (c == 'r' || c == 'l') {
                sb.append('w');
            } else if (c == 's' && (i == 0 || !Character.isLetter(text.charAt(i - 1)))) {
                sb.append("sh");
            } else {
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }

    /** Replaces message with random babbling. */
    private static String toBabbling(String text) {
        int wordCount = Math.max(2, text.split("\\s+").length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) sb.append(" ");
            sb.append(BABBLE_SYLLABLES[ThreadLocalRandom.current().nextInt(BABBLE_SYLLABLES.length)]);
        }
        sb.append(" *sucks pacifier*");
        return sb.toString();
    }
}
