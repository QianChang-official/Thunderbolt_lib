package com.qianchang.ae2lt_api;

import com.mojang.logging.LogUtils;
import com.qianchang.ae2lt_api.api.AE2LTAPI;
import com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities;
import com.qianchang.ae2lt_api.internal.PluginLoader;
import com.qianchang.ae2lt_api.internal.compat.AE2LTCompatBootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Main mod class for the Thunderbolt_lib.
 *
 * <p>This mod provides a stable, versioned API surface for developing addons
 * that integrate with AE2 Lightning Tech (ae2lt). It exposes:
 * <ul>
 *   <li>NeoForge {@link AE2LTCapabilities capabilities} for lightning energy I/O</li>
 *   <li>A {@link PluginLoader plugin system} ({@link com.qianchang.ae2lt_api.api.plugin.AE2LTPlugin @AE2LTPlugin})</li>
 *   <li>Recipe JSON builders for AE2LT machine and ritual recipe types</li>
 *   <li>NeoForge events fired at addon-safe AE2LT integration points</li>
 * </ul>
 */
@Mod(AE2LTAddonFramework.MODID)
public class AE2LTAddonFramework {

    public static final String MODID = "ae2lt_api";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AE2LTAddonFramework(IEventBus modEventBus) {
        if (ModList.get().isLoaded("ae2lt")) {
            AE2LTCompatBootstrap.install(modEventBus);
        }
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[AE2LT API] Thunderbolt_lib {} initialized.", AE2LTCapabilities.API_VERSION);
        event.enqueueWork(() -> {
            AE2LTAPI.init();
            PluginLoader.discoverAndLoad();
        });
    }
}

