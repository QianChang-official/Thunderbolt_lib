# Thunderbolt_lib

[中文文档](README_zh_CN.md)

`Thunderbolt_lib` is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech).

> Runtime mod id remains `ae2lt_api`.
> Target versions: AE2 Lightning Tech 1.0.0+ (reconciled against 1.0.2 schemas), Minecraft 1.21.1, NeoForge 21.1.x.
> Latest release: **1.0.2** — see [CHANGELOG.md](CHANGELOG.md).

## What It Provides

- Lightning energy capability API: `ILightningEnergyHandler`
- Runtime bridge for AE2LT lightning-connected machines (5 grid-connected block entities; see below)
- Collector interception event: `LightningCollectedEvent`
- Recipe builders for current AE2LT machine and ritual recipe ids
- Plugin loading via `@AE2LTPlugin`, `IAE2LTPlugin`, and `ServiceLoader`
- Static helper facade: `AE2LTAPI`

## Runtime Bridge Coverage

`AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK` is wired onto the five grid-connected machines that AE2LT 1.0.2 publicly exposes:

| Block entity id | Role |
|-----------------|------|
| `ae2lt:lightning_collector` | Collects natural / artificial lightning |
| `ae2lt:lightning_simulation_room` | Simulates lightning strikes for crafting |
| `ae2lt:lightning_assembly_chamber` | Assembles items from lightning + inputs |
| `ae2lt:overload_processing_factory` | Heavy-duty lightning processing |
| `ae2lt:tesla_coil` | Discharges lightning energy |

`ae2lt:crystal_catalyzer` runs on FE only and is intentionally excluded from the lightning-energy bridge.

## Runtime Naming

- Git repository / project name: `Thunderbolt_lib`
- Built jar name: `Thunderbolt_lib-1.0.2.jar`
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
    versionRange = "[1.0.0,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.0,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Build Output

```bash
./gradlew build
```

```text
build/libs/Thunderbolt_lib-1.0.2.jar
```

## Versioning

This project tracks AE2 Lightning Tech's release line. See [CHANGELOG.md](CHANGELOG.md) for per-version notes.

- `1.0.2` — bumps version to track AE2LT 1.0.2 release line; content identical to 1.0.1.
- `1.0.1` — reconciles API with AE2LT 1.0.2 recipe schemas (Crystal Catalyzer dust mode + tag output, corrected 5-BE bridge list).
- `1.0.0` — initial Thunderbolt_lib release, aligned with AE2LT 1.0.0.

## Disclaimer

This name is used for non-commercial community purposes only. If the name is considered infringing or unsuitable by any rights holder, contact the maintainer and it will be changed promptly.

Full notice: [DISCLAIMER.md](DISCLAIMER.md)
