package com.qianchang.ae2lt_api.internal.compat;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
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
    private static final String ACTIONABLE_CLASS = "appeng.api.config.Actionable";
    private static final String ACTION_HOST_CLASS = "appeng.api.networking.security.IActionHost";
    private static final String ACTION_SOURCE_CLASS = "appeng.api.networking.security.IActionSource";
    private static final String GRID_NODE_CLASS = "appeng.api.networking.IGridNode";
    private static final String GRID_CLASS = "appeng.api.networking.IGrid";
    private static final String AE_KEY_CLASS = "appeng.api.stacks.AEKey";
    private static final String ME_STORAGE_CLASS = "appeng.api.storage.MEStorage";

    // AE2LT 1.0.2 publicly registers LIGHTNING_ENERGY_BLOCK on these five
    // grid-connected machines. Crystal Catalyzer runs on FE only, so it is
    // intentionally excluded. AE2LT 1.0.5 left this list unchanged.
    private static final List<Identifier> BRIDGED_BLOCK_ENTITY_IDS = List.of(
            Identifier.fromNamespaceAndPath("ae2lt", "lightning_collector"),
            Identifier.fromNamespaceAndPath("ae2lt", "lightning_simulation_room"),
            Identifier.fromNamespaceAndPath("ae2lt", "lightning_assembly_chamber"),
            Identifier.fromNamespaceAndPath("ae2lt", "overload_processing_factory"),
            Identifier.fromNamespaceAndPath("ae2lt", "tesla_coil"));

    /** Per-call-site cache for hot-path reflective method/field lookups. */
    private static final ConcurrentMap<MethodKey, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<FieldKey, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    /** Cache marker for "method/field not found" so we don't re-walk the class hierarchy each tick. */
    private static final Method MISSING_METHOD;
    private static final Field MISSING_FIELD;
    private static volatile Boolean cachedGridBridgeAvailability;

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

    static List<Identifier> bridgedBlockEntityIds() {
        return BRIDGED_BLOCK_ENTITY_IDS;
    }

    static boolean isGridBridgeAvailable() {
        Boolean cached = cachedGridBridgeAvailability;
        if (cached != null) {
            return cached;
        }
        boolean available = validateGridBridgeContract();
        cachedGridBridgeAvailability = available;
        return available;
    }

    static boolean hasGrid(BlockEntity blockEntity) {
        return resolveGrid(blockEntity) != null;
    }

    static long getStoredInGrid(BlockEntity blockEntity, LightningEnergyTier tier) {
        Object cachedInventory = getCachedGridInventory(blockEntity);
        if (cachedInventory == null) {
            return 0L;
        }
        Object key = getLightningKey(tier);
        if (!isInstanceOf(key, AE_KEY_CLASS)) {
            return 0L;
        }
        return invokeCachedStorageRead(cachedInventory, key);
    }

    static long extractFromGrid(BlockEntity blockEntity, LightningEnergyTier tier, long amount, boolean simulate) {
        return transferWithGrid(blockEntity, tier, amount, simulate, false);
    }

    static long insertIntoGrid(BlockEntity blockEntity, LightningEnergyTier tier, long amount, boolean simulate) {
        return transferWithGrid(blockEntity, tier, amount, simulate, true);
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
            boolean simulate,
            boolean insert) {
        if (amount <= 0L) {
            return 0L;
        }
        Object actionHost = asActionHost(blockEntity);
        if (actionHost == null) {
            return 0L;
        }

        Object storage = getGridInventory(blockEntity);
        if (storage == null) {
            return 0L;
        }

        Object key = getLightningKey(tier);
        if (!isInstanceOf(key, AE_KEY_CLASS)) {
            return 0L;
        }

        Object actionable = getActionable(simulate);
        Object source = getActionSource(actionHost);
        if (actionable == null || source == null) {
            return 0L;
        }

        return invokeStorageTransfer(storage, insert ? "insert" : "extract", key, amount, actionable, source);
    }

    private static boolean validateGridBridgeContract() {
        try {
            Class<?> actionHostClass = requireClass(ACTION_HOST_CLASS);
            Class<?> actionSourceClass = requireClass(ACTION_SOURCE_CLASS);
            Class<?> actionableClass = requireClass(ACTIONABLE_CLASS);
            Class<?> gridNodeClass = requireClass(GRID_NODE_CLASS);
            Class<?> gridClass = requireClass(GRID_CLASS);
            Class<?> aeKeyClass = requireClass(AE_KEY_CLASS);
            Class<?> meStorageClass = requireClass(ME_STORAGE_CLASS);
            Class<?> lightningKeyClass = requireClass(LIGHTNING_KEY_CLASS);

            resolveRequiredMethod(actionHostClass, "getActionableNode");
            Method getGrid = resolveRequiredMethod(gridNodeClass, "getGrid");
            if (getGrid.getReturnType() != gridClass) {
                throw new IllegalStateException("AppEng IGridNode#getGrid no longer returns appeng.api.networking.IGrid");
            }

            Method getStorageService = resolveRequiredMethod(gridClass, "getStorageService");
            Class<?> storageServiceClass = getStorageService.getReturnType();
            resolveRequiredMethod(storageServiceClass, "getInventory");
            resolveRequiredMethod(storageServiceClass, "getCachedInventory");
            resolveRequiredMethod(actionSourceClass, "ofMachine", actionHostClass);
            resolveRequiredMethod(meStorageClass, "insert", aeKeyClass, long.class, actionableClass, actionSourceClass);
            resolveRequiredMethod(meStorageClass, "extract", aeKeyClass, long.class, actionableClass, actionSourceClass);

            lightningKeyClass.getField("HIGH_VOLTAGE");
            lightningKeyClass.getField("EXTREME_HIGH_VOLTAGE");
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.warn(
                    "[AE2LT API] AppEng/AE2LT grid bridge preflight failed; lightning capability bridging will fail closed until the runtime contract matches the verified 26.1.2 port line.",
                    e);
            return false;
        }
    }

    private static Class<?> requireClass(String className) {
        Class<?> type = loadClass(className);
        if (type == null) {
            throw new IllegalStateException("Required compatibility class is missing: " + className);
        }
        return type;
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

    private static Object getGridInventory(BlockEntity blockEntity) {
        try {
            Object grid = resolveGrid(blockEntity);
            if (grid == null) {
                return null;
            }
            Object storageService = invoke(grid, "getStorageService", new Class<?>[0]);
            if (storageService == null) {
                return null;
            }
            Object inventory = invoke(storageService, "getInventory", new Class<?>[0]);
            return isInstanceOf(inventory, ME_STORAGE_CLASS) ? inventory : null;
        } catch (IllegalStateException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to access AE2LT grid storage bridge: {}", e.getMessage());
            return null;
        }
    }

    private static Object getCachedGridInventory(BlockEntity blockEntity) {
        try {
            Object grid = resolveGrid(blockEntity);
            if (grid == null) {
                return null;
            }
            Object storageService = invoke(grid, "getStorageService", new Class<?>[0]);
            if (storageService == null) {
                return null;
            }
            return invoke(storageService, "getCachedInventory", new Class<?>[0]);
        } catch (IllegalStateException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to access AE2LT cached grid inventory bridge: {}", e.getMessage());
            return null;
        }
    }

    private static Object resolveGrid(BlockEntity blockEntity) {
        try {
            Object node = resolveGridNode(blockEntity);
            if (node == null) {
                return null;
            }
            return invoke(node, "getGrid", new Class<?>[0]);
        } catch (IllegalStateException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to resolve AE2LT grid node bridge: {}", e.getMessage());
            return null;
        }
    }

    private static Object resolveGridNode(BlockEntity blockEntity) {
        Object actionHost = asActionHost(blockEntity);
        if (actionHost != null) {
            Object actionableNode = invoke(actionHost, "getActionableNode", new Class<?>[0]);
            if (actionableNode != null) {
                return actionableNode;
            }
        }
        return invoke(blockEntity, "getMainNode", new Class<?>[0]);
    }

    private static Object asActionHost(BlockEntity blockEntity) {
        return isInstanceOf(blockEntity, ACTION_HOST_CLASS) ? blockEntity : null;
    }

    private static Object getActionable(boolean simulate) {
        Class<?> actionableClass = loadClass(ACTIONABLE_CLASS);
        if (actionableClass == null || !Enum.class.isAssignableFrom(actionableClass)) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) actionableClass.asSubclass(Enum.class);
            return Enum.valueOf(enumClass, simulate ? "SIMULATE" : "MODULATE");
        } catch (IllegalArgumentException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to resolve AppEng Actionable {} enum constant.", simulate ? "SIMULATE" : "MODULATE", e);
            return null;
        }
    }

    private static Object getActionSource(Object actionHost) {
        try {
            Class<?> actionHostClass = loadClass(ACTION_HOST_CLASS);
            Class<?> actionSourceClass = loadClass(ACTION_SOURCE_CLASS);
            if (actionHostClass == null || actionSourceClass == null) {
                return null;
            }
            Method ofMachine = actionSourceClass.getMethod("ofMachine", actionHostClass);
            return ofMachine.invoke(null, actionHost);
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to resolve AppEng machine action source.", e);
            return null;
        }
    }

    private static long invokeStorageTransfer(
            Object storage,
            String methodName,
            Object key,
            long amount,
            Object actionable,
            Object source) {
        try {
            Class<?> keyClass = loadClass(AE_KEY_CLASS);
            Class<?> actionableClass = loadClass(ACTIONABLE_CLASS);
            Class<?> actionSourceClass = loadClass(ACTION_SOURCE_CLASS);
            if (keyClass == null || actionableClass == null || actionSourceClass == null) {
                return 0L;
            }
            Method method = storage.getClass().getMethod(methodName, keyClass, long.class, actionableClass, actionSourceClass);
            Object value = method.invoke(storage, key, amount, actionable, source);
            return value instanceof Number number ? number.longValue() : 0L;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to invoke AppEng MEStorage#{} reflectively.", methodName, e);
            return 0L;
        }
    }

    private static long invokeCachedStorageRead(Object cachedInventory, Object key) {
        try {
            Class<?> keyClass = loadClass(AE_KEY_CLASS);
            if (keyClass == null) {
                return 0L;
            }
            Method method = cachedInventory.getClass().getMethod("get", keyClass);
            Object value = method.invoke(cachedInventory, key);
            return value instanceof Number number ? number.longValue() : 0L;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AE2LTAddonFramework.LOGGER.debug("[AE2LT API] Failed to read AppEng cached inventory reflectively.", e);
            return 0L;
        }
    }

    private static boolean isInstanceOf(Object value, String className) {
        Class<?> type = loadClass(className);
        return type != null && value != null && type.isInstance(value);
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
