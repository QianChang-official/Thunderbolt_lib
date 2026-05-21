package com.qianchang.ae2lt_api.internal.compat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class AE2LTCompatBootstrap {

    private AE2LTCompatBootstrap() {
    }

    public static void install(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, AE2LTCapabilityBridge::attachBlockEntityCapabilities);
        AE2LTLightningCollectorEventBridge.install();
    }
}
