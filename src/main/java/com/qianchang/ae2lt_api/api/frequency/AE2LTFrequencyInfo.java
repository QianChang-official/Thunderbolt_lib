package com.qianchang.ae2lt_api.api.frequency;

import java.util.UUID;

/**
 * Immutable Thunderbolt_lib-side snapshot of AE2LT 1.0.8 frequency metadata.
 *
 * @param id stable frequency id greater than zero
 * @param name display name chosen in AE2LT's frequency UI
 * @param color packed {@code 0xRRGGBB} color
 * @param owner UUID of the player that created the frequency
 * @param security access level required to use the frequency
 * @since 1.0.8
 */
public record AE2LTFrequencyInfo(
        int id,
        String name,
        int color,
        UUID owner,
        AE2LTFrequencySecurity security) {
}
