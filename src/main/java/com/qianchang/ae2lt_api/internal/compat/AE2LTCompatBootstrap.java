package com.qianchang.ae2lt_api.internal.compat;

import net.neoforged.bus.api.IEventBus;

public final class AE2LTCompatBootstrap {

    private AE2LTCompatBootstrap() {
    }

    public static void install(IEventBus modEventBus) {
        modEventBus.addListener(AE2LTCapabilityBridge::registerCapabilities);
        AE2LTLightningCollectorEventBridge.install();
    }
}
