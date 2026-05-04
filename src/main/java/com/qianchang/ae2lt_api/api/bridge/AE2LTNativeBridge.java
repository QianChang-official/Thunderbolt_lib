package com.qianchang.ae2lt_api.api.bridge;

import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.resources.ResourceLocation;

/**
 * Detection helpers for AE2 Lightning Tech's own first-party API package
 * ({@code com.moakiee.ae2lt.api}, introduced in AE2LT 1.0.2 / 1.0.3).
 *
 * <h2>Why two APIs?</h2>
 * <p>Thunderbolt_lib's API ({@code com.qianchang.ae2lt_api.api}, namespace
 * {@code ae2lt_api}) predates AE2LT's own first-party API ({@code com.moakiee.ae2lt.api},
 * namespace {@code ae2lt}). The two are deliberately split: AE2LT's own package
 * notes that "addons that want to use this mod's first-party API must query these
 * capabilities, not the library's." The two namespaces are NOT interchangeable.</p>
 *
 * <p>For most addon authors, the library's API is the right choice: it works
 * standalone (without AE2LT installed), exposes recipe builders and a plugin
 * loader that AE2LT's first-party API does not, and is byte-stable across
 * Thunderbolt_lib releases. Use AE2LT's first-party API only when you specifically
 * need to talk to AE2LT's own block entities under their native namespace.</p>
 *
 * <h2>What this class provides</h2>
 * <ul>
 *   <li>{@link #isNativeApiAvailable()} — runtime check for whether AE2LT 1.0.2+
 *       is loaded with its first-party API package.</li>
 *   <li>{@link #nativeNamespace()} / {@link #libraryNamespace()} — the two
 *       resource-location namespaces in play.</li>
 *   <li>{@link #toNativeTierName(LightningEnergyTier)} — the serialized tier name
 *       used by AE2LT's own {@code LightningTier}, which happens to match the
 *       library's {@link LightningEnergyTier#getSerializedName()}.</li>
 * </ul>
 *
 * <p>This class deliberately does NOT depend on any
 * {@code com.moakiee.ae2lt.api.*} type at compile time: AE2LT is an optional
 * runtime dependency for Thunderbolt_lib, so all interactions with AE2LT's
 * first-party API stay reflective.</p>
 *
 * @since 1.0.3
 */
public final class AE2LTNativeBridge {

    private static final String NATIVE_CAPABILITIES_CLASS = "com.moakiee.ae2lt.api.AE2LTCapabilities";
    private static final String LIGHTNING_ENERGY = "lightning_energy";
    private static final String LIGHTNING_ENERGY_ITEM = "lightning_energy_item";

    private static final ResourceLocation NATIVE_LIGHTNING_ENERGY_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(nativeNamespace(), LIGHTNING_ENERGY);
    private static final ResourceLocation NATIVE_LIGHTNING_ENERGY_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath(nativeNamespace(), LIGHTNING_ENERGY_ITEM);
    private static final ResourceLocation LIBRARY_LIGHTNING_ENERGY_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(libraryNamespace(), LIGHTNING_ENERGY);
    private static final ResourceLocation LIBRARY_LIGHTNING_ENERGY_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath(libraryNamespace(), LIGHTNING_ENERGY_ITEM);

    private static volatile Boolean cachedAvailability;

    private AE2LTNativeBridge() {
    }

    /**
     * @return {@code true} if AE2LT's first-party API package
     *         ({@code com.moakiee.ae2lt.api}) is present at runtime. Caches the
     *         result; classloader state cannot legitimately change after mod load.
     */
    public static boolean isNativeApiAvailable() {
        Boolean cached = cachedAvailability;
        if (cached != null) {
            return cached;
        }
        boolean available;
        try {
            Class.forName(NATIVE_CAPABILITIES_CLASS, false,
                    AE2LTNativeBridge.class.getClassLoader());
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
        cachedAvailability = available;
        return available;
    }

    /** AE2LT's own resource-location namespace ({@code "ae2lt"}). */
    public static String nativeNamespace() {
        return "ae2lt";
    }

    /** Thunderbolt_lib's resource-location namespace ({@code "ae2lt_api"}). */
    public static String libraryNamespace() {
        return "ae2lt_api";
    }

    /**
     * AE2LT first-party block capability id ({@code ae2lt:lightning_energy}).
     *
     * @since 1.0.4
     */
    public static ResourceLocation nativeLightningEnergyBlockId() {
        return NATIVE_LIGHTNING_ENERGY_BLOCK_ID;
    }

    /**
     * AE2LT first-party item capability id ({@code ae2lt:lightning_energy_item}).
     *
     * @since 1.0.4
     */
    public static ResourceLocation nativeLightningEnergyItemId() {
        return NATIVE_LIGHTNING_ENERGY_ITEM_ID;
    }

    /**
     * Thunderbolt_lib block capability id ({@code ae2lt_api:lightning_energy}).
     *
     * @since 1.0.4
     */
    public static ResourceLocation libraryLightningEnergyBlockId() {
        return LIBRARY_LIGHTNING_ENERGY_BLOCK_ID;
    }

    /**
     * Thunderbolt_lib item capability id ({@code ae2lt_api:lightning_energy_item}).
     *
     * @since 1.0.4
     */
    public static ResourceLocation libraryLightningEnergyItemId() {
        return LIBRARY_LIGHTNING_ENERGY_ITEM_ID;
    }

    /**
     * Serialized tier name shared by both APIs. Both
     * {@link LightningEnergyTier#getSerializedName()} and AE2LT's own
     * {@code LightningTier#getSerializedName()} use the same strings
     * ({@code "high_voltage"} / {@code "extreme_high_voltage"}), so this value can
     * be passed safely across the API boundary in JSON, NBT, or packets.
     *
     * @param tier library-side tier
     * @return the serialized name; never {@code null}
     */
    public static String toNativeTierName(LightningEnergyTier tier) {
        return tier.getSerializedName();
    }
}
