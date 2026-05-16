package com.qianchang.ae2lt_api.api.frequency;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Reflective read-only bridge for AE2LT 1.0.8's public wireless frequency API.
 *
 * <p>AE2LT exposes these symbols under
 * {@code com.moakiee.ae2lt.api.frequency}. This wrapper keeps Thunderbolt_lib's
 * public signatures free of AE2LT classes while still letting addons query
 * frequency metadata when AE2LT 1.0.8+ is present.</p>
 *
 * <p>All query methods fail closed: they return {@link Optional#empty()},
 * {@link OptionalInt#empty()}, or {@code false} if AE2LT is absent, older than
 * 1.0.8, not initialized yet, or if the runtime contract has drifted.</p>
 *
 * @since 1.0.8
 */
public final class AE2LTFrequencyApi {

    public static final String FREQUENCY_API_CLASS_NAME = "com.moakiee.ae2lt.api.frequency.FrequencyApi";
    public static final String FREQUENCY_INFO_CLASS_NAME = "com.moakiee.ae2lt.api.frequency.FrequencyInfo";
    public static final String TRANSMITTER_INFO_CLASS_NAME = "com.moakiee.ae2lt.api.frequency.TransmitterInfo";
    public static final String FREQUENCY_SECURITY_CLASS_NAME = "com.moakiee.ae2lt.api.frequency.FrequencySecurity";
    public static final String FREQUENCY_BINDING_ACCESS_CLASS_NAME =
            "com.moakiee.ae2lt.api.frequency.FrequencyBindingAccess";
    public static final String FREQUENCY_BINDING_HOST_CLASS_NAME =
            "com.moakiee.ae2lt.api.frequency.FrequencyBindingHost";
    public static final String FREQUENCY_BINDING_MENU_HOST_CLASS_NAME =
            "com.moakiee.ae2lt.api.frequency.FrequencyBindingMenuHost";

    private static volatile Contract cachedContract;
    private static volatile boolean contractLookupComplete;

    private AE2LTFrequencyApi() {
    }

    /** Returns {@code true} if AE2LT 1.0.8's public frequency API is loadable. */
    public static boolean isRuntimeAvailable() {
        return contract() != null;
    }

    /**
     * Reads the frequency id bound to a receiver block entity or wireless
     * transmitter block entity.
     */
    public static OptionalInt getBoundFrequencyId(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return OptionalInt.empty();
        }
        Contract contract = contract();
        if (contract == null) {
            return OptionalInt.empty();
        }
        try {
            Object value = contract.getBoundFrequencyId.invoke(null, blockEntity);
            return value instanceof OptionalInt optional ? optional : OptionalInt.empty();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return OptionalInt.empty();
        }
    }

    /** Looks up immutable public metadata for a registered frequency id. */
    public static Optional<AE2LTFrequencyInfo> getFrequencyInfo(MinecraftServer server, int frequencyId) {
        if (server == null || frequencyId <= 0) {
            return Optional.empty();
        }
        Contract contract = contract();
        if (contract == null) {
            return Optional.empty();
        }
        try {
            Object value = contract.getFrequencyInfo.invoke(null, server, frequencyId);
            if (!(value instanceof Optional<?> optional) || optional.isEmpty()) {
                return Optional.empty();
            }
            return mapFrequencyInfo(contract, optional.get());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Looks up the transmitter location registered for a frequency id. */
    public static Optional<AE2LTTransmitterInfo> getTransmitter(MinecraftServer server, int frequencyId) {
        if (server == null || frequencyId <= 0) {
            return Optional.empty();
        }
        Contract contract = contract();
        if (contract == null) {
            return Optional.empty();
        }
        try {
            Object value = contract.getTransmitter.invoke(null, server, frequencyId);
            if (!(value instanceof Optional<?> optional) || optional.isEmpty()) {
                return Optional.empty();
            }
            return mapTransmitterInfo(contract, optional.get());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Returns whether AE2LT currently has a registered frequency with this id. */
    public static boolean isValidFrequency(MinecraftServer server, int frequencyId) {
        if (server == null || frequencyId <= 0) {
            return false;
        }
        Contract contract = contract();
        if (contract == null) {
            return false;
        }
        try {
            Object value = contract.isValidFrequency.invoke(null, server, frequencyId);
            return value instanceof Boolean valid && valid;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static Optional<AE2LTFrequencyInfo> mapFrequencyInfo(Contract contract, Object nativeInfo)
            throws ReflectiveOperationException {
        if (!contract.frequencyInfoClass.isInstance(nativeInfo)) {
            return Optional.empty();
        }
        Object security = contract.frequencyInfoSecurity.invoke(nativeInfo);
        String securityName = security instanceof Enum<?> securityEnum ? securityEnum.name() : null;
        return Optional.of(new AE2LTFrequencyInfo(
                (Integer) contract.frequencyInfoId.invoke(nativeInfo),
                (String) contract.frequencyInfoName.invoke(nativeInfo),
                (Integer) contract.frequencyInfoColor.invoke(nativeInfo),
                (UUID) contract.frequencyInfoOwner.invoke(nativeInfo),
                AE2LTFrequencySecurity.fromNativeName(securityName)));
    }

    @SuppressWarnings("unchecked")
    private static Optional<AE2LTTransmitterInfo> mapTransmitterInfo(Contract contract, Object nativeInfo)
            throws ReflectiveOperationException {
        if (!contract.transmitterInfoClass.isInstance(nativeInfo)) {
            return Optional.empty();
        }
        return Optional.of(new AE2LTTransmitterInfo(
                (ResourceKey<Level>) contract.transmitterInfoDimension.invoke(nativeInfo),
                (BlockPos) contract.transmitterInfoPos.invoke(nativeInfo),
                (Boolean) contract.transmitterInfoAdvanced.invoke(nativeInfo)));
    }

    private static Contract contract() {
        if (contractLookupComplete) {
            return cachedContract;
        }
        cachedContract = loadContract();
        contractLookupComplete = true;
        return cachedContract;
    }

    private static Contract loadContract() {
        try {
            ClassLoader loader = AE2LTFrequencyApi.class.getClassLoader();
            Class<?> apiClass = Class.forName(FREQUENCY_API_CLASS_NAME, false, loader);
            Class<?> frequencyInfoClass = Class.forName(FREQUENCY_INFO_CLASS_NAME, false, loader);
            Class<?> transmitterInfoClass = Class.forName(TRANSMITTER_INFO_CLASS_NAME, false, loader);

            return new Contract(
                    apiClass.getMethod("getBoundFrequencyId", BlockEntity.class),
                    apiClass.getMethod("getFrequencyInfo", MinecraftServer.class, int.class),
                    apiClass.getMethod("getTransmitter", MinecraftServer.class, int.class),
                    apiClass.getMethod("isValidFrequency", MinecraftServer.class, int.class),
                    frequencyInfoClass,
                    frequencyInfoClass.getMethod("id"),
                    frequencyInfoClass.getMethod("name"),
                    frequencyInfoClass.getMethod("color"),
                    frequencyInfoClass.getMethod("owner"),
                    frequencyInfoClass.getMethod("security"),
                    transmitterInfoClass,
                    transmitterInfoClass.getMethod("dimension"),
                    transmitterInfoClass.getMethod("pos"),
                    transmitterInfoClass.getMethod("advanced"));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private record Contract(
            Method getBoundFrequencyId,
            Method getFrequencyInfo,
            Method getTransmitter,
            Method isValidFrequency,
            Class<?> frequencyInfoClass,
            Method frequencyInfoId,
            Method frequencyInfoName,
            Method frequencyInfoColor,
            Method frequencyInfoOwner,
            Method frequencyInfoSecurity,
            Class<?> transmitterInfoClass,
            Method transmitterInfoDimension,
            Method transmitterInfoPos,
            Method transmitterInfoAdvanced) {
    }
}
