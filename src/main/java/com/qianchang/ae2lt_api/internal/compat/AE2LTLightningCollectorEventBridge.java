package com.qianchang.ae2lt_api.internal.compat;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.event.LightningCollectedEvent;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Method;
import java.util.function.Consumer;

final class AE2LTLightningCollectorEventBridge {

    private static final EventPriority BRIDGE_PRIORITY = EventPriority.LOWEST;
    private static final String NATIVE_LIGHTNING_COLLECTED_EVENT_CLASS =
            "com.moakiee.ae2lt.api.event.LightningCollectedEvent";
    private static volatile BridgeContract bridgeContract;

    private AE2LTLightningCollectorEventBridge() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void install() {
        try {
            Class<? extends Event> nativeEventClass = loadNativeEventClass();
            if (nativeEventClass == null) {
            AE2LTAddonFramework.LOGGER.warn(
                "[AE2LT API] Skipping lightning event mirror because AE2LT's public LightningCollectedEvent is unavailable. "
                    + "Thunderbolt_lib's library-side LightningCollectedEvent will not fire, but capability bridges, recipe builders, and plugin bootstrap remain active.");
            return;
            }

            BridgeContract contract = resolveBridgeContract(nativeEventClass);
            bridgeContract = contract;

            // LOWEST is intentional: AE2LT posts its public event inside
            // LightningCollectorBlockEntity#captureLightning and only reads the
            // final canceled/amount state after EventBus.post(...) returns.
            // Registering this mirror late lets Thunderbolt_lib see the final
            // uncanceled AE2LT payload and write back library-side edits before
            // AE2LT inserts into the grid.
            NeoForge.EVENT_BUS.addListener(
                BRIDGE_PRIORITY,
                false,
                (Class) nativeEventClass,
                (Consumer) (event -> onNativeLightningCollected((Event) event)));

            AE2LTAddonFramework.LOGGER.debug(
                    "[AE2LT API] Installed lightning compatibility bridge at {} priority with cached native event methods.",
                    BRIDGE_PRIORITY);
        } catch (RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] AE2LT compatibility bridge failed to initialize; library-side LightningCollectedEvent mirroring is disabled. "
                        + "Capability bridges, recipe builders, and plugin bootstrap remain active.",
                e);
        }
    }

    private static void onNativeLightningCollected(Event nativeEvent) {
        try {
                BridgeContract contract = bridgeContract;
                if (contract == null) {
                AE2LTAddonFramework.LOGGER.warn(
                    "[AE2LT API] Received AE2LT lightning event before the compatibility bridge contract was initialized; skipping mirror.");
                return;
                }

                ServerLevel level = invokeCached(contract.getLevel(), nativeEvent) instanceof ServerLevel value
                    ? value
                    : null;
                BlockPos collectorPos = invokeCached(contract.getCollectorPos(), nativeEvent) instanceof BlockPos value
                    ? value
                    : null;
                LightningEnergyTier tier = mapNativeTier(invokeCached(contract.getTier(), nativeEvent));
                boolean naturalWeather = invokeCached(contract.isNaturalWeather(), nativeEvent) instanceof Boolean value && value;
                long amount = invokeCached(contract.getAmount(), nativeEvent) instanceof Number value
                    ? value.longValue()
                    : 0L;

            if (level == null || collectorPos == null || tier == null) {
                AE2LTAddonFramework.LOGGER.warn(
                    "[AE2LT API] Skipping mirrored lightning event because the AE2LT payload is incomplete: eventType={}, level={}, collectorPos={}, tier={}",
                    nativeEvent.getClass().getName(),
                    level,
                    collectorPos,
                    tier);
                return;
            }

            long hvAmount = tier == LightningEnergyTier.HIGH_VOLTAGE ? amount : 0L;
            long ehvAmount = tier == LightningEnergyTier.EXTREME_HIGH_VOLTAGE ? amount : 0L;
            LightningCollectedEvent mirroredEvent =
                    new LightningCollectedEvent(level, collectorPos, hvAmount, ehvAmount, naturalWeather);
            NeoForge.EVENT_BUS.post(mirroredEvent);

            if (mirroredEvent.isCanceled()) {
                if (nativeEvent instanceof ICancellableEvent cancellableEvent) {
                    cancellableEvent.setCanceled(true);
                }
                return;
            }

            invokeCached(contract.setAmount(), nativeEvent, mirroredEvent.getAmount(tier));
        } catch (RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] Failed to mirror AE2LT's public LightningCollectedEvent; leaving the original capture untouched.",
                    e);
        }
    }

    private static BridgeContract resolveBridgeContract(Class<? extends Event> nativeEventClass) {
        if (!ICancellableEvent.class.isAssignableFrom(nativeEventClass)) {
            throw new IllegalStateException(
                    "AE2LT public LightningCollectedEvent no longer implements ICancellableEvent: "
                            + nativeEventClass.getName());
        }
        return new BridgeContract(
                nativeEventClass,
                AE2LTReflection.resolveRequiredMethod(nativeEventClass, "getLevel"),
                AE2LTReflection.resolveRequiredMethod(nativeEventClass, "getCollectorPos"),
                AE2LTReflection.resolveRequiredMethod(nativeEventClass, "getTier"),
                AE2LTReflection.resolveRequiredMethod(nativeEventClass, "isNaturalWeather"),
                AE2LTReflection.resolveRequiredMethod(nativeEventClass, "getAmount"),
                AE2LTReflection.resolveRequiredMethod(nativeEventClass, "setAmount", long.class));
    }

    private static LightningEnergyTier mapNativeTier(Object nativeTier) {
        if (!(nativeTier instanceof Enum<?> enumValue)) {
            return null;
        }
        return switch (enumValue.name()) {
            case "EXTREME_HIGH_VOLTAGE" -> LightningEnergyTier.EXTREME_HIGH_VOLTAGE;
            case "HIGH_VOLTAGE" -> LightningEnergyTier.HIGH_VOLTAGE;
            default -> null;
        };
    }

    private static Class<? extends Event> loadNativeEventClass() {
        return AE2LTReflection.loadSubclass(
                NATIVE_LIGHTNING_COLLECTED_EVENT_CLASS,
                Event.class,
                "AE2LT compatibility bridge");
    }

    private static Object invokeCached(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] Cached compatibility invocation failed: {}#{}",
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    e);
            throw new IllegalStateException(
                    "Cached compatibility invocation failed: "
                            + method.getDeclaringClass().getName() + "#" + method.getName(),
                    e);
        }
    }

    private record BridgeContract(
            Class<? extends Event> eventClass,
            Method getLevel,
            Method getCollectorPos,
            Method getTier,
            Method isNaturalWeather,
            Method getAmount,
            Method setAmount) {
    }
}
