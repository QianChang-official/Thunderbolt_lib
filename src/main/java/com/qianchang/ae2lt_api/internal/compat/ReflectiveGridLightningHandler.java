package com.qianchang.ae2lt_api.internal.compat;

import appeng.api.config.Actionable;
import com.qianchang.ae2lt_api.api.lightning.ILightningEnergyHandler;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.world.level.block.entity.BlockEntity;

final class ReflectiveGridLightningHandler implements ILightningEnergyHandler {

    private final BlockEntity owner;

    ReflectiveGridLightningHandler(BlockEntity owner) {
        this.owner = owner;
    }

    @Override
    public long getLightningStored(LightningEnergyTier tier) {
        return AE2LTReflection.extractFromGrid(owner, tier, Long.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public long getLightningCapacity(LightningEnergyTier tier) {
        return AE2LTReflection.hasGrid(owner) ? Long.MAX_VALUE : 0L;
    }

    @Override
    public long insertLightning(LightningEnergyTier tier, long amount, boolean simulate) {
        return AE2LTReflection.insertIntoGrid(owner, tier, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
    }

    @Override
    public long extractLightning(LightningEnergyTier tier, long amount, boolean simulate) {
        return AE2LTReflection.extractFromGrid(owner, tier, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
    }

    @Override
    public boolean canInsert(LightningEnergyTier tier) {
        return AE2LTReflection.hasGrid(owner);
    }

    @Override
    public boolean canExtract(LightningEnergyTier tier) {
        return AE2LTReflection.hasGrid(owner);
    }
}
