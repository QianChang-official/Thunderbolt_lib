# Thunderbolt_lib

[中文文档](README_zh_CN.md)

`Thunderbolt_lib` is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech).

> Runtime mod id remains `ae2lt_api`.
> Target versions: AE2 Lightning Tech 1.0.0+, Minecraft 1.21.1, NeoForge 21.1.x.

## What It Provides

- Lightning energy capability API: `ILightningEnergyHandler`
- Runtime bridge for AE2LT lightning-connected machines
- Collector interception event: `LightningCollectedEvent`
- Recipe builders for current AE2LT machine and ritual recipe ids
- Plugin loading via `@AE2LTPlugin`, `IAE2LTPlugin`, and `ServiceLoader`
- Static helper facade: `AE2LTAPI`

## Runtime Naming

- Git repository / project name: `Thunderbolt_lib`
- Built jar name: `Thunderbolt_lib-1.0.0.jar`
- Runtime mod id: `ae2lt_api`

Keeping `mod_id = ae2lt_api` avoids breaking existing addon dependency declarations in `neoforge.mods.toml` and capability lookups.

## Current Recipe Coverage

| Builder | Recipe type |
|---------|-------------|
| `LightningAssemblyRecipeBuilder` | `ae2lt:lightning_assembly` |
| `LightningTransformRecipeBuilder` | `ae2lt:lightning_transform` |
| `LightningSimulationRecipeBuilder` | `ae2lt:lightning_simulation` |
| `OverloadProcessingRecipeBuilder` | `ae2lt:overload_processing` |
| `CrystalCatalyzerRecipeBuilder` | `ae2lt:crystal_catalyzer` |
| `LightningStrikeRecipeBuilder` | `ae2lt:lightning_strike` |

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
build/libs/Thunderbolt_lib-1.0.0.jar
```

## Disclaimer

This name is used for non-commercial community purposes only. If the name is considered infringing or unsuitable by any rights holder, contact the maintainer and it will be changed promptly.

Full notice: [DISCLAIMER.md](DISCLAIMER.md)
