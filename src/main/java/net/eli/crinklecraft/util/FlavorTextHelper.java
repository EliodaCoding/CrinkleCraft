package net.eli.crinklecraft.util;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.util.RandomSource;

import java.util.List;

/** Picks a random flavor text key from a list. */
public final class FlavorTextHelper {

    private static final String PREFIX = "message." + CrinkleCraft.MOD_ID + ".";

    public static final List<String> ACCIDENT_SELF_ABSORBED = List.of(
            PREFIX + "accident.self.absorbed",
            PREFIX + "accident.self.absorbed.2",
            PREFIX + "accident.self.absorbed.3"
    );
    public static final List<String> ACCIDENT_SELF_LEAKED = List.of(
            PREFIX + "accident.self.leaked",
            PREFIX + "accident.self.leaked.2",
            PREFIX + "accident.self.leaked.3"
    );
    public static final List<String> ACCIDENT_NEARBY_ABSORBED = List.of(
            PREFIX + "accident.nearby.absorbed",
            PREFIX + "accident.nearby.absorbed.2"
    );
    public static final List<String> ACCIDENT_NEARBY_LEAKED = List.of(
            PREFIX + "accident.nearby.leaked",
            PREFIX + "accident.nearby.leaked.2"
    );
    public static final List<String> HOLDIT_SUCCESS = List.of(
            "command.crinklecraft.holdit.success",
            "command.crinklecraft.holdit.success.2",
            "command.crinklecraft.holdit.success.3"
    );

    public static String pick(RandomSource random, List<String> keys) {
        return keys.get(random.nextInt(keys.size()));
    }
}
