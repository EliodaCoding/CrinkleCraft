package net.eli.crinklecraft.potty;

import net.eli.crinklecraft.Config;
import net.eli.crinklecraft.item.custom.DiaperItem;
import net.minecraft.world.item.ItemStack;

/**
 * Per-player potty/continence data: pee and mess gauges (0–100), continence (0–100),
 * equipped diaper (custom slot), and whether mess mechanic is enabled.
 * QTE (potty check) triggers at thresholds; urgency increases with gauge level.
 */
public class PottyPlayerData {
    public static final float MAX_GAUGE = 100f;
    /** Base fill: ~100 in ~33 min if not emptied. */
    public static final float DEFAULT_GAIN_PER_TICK = 0.10f;
    public static final int TICKS_BETWEEN_GAIN = 40;          // every 2 seconds

    /** Gauge thresholds that trigger a QTE (potty check). */
    public static final float[] QTE_THRESHOLDS = { 25f, 50f, 75f, 100f };
    /** Ticks allowed to react at each threshold (index matches QTE_THRESHOLDS). Higher gauge = less time. */
    public static final int[] QTE_REACTION_TICKS = { 600, 400, 240, 120 };  // 30s, 20s, 12s, 6s
    /** Random variance for reaction time: base * random(0.65, 1.35) so players can't memorize timings. */
    public static final float QTE_REACTION_VARIANCE_MIN = 0.65f;
    public static final float QTE_REACTION_VARIANCE_MAX = 1.35f;
    /** Chance per tick to trigger "early" check when gauge is in danger zone below threshold. Keeps players focused. */
    public static final float QTE_EARLY_TRIGGER_CHANCE = 0.018f;

    /** Default continence (0–100). Higher = more continent, affects potty check timing/difficulty. */
    public static final float DEFAULT_CONTINENCE = 75f;
    /** Continence change when player succeeds a potty check (e.g. uses potty in time). */
    public static final float CONTINENCE_ON_SUCCESS = 3f;
    /** Continence change when player fails a potty check (accident). */
    public static final float CONTINENCE_ON_FAIL = -12f;
    /** Continence change when player deliberately uses diaper during a potty check (habit/dependence). */
    public static final float CONTINENCE_ON_CHOSE_DIAPER = -8f;

    private float peeLevel;
    private float messLevel;
    private float continence;
    private boolean messingEnabled;
    private int ticksSinceLastGain;
    /** Last threshold we already triggered a QTE for (so we don't re-trigger). */
    private float lastPeeThreshold;
    private float lastMessThreshold;
    /** QTE state (not persisted across logout). */
    private boolean inPottyCheck;
    private long pottyCheckStartTick;
    /** Randomized reaction ticks for current check (set when check starts). */
    private int pottyCheckAllowedTicks;
    private boolean pottyCheckIsPee;  // true = pee, false = mess
    private float pottyCheckThreshold;
    /** Equipped diaper (custom slot). Serialized in PottySavedData. */
    private ItemStack equippedDiaper;
    /** Equipped pacifier (custom slot). Serialized in PottySavedData. */
    private ItemStack equippedPacifier;
    /** Equipped mittens (custom slot). Serialized in PottySavedData. */
    private ItemStack equippedMittens;
    /** Equipped onesie (custom slot). Serialized in PottySavedData. */
    private ItemStack equippedOnesie;
    /** Last game tick when magic diaper restored one use (for regen cooldown). Serialized in PottySavedData. */
    private long lastMagicDiaperRegenTick;
    /** Temporary boost to pee fill after drinking (not persisted). */
    private int peeBoostTicks;
    private float peeBoostMultiplier;
    /** Potty chart: times player held it (success). */
    private int successCount;
    /** Potty chart: times player had an accident. */
    private int accidentCount;

    public PottyPlayerData() {
        this(0f, 0f, DEFAULT_CONTINENCE, false);
    }

