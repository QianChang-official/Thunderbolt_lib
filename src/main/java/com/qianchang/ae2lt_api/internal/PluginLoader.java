package com.qianchang.ae2lt_api.internal;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.plugin.AE2LTApiContext;
import com.qianchang.ae2lt_api.api.plugin.AE2LTPlugin;
import com.qianchang.ae2lt_api.api.plugin.IAE2LTPlugin;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Internal plugin loader that discovers and invokes all {@link IAE2LTPlugin} implementations
 * annotated with {@link AE2LTPlugin}.
 *
 * <p>Discovery uses Java's {@link ServiceLoader} mechanism.  To register a plugin,
 * create the file:</p>
 * <pre>
 *   src/main/resources/META-INF/services/com.qianchang.ae2lt_api.api.plugin.IAE2LTPlugin
 * </pre>
 * <p>and list the fully-qualified class name(s) of your plugin implementations,
 * one per line. The class must also be annotated with {@link AE2LTPlugin}.</p>
 *
 * <p>This class is internal to the framework — addon mods should not reference it directly.</p>
 */
public final class PluginLoader {

    private static final List<IAE2LTPlugin> loadedPlugins = new ArrayList<>();

    private PluginLoader() {}

    /**
     * Discovers all {@link IAE2LTPlugin} implementations via {@link ServiceLoader},
     * filters those annotated with {@link AE2LTPlugin}, and calls their lifecycle methods.
     *
     * <p>Called by {@link AE2LTAddonFramework} during {@code FMLCommonSetupEvent}.</p>
     */
    public static void discoverAndLoad() {
        AE2LTApiContext ctx = new ContextImpl();

        ServiceLoader<IAE2LTPlugin> loader = ServiceLoader.load(IAE2LTPlugin.class);
        for (IAE2LTPlugin plugin : loader) {
            if (!plugin.getClass().isAnnotationPresent(AE2LTPlugin.class)) {
                AE2LTAddonFramework.LOGGER.warn(
                        "[AE2LT API] Plugin {} is missing @AE2LTPlugin annotation — skipping.",
                        plugin.getClass().getName());
                continue;
            }
            try {
                plugin.onInitialize(ctx);
                loadedPlugins.add(plugin);
                AE2LTAddonFramework.LOGGER.info(
                        "[AE2LT API] Loaded plugin: {}", plugin.getClass().getName());
            } catch (Exception e) {
                AE2LTAddonFramework.LOGGER.error(
                        "[AE2LT API] Failed to initialize plugin {}: {}",
                        plugin.getClass().getName(), e.getMessage(), e);
            }
        }

        AE2LTAddonFramework.LOGGER.info(
                "[AE2LT API] {} plugin(s) loaded.", loadedPlugins.size());
    }

    /** Returns an unmodifiable view of all successfully loaded plugins. */
    public static List<IAE2LTPlugin> getLoadedPlugins() {
        return List.copyOf(loadedPlugins);
    }

    private static final class ContextImpl implements AE2LTApiContext {
        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }
    }
}
