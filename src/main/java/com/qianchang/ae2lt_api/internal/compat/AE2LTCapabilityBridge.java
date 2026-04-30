package com.qianchang.ae2lt_api.internal.compat;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

final class AE2LTCapabilityBridge {

    private AE2LTCapabilityBridge() {
    }

    static void registerCapabilities(RegisterCapabilitiesEvent event) {
        int registered = 0;
        for (ResourceLocation id : AE2LTReflection.bridgedBlockEntityIds()) {
            if (registerBridge(event, id)) {
                registered++;
            }
        }
        AE2LTAddonFramework.LOGGER.info("[AE2LT API] Registered {} AE2LT lightning capability bridge(s).", registered);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean registerBridge(RegisterCapabilitiesEvent event, ResourceLocation id) {
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            AE2LTAddonFramework.LOGGER.warn("[AE2LT API] Skipping missing AE2LT block entity type {}.", id);
            return false;
        }

        event.registerBlockEntity(
                AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK,
                (BlockEntityType) type,
                (blockEntity, side) -> AE2LTReflection.hasGrid(blockEntity)
                        ? new ReflectiveGridLightningHandler(blockEntity)
                        : null);
        return true;
    }
}
