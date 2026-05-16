package com.qianchang.ae2lt_api.api.frequency;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Immutable Thunderbolt_lib-side snapshot of the AE2LT wireless transmitter
 * registered for a frequency.
 *
 * @param dimension level key that owns the transmitter
 * @param pos transmitter block position
 * @param advanced whether the transmitter is the cross-dimensional variant
 * @since 1.0.8
 */
public record AE2LTTransmitterInfo(ResourceKey<Level> dimension, BlockPos pos, boolean advanced) {
}