    public PottyPlayerData(float peeLevel, float messLevel, boolean messingEnabled) {
        this(peeLevel, messLevel, DEFAULT_CONTINENCE, messingEnabled);
    }

    public PottyPlayerData(float peeLevel, float messLevel, float continence, boolean messingEnabled) {
        this.peeLevel = clamp(peeLevel);
        this.messLevel = clamp(messLevel);
        this.continence = clamp(continence);
        this.messingEnabled = messingEnabled;
        this.ticksSinceLastGain = 0;
        this.lastPeeThreshold = 0;
        this.lastMessThreshold = 0;
        this.inPottyCheck = false;
        this.pottyCheckStartTick = 0;
        this.pottyCheckIsPee = true;
        this.pottyCheckThreshold = 0;
        this.equippedDiaper = ItemStack.EMPTY;
        this.equippedPacifier = ItemStack.EMPTY;
        this.equippedMittens = ItemStack.EMPTY;
        this.equippedOnesie = ItemStack.EMPTY;
        this.lastMagicDiaperRegenTick = 0;
        this.peeBoostTicks = 0;
        this.peeBoostMultiplier = 1f;
        this.successCount = 0;
        this.accidentCount = 0;
    }

    public float getPeeLevel() { return peeLevel; }
    public void setPeeLevel(float v) { this.peeLevel = clamp(v); }
    public void addPee(float v) { this.peeLevel = clamp(this.peeLevel + v); }

    public float getMessLevel() { return messLevel; }
    public void setMessLevel(float v) { this.messLevel = clamp(v); }
    public void addMess(float v) { this.messLevel = clamp(this.messLevel + v); }

    /** Multiply pee gain for a short duration (e.g. after drinking). */
    public void applyPeeBoost(float multiplier, int durationTicks) {
        this.peeBoostMultiplier = Math.max(1f, multiplier);
        this.peeBoostTicks = Math.max(this.peeBoostTicks, Math.max(0, durationTicks));
    }

    public float getPeeGainMultiplier() {
        return peeBoostTicks > 0 ? peeBoostMultiplier : 1f;
    }

    /** Called once per tick to decay temporary boosts. */
    public void tickTransientEffects() {
        if (peeBoostTicks > 0) peeBoostTicks--;
        if (peeBoostTicks <= 0) peeBoostMultiplier = 1f;
    }

    /** 0–100. Higher = more continent; affects how often potty checks happen and how fast the player must react. */
    public float getContinence() { return continence; }
    public void setContinence(float v) { this.continence = clamp(v); }
    public void addContinence(float delta) { this.continence = clamp(this.continence + delta); }

    /** Called when the player succeeds a potty check (e.g. used potty in time). Increases continence. */
    public void onPottyCheckSuccess() {
        onPottyCheckSuccess((float) Config.continenceOnSuccess);
    }

    /** Same as {@link #onPottyCheckSuccess()} with a custom delta (e.g. from config). */
    public void onPottyCheckSuccess(float delta) {
        addContinence(delta);
        successCount++;
    }

    /** Called when the player fails a potty check (accident). Decreases continence. */
    public void onPottyCheckFail() {
        onPottyCheckFail((float) Config.continenceOnFail);
    }

    /** Same as {@link #onPottyCheckFail()} with a custom delta (e.g. from config). */
    public void onPottyCheckFail(float delta) {
        addContinence(delta);
        accidentCount++;
    }

    /** Called when the player deliberately pees (or messes) in their diaper during a potty check. Lowers continence (habit/dependence). */
    public void onChoseToUseDiaperDuringPottyCheck() {
        onChoseToUseDiaperDuringPottyCheck((float) Config.continenceOnChoseDiaper);
    }

    /** Same as {@link #onChoseToUseDiaperDuringPottyCheck()} with a custom delta (e.g. from config). */
    public void onChoseToUseDiaperDuringPottyCheck(float delta) {
        addContinence(delta);
    }

    public boolean isMessingEnabled() { return messingEnabled; }
    public void setMessingEnabled(boolean v) { this.messingEnabled = v; }

