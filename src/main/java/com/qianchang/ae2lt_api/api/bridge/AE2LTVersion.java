package com.qianchang.ae2lt_api.api.bridge;

import com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities;
import net.neoforged.fml.ModList;

import java.util.Optional;

/**
 * Version helpers for addon code that needs to gate AE2 Lightning Tech
 * integration behavior at runtime.
 *
 * <p>AE2LT 1.0.8 keeps the first-party lightning capability/event contracts
 * used by the previous checked release lines and adds a public wireless
 * frequency API under {@code com.moakiee.ae2lt.api.frequency}.</p>
 *
 * @since 1.0.4
 */
public final class AE2LTVersion {

    /** Thunderbolt_lib API version for this jar. */
    public static final String LIBRARY_API_VERSION = AE2LTCapabilities.API_VERSION;

    /** AE2 Lightning Tech release this Thunderbolt_lib version was checked against. */
    public static final String TARGET_AE2LT_VERSION = "1.0.8";

    /** First AE2LT release line that exposed the native first-party API package. */
    public static final String FIRST_PARTY_API_INTRODUCED_VERSION = "1.0.2";

    /** Newest AE2LT version whose native API and recipe schemas were verified. */
    public static final String FIRST_PARTY_API_LAST_VERIFIED_VERSION = TARGET_AE2LT_VERSION;

    /**
     * AE2LT release line that introduced the BE-level frequency-binding host
     * mechanism ({@code com.moakiee.ae2lt.grid.FrequencyBindingHost}).
     *
     * @since 1.0.5
     */
    public static final String FREQUENCY_BINDING_INTRODUCED_VERSION = "1.0.5";

    /** AE2LT release line that introduced the public wireless frequency API. */
    public static final String PUBLIC_FREQUENCY_API_INTRODUCED_VERSION = "1.0.8";

    private static final String AE2LT_MOD_ID = "ae2lt";

    private AE2LTVersion() {
    }

    /** Returns the installed AE2LT mod version, if AE2LT is loaded. */
    public static Optional<String> loadedAE2LTVersion() {
        return ModList.get()
                .getModContainerById(AE2LT_MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString());
    }

    /** Returns {@code true} when AE2 Lightning Tech is loaded at runtime. */
    public static boolean isAE2LTLoaded() {
        return ModList.get().isLoaded(AE2LT_MOD_ID);
    }

    /**
     * Returns whether the loaded AE2LT version is at least {@code minimumVersion}.
     *
     * <p>The comparison handles the numeric release parts used by AE2LT
     * ({@code major.minor.patch}) and ignores trailing metadata such as
     * {@code -beta} or {@code +build}.</p>
     */
    public static boolean isLoadedAE2LTAtLeast(String minimumVersion) {
        return loadedAE2LTVersion()
                .map(version -> compareDottedVersions(version, minimumVersion) >= 0)
                .orElse(false);
    }

    /**
     * Returns whether the loaded AE2LT version is within the range verified by
     * this library release.
     */
    public static boolean isLoadedAE2LTKnownCompatible() {
        return loadedAE2LTVersion()
                .map(AE2LTVersion::isKnownCompatibleAE2LTVersion)
                .orElse(false);
    }

    /**
     * Returns whether the loaded AE2LT version is at least
     * {@link #FREQUENCY_BINDING_INTRODUCED_VERSION}, i.e. supports the BE-level
     * frequency-binding host mechanism.
     *
     * <p>This is a version-only gate; combine with
     * {@link AE2LTNativeBridge#isFrequencyBindingAvailable()} when you also want
     * a classloader-level guarantee.</p>
     *
     * @since 1.0.5
     */
    public static boolean isLoadedAE2LTAtLeastFrequencyBinding() {
        return isLoadedAE2LTAtLeast(FREQUENCY_BINDING_INTRODUCED_VERSION);
    }

    /**
     * Returns whether the loaded AE2LT version is at least
     * {@link #PUBLIC_FREQUENCY_API_INTRODUCED_VERSION}.
     *
     * @since 1.0.8
     */
    public static boolean isLoadedAE2LTAtLeastPublicFrequencyApi() {
        return isLoadedAE2LTAtLeast(PUBLIC_FREQUENCY_API_INTRODUCED_VERSION);
    }

    /**
     * Returns whether {@code version} is in the verified first-party API range.
     * Future AE2LT versions may still work, but are intentionally not reported as
     * known-compatible until checked.
     */
    public static boolean isKnownCompatibleAE2LTVersion(String version) {
        return compareDottedVersions(version, FIRST_PARTY_API_INTRODUCED_VERSION) >= 0
                && compareDottedVersions(version, FIRST_PARTY_API_LAST_VERIFIED_VERSION) <= 0;
    }

    /**
     * Compares two dotted numeric version strings.
     *
     * @return a negative value if {@code left < right}, zero if equal, or a
     *         positive value if {@code left > right}
     */
    public static int compareDottedVersions(String left, String right) {
        int[] leftParts = parseVersion(left);
        int[] rightParts = parseVersion(right);
        int size = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < size; i++) {
            int leftPart = i < leftParts.length ? leftParts[i] : 0;
            int rightPart = i < rightParts.length ? rightParts[i] : 0;
            int comparison = Integer.compare(leftPart, rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static int[] parseVersion(String version) {
        String[] rawParts = version == null ? new String[0] : version.split("\\.");
        int[] parts = new int[rawParts.length];
        for (int i = 0; i < rawParts.length; i++) {
            parts[i] = parseLeadingInt(rawParts[i]);
        }
        return parts;
    }

    private static int parseLeadingInt(String value) {
        int end = 0;
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(0, end));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
