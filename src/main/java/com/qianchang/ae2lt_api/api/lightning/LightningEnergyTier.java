package com.qianchang.ae2lt_api.api.lightning;

import net.minecraft.util.StringRepresentable;

/**
 * The two tiers of lightning energy used by AE2 Lightning Tech.
 *
 * <p>High Voltage (HV) is the lower tier and can be collected directly from natural
 * lightning strikes. Extreme High Voltage (EHV) requires further refinement via
 * the Tesla Coil.</p>
 */
public enum LightningEnergyTier implements StringRepresentable {

    /** High Voltage lightning — the base tier, produced by the Lightning Collector. */
    HIGH_VOLTAGE("hv", "High Voltage", 1),

    /** Extreme High Voltage lightning — the refined tier, produced by the Tesla Coil. */
    EXTREME_HIGH_VOLTAGE("ehv", "Extreme High Voltage", 2);

    private final String serializedName;
    private final String displayName;
    private final int level;

    LightningEnergyTier(String serializedName, String displayName, int level) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.level = level;
    }

    /** The lowercase identifier used in recipe JSON (e.g. {@code "hv"}, {@code "ehv"}). */
    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /** Human-readable display name for GUIs and tooltips. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Numeric level — higher is more refined.
     * HV = 1, EHV = 2.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Look up a tier by its serialized JSON name.
     *
     * @param id The serialized name (case-insensitive).
     * @return The matching tier, or {@link #HIGH_VOLTAGE} as the default.
     */
    public static LightningEnergyTier fromId(String id) {
        for (LightningEnergyTier tier : values()) {
            if (tier.serializedName.equalsIgnoreCase(id)) {
                return tier;
            }
        }
        return HIGH_VOLTAGE;
    }
}
