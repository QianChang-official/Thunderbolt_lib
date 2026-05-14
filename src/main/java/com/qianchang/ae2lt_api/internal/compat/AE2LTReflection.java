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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class AE2LTReflection {

    private static final String LIGHTNING_KEY_CLASS = "com.moakiee.ae2lt.me.key.LightningKey";
    private static final String LIGHTNING_TIER_CLASS = "com.moakiee.ae2lt.me.key.LightningKey$Tier";
    private static final String NATURAL_HANDLER_CLASS = "com.moakiee.ae2lt.event.NaturalLightningTransformationHandler";
    private static final String COMMON_CONFIG_CLASS = "com.moakiee.ae2lt.config.AE2LTCommonConfig";
    private static final String LIGHTNING_COLLECTOR_CLASS = "com.moakiee.ae2lt.blockentity.LightningCollectorBlockEntity";

    // AE2LT 1.0.2 publicly registers LIGHTNING_ENERGY_BLOCK on these five
    // grid-connected machines. Crystal Catalyzer runs on FE only, so it is
    // intentionally excluded. AE2LT 1.0.5 left this list unchanged.
    private static final List<ResourceLocation> BRIDGED_BLOCK_ENTITY_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath("ae2lt", "lightning_collector"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "lightning_simulation_room"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "lightning_assembly_chamber"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "overload_processing_factory"),
            ResourceLocation.fromNamespaceAndPath("ae2lt", "tesla_coil"));

    /** Per-call-site cache for hot-path reflective method/field lookups. */
    private static final ConcurrentMap<MethodKey, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<FieldKey, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    /** Cache marker for "method/field not found" so we don't re-walk the class hierarchy each tick. */
    private static final Method MISSING_METHOD;
    private static final Field MISSING_FIELD;

    static {
        try {
            MISSING_METHOD = AE2LTReflection.class.getDeclaredMethod("missingMethodMarker");
            MISSING_FIELD = AE2LTReflection.class.getDeclaredField("MISSING_FIELD_MARKER");
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused") // sentinel target for MISSING_FIELD
    private static final Object MISSING_FIELD_MARKER = new Object();

    @SuppressWarnings("unused") // sentinel target for MISSING_METHOD
    private static void missingMethodMarker() {
    }

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

    static <T> Class<? extends T> loadSubclass(String className, Class<T> expectedType, String context) {
        Class<?> rawClass = loadClass(className);
        if (rawClass == null) {
            return null;
        }
        try {
            return rawClass.asSubclass(expectedType);
        } catch (ClassCastException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] {} expected {} to extend {}, but found an incompatible runtime type.",
                    context,
                    className,
                    expectedType.getName(),
                    e);
            return null;
        }
    }

    static void requireMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        resolveRequiredMethod(type, name, parameterTypes);
    }

    static Method resolveRequiredMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        if (findMethod(type, name, parameterTypes) != null) {
            return findMethod(type, name, parameterTypes);
        }
        String signature = type.getName() + "#" + name + Arrays.toString(parameterTypes);
        AE2LTAddonFramework.LOGGER.error(
                "[AE2LT API] Required compatibility method is missing: {}",
                signature);
        throw new IllegalStateException("Required compatibility method is missing: " + signature);
    }

    static Object getRuntimeTier(LightningEnergyTier tier) {
        try {
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
        } catch (RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] Failed to resolve AE2LT lightning tier constant for {}.",
                    tier,
                    e);
            throw new IllegalStateException("Failed to resolve AE2LT lightning tier constant for " + tier, e);
        }
    }

    static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Objects.requireNonNull(target, "target");
            Method method = findMethod(target.getClass(), name, parameterTypes);
            if (method == null) {
                return null;
            }
            return method.invoke(target, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] Reflective invoke failed: {}#{}{}",
                    target == null ? "<null>" : target.getClass().getName(),
                    name,
                    Arrays.toString(parameterTypes),
                    e);
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
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] Reflective field write failed: {}#{}",
                    target == null ? "<null>" : target.getClass().getName(),
                    fieldName,
                    e);
            throw new IllegalStateException("Failed to set field " + fieldName + " on " + target.getClass().getName(), e);
        }
    }

    static int lightningCollectorCooldownTicks() {
        try {
            Class<?> configClass = Objects.requireNonNull(loadClass(COMMON_CONFIG_CLASS));
            Method method = configClass.getMethod("lightningCollectorCooldownTicks");
            return (Integer) method.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error("[AE2LT API] Failed to query AE2LT lightning collector cooldown.", e);
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
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error("[AE2LT API] Failed to invoke AE2LT lightning rod transform hook.", e);
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
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.error(
                    "[AE2LT API] Failed to resolve AE2LT lightning key {}.",
                    fieldName,
                    e);
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
        MethodKey key = new MethodKey(type, name, parameterTypes);
        Method cached = METHOD_CACHE.get(key);
        if (cached != null) {
            return cached == MISSING_METHOD ? null : cached;
        }
        Method found = lookupMethod(type, name, parameterTypes);
        METHOD_CACHE.put(key, found != null ? found : MISSING_METHOD);
        return found;
    }

    private static Method lookupMethod(Class<?> type, String name, Class<?>... parameterTypes) {
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
        FieldKey key = new FieldKey(type, name);
        Field cached = FIELD_CACHE.get(key);
        if (cached != null) {
            return cached == MISSING_FIELD ? null : cached;
        }
        Field found = lookupField(type, name);
        FIELD_CACHE.put(key, found != null ? found : MISSING_FIELD);
        return found;
    }

    private static Field lookupField(Class<?> type, String name) {
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

    private record MethodKey(Class<?> owner, String name, Class<?>[] parameterTypes) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodKey other)) return false;
            return owner == other.owner
                    && name.equals(other.name)
                    && Arrays.equals(parameterTypes, other.parameterTypes);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(owner) * 31 + name.hashCode() * 31 + Arrays.hashCode(parameterTypes);
        }
    }

    private record FieldKey(Class<?> owner, String name) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FieldKey other)) return false;
            return owner == other.owner && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(owner) * 31 + name.hashCode();
        }
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, AE2LTReflection.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            AE2LTAddonFramework.LOGGER.debug(
                    "[AE2LT API] Optional AE2LT runtime class missing or failed to link: {}",
                    className,
                    e);
            return null;
        }
    }
}
