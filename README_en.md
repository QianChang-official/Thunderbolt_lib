# Thunderbolt_lib

[中文](README.md)

Thunderbolt_lib is the addon API and runtime bridge library for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech). It gives downstream addons a stable, low-coupling integration surface for AE2LT lightning energy systems, recipe schemas, runtime compatibility bridges, and related helper APIs.

> Runtime mod id remains `ae2lt_api`
>
> Documentation is maintained only on the `main` branch
>
> Branch-specific README files are no longer authoritative documentation entry points

## What Thunderbolt_lib Is

Thunderbolt_lib is not the AE2LT gameplay mod itself. It is the addon-facing layer that sits between addon code and AE2LT / AE2 runtime details.

- **Thunderbolt_lib** exposes addon-oriented APIs, bridge helpers, version helpers, and recipe builders
- **AE2 Lightning Tech** provides the actual machines, frequency system, recipes, and first-party API
- **Applied Energistics 2** remains part of the runtime contract that AE2LT builds on

The namespace split remains intentional:

| Item | Thunderbolt_lib | AE2LT first-party API |
|---|---|---|
| Java package | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| Namespace | `ae2lt_api` | `ae2lt` |
| Runtime mod id | `ae2lt_api` | `ae2lt` |
| Main purpose | Stable addon bridge | First-party mod API |

## Maintained Lines

| Line | Repository / branch | Thunderbolt_lib version | AE2LT target | Minecraft | Loader | Java | Public asset |
|---|---|---|---|---|---|---|---|
| Forge 1.20.1 | `Thunderbolt_lib_forge_1.20.1` / `release/forge-1.20.1-v1.0.10` | `1.0.10-hotfix1-1.20.1forge` | `1.0.10-1.20.1forge` | `1.20.1` | Forge `47.4.20` | `17-21` | `Thunderbolt_lib_1.20.1_forge_1.0.10-hotfix1.jar` |
| NeoForge 1.21.1 | `Thunderbolt_lib_neoforge_1.21.1` / `main` | `1.0.12-hotfix1` | `1.0.12` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar` |
| NeoForge 26.1.2 | `Thunderbolt_lib_neoforge_26.1.2` / `Minecraft26.1.2neoforge` | `1.0.11-alpha.26.1.2neoforge` | `1.0.1alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar` |

## Recommended Downloads

- For the current NeoForge 1.21.1 main line, use `Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar`
- Forge 1.20.1 remains available from the **Thunderbolt_lib 1.0.11 Hotfix**
- NeoForge 26.1.2 remains available from the **Thunderbolt_lib 1.0.12**

Thunderbolt_lib maintains multiple release lines, but **not every release re-uploads every maintained jar**.

## Public Asset Naming

Public release assets use this format:

```text
Thunderbolt_lib_<minecraft>_<loader>_<ae2lt-target-name>.jar
```

Examples:

```text
Thunderbolt_lib_1.20.1_forge_1.0.10-hotfix1.jar
Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar
Thunderbolt_lib_1.21.1_neoforge_1.0.11-hotfix1.jar
Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar
```

Special rule for the 26.1.2 port line:

- Internal AE2LT target: `1.0.1alpha-26.1.2neoforge`
- Public asset name: `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar`
- Do **not** publish `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha-26.1.2neoforge.jar`

## Release Policy

Thunderbolt_lib has multiple maintained lines, but release uploads are line-specific:

- Only lines that actually changed in the current round belong in the current release
- Unchanged maintained lines stay available from their previous release
- Old assets must not be duplicated into a new tag just because those lines still exist

For example:

- `v1.0.11` is the NeoForge 1.21.1 update line
- Forge 1.20.1 and NeoForge 26.1.2 assets stay on `v1.0.10`

## API Overview

### Lightning energy surface

- `ILightningEnergyHandler`
- `AE2LTCapabilities`
- `LightningEnergyTier`

### Runtime bridge and version helpers

- `AE2LTAPI`
- `AE2LTNativeBridge`
- `AE2LTVersion`
- `AE2LTBlockEntityIds`
- `AE2LTRecipeIds`

### Frequency surface

- `AE2LTFrequencyBinding`
- `AE2LTFrequencyApi`
- `AE2LTFrequencyInfo`
- `AE2LTTransmitterInfo`
- `AE2LTFrequencySecurity`

### Pattern provider bridge

On the NeoForge 1.21.1 main line, `AE2LTPatternProviderApi` and `AE2LTPatternProviderUiProfileInfo` reflect AE2LT 1.0.11's public `PatternProviderUiProfile` contract.

### Recipe builders

- `LightningAssemblyRecipeBuilder`
- `LightningTransformRecipeBuilder`
- `LightningSimulationRecipeBuilder`
- `OverloadProcessingRecipeBuilder`
- `CrystalCatalyzerRecipeBuilder`
- `LightningStrikeRecipeBuilder`

### Events and plugin entrypoints

- `LightningCollectedEvent`
- `@AE2LTPlugin`
- `IAE2LTPlugin`

## Installation and Dependency Notes

At runtime, Thunderbolt_lib still expects the matching AE2LT line to be present.

Typical addon dependency declarations should include both:

- `ae2lt_api`
- `ae2lt`

Example for the NeoForge 1.21.1 main line:

```toml
[[dependencies.yourmodid]]
    modId = "ae2lt_api"
    type = "required"
    versionRange = "[1.0.12-hotfix1,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.yourmodid]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.12,)"
    ordering = "AFTER"
    side = "BOTH"
```

Use the corresponding minimum version for the Forge 1.20.1 or NeoForge 26.1.2 maintenance lines when targeting those branches.

## Runtime Bridge and Graceful Degrade

Thunderbolt_lib tries to keep compatibility handling inside the bridge layer:

- reflective helpers are preferred when AE2LT internals are not stable enough for hard linking
- collector event mirroring fails closed if the verified public event contract is missing or has drifted
- high-version runtime bridges can refuse to register when the verified AE2 / AE2LT / AppEng contract is not present

This does **not** mean Thunderbolt_lib is intended to be installed without AE2LT by players. The matching AE2LT line is still a runtime dependency.

## Release tag / title historical note

Because GitHub tags should not be rewritten once created (to avoid breaking existing references), some Thunderbolt_lib releases may have a tag name that does not exactly match the public release title:

- The tag may keep the original internal version (e.g. `v1.0.12-hotfix1`, `v1.0.12`).
- The Release title and asset filenames are corrected to reflect the actual AE2LT target version (e.g. `Thunderbolt_lib 1.0.12`, `Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar`).

**When downloading, use the Release title, the asset name in Release notes, and the SHA256 as the authoritative reference. Do not rely on the tag name alone to determine the AE2LT target version.**

## Documentation Policy

From now on:

- `README.md` is the canonical Chinese homepage on `main`
- `README_en.md` is the English documentation entry
- `README_zh_CN.md` is kept only as a short compatibility redirect
- documentation is maintained only on the `main` branch
- branch-specific README files are no longer authoritative

If you need installation guidance, compatibility information, maintained-line status, or release asset naming rules, always use the `main` branch README.

## License and Disclaimer

- **Code license**: MIT
- **Name / project notice**: see [DISCLAIMER.md](DISCLAIMER.md)
- **Version history**: see [CHANGELOG.md](CHANGELOG.md)
