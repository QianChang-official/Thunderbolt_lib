# Thunderbolt_lib

[中文文档](README_zh_CN.md)

`Thunderbolt_lib` is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech).

> Runtime mod id remains `ae2lt_api`.
> Target versions: AE2 Lightning Tech 1.0.6, Minecraft 1.21.1, NeoForge 21.1.x.
> Latest release: **1.0.6** — see [CHANGELOG.md](CHANGELOG.md).

## What It Provides

- Lightning energy capability API: `ILightningEnergyHandler`
- Runtime bridge for AE2LT lightning-connected machines (5 grid-connected block entities; see below)
- Collector interception event: `LightningCollectedEvent` (with `isNaturalWeather()` since 1.0.3)
- Recipe builders for current AE2LT machine and ritual recipe ids
- Plugin loading via `@AE2LTPlugin`, `IAE2LTPlugin`, and `ServiceLoader`
- Static helper facade: `AE2LTAPI`
- Frozen ID constants: `AE2LTBlockEntityIds`, `AE2LTRecipeIds` (since 1.0.3)
- Codec helpers: `LightningEnergyTier.CODEC` / `STREAM_CODEC` (since 1.0.3)
- Native API detection: `AE2LTNativeBridge` (since 1.0.3)
- Version helpers: `AE2LTVersion` and `AE2LTAPI#getLoadedAE2LTVersion()` (since 1.0.4)
- First-party naming aliases on `ILightningEnergyHandler` and `LightningEnergyTier` (since 1.0.4)
- Frequency-binding detection: `AE2LTNativeBridge#isFrequencyBindingAvailable()` and `AE2LTAPI#isAE2LTFrequencyBindingAvailable()` (since 1.0.5)
- Frequency-binding host helpers: `AE2LTFrequencyBinding` plus `AE2LTAPI` facade methods for reading/writing host frequency ids, connection state, and grid channel counts (since 1.0.6)

## Runtime Bridge Coverage

`AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK` is wired onto the five grid-connected machines that AE2LT 1.0.2+ publicly exposes:

| Block entity id | Role |
|-----------------|------|
| `ae2lt:lightning_collector` | Collects natural / artificial lightning |
| `ae2lt:lightning_simulation_room` | Simulates lightning strikes for crafting |
| `ae2lt:lightning_assembly_chamber` | Assembles items from lightning + inputs |
| `ae2lt:overload_processing_factory` | Heavy-duty lightning processing |
| `ae2lt:tesla_coil` | Discharges lightning energy |

`ae2lt:crystal_catalyzer` runs on FE only and is intentionally excluded from the lightning-energy bridge.

The same five IDs are exposed as `AE2LTBlockEntityIds.LIGHTNING_GRID_MEMBERS` (and individually as `LIGHTNING_COLLECTOR`, etc.) for addon code that wants to iterate or query without hardcoding strings.

## Relationship to AE2LT 1.0.6's First-Party API

AE2LT 1.0.2+ introduced its own first-party API package `com.moakiee.ae2lt.api` under the `ae2lt` namespace. AE2LT 1.0.6 does not change that public package or the recipe schemas checked in 1.0.3 / 1.0.4. Internally, AE2LT 1.0.6 expands the BE-level frequency-binding mechanism (`com.moakiee.ae2lt.grid.FrequencyBindingHost`) from wireless devices to more AE2LT machines. That class is part of AE2LT's internal grid implementation, not its frozen API. Thunderbolt_lib 1.0.6 exposes runtime detection plus a reflective helper facade (`AE2LTFrequencyBinding`) so addons can light-touch around the feature without hard-binding to non-API symbols. The two namespaces remain deliberately distinct:

| | Library (this repo) | AE2LT first-party |
|--|---------------------|-------------------|
| Java package | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| Namespace | `ae2lt_api` | `ae2lt` |
| Capability id | `ae2lt_api:lightning_energy` | `ae2lt:lightning_energy` |
| Tier enum | `LightningEnergyTier` | `LightningTier` |
| Recipe builders | yes | no |
| Plugin loader | yes | no |
| Runtime without AE2LT loaded | no; metadata requires AE2LT 1.0.6+ | no |

