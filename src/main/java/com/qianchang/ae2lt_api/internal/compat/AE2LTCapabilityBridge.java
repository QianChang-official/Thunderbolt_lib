package com.qianchang.ae2lt_api.internal.compat;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

final class AE2LTCapabilityBridge {

    private static final ResourceLocation BLOCK_ENTITY_CAP_PROVIDER_ID =
            new ResourceLocation("ae2lt_api:block_entity_cap_provider");

    private AE2LTCapabilityBridge() {
    }

    static void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity blockEntity = event.getObject();
        if (!AE2LTReflection.shouldAttachBridge(blockEntity)) {
            return;
        }

        var provider = new AttachedBlockEntityCapabilityProvider(blockEntity);
        event.addCapability(BLOCK_ENTITY_CAP_PROVIDER_ID, provider);
        event.addListener(provider::invalidate);
    }

    private static final class AttachedBlockEntityCapabilityProvider implements ICapabilityProvider {
        private final BlockEntity blockEntity;
        private LazyOptional<ReflectiveGridLightningHandler> handler;

        private AttachedBlockEntityCapabilityProvider(BlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            if (capability != AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK) {
                return LazyOptional.empty();
            }
            if (!AE2LTReflection.hasGrid(blockEntity)) {
                return LazyOptional.empty();
            }
            if (handler == null) {
                handler = LazyOptional.of(() -> new ReflectiveGridLightningHandler(blockEntity));
            }
            return handler.cast();
        }

        private void invalidate() {
            if (handler != null) {
                handler.invalidate();
                handler = null;
            }
            AE2LTAddonFramework.LOGGER.debug(
                    "[AE2LT API] Invalidated attached lightning capability bridge for {}.",
                    blockEntity.getType());
        }
    }
}
