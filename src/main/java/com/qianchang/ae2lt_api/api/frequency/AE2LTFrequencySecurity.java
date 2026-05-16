package com.qianchang.ae2lt_api.api.frequency;

/**
 * Public security level snapshot for an AE2LT wireless frequency.
 *
 * <p>Mirrors AE2LT 1.0.8's
 * {@code com.moakiee.ae2lt.api.frequency.FrequencySecurity} enum without
 * requiring addons to compile against AE2LT's jar.</p>
 *
 * @since 1.0.8
 */
public enum AE2LTFrequencySecurity {
    PUBLIC,
    ENCRYPTED,
    PRIVATE,
    UNKNOWN;

    static AE2LTFrequencySecurity fromNativeName(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