For most addons, the library remains the right choice: it exposes recipe builders, plugin loading, version helpers, and a byte-stable API surface across Thunderbolt_lib releases. Use `AE2LTNativeBridge.isNativeApiAvailable()` to detect whether AE2LT's first-party API is loaded at runtime, and `AE2LTVersion` when you need version gates.

## Runtime Naming

- Git repository / project name: `Thunderbolt_lib`
- Built jar name: `Thunderbolt_lib-1.0.6.jar`
- Runtime mod id: `ae2lt_api`

Keeping `mod_id = ae2lt_api` avoids breaking existing addon dependency declarations in `neoforge.mods.toml` and capability lookups.

## Current Recipe Coverage

| Builder | Recipe type | Notes |
|---------|-------------|-------|
| `LightningAssemblyRecipeBuilder` | `ae2lt:lightning_assembly` | Multi-input + lightning tier + total energy |
| `LightningTransformRecipeBuilder` | `ae2lt:lightning_transform` | Simple input → result |
| `LightningSimulationRecipeBuilder` | `ae2lt:lightning_simulation` | Multi-input + lightning tier + total energy |
| `OverloadProcessingRecipeBuilder` | `ae2lt:overload_processing` | Items + optional input fluid + multi-result |
| `CrystalCatalyzerRecipeBuilder` | `ae2lt:crystal_catalyzer` | Catalyst slot, item or tag output, supports `dust` mode |
| `LightningStrikeRecipeBuilder` | `ae2lt:lightning_strike` | Multi-block ritual triggered by lightning |

`CrystalCatalyzerRecipeBuilder` aligns with AE2LT 1.0.2's `crystal_catalyzer/dust/*.json` files: call `dustMode()` (or `mode("dust")`) plus `outputTag(tagId, count)` to emit a tag-resolved output stack.

## Dependency Example

```toml
[[dependencies.your_mod_id]]
    modId = "ae2lt_api"
    type = "required"
    versionRange = "[1.0.6,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.6,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Build Output

```bash
./gradlew build
```

```text
build/libs/Thunderbolt_lib-1.0.6.jar
```

## Versioning

This project tracks AE2 Lightning Tech's release line. See [CHANGELOG.md](CHANGELOG.md) for per-version notes.

- `1.0.6` — tracks AE2LT 1.0.6. AE2LT's public API package and recipe schemas are unchanged, while its frequency-binding subsystem now applies to more machines; this release adds reflective frequency host helpers while preserving existing symbols.
- `1.0.5` — tracks AE2LT 1.0.5. AE2LT's public API package and recipe schemas are unchanged from 1.0.4; this release adds frequency-binding detection helpers and caches hot-path reflective lookups while preserving existing symbols.
- `1.0.4` — tracks AE2LT 1.0.4. AE2LT's public API package and recipe schemas are unchanged from 1.0.3; this release adds version helpers, capability-id helpers, and first-party naming aliases while preserving existing symbols.
- `1.0.3` — adds frozen ID constants, Mojang/Stream codecs on the tier enum, native-API detection bridge, and a `naturalWeather` flag on `LightningCollectedEvent`. Aligns with AE2LT 1.0.3's first-party API package.
- `1.0.2` — bumps version to track AE2LT 1.0.2 release line; content identical to 1.0.1.
- `1.0.1` — reconciles API with AE2LT 1.0.2 recipe schemas (Crystal Catalyzer dust mode + tag output, corrected 5-BE bridge list).
- `1.0.0` — initial Thunderbolt_lib release, aligned with AE2LT 1.0.0.

## Disclaimer

This name is used for non-commercial community purposes only. If the name is considered infringing or unsuitable by any rights holder, contact the maintainer and it will be changed promptly.

Full notice: [DISCLAIMER.md](DISCLAIMER.md)
