package com.qianchang.ae2lt_api.internal.compat;

import appeng.api.config.Actionable;
import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.event.LightningCollectedEvent;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

final class AE2LTLightningCollectorEventBridge {

    private static final String NATURAL_WEATHER_LIGHTNING_TAG = "ae2lt.natural_weather_lightning";
    private static final String TRANSFORMATION_CHECKED_TAG = "ae2lt.natural_transform_checked";
    private static final int COLLECTOR_WORKING_DURATION_TICKS = 20;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLightningTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LightningBolt lightningBolt)
                || !(lightningBolt.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag data = lightningBolt.getPersistentData();
        if (data.getBoolean(TRANSFORMATION_CHECKED_TAG)) {
            return;
        }

        CollectorTarget target = findCollector(serverLevel, lightningBolt.blockPosition());
        if (target == null) {
            return;
        }

        boolean naturalWeatherLightning = data.getBoolean(NATURAL_WEATHER_LIGHTNING_TAG);
        LightningEnergyTier tier = naturalWeatherLightning
                ? LightningEnergyTier.EXTREME_HIGH_VOLTAGE
                : LightningEnergyTier.HIGH_VOLTAGE;

        data.putBoolean(TRANSFORMATION_CHECKED_TAG, true);

        try {
            handleCollectorCapture(serverLevel, target.collector(), tier, naturalWeatherLightning);
            AE2LTReflection.invokeNearbyLightningRodTransform(serverLevel, lightningBolt.blockPosition(), naturalWeatherLightning);
        } catch (RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error("[AE2LT API] Collector event bridge failed, falling back to AE2LT defaults.", e);
            data.remove(TRANSFORMATION_CHECKED_TAG);
        }
    }

    private void handleCollectorCapture(
            ServerLevel serverLevel,
            BlockEntity collector,
            LightningEnergyTier tier,
            boolean naturalWeatherLightning) {
        Object runtimeTier = AE2LTReflection.getRuntimeTier(tier);
        if (runtimeTier == null) {
            return;
        }

        Object preview = AE2LTReflection.invoke(collector, "getPreview", new Class<?>[]{runtimeTier.getClass()}, runtimeTier);
        int rolledOutput = preview == null
                ? 0
                : (Integer) AE2LTReflection.invoke(preview, "roll", new Class<?>[]{RandomSource.class}, serverLevel.random);

        long hvAmount = tier == LightningEnergyTier.HIGH_VOLTAGE ? rolledOutput : 0L;
        long ehvAmount = tier == LightningEnergyTier.EXTREME_HIGH_VOLTAGE ? rolledOutput : 0L;

        LightningCollectedEvent collectedEvent =
                new LightningCollectedEvent(serverLevel, collector.getBlockPos(), hvAmount, ehvAmount);
        NeoForge.EVENT_BUS.post(collectedEvent);

        if (collectedEvent.isCanceled()) {
            return;
        }

        long amountToInsert = collectedEvent.getAmount(tier);
        long inserted = AE2LTReflection.insertIntoGrid(collector, tier, amountToInsert, Actionable.MODULATE);
        if (inserted <= 0L) {
            return;
        }

        if (tier == LightningEnergyTier.EXTREME_HIGH_VOLTAGE
                && naturalWeatherLightning
                && AE2LTReflection.invokeBoolean(
                        collector,
                        "canCultivateFromNaturalStrike",
                        new Class<?>[]{ServerLevel.class},
                        serverLevel)) {
            boolean cultivated = AE2LTReflection.invokeBoolean(
                    collector,
                    "cultivateCrystal",
                    new Class<?>[]{RandomSource.class},
                    serverLevel.random);
            if (cultivated) {
                AE2LTReflection.setField(collector, "lastNaturalCultivationGameTime", serverLevel.getGameTime());
            }
        }

        AE2LTReflection.setField(collector, "lastCaptureGameTime", serverLevel.getGameTime());
        AE2LTReflection.setField(collector, "cooldownTicks", AE2LTReflection.lightningCollectorCooldownTicks());
        AE2LTReflection.setField(collector, "workingTicks", COLLECTOR_WORKING_DURATION_TICKS);
        AE2LTReflection.invoke(collector, "updateWorkingBlockState", new Class<?>[]{boolean.class}, true);
        AE2LTReflection.invoke(collector, "saveChanges", new Class<?>[0]);
        AE2LTReflection.invoke(collector, "markForClientUpdate", new Class<?>[0]);
    }

    private CollectorTarget findCollector(ServerLevel level, BlockPos lightningPos) {
        for (int yOffset = 0; yOffset <= 2; yOffset++) {
            BlockPos rodPos = lightningPos.below(yOffset);
            if (!level.getBlockState(rodPos).is(Blocks.LIGHTNING_ROD)) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(rodPos.below());
            if (blockEntity == null || !AE2LTReflection.isLightningCollector(blockEntity)) {
                continue;
            }
            if (!AE2LTReflection.invokeBoolean(blockEntity, "canCaptureLightning", new Class<?>[0])) {
                continue;
            }

            return new CollectorTarget(blockEntity);
        }
        return null;
    }

    private record CollectorTarget(BlockEntity collector) {
    }
}
