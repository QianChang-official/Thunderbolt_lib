package com.qianchang.ae2lt_api.internal.compat;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

final class AE2LTReflection {

    private static final String LIGHTNING_KEY_CLASS = "com.moakiee.ae2lt.me.key.LightningKey";
    private static final String LIGHTNING_TIER_CLASS = "com.moakiee.ae2lt.me.key.LightningKey$Tier";
    private static final String NATURAL_HANDLER_CLASS = "com.moakiee.ae2lt.event.NaturalLightningTransformationHandler";
    private static final String COMMON_CONFIG_CLASS = "com.moakiee.ae2lt.config.AE2LTCommonConfig";
    private static final String LIGHTNING_COLLECTOR_CLASS = "com.moakiee.ae2lt.blockentity.LightningCollectorBlockEntity";

    private static final List<ResourceLocation> BRIDGED_BLOCK_ENTITY_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath("ae2lt", "lightning_collector"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "lightning_simulation_room"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "lightning_assembly_chamber"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "overload_processing_factory"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "tesla_coil"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "crystal_catalyzer"));

    private AE2LTReflection() {
    }

    static List<ResourceLocation> bridgedBlockEntityIds() {
        return BRIDGED_BLOCK_ENTITY_IDS;
    }

    static boolean hasGrid(BlockEntity blockEntity) {
        return getGridInventory(blockEntity) != null;
    }

    static long extractFromGrid(BlockEntity blockEntity, LightningEnergyTier tier, long amount, Actionable actionable) {
        return transferWithGrid(blockEntity, tier, amount, actionable, false);
    }

    static long insertIntoGrid(BlockEntity blockEntity, LightningEnergyTier tier, long amount, Actionable actionable) {
        return transferWithGrid(blockEntity, tier, amount, actionable, true);
    }

    static boolean isLightningCollector(BlockEntity blockEntity) {
        return blockEntity.getClass().getName().equals(LIGHTNING_COLLECTOR_CLASS);
    }

    static Object getRuntimeTier(LightningEnergyTier tier) {
        Class<?> tierClass = loadClass(LIGHTNING_TIER_CLASS);
        if (tierClass == null) {
            return null;
        }
        String name = tier == LightningEnergyTier.EXTREME_HIGH_VOLTAGE
                ? "EXTREME_HIGH_VOLTAGE"
                : "HIGH_VOLTAGE";
        @SuppressWarnings("unchecked")
        Object enumValue = Enum.valueOf((Class<Enum>) tierClass.asSubclass(Enum.class), name);
        return enumValue;
    }

    static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = findMethod(target.getClass(), name, parameterTypes);
            if (method == null) {
                return null;
            }
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke method " + name + " on " + target.getClass().getName(), e);
        }
    }

    static boolean invokeBoolean(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        Object result = invoke(target, name, parameterTypes, args);
        return result instanceof Boolean value && value;
    }

    static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field " + fieldName + " on " + target.getClass().getName(), e);
        }
    }

    static int lightningCollectorCooldownTicks() {
        try {
            Class<?> configClass = Objects.requireNonNull(loadClass(COMMON_CONFIG_CLASS));
            Method method = configClass.getMethod("lightningCollectorCooldownTicks");
            return (Integer) method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to query AE2LT lightning collector cooldown", e);
        }
    }

    static void invokeNearbyLightningRodTransform(ServerLevel level, BlockPos lightningPos, boolean naturalWeather) {
        try {
            Class<?> handlerClass = Objects.requireNonNull(loadClass(NATURAL_HANDLER_CLASS));
            Method method = handlerClass.getDeclaredMethod(
                    "tryTransformFromNearbyLightningRod",
                    ServerLevel.class,
                    BlockPos.class,
                    boolean.class);
            method.setAccessible(true);
            method.invoke(null, level, lightningPos, naturalWeather);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke AE2LT lightning rod transform hook", e);
        }
    }

    private static long transferWithGrid(
            BlockEntity blockEntity,
            LightningEnergyTier tier,
            long amount,
            Actionable actionable,
            boolean insert) {
        if (amount <= 0L) {
            return 0L;
        }
        if (!(blockEntity instanceof IActionHost actionHost)) {
            return 0L;
        }

        MEStorage storage = getGridInventory(blockEntity);
        if (storage == null) {
            return 0L;
        }

        Object key = getLightningKey(tier);
        if (!(key instanceof AEKey aeKey)) {
            return 0L;
        }

        IActionSource source = IActionSource.ofMachine(actionHost);
        return insert
                ? storage.insert(aeKey, amount, actionable, source)
                : storage.extract(aeKey, amount, actionable, source);
    }

    private static Object getLightningKey(LightningEnergyTier tier) {
        String fieldName = tier == LightningEnergyTier.EXTREME_HIGH_VOLTAGE
                ? "EXTREME_HIGH_VOLTAGE"
                : "HIGH_VOLTAGE";
        try {
            Class<?> keyClass = Objects.requireNonNull(loadClass(LIGHTNING_KEY_CLASS));
            Field field = keyClass.getField(fieldName);
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve AE2LT lightning key " + fieldName, e);
        }
    }

    private static MEStorage getGridInventory(BlockEntity blockEntity) {
        try {
            Object mainNode = invoke(blockEntity, "getMainNode", new Class<?>[0]);
            if (mainNode == null) {
                return null;
            }
            Object grid = invoke(mainNode, "getGrid", new Class<?>[0]);
            if (grid == null) {
                return null;
            }
            Object storageService = invoke(grid, "getStorageService", new Class<?>[0]);
            if (storageService == null) {
                return null;
            }
            Object inventory = invoke(storageService, "getInventory", new Class<?>[0]);
            return inventory instanceof MEStorage storage ? storage : null;
        } catch (IllegalStateException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to access AE2LT grid storage bridge: {}", e.getMessage());
            return null;
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Optional AE2LT runtime class missing: {}", className);
            return null;
        }
    }
}
