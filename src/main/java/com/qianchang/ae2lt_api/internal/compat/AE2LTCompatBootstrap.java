package com.qianchang.ae2lt_api.internal.compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class AE2LTCompatBootstrap {

    private AE2LTCompatBootstrap() {
    }

    public static void install(IEventBus modEventBus) {
        modEventBus.addListener(AE2LTCapabilityBridge::registerCapabilities);
        NeoForge.EVENT_BUS.register(new AE2LTLightningCollectorEventBridge());
    }
}
