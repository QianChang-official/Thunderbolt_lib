package com.qianchang.ae2lt_api.api.plugin;

/**
 * Interface for AE2 Lightning Tech addon plugins.
 *
 * <p>Implement this interface and annotate your class with {@link AE2LTPlugin} to
 * participate in the addon framework's initialization lifecycle. All methods are
 * optional (default no-op implementations are provided).</p>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #onInitialize(AE2LTApiContext)} — called during
 *       {@code FMLCommonSetupEvent}, after NeoForge capabilities are registered.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @AE2LTPlugin
 * public class MyAddonPlugin implements IAE2LTPlugin {
 *
 *     @Override
 *     public void onInitialize(AE2LTApiContext ctx) {
 *         MyAddon.LOGGER.info("AE2LT loaded: {}", ctx.isAE2LTLoaded());
 *     }
 * }
 * }</pre>
 */
public interface IAE2LTPlugin {

    /**
     * Called when the AE2LT Addon Framework has finished its setup.
     * Use this method to perform any initialization that depends on
     * the framework or AE2LT being loaded.
     *
     * @param ctx Runtime context — use this to check which mods are present.
     */
    default void onInitialize(AE2LTApiContext ctx) {}
}