    public int getTicksSinceLastGain() { return ticksSinceLastGain; }
    public void setTicksSinceLastGain(int v) { this.ticksSinceLastGain = v; }

    public float getLastPeeThreshold() { return lastPeeThreshold; }
    public void setLastPeeThreshold(float v) { this.lastPeeThreshold = v; }
    public float getLastMessThreshold() { return lastMessThreshold; }
    public void setLastMessThreshold(float v) { this.lastMessThreshold = v; }

    public boolean isInPottyCheck() { return inPottyCheck; }
    public void setInPottyCheck(boolean v) { this.inPottyCheck = v; }
    public long getPottyCheckStartTick() { return pottyCheckStartTick; }
    public void setPottyCheckStartTick(long v) { this.pottyCheckStartTick = v; }
    public boolean isPottyCheckPee() { return pottyCheckIsPee; }
    public void setPottyCheckPee(boolean v) { this.pottyCheckIsPee = v; }
    public float getPottyCheckThreshold() { return pottyCheckThreshold; }
    public void setPottyCheckThreshold(float v) { this.pottyCheckThreshold = v; }
    /** Base reaction ticks for current threshold. Higher continence = more time (scaled 0.5x–1.5x by continence/100). */
    public int getReactionTicksForCurrentCheck() {
        int base = QTE_REACTION_TICKS[QTE_REACTION_TICKS.length - 1];
        for (int i = 0; i < QTE_THRESHOLDS.length; i++) {
            if (pottyCheckThreshold <= QTE_THRESHOLDS[i]) {
                base = QTE_REACTION_TICKS[i];
                break;
            }
        }
        // Scale by continence: 0 continence = 0.5x time, 100 continence = 1.5x time
        float scale = 0.5f + (continence / MAX_GAUGE);
        return Math.max(60, (int) (base * scale));
    }

    /** Randomized allowed ticks for current check. Set when check starts; used for timeout. */
    public int getPottyCheckAllowedTicks() { return pottyCheckAllowedTicks; }
    public void setPottyCheckAllowedTicks(int v) { this.pottyCheckAllowedTicks = Math.max(40, v); }

    /** Potty chart: number of successful holds. */
    public int getSuccessCount() { return successCount; }
    /** Potty chart: number of accidents. */
    public int getAccidentCount() { return accidentCount; }
    public void setSuccessCount(int v) { this.successCount = Math.max(0, v); }
    public void setAccidentCount(int v) { this.accidentCount = Math.max(0, v); }

    public ItemStack getEquippedDiaper() { return equippedDiaper == null ? ItemStack.EMPTY : equippedDiaper; }
    public void setEquippedDiaper(ItemStack stack) { this.equippedDiaper = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack; }

    public ItemStack getEquippedPacifier() { return equippedPacifier == null ? ItemStack.EMPTY : equippedPacifier; }
    public void setEquippedPacifier(ItemStack stack) { this.equippedPacifier = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy(); }

    public ItemStack getEquippedMittens() { return equippedMittens == null ? ItemStack.EMPTY : equippedMittens; }
    public void setEquippedMittens(ItemStack stack) { this.equippedMittens = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy(); }

    public ItemStack getEquippedOnesie() { return equippedOnesie == null ? ItemStack.EMPTY : equippedOnesie; }
    public void setEquippedOnesie(ItemStack stack) { this.equippedOnesie = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy(); }

    public long getLastMagicDiaperRegenTick() { return lastMagicDiaperRegenTick; }
    public void setLastMagicDiaperRegenTick(long v) { this.lastMagicDiaperRegenTick = v; }

    /** Returns true if the player has a usable equipped diaper (non-empty, DiaperItem, not fully used). */
    public boolean hasUsableDiaper() {
        ItemStack diaper = getEquippedDiaper();
        if (diaper.isEmpty() || !(diaper.getItem() instanceof DiaperItem di)) return false;
        return !di.isFullyUsed(diaper);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(MAX_GAUGE, v));
    }
}
