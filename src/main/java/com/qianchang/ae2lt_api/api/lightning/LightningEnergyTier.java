package com.qianchang.ae2lt_api.api.lightning;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The two tiers of lightning energy used by AE2 Lightning Tech.
 *
 * <p>High Voltage (HV) is the lower tier and can be collected directly from natural
 * lightning strikes. Extreme High Voltage (EHV) requires further refinement via
 * the Tesla Coil.</p>
 *
 * <p>The two constants and their {@linkplain #getSerializedName() serialized names}
 * ({@code "high_voltage"} / {@code "extreme_high_voltage"}) are part of the frozen
 * API contract. They match AE2LT's own {@code com.moakiee.ae2lt.api.lightning.LightningTier}
 * exactly, so values are interchangeable across the JSON / NBT / packet
 * boundary.</p>
 */
public enum LightningEnergyTier implements StringRepresentable {

    /** High Voltage lightning — the base tier, produced by the Lightning Collector. */
    HIGH_VOLTAGE("high_voltage", "High Voltage", 1),

    /** Extreme High Voltage lightning — the refined tier, produced by the Tesla Coil. */
    EXTREME_HIGH_VOLTAGE("extreme_high_voltage", "Extreme High Voltage", 2);

    /**
     * Mojang {@link Codec} for use in NBT, datapack JSON, or any other
     * {@code DataResult}-driven codec pipeline.
     *
     * @since 1.0.3
     */
    public static final Codec<LightningEnergyTier> CODEC =
            StringRepresentable.fromEnum(LightningEnergyTier::values);

    /**
     * Network {@link StreamCodec} for use in {@code CustomPacketPayload} or any
     * other vanilla packet serialization. Encodes a single byte of ordinal data
     * (matches AE2LT's own wire format so packets are interoperable).
     *
     * @since 1.0.3
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, LightningEnergyTier> STREAM_CODEC =
            StreamCodec.of(
                    (buf, tier) -> buf.writeByte(tier.ordinal()),
                    buf -> fromOrdinal(buf.readByte()));

    private final String serializedName;
    private final String displayName;
    private final int level;

    LightningEnergyTier(String serializedName, String displayName, int level) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.level = level;
    }

    /** The lowercase identifier used in recipe JSON (e.g. {@code "high_voltage"}). */
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
        if ("hv".equalsIgnoreCase(id)) {
            return HIGH_VOLTAGE;
        }
        if ("ehv".equalsIgnoreCase(id)) {
            return EXTREME_HIGH_VOLTAGE;
        }
        return HIGH_VOLTAGE;
    }

    /**
     * Decode a tier from a network ordinal byte. Mirrors AE2LT's own
     * {@code LightningTier.fromOrdinal} so addons can use a single ordinal wire
     * format compatible with both this library and AE2LT's first-party API.
     *
     * <p>Unknown ordinal values intentionally degrade to {@link #HIGH_VOLTAGE}
     * rather than throwing, so future-facing or malformed packet data does not
     * hard-fail.</p>
     *
     * @param ordinal byte value ({@link #ordinal()}) read off the network
     * @return the matching tier, or {@link #HIGH_VOLTAGE} on unknown ordinals
     * @since 1.0.3
     */
    public static LightningEnergyTier fromOrdinal(int ordinal) {
        return ordinal == EXTREME_HIGH_VOLTAGE.ordinal() ? EXTREME_HIGH_VOLTAGE : HIGH_VOLTAGE;
    }
}
