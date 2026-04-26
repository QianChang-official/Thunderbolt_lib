package com.qianchang.ae2lt_api.api.plugin;

/**
 * Context object passed to plugins during initialization.
 *
 * <p>Provides runtime information about the loaded mods and the current
 * game state so that plugins can make conditional decisions.</p>
 */
public interface AE2LTApiContext {

    /**
     * Checks whether a mod with the given mod-id is currently loaded.
     *
     * @param modId The mod identifier to check.
     * @return {@code true} if the mod is present at runtime.
     */
    boolean isModLoaded(String modId);

    /** Shortcut for {@code isModLoaded("ae2lt")}. */
    default boolean isAE2LTLoaded() {
        return isModLoaded("ae2lt");
    }

    /** Shortcut for {@code isModLoaded("ae2")}. */
    default boolean isAE2Loaded() {
        return isModLoaded("ae2");
    }
}
