package com.qianchang.ae2lt_api.api.pattern;

/**
 * Immutable Thunderbolt_lib-side snapshot of AE2LT 1.0.11's public pattern-provider UI profile.
 *
 * @param packagedProviderUi whether the menu should render the packaged-provider chrome
 * @param modeSwitchVisible whether the provider-mode toggle should stay visible
 * @param filteredImportVisible whether the filtered-import toggle should stay visible
 * @param wirelessTuningVisible whether wireless tuning controls should stay visible
 * @param blockingModeVisible whether blocking-mode controls should stay visible
 * @param titleTranslationKey translation key advertised for the shared menu title
 * @since 1.0.11
 */
public record AE2LTPatternProviderUiProfileInfo(
        boolean packagedProviderUi,
        boolean modeSwitchVisible,
        boolean filteredImportVisible,
        boolean wirelessTuningVisible,
        boolean blockingModeVisible,
        String titleTranslationKey) {
}
