# Thunderbolt_lib

[中文文档](README_zh_CN.md)

`Thunderbolt_lib` is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech).

> Current repository line: AE2LT `1.0.10-1.20.1forge`, Minecraft `1.20.1`, Forge `47.4.20`, Java `17+`.
> Runtime mod id stays `ae2lt_api`.
> Public asset naming stays normalized as `Thunderbolt_lib_<minecraft>_<loader>_<ae2lt-target>.jar`.
> GitHub release `v1.0.10` is used as the unified download entry for all currently maintained Thunderbolt_lib jars.

## Release Matrix

| Line | Repository / branch | Thunderbolt_lib version | Target AE2LT | Minecraft | Loader | Java | Public release asset |
|------|----------------------|--------------------------|--------------|-----------|--------|------|----------------------|
| Current Forge port | `Thunderbolt_lib_forge_1.20.1` / `release/forge-1.20.1-v1.0.10` | `1.0.10-1.20.1forge` | `1.0.10-1.20.1forge` | `1.20.1` | Forge `47.4.20` | `17-21` | `Thunderbolt_lib_1.20.1_forge_1.0.10.jar` |
| Stable NeoForge line | `Thunderbolt_lib_neoforge_1.21.1` / `main` | `1.0.10` | `1.0.10` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` |
| High-version port | `Thunderbolt_lib_neoforge_26.1.2` / `Minecraft26.1.2neoforge` | `1.0.10-alpha.26.1.2neoforge` | `1.0.0alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar` |

## What This Repository Contains

This worktree is the Forge `1.20.1` port of Thunderbolt_lib. It keeps the addon-facing API surface aligned with AE2LT `1.0.10-1.20.1forge` while adapting the loader-specific runtime hooks back to Forge:

- Forge capability tokens plus `LazyOptional`-based block/item access.
- Attached capability providers for AE2LT block entities instead of NeoForge registration callbacks.
- Forge event-bus wiring for the collector-event mirror and compatibility bootstrap.
- `FriendlyByteBuf` helpers for `LightningEnergyTier` packet serialization on `1.20.1`.
- Forge registry lookup in `AE2LTReflection.shouldAttachBridge()` to avoid the remapped `BuiltInRegistries.BLOCK_ENTITY_TYPE` runtime failure seen during combined launches.

The library still provides the same addon-facing surface used by downstream integrations:

- `ILightningEnergyHandler`
- `AE2LTCapabilities`
- `AE2LTAPI`
- `AE2LTNativeBridge`
- `AE2LTVersion`
- `AE2LTFrequencyBinding`
- `AE2LTFrequencyApi`
- `LightningCollectedEvent`
- AE2LT recipe builders
- `@AE2LTPlugin` / `IAE2LTPlugin`

## Runtime Bridge Coverage

`AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK` bridges the five AE2LT grid-connected block entities that are supposed to expose lightning energy:

| Block entity id | Role |
|-----------------|------|
| `ae2lt:lightning_collector` | Collects natural or artificial lightning |
| `ae2lt:lightning_simulation_room` | Simulates lightning for crafting |
| `ae2lt:lightning_assembly_chamber` | Assembles items from lightning and ingredients |
| `ae2lt:overload_processing_factory` | Heavy-duty lightning processing |
| `ae2lt:tesla_coil` | Discharges stored lightning energy |

`ae2lt:crystal_catalyzer` is intentionally excluded because it stays on FE only.

## Namespace Split vs AE2LT First-Party API

Thunderbolt_lib intentionally keeps its existing addon namespace separate from AE2LT's own first-party API:

| | Thunderbolt_lib | AE2LT first-party API |
|--|-----------------|-----------------------|
| Java package | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| Namespace | `ae2lt_api` | `ae2lt` |
| Runtime mod id | `ae2lt_api` | `ae2lt` |
| Main use | Stable addon bridge | First-party mod API |

Keeping `mod_id = ae2lt_api` avoids breaking existing addon dependency declarations and capability lookups.

## Build

```powershell
.\gradlew.bat clean build --no-daemon
```

Expected local output:

```text
build/libs/Thunderbolt_lib-1.0.10-1.20.1forge.jar
```

Recommended public release asset names:

```text
Thunderbolt_lib_1.20.1_forge_1.0.10.jar
Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar
Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar
```

## Dependency Example (`mods.toml`)

```toml
[[dependencies.your_mod_id]]
    modId = "ae2lt_api"
    mandatory = true
    versionRange = "[1.0.10-1.20.1forge,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    mandatory = true
    versionRange = "[1.0.10-1.20.1forge,)"
    ordering = "AFTER"
    side = "BOTH"
```

## GitHub Release Layout

Release tag `v1.0.10` should contain the three currently maintained Thunderbolt_lib artifacts together:

- `Thunderbolt_lib_1.20.1_forge_1.0.10.jar`
- `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar`
- `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar`

If a version string repeats the Minecraft / loader suffix internally, shorten the public filename before upload.

## Versioning

This repository tracks AE2 Lightning Tech's release line. See [CHANGELOG.md](CHANGELOG.md) for detailed per-version notes.

- `1.0.10-1.20.1forge` — Forge `1.20.1` port of Thunderbolt_lib `1.0.10`, including ForgeGradle build migration, Forge capability/event integration, and the runtime registry lookup fix for `shouldAttachBridge()`.
- `1.0.10` — NeoForge `1.21.1` stable line.
- `1.0.10-alpha.26.1.2neoforge` — isolated NeoForge `26.1.2` high-version port line.

## Disclaimer

This name is used for non-commercial community purposes only. Full notice: [DISCLAIMER.md](DISCLAIMER.md)
