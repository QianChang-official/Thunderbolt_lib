# Thunderbolt_lib

[中文文档](README_zh_CN.md)

`Thunderbolt_lib` is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech).

> Runtime mod id remains `ae2lt_api`.
> Stable line: AE2 Lightning Tech `1.0.10`, Minecraft `1.21.1`, NeoForge `21.1.x`, Java `21`.
> Port line: AE2 Lightning Tech `1.0.0alpha-26.1.2neoforge`, Minecraft `26.1.2`, NeoForge `26.1.2.21-beta`, Java `25`.
> Unified release asset naming: `Thunderbolt_lib_<minecraft>_<loader>_<ae2lt-target>.jar`.
> Latest stable release: **1.0.10**. Companion port build: **1.0.10-alpha.26.1.2neoforge** on branch `Minecraft26.1.2neoforge`.

## Release Matrix

| Line | Thunderbolt_lib version | Branch | Target AE2LT | Minecraft | Loader | Java | Public release asset |
|------|--------------------------|--------|--------------|-----------|--------|------|----------------------|
| Stable | `1.0.10` | `main` | `1.0.10` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` |
| Port | `1.0.10-alpha.26.1.2neoforge` | `Minecraft26.1.2neoforge` | `1.0.0alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha-26.1.2neoforge.jar` |

## Upstream Tracking

This project is maintained against three upstream repositories:

- AE2 upstream: <https://github.com/AppliedEnergistics/Applied-Energistics-2>
- AE2 Lightning Tech upstream: <https://github.com/MOAKIEE/AE2-Lightning-Tech>
- Thunderbolt_lib upstream: <https://github.com/QianChang-official/Thunderbolt_lib>

Validation snapshot for this document refresh (`2026-05-20`):

- Local `AE2-Lightning-Tech` `main` was behind `origin/main` by 24 commits and was fast-forwarded before this rewrite.
- Local `AE2-Lightning-Tech` port worktree matches `origin/port/26.1.2-neoforge` at `1.0.0alpha-26.1.2neoforge`.
- Local `Thunderbolt_lib` `main` and `Minecraft26.1.2neoforge` are in sync with `origin`.
- `Applied-Energistics-2` was cloned locally for API/runtime contract review against current upstream `main`.

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

## Relationship to AE2LT's First-Party API

AE2LT 1.0.2+ introduced its own first-party API package `com.moakiee.ae2lt.api` under the `ae2lt` namespace. Thunderbolt_lib keeps the existing namespace split across both the stable `1.21.1` line and the isolated `26.1.2` port line. The stable `1.0.10` release verifies compatibility through the AE2LT 1.0.9 / 1.0.10 wireless-frequency changes, while the `1.0.10-alpha.26.1.2neoforge` branch carries those same addon-facing contracts forward into the high-version port and adds extra fail-closed runtime guards around AE2 / AE2LT grid integration. The two namespaces remain deliberately distinct:

| | Library (this repo) | AE2LT first-party |
|--|---------------------|-------------------|
| Java package | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| Namespace | `ae2lt_api` | `ae2lt` |
| Capability id | `ae2lt_api:lightning_energy` | `ae2lt:lightning_energy` |
| Tier enum | `LightningEnergyTier` | `LightningTier` |
| Recipe builders | yes | no |
| Plugin loader | yes | no |
| Runtime without AE2LT loaded | no; metadata requires the matching AE2LT target for the line you build | no |

For most addons, the library remains the right choice: it exposes recipe builders, plugin loading, version helpers, and a byte-stable API surface across Thunderbolt_lib releases. Use `AE2LTNativeBridge.isNativeApiAvailable()` to detect whether AE2LT's first-party API is loaded at runtime, and `AE2LTVersion` when you need version gates.

`LightningCollectedEvent` now mirrors AE2LT's own public collector event instead of taking over lightning-entity ticks. Library listeners still receive a cancellable event with HV/EHV convenience accessors, and any cancellation or active-tier amount rewrite is synchronized back onto AE2LT's public event before the collector inserts into the grid.

AE2LT 1.0.8 adds `com.moakiee.ae2lt.api.frequency.FrequencyApi`. Thunderbolt_lib mirrors its public query surface through `AE2LTFrequencyApi` without putting AE2LT classes in public method signatures. Addons can query bound frequency ids, frequency metadata, transmitter locations, current validity, public binding-host display names, public menu-host block positions/tokens, and can request AE2LT's shared binding screen through the static helper or the `AE2LTAPI` facade. The provider SPI and public host/access/menu contracts are still surfaced as class-name constants so advanced integrations can decide when they want to compile directly against AE2LT's first-party API jar.

If that compatibility mirror cannot initialize because AE2LT's public event contract is missing or has drifted, Thunderbolt_lib now fails closed: the library-side `LightningCollectedEvent` will stop firing, but the reflective block-entity capability bridge, recipe builders, and plugin/bootstrap surface remain available. On the `26.1.2` port branch, the high-version grid bridge also performs a startup contract preflight and refuses to register when the verified AppEng / AE2LT runtime contract is missing. This project still declares AE2LT as a required runtime dependency, so "Thunderbolt_lib without AE2LT" is not a supported player install state.

## Runtime Naming

- Git repository / project name: `Thunderbolt_lib`
- Internal build outputs: `Thunderbolt_lib-1.0.10.jar` (`main`) and `Thunderbolt_lib-1.0.10-alpha.26.1.2neoforge.jar` (`Minecraft26.1.2neoforge`)
- Public release assets: `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` and `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha-26.1.2neoforge.jar`
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
    versionRange = "[1.0.10,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.10,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Build Output

```bash
./gradlew build
```

```text
main/build/libs/Thunderbolt_lib-1.0.10.jar
Minecraft26.1.2neoforge/build/libs/Thunderbolt_lib-1.0.10-alpha.26.1.2neoforge.jar
```

Recommended public release asset names:

```text
Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar
Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha-26.1.2neoforge.jar
```

## Versioning

This project tracks AE2 Lightning Tech's release line. See [CHANGELOG.md](CHANGELOG.md) for per-version notes.

### Stable line — Minecraft 1.21.1 / NeoForge 21.1.x / AE2LT 1.0.x

- `1.0.10` — tracks AE2LT `1.0.10`, verifies the AE2LT `1.0.9` / `1.0.10` wireless-frequency line, adds reflective helpers for public frequency binding hosts / menus plus a fail-closed shared binding-screen request bridge, and hardens plugin discovery against duplicate or malformed service entries.
- `1.0.8` — tracks AE2LT `1.0.8` and adds a reflective bridge for the new public wireless frequency API: bound-frequency id lookup, frequency metadata snapshots, transmitter location snapshots, validity checks, and class-name constants for the public binding/UI contracts.
- `1.0.7` — tracks AE2LT `1.0.7` and ships the collector-event compatibility hotfix: Thunderbolt_lib now mirrors AE2LT's public `LightningCollectedEvent`, keeps cancellation/amount rewrites inside AE2LT's native collector flow, and records runtime verification scope as GameTest integration validation + client startup compatibility + log scanning.
- `1.0.6` — tracks AE2LT `1.0.6`. AE2LT's public API package and recipe schemas are unchanged, while its frequency-binding subsystem now applies to more machines; this release adds reflective frequency host helpers while preserving existing symbols.
- `1.0.5` — tracks AE2LT `1.0.5`. AE2LT's public API package and recipe schemas are unchanged from `1.0.4`; this release adds frequency-binding detection helpers and caches hot-path reflective lookups while preserving existing symbols.
- `1.0.4` — tracks AE2LT `1.0.4`. AE2LT's public API package and recipe schemas are unchanged from `1.0.3`; this release adds version helpers, capability-id helpers, and first-party naming aliases while preserving existing symbols.
- `1.0.3` — adds frozen ID constants, Mojang/Stream codecs on the tier enum, native-API detection bridge, and a `naturalWeather` flag on `LightningCollectedEvent`. Aligns with AE2LT `1.0.3`'s first-party API package.
- `1.0.2` — bumps version to track the AE2LT `1.0.2` release line; content is functionally identical to `1.0.1`.
- `1.0.1` — reconciles API with AE2LT `1.0.2` recipe schemas (Crystal Catalyzer dust mode + tag output, corrected 5-BE bridge list).
- `1.0.0` — initial Thunderbolt_lib release, aligned with AE2LT `1.0.0`.

### Port line — Minecraft 26.1.2 / NeoForge 26.1.2.21-beta / AE2LT 1.0.0alpha-26.1.2neoforge

- `1.0.10-alpha.26.1.2neoforge` — isolated migration branch for the AE2LT `26.1.2` NeoForge port. Upgrades the toolchain to JDK 25 / Gradle 9, migrates identifier usage to the new `net.minecraft.resources.Identifier` API, removes the remaining direct AppEng compile-time dependency by making grid interaction fully reflective, aligns grid reads with AE2LT's `GridLightningEnergyHandler`, and adds startup contract preflight so capability bridging fails closed on incompatible high-version AE2 / AE2LT runtimes.

## Disclaimer

This name is used for non-commercial community purposes only. If the name is considered infringing or unsuitable by any rights holder, contact the maintainer and it will be changed promptly.

Full notice: [DISCLAIMER.md](DISCLAIMER.md)
