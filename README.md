# Thunderbolt_lib

[中文文档](README_zh_CN.md)

`Thunderbolt_lib` is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech).

> Runtime mod id remains `ae2lt_api`.
> Target branch: AE2 Lightning Tech `1.0.0alpha-26.1.2neoforge`, Minecraft `26.1.2`, NeoForge `26.1.2.21-beta`, JDK `25`.
> Current branch artifact: **1.0.10-alpha.26.1.2neoforge** — see [CHANGELOG.md](CHANGELOG.md).

## What It Provides

- Lightning energy capability API: `ILightningEnergyHandler`
- Runtime bridge for AE2LT lightning-connected machines (5 grid-connected block entities; see below)
- Collector capture mirror event: `LightningCollectedEvent` (with `isNaturalWeather()` since 1.0.3)
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
- AE2LT 1.0.8+ public frequency API bridge: `AE2LTFrequencyApi`, `AE2LTFrequencyInfo`, `AE2LTTransmitterInfo`, `AE2LTFrequencySecurity`, plus `AE2LTAPI` facade methods for bound-frequency, metadata, transmitter, validity, public binding-host inspection, public menu-host inspection, and shared binding-screen requests (query surface since 1.0.8; expanded bridge helpers in 1.0.10)

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

## Relationship to the AE2LT 26.1.2 Port's First-Party API

AE2LT 1.0.2+ introduced its own first-party API package `com.moakiee.ae2lt.api` under the `ae2lt` namespace. This branch targets the AE2LT `1.0.0alpha-26.1.2neoforge` port line, keeps the existing namespace split, keeps the collector compatibility mirror from 1.0.7, carries forward the public wireless-frequency bridge, and adds stronger fail-closed guards around the high-version AppEng / AE2LT grid contract. The two namespaces remain deliberately distinct:

| | Library (this repo) | AE2LT first-party |
|--|---------------------|-------------------|
| Java package | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| Namespace | `ae2lt_api` | `ae2lt` |
| Capability id | `ae2lt_api:lightning_energy` | `ae2lt:lightning_energy` |
| Tier enum | `LightningEnergyTier` | `LightningTier` |
| Recipe builders | yes | no |
| Plugin loader | yes | no |
| Runtime without AE2LT loaded | no; metadata requires AE2LT `1.0.0alpha-26.1.2neoforge+` on this branch | no |

For most addons, the library remains the right choice: it exposes recipe builders, plugin loading, version helpers, and a stable addon-facing API surface while keeping the AE2 / AE2LT integration logic reflective. Use `AE2LTNativeBridge.isNativeApiAvailable()` to detect whether AE2LT's first-party API is loaded at runtime, and `AE2LTVersion` when you need feature gates that understand both the stable `1.0.x` line and the verified `26.1.2` port string.

`LightningCollectedEvent` now mirrors AE2LT's own public collector event instead of taking over lightning-entity ticks. Library listeners still receive a cancellable event with HV/EHV convenience accessors, and any cancellation or active-tier amount rewrite is synchronized back onto AE2LT's public event before the collector inserts into the grid.

AE2LT 1.0.8 adds `com.moakiee.ae2lt.api.frequency.FrequencyApi`. Thunderbolt_lib mirrors its public query surface through `AE2LTFrequencyApi` without putting AE2LT classes in public method signatures. Addons can query bound frequency ids, frequency metadata, transmitter locations, current validity, public binding-host display names, public menu-host block positions/tokens, and can request AE2LT's shared binding screen through the static helper or the `AE2LTAPI` facade. The provider SPI and public host/access/menu contracts are still surfaced as class-name constants so advanced integrations can decide when they want to compile directly against AE2LT's first-party API jar.

If that compatibility mirror cannot initialize because AE2LT's public event contract is missing or has drifted, Thunderbolt_lib now fails closed: the library-side `LightningCollectedEvent` will stop firing, but the reflective block-entity capability bridge, recipe builders, and plugin/bootstrap surface remain available. The high-version grid capability bridge now also performs a startup contract preflight against AppEng / AE2LT runtime classes and refuses to register if the verified `26.1.2` storage contract is not present. This project still declares AE2LT as a required runtime dependency, so "Thunderbolt_lib without AE2LT" is not a supported player install state.

## Runtime Naming

- Git repository / project name: `Thunderbolt_lib`
- Built jar name: `Thunderbolt_lib-1.0.10-alpha.26.1.2neoforge.jar`
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
    versionRange = "[1.0.10-alpha.26.1.2neoforge,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.0alpha-26.1.2neoforge,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Build Output

```bash
./gradlew build
```

```text
build/libs/Thunderbolt_lib-1.0.10-alpha.26.1.2neoforge.jar
```

## Versioning

This project tracks AE2 Lightning Tech's release line. See [CHANGELOG.md](CHANGELOG.md) for per-version notes.

- `1.0.10-alpha.26.1.2neoforge` — isolated migration branch for the AE2LT `26.1.2` NeoForge port. Upgrades the toolchain to JDK 25 / Gradle 9, migrates identifier usage to the new `net.minecraft.resources.Identifier` API, makes the remaining AppEng interaction fully reflective, aligns grid reads with AE2LT's `GridLightningEnergyHandler`, and adds startup contract preflight so capability bridging fails closed on incompatible high-version AE2 / AE2LT runtimes.
- `1.0.10` — tracks AE2LT 1.0.10, verifies the AE2LT 1.0.9 / 1.0.10 wireless-frequency line, adds reflective helpers for public frequency binding hosts / menus plus a fail-closed shared binding-screen request bridge, and hardens plugin discovery against duplicate or malformed service entries.
- `1.0.8` — tracks AE2LT 1.0.8 and adds a reflective bridge for the new public wireless frequency API: bound-frequency id lookup, frequency metadata snapshots, transmitter location snapshots, validity checks, and class-name constants for the public binding/UI contracts.
- `1.0.7` — tracks AE2LT 1.0.7 and ships the collector-event compatibility hotfix: Thunderbolt_lib now mirrors AE2LT's public `LightningCollectedEvent`, keeps cancellation/amount rewrites inside AE2LT's native collector flow, and records runtime verification scope as GameTest integration validation + client startup compatibility + log scanning.
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
