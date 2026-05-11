package com.qianchang.ae2lt_api.api.frequency;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Reflective helpers for AE2 Lightning Tech's receiver-side frequency-binding
 * hosts.
 *
 * <p>AE2LT keeps {@code com.moakiee.ae2lt.grid.FrequencyBindingHost} in an
 * internal implementation package. This bridge intentionally avoids exposing
 * AE2LT classes in public method signatures, so addons can probe and lightly
 * interact with frequency-bound AE2LT machines without hard-linking to that
 * internal package.</p>
 *
 * <p>All methods fail closed when AE2LT is absent or the target block entity is
 * not a frequency-binding host.</p>
 *
 * @since 1.0.6
 */
public final class AE2LTFrequencyBinding {

    public static final String HOST_CLASS_NAME = "com.moakiee.ae2lt.grid.FrequencyBindingHost";
    public static final String HELPER_CLASS_NAME = "com.moakiee.ae2lt.grid.FrequencyBindingHelper";
    public static final String TAG_FREQUENCY_ID = "FrequencyId";
    public static final String TAG_MEMORY_FREQUENCY = "Frequency";

    private static final ConcurrentMap<MethodKey, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Method MISSING_METHOD;

    private static volatile Class<?> cachedHostClass;
    private static volatile boolean hostClassLookupComplete;

    static {
        try {
            MISSING_METHOD = AE2LTFrequencyBinding.class.getDeclaredMethod("missingMethodMarker");
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AE2LTFrequencyBinding() {
    }

    @SuppressWarnings("unused")
    private static void missingMethodMarker() {
    }

    /**
     * Returns {@code true} if AE2LT's internal frequency-binding host interface
     * is loadable in the current runtime.
     */
    public static boolean isRuntimeAvailable() {
        return hostClass() != null;
    }

    /**
     * Returns {@code true} when the block entity implements AE2LT's
     * {@code FrequencyBindingHost} interface.
     */
    public static boolean isHost(BlockEntity blockEntity) {
        Class<?> hostClass = hostClass();
        return hostClass != null && blockEntity != null && hostClass.isInstance(blockEntity);
    }

    /** Reads the host's bound frequency id, or {@link OptionalInt#empty()}. */
    public static OptionalInt getFrequencyId(BlockEntity blockEntity) {
        Object value = invokeHost(blockEntity, "getFrequencyId");
        return value instanceof Integer frequencyId ? OptionalInt.of(frequencyId) : OptionalInt.empty();
    }

    /**
     * Sets the host's frequency id.
     *
     * @return {@code true} if the target was a host and AE2LT accepted the call
     */
    public static boolean setFrequency(BlockEntity blockEntity, int frequencyId) {
        if (frequencyId <= 0) {
            return clearFrequency(blockEntity);
        }
        return invokeHostResult(blockEntity, "setFrequency", new Class<?>[]{int.class}, frequencyId).called();
    }

    /**
     * Clears the host's bound frequency.
     *
     * @return {@code true} if the target was a host and AE2LT accepted the call
     */
    public static boolean clearFrequency(BlockEntity blockEntity) {
        return invokeHostResult(blockEntity, "clearFrequency", new Class<?>[0]).called();
    }

    /** Returns whether the host currently has a live virtual grid connection. */
    public static boolean isConnected(BlockEntity blockEntity) {
        Object value = invokeHost(blockEntity, "isFrequencyConnected");
        return value instanceof Boolean connected && connected;
    }

    /**
     * Returns the used channel count of the grid reached through the frequency
     * binding, or empty when unavailable.
     */
    public static OptionalInt getGridUsedChannels(BlockEntity blockEntity) {
        Object value = invokeHost(blockEntity, "getGridUsedChannels");
        return value instanceof Integer channels ? OptionalInt.of(channels) : OptionalInt.empty();
    }

    /**
     * Returns the maximum channel count of the grid reached through the
     * frequency binding. AE2LT returns {@code -1} for infinite channel mode.
     */
    public static OptionalInt getGridMaxChannels(BlockEntity blockEntity) {
        Object value = invokeHost(blockEntity, "getGridMaxChannels");
        return value instanceof Integer channels ? OptionalInt.of(channels) : OptionalInt.empty();
    }

    private static Object invokeHost(BlockEntity blockEntity, String name) {
        return invokeHostResult(blockEntity, name, new Class<?>[0]).value();
    }

    private static InvocationResult invokeHostResult(
            BlockEntity blockEntity,
            String name,
            Class<?>[] parameterTypes,
            Object... args) {
        if (!isHost(blockEntity)) {
            return InvocationResult.notCalled();
        }
        try {
            Method method = findMethod(hostClass(), name, parameterTypes);
            if (method == null) {
                method = findMethod(blockEntity.getClass(), name, parameterTypes);
            }
            if (method == null) {
                return InvocationResult.notCalled();
            }
            return InvocationResult.called(method.invoke(blockEntity, args));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return InvocationResult.notCalled();
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

    private static Class<?> hostClass() {
        if (hostClassLookupComplete) {
            return cachedHostClass;
        }
        try {
            cachedHostClass = Class.forName(HOST_CLASS_NAME, false, AE2LTFrequencyBinding.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            cachedHostClass = null;
        }
        hostClassLookupComplete = true;
        return cachedHostClass;
    }

    private record InvocationResult(boolean called, Object value) {
        static InvocationResult called(Object value) {
            return new InvocationResult(true, value);
        }

        static InvocationResult notCalled() {
            return new InvocationResult(false, null);
        }
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
}
