package com.qianchang.ae2lt_api.api;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.bridge.AE2LTNativeBridge;
import com.qianchang.ae2lt_api.api.bridge.AE2LTVersion;
import com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities;
import com.qianchang.ae2lt_api.api.lightning.ILightningEnergyHandler;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Main static entry point for the AE2 Lightning Tech Addon Framework API.
 */
public final class AE2LTAPI {

    private static AE2LTAPI instance;

    private AE2LTAPI() {
    }

    /**
     * Returns the singleton API instance.
     *
     * @throws IllegalStateException if the framework has not yet been initialized.
     */
    public static AE2LTAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "[AE2LT API] API accessed before initialization. "
                            + "Ensure your mod depends on '" + AE2LTAddonFramework.MODID + "'.");
        }
        return instance;
    }

    /** Called by the framework mod class during setup — do not call this yourself. */
    public static void init() {
        if (instance == null) {
            instance = new AE2LTAPI();
        }
    }

    /** Returns {@code true} if AE2 Lightning Tech (ae2lt) is loaded at runtime. */
    public boolean isAE2LTLoaded() {
        return ModList.get().isLoaded("ae2lt");
    }

    /** Returns this Thunderbolt_lib API version. */
    public String getApiVersion() {
        return AE2LTCapabilities.API_VERSION;
    }

    /** Returns the AE2LT release version this Thunderbolt_lib build was verified against. */
    public String getTargetAE2LTVersion() {
        return AE2LTVersion.TARGET_AE2LT_VERSION;
    }

    /** Returns the installed AE2LT mod version, if AE2LT is loaded. */
    public Optional<String> getLoadedAE2LTVersion() {
        return AE2LTVersion.loadedAE2LTVersion();
    }

    /** Returns whether the installed AE2LT version is at least {@code minimumVersion}. */
    public boolean isAE2LTVersionAtLeast(String minimumVersion) {
        return AE2LTVersion.isLoadedAE2LTAtLeast(minimumVersion);
    }

    /**
     * Returns {@code true} if AE2LT's BE-level frequency-binding mechanism is
     * available at runtime (introduced in AE2LT 1.0.5). Convenience facade over
     * {@link AE2LTNativeBridge#isFrequencyBindingAvailable()}.
     *
     * @since 1.0.5
     */
    public boolean isAE2LTFrequencyBindingAvailable() {
        return AE2LTNativeBridge.isFrequencyBindingAvailable();
    }

    /** Returns {@code true} if the given mod id is loaded at runtime. */
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * Queries the lightning-energy block capability from a block at the given position.
     */
    public Optional<ILightningEnergyHandler> getLightningHandler(
            Level level, BlockPos pos, @Nullable Direction side) {
        ILightningEnergyHandler handler =
                level.getCapability(AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK, pos, side);
        return Optional.ofNullable(handler);
    }

    /**
     * Queries the lightning-energy item capability from an item stack.
     */
    public Optional<ILightningEnergyHandler> getLightningHandler(ItemStack stack) {
        ILightningEnergyHandler handler = stack.getCapability(AE2LTCapabilities.LIGHTNING_ENERGY_ITEM);
        return Optional.ofNullable(handler);
    }

    /**
     * Returns the amount of lightning energy stored for a given tier at a position.
     */
    public long getLightningStored(
            Level level, BlockPos pos, @Nullable Direction side, LightningEnergyTier tier) {
        return getLightningHandler(level, pos, side)
                .map(h -> h.getLightningStored(tier))
                .orElse(0L);
    }

    /**
     * Returns the amount of lightning energy stored in an item stack for a given tier.
     */
    public long getLightningStored(ItemStack stack, LightningEnergyTier tier) {
        return getLightningHandler(stack)
                .map(h -> h.getLightningStored(tier))
                .orElse(0L);
    }

    /**
     * Inserts lightning energy into a block capability. Returns 0 if no handler is present.
     */
    public long insertLightning(
            Level level, BlockPos pos, @Nullable Direction side,
            LightningEnergyTier tier, long amount, boolean simulate) {
        return getLightningHandler(level, pos, side)
                .map(h -> h.insertLightning(tier, amount, simulate))
                .orElse(0L);
    }

    /**
     * Inserts lightning energy into an item capability. Returns 0 if no handler is present.
     */
    public long insertLightning(ItemStack stack, LightningEnergyTier tier, long amount, boolean simulate) {
        return getLightningHandler(stack)
                .map(h -> h.insertLightning(tier, amount, simulate))
                .orElse(0L);
    }

    /**
     * Extracts lightning energy from a block capability. Returns 0 if no handler is present.
     */
    public long extractLightning(
            Level level, BlockPos pos, @Nullable Direction side,
            LightningEnergyTier tier, long amount, boolean simulate) {
        return getLightningHandler(level, pos, side)
                .map(h -> h.extractLightning(tier, amount, simulate))
                .orElse(0L);
    }

    /**
     * Extracts lightning energy from an item capability. Returns 0 if no handler is present.
     */
    public long extractLightning(ItemStack stack, LightningEnergyTier tier, long amount, boolean simulate) {
        return getLightningHandler(stack)
                .map(h -> h.extractLightning(tier, amount, simulate))
                .orElse(0L);
    }
}
