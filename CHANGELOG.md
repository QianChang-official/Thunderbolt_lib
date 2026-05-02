# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2026-05-03

### Changed
- Bumped `mod_version` and `AE2LTCapabilities.API_VERSION` to track the AE2LT 1.0.2 release line.
- No functional changes from 1.0.1; jar contents are byte-identical apart from the version strings.

## [1.0.1] - 2026-05-03

### Added
- `CrystalCatalyzerRecipeBuilder.outputTag(String tagId, int count)` for tag-resolved output stacks.
- `CrystalCatalyzerRecipeBuilder.mode(String)` and `dustMode()` to opt into AE2LT's `dust` catalyzer mode.
- `CrystalCatalyzerRecipeBuilder.MODE_DUST` constant.

### Changed
- `AE2LTReflection.BRIDGED_BLOCK_ENTITY_IDS` corrected to the five grid-connected machines AE2LT 1.0.2 publicly registers `LIGHTNING_ENERGY_BLOCK` on: Lightning Collector, Lightning Simulation Room, Lightning Assembly Chamber, Overload Processing Factory, Tesla Coil.
- `AE2LTCapabilities` Javadoc refreshed to match the 5-BE bridge list.

### Removed
- `ae2lt:crystal_catalyzer` removed from the lightning-energy bridge list. Crystal Catalyzer runs on FE only and is not part of the AE2LT lightning-energy network.

## [1.0.0] - 2026-05-02

### Added
- Initial public release of Thunderbolt_lib (renamed from the internal AE2LT Addon Framework).
- Lightning energy capability API: `ILightningEnergyHandler`, `LightningEnergyTier` (`HIGH_VOLTAGE`, `EXTREME_HIGH_VOLTAGE`).
- NeoForge capabilities `LIGHTNING_ENERGY_BLOCK` (sided) and `LIGHTNING_ENERGY_ITEM` (void).
- Recipe builders for AE2LT recipe types: `LightningAssemblyRecipeBuilder`, `LightningTransformRecipeBuilder`, `LightningSimulationRecipeBuilder`, `OverloadProcessingRecipeBuilder`, `CrystalCatalyzerRecipeBuilder`, `LightningStrikeRecipeBuilder`.
- Static facade `AE2LTAPI` for capability lookups and bridge access.
- Plugin discovery via `@AE2LTPlugin`, `IAE2LTPlugin`, and `ServiceLoader`.
- `LightningCollectedEvent` for intercepting AE2LT collector pickups.
- Project renamed to `Thunderbolt_lib`; runtime mod id retained as `ae2lt_api` for backward compatibility.

[1.0.2]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.2
[1.0.1]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.1
[1.0.0]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.0
