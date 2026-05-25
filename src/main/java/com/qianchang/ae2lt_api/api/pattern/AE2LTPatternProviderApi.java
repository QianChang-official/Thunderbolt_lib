package com.qianchang.ae2lt_api.api.pattern;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflective bridge for AE2LT 1.0.11's public overloaded pattern-provider UI profile API.
 *
 * <p>AE2LT exposes this contract under {@code com.moakiee.ae2lt.api.pattern}.
 * Thunderbolt_lib mirrors it reflectively so addon code can inspect shared menu
 * customization flags without taking a hard compile-time dependency on AE2LT.</p>
 *
 * <p>All methods fail closed and return empty / {@code false} when AE2LT is
 * absent, older than 1.0.11, or if the runtime contract drifts.</p>
 *
 * @since 1.0.11
 */
public final class AE2LTPatternProviderApi {

    public static final String PATTERN_PROVIDER_UI_PROFILE_CLASS_NAME =
            "com.moakiee.ae2lt.api.pattern.PatternProviderUiProfile";
    public static final String DEFAULT_TITLE_TRANSLATION_KEY =
            "ae2lt.gui.title.overloaded_pattern_provider";

    private static volatile Contract cachedContract;
    private static volatile boolean contractLookupComplete;

    private AE2LTPatternProviderApi() {
    }

    /** Returns {@code true} if AE2LT 1.0.11's public pattern-provider UI profile is loadable. */
    public static boolean isRuntimeAvailable() {
        return contract() != null;
    }

    /** Returns whether the block entity implements AE2LT's public pattern-provider UI profile API. */
    public static boolean isUiProfileHost(BlockEntity blockEntity) {
        Contract contract = contract();
        return contract != null
                && blockEntity != null
                && contract.patternProviderUiProfileClass.isInstance(blockEntity);
    }

    /** Returns a library-side snapshot of the public pattern-provider UI profile, if available. */
    public static Optional<AE2LTPatternProviderUiProfileInfo> getUiProfile(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return Optional.empty();
        }
        Contract contract = contract();
        if (contract == null || !contract.patternProviderUiProfileClass.isInstance(blockEntity)) {
            return Optional.empty();
        }
        try {
            String titleTranslationKey = stringOrDefault(
                    contract.titleTranslationKey.invoke(blockEntity),
                    DEFAULT_TITLE_TRANSLATION_KEY);
            return Optional.of(new AE2LTPatternProviderUiProfileInfo(
                    booleanValue(contract.packagedProviderUi.invoke(blockEntity)),
                    booleanValue(contract.modeSwitchVisible.invoke(blockEntity)),
                    booleanValue(contract.filteredImportVisible.invoke(blockEntity)),
                    booleanValue(contract.wirelessTuningVisible.invoke(blockEntity)),
                    booleanValue(contract.blockingModeVisible.invoke(blockEntity)),
                    titleTranslationKey));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private static boolean booleanValue(Object value) {
        return value instanceof Boolean bool && bool;
    }

    private static String stringOrDefault(Object value, String defaultValue) {
        return value instanceof String text && !text.isBlank() ? text : defaultValue;
    }

    private static Contract contract() {
        if (contractLookupComplete) {
            return cachedContract;
        }
        cachedContract = loadContract();
        contractLookupComplete = true;
        return cachedContract;
    }

    private static Contract loadContract() {
        try {
            ClassLoader loader = AE2LTPatternProviderApi.class.getClassLoader();
            Class<?> uiProfileClass = Class.forName(PATTERN_PROVIDER_UI_PROFILE_CLASS_NAME, false, loader);
            return new Contract(
                    uiProfileClass,
                    uiProfileClass.getMethod("ae2lt$isPackagedProviderUi"),
                    uiProfileClass.getMethod("ae2lt$isModeSwitchVisible"),
                    uiProfileClass.getMethod("ae2lt$isFilteredImportVisible"),
                    uiProfileClass.getMethod("ae2lt$isWirelessTuningVisible"),
                    uiProfileClass.getMethod("ae2lt$isBlockingModeVisible"),
                    uiProfileClass.getMethod("ae2lt$titleTranslationKey"));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private record Contract(
            Class<?> patternProviderUiProfileClass,
            Method packagedProviderUi,
            Method modeSwitchVisible,
            Method filteredImportVisible,
            Method wirelessTuningVisible,
            Method blockingModeVisible,
            Method titleTranslationKey) {
    }
}
