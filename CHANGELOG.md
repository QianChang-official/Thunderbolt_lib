# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.14] - 2026-06-07

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.14`.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.14` after source-level review of the AE2LT 1.0.14 release line.

### Compatibility
- AE2LT 1.0.13-1.0.14 do not introduce new public API surface; the first-party lightning capability/event, wireless frequency, and pattern-provider UI profile contracts remain compatible with 1.0.12.
- The overloaded pattern-provider menu now syncs UI profile flags and title translation key from the block entity, matching AE2LT 1.0.14's menu improvements.
- No new bridge classes were required for this alignment; existing reflective bridges remain valid.

### Documentation
- Updated changelog with 1.0.14 release notes and compatibility verification results.

## [1.0.12-hotfix1] - 2026-05-31
- Consolidated local maintenance notes into the canonical changelog and removed branch-local draft documents (`BLOCKERS.md`, `PROJECT_FRAMEWORK_COMPARISON_zh_CN.md`) from tracking.
- Documented that README / Markdown documentation is maintained only on the `main` branch.
- Clarified maintenance handling for release title / tag history and multi-line release assets.

### Maintenance
- Recorded that GitHub Release tags may preserve historical internal version tags while public release titles and asset names reflect the AE2LT target version when needed.
- Clarified that multi-line release assets can originate from separate maintenance branches even when GitHub Release `targetCommitish` points to `main`.

## [1.0.12-hotfix1] - 2026-05-31

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.12-hotfix1`.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.12` after source-level review of the AE2LT 1.0.12 release line.

### Compatibility
- AE2LT 1.0.12 does not introduce new public API changes; the first-party lightning capability/event, wireless frequency, and pattern-provider UI profile contracts remain compatible with 1.0.11.
- No new bridge classes were required for this alignment.

## [1.0.12] - 2026-05-30

### Fixed
- Event bridge no longer unconditionally writes back the mirrored amount to AE2LT's native `LightningCollectedEvent`. The reflective `setAmount` call is now skipped when no listener modified the mirrored event, reducing unnecessary event bridge intervention in AE2LT's native lightning collection flow.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.12`.

## [1.0.11] - 2026-05-25

### Added
- `com.qianchang.ae2lt_api.api.pattern.AE2LTPatternProviderApi` — reflective read-only bridge for AE2LT 1.0.11's public `com.moakiee.ae2lt.api.pattern.PatternProviderUiProfile`.
- Immutable library-side snapshot `AE2LTPatternProviderUiProfileInfo`.
- `AE2LTNativeBridge#isPatternProviderUiProfileAvailable()` and `patternProviderUiProfileClassName()`.
- `AE2LTAPI#isAE2LTPatternProviderUiProfileAvailable()`, `isAE2LTPatternProviderUiProfileHost(BlockEntity)`, and `getAE2LTPatternProviderUiProfile(BlockEntity)`.
- `AE2LTVersion#PATTERN_PROVIDER_UI_PROFILE_INTRODUCED_VERSION` and `isLoadedAE2LTAtLeastPatternProviderUiProfile()`.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.11` to track the AE2LT 1.0.11 release line.
- Runtime metadata now requires AE2LT `1.0.11+`.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.11` after checking the new public overloaded pattern-provider UI profile contract.

### Compatibility
- AE2LT 1.0.11 keeps the public lightning capability/event and wireless frequency contracts compatible with 1.0.10.
- The new pattern-provider UI profile bridge is reflective and fails closed when AE2LT is absent, older than 1.0.11, not initialized yet, or if the runtime contract drifts.

## [1.0.10-alpha.26.1.2neoforge] - 2026-05-20

### Added
- Startup preflight for the verified AppEng / AE2LT grid storage contract on the 26.1.2 NeoForge port line. Thunderbolt_lib now refuses to register the lightning capability bridge when the high-version runtime contract drifts.
- Feature-aware version gating in `AE2LTVersion` so the verified AE2LT `1.0.0alpha-26.1.2neoforge` port line is recognized as compatible with the first-party API and public frequency features carried forward from the stable `1.0.x` line.

### Changed
- Isolated the 26.1.2 migration into its own worktree / branch baseline with JDK 25, Gradle 9.2.1, and NeoForge `26.1.2.21-beta`.
- Migrated identifier usage from `net.minecraft.resources.ResourceLocation` to `net.minecraft.resources.Identifier` where required by the 26.1.2 port.
- Removed the remaining direct AE2 compile-time dependency from the branch by converting AppEng-facing grid operations to fully reflective code.
- Aligned the reflective grid read path with AE2LT 26.1.2's own `GridLightningEnergyHandler`: stored amounts are now read via `getStorageService().getCachedInventory().get(...)`, while writes still use `getInventory().insert/extract(...)`.
- Grid node resolution now prefers `IActionHost#getActionableNode()` and only falls back to `getMainNode()` for older compatible runtimes.
- Main-branch documentation now tracks the stable `1.21.1` line and the isolated `26.1.2` port line together, with explicit target-version and release-asset naming guidance.

### Compatibility
- Source-level review against `AE2-Lightning-Tech` `origin/port/26.1.2-neoforge` confirmed that the public `com.moakiee.ae2lt.api.event.LightningCollectedEvent` and `com.moakiee.ae2lt.api.frequency.*` contracts used by Thunderbolt_lib remain intact on the port line.
- Source-level review also confirmed the continued presence of `NaturalLightningTransformationHandler#tryTransformFromNearbyLightningRod(ServerLevel, BlockPos, boolean)`, `LightningCollectorBlockEntity#captureLightning(boolean)`, `LightningKey`, `IActionSource.ofMachine(...)`, and the AE2 storage access path used by the runtime bridge.
- Local validation passed with `./gradlew.bat clean build --no-daemon` in the isolated `Thunderbolt_lib_neoforge_26.1.2` directory, producing `build/libs/Thunderbolt_lib-1.0.10-alpha.26.1.2neoforge.jar`.

## [1.0.10] - 2026-05-20

### Added
- `AE2LTFrequencyApi.FREQUENCY_API_PROVIDER_CLASS_NAME` and `AE2LTNativeBridge#frequencyApiProviderClassName()` for addons that need AE2LT's public frequency provider SPI name without hardcoding it.
- `AE2LTFrequencyApi#isPublicBindingHost(BlockEntity)`, `getBindingDeviceName(BlockEntity)`, `isPublicBindingMenuHost(AbstractContainerMenu)`, `getBindingMenuBlockPos(AbstractContainerMenu)`, `getBindingMenuToken(AbstractContainerMenu)`, and `openBindingScreen(AbstractContainerMenu)`.
- Matching `AE2LTAPI` facade methods for the new public binding-host and menu-host bridge helpers.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.10` to track the AE2LT 1.0.10 release line.
- Runtime metadata now requires AE2LT `1.0.10+`.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.10` after checking the AE2LT 1.0.9 / 1.0.10 wireless-frequency line.
- `PluginLoader` now guards against duplicate discovery runs, skips duplicate service entries, uses the framework class loader explicitly, and degrades cleanly when `ServiceLoader` encounters malformed service metadata.
- README files now document the 1.0.10 target, the public frequency menu-host helpers, and the raised runtime dependency floor.

### Compatibility
- AE2LT 1.0.9 / 1.0.10 keep the public lightning capability/event contracts introduced earlier and remain compatible with Thunderbolt_lib's collector-event and capability bridges.
- AE2LT's public frequency API package introduced in 1.0.8 remains source-compatible through 1.0.10; Thunderbolt_lib now mirrors the shared menu-host / UI entry points reflectively while continuing to fail closed if the runtime contract drifts.
- The internal wireless-connection rewrites in AE2LT 1.0.9 / 1.0.10 were reviewed against Thunderbolt_lib's reflective query bridge; no additional hard-linked integration layer was required.

## [1.0.8] - 2026-05-16

### Added
- `com.qianchang.ae2lt_api.api.frequency.AE2LTFrequencyApi` — reflective read-only bridge for AE2LT 1.0.8's public `com.moakiee.ae2lt.api.frequency.FrequencyApi`.
- `AE2LTFrequencyApi#getBoundFrequencyId(BlockEntity)`, `getFrequencyInfo(MinecraftServer, int)`, `getTransmitter(MinecraftServer, int)`, and `isValidFrequency(MinecraftServer, int)`.
- Immutable library-side snapshots `AE2LTFrequencyInfo` and `AE2LTTransmitterInfo`, plus `AE2LTFrequencySecurity`.
- `AE2LTNativeBridge#isFrequencyApiAvailable()`, `frequencyApiClassName()`, `publicFrequencyBindingHostClassName()`, `publicFrequencyBindingAccessClassName()`, and `publicFrequencyBindingMenuHostClassName()`.
- `AE2LTAPI` facade methods for the new public frequency query bridge.
- `AE2LTVersion#PUBLIC_FREQUENCY_API_INTRODUCED_VERSION` and `isLoadedAE2LTAtLeastPublicFrequencyApi()`.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.8` to track the AE2LT 1.0.8 release line.
- Runtime metadata now requires AE2LT `1.0.8+`.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.8`.
- README files now document AE2LT 1.0.8's public wireless frequency API bridge and the new runtime dependency floor.

### Compatibility
- AE2LT 1.0.8 keeps the first-party lightning capability, collector event, and recipe schema contracts compatible with 1.0.7.
- The public frequency API bridge is deliberately reflective and fails closed when AE2LT is absent, older than 1.0.8, not initialized yet, or if the frequency contract drifts.
- Mutation and UI entry points from AE2LT's public frequency package are exposed as class-name constants only; addons implementing third-party wireless receivers should compile directly against AE2LT's first-party API jar.

## [1.0.7] - 2026-05-14

### Fixed
- `AE2LTLightningCollectorEventBridge` no longer intercepts lightning entity ticks ahead of AE2LT. It now mirrors AE2LT's public `com.moakiee.ae2lt.api.event.LightningCollectedEvent`, keeping cancellation and amount rewrites inside AE2LT's native collector capture path.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.7` to track the AE2LT 1.0.7 release line.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.7`.
- `LightningCollectedEvent` documentation and README text now describe the event as a mirror of AE2LT's public collector event, including the rule that only the active tier is synchronized back to AE2LT.
- AE2LT compatibility bridge initialization now caches the native event `Method` handles up front and logs the exact degradation scope when library-side collector-event mirroring is disabled.

### Validation
- Release validation scope for this line: GameTest integration verification, client startup compatibility, and bridge-focused log scanning.
- Verified scenarios covered natural EHV storage, artificial HV storage, natural-strike crystal cultivation / lightning-rod side effects, and a natural-only lightning ritual path.

### Compatibility
- Bridge-focused server/client log scans found no reappearance of AE2LT's natural-lightning interception warning and no `AE2LT compatibility bridge failed to initialize` messages.
- The surrounding AE2LT dev environment still emits unrelated loot / recipe parse errors for missing `mekanism_extras`, `extendedae`, `neoecoae`, and `minecraft:nether_quartz` recipe inputs; those remain outside the scope of this library release.

## [1.0.6] - 2026-05-11

### Added
- `com.qianchang.ae2lt_api.api.frequency.AE2LTFrequencyBinding` — reflective helper facade for AE2LT's internal `FrequencyBindingHost` contract.
- `AE2LTFrequencyBinding#isHost(BlockEntity)`, `getFrequencyId(BlockEntity)`, `setFrequency(BlockEntity, int)`, `clearFrequency(BlockEntity)`, `isConnected(BlockEntity)`, `getGridUsedChannels(BlockEntity)`, and `getGridMaxChannels(BlockEntity)`.
- `AE2LTFrequencyBinding` constants for AE2LT's internal host/helper class names and frequency NBT tag keys (`FrequencyId`, `Frequency`).
- `AE2LTAPI` facade methods for the new frequency-binding host helpers.
- `AE2LTNativeBridge#frequencyBindingHelperClassName()`.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.6` to track the AE2LT 1.0.6 release line.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.6`.
- README files now document the 1.0.6 target and the frequency-binding host helper surface.

### Compatibility
- AE2LT 1.0.6 keeps the first-party API package (`com.moakiee.ae2lt.api`) and recipe schemas compatible with 1.0.5.
- The new frequency-binding helpers are deliberately reflective and fail closed when AE2LT is absent or the block entity is not a frequency-binding host.
- All existing Thunderbolt_lib 1.0.5 API symbols are preserved; this release is additive.

## [1.0.5] - 2026-05-06

### Added
- `AE2LTNativeBridge#isFrequencyBindingAvailable()` — runtime detection of AE2LT 1.0.5's BE-level frequency-binding host mechanism (`com.moakiee.ae2lt.grid.FrequencyBindingHost`). Caches the result.
- `AE2LTNativeBridge#frequencyBindingHostClassName()` and `wirelessFrequencyManagerClassName()` constants for addons that want to introspect the binding subsystem reflectively without hardcoding class names.
- `AE2LTVersion#FREQUENCY_BINDING_INTRODUCED_VERSION` constant (`"1.0.5"`).
- `AE2LTVersion#isLoadedAE2LTAtLeastFrequencyBinding()` convenience version gate.
- `AE2LTAPI#isAE2LTFrequencyBindingAvailable()` facade method.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.5` to track the AE2LT 1.0.5 release line.
- `AE2LTVersion.TARGET_AE2LT_VERSION` and `FIRST_PARTY_API_LAST_VERIFIED_VERSION` advanced to `1.0.5`.
- `AE2LTReflection`: hot-path `findMethod`/`findField` now memoize their `Method`/`Field` lookups in a `ConcurrentHashMap`, with a sentinel for misses, so repeated lightning-tick reflection no longer re-walks the class hierarchy each call.
- `AE2LTNativeBridge`: split the cached-availability volatile into two (`cachedNativeApiAvailability`, `cachedFrequencyBindingAvailability`); collapsed the duplicate `Class.forName` boilerplate into a private `classExists` helper.
- Project description updated to state the 1.0.5 target and call out the frequency-binding detection surface.

### Compatibility
- AE2LT 1.0.5 does not change the first-party API package (`com.moakiee.ae2lt.api`) or recipe schemas compared with 1.0.3 / 1.0.4, so no recipe builders or bridge block-entity IDs changed.
- All existing Thunderbolt_lib 1.0.4 API symbols are preserved; this release is purely additive on the Java API surface.

## [1.0.4] - 2026-05-04

### Added
- `com.qianchang.ae2lt_api.api.bridge.AE2LTVersion` — runtime helpers for loaded AE2LT version detection, known-compatible range checks, and dotted version comparisons.
- `AE2LTAPI#getApiVersion()`, `getTargetAE2LTVersion()`, `getLoadedAE2LTVersion()`, and `isAE2LTVersionAtLeast(String)`.
- Capability ID helpers on `AE2LTNativeBridge`: native/library block and item lightning-energy IDs.
- AE2LT first-party naming aliases on `ILightningEnergyHandler`: `getStored`, `getCapacity`, `insert`, and `extract`.
- `LightningEnergyTier.fromSerializedName(String)` alias for parity with AE2LT's first-party `LightningTier`.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.4` to track the AE2LT 1.0.4 release line.
- Runtime metadata now requires AE2LT `1.0.4+`.
- Project description updated to state the 1.0.4 target and exposed integration surface more directly.

### Compatibility
- AE2LT 1.0.4 does not change the first-party API package or recipe schemas compared with 1.0.3, so no recipe builders or bridge block-entity IDs changed.
- Existing Thunderbolt_lib 1.0.3 API symbols are preserved; this release is additive apart from the tighter AE2LT runtime version requirement.

## [1.0.3] - 2026-05-03

### Added
- `com.qianchang.ae2lt_api.api.ids.AE2LTBlockEntityIds` — frozen `ResourceLocation` constants for the six AE2LT block entities, plus `LIGHTNING_GRID_MEMBERS` list capturing the five lightning-grid-connected ones.
- `com.qianchang.ae2lt_api.api.ids.AE2LTRecipeIds` — frozen `ResourceLocation` constants for the six AE2LT recipe types.
- `com.qianchang.ae2lt_api.api.bridge.AE2LTNativeBridge` — runtime detection and namespace helpers for AE2LT 1.0.3's own first-party API package (`com.moakiee.ae2lt.api`).
- `LightningEnergyTier.CODEC` (Mojang `Codec`) and `LightningEnergyTier.STREAM_CODEC` (vanilla `StreamCodec<RegistryFriendlyByteBuf, _>`), wire-format-compatible with AE2LT's first-party `LightningTier`.
- `LightningEnergyTier.fromOrdinal(int)` static decoder for packet round-trips.
- `LightningCollectedEvent.isNaturalWeather()` and the matching 5-arg constructor; the existing 4-arg constructor is preserved and defaults `naturalWeather` to `false`.

### Changed
- `mod_version` and `AE2LTCapabilities.API_VERSION` bumped to `1.0.3` to track the AE2LT 1.0.3 release line.
- `AE2LTCapabilities` Javadoc now documents the deliberate namespace split between `ae2lt_api:lightning_energy` (this library) and AE2LT's own `ae2lt:lightning_energy` (1.0.2+).
- `AE2LTLightningCollectorEventBridge` now propagates the natural-weather flag through to `LightningCollectedEvent`.

### Compatibility
- All 1.0.2 API symbols and behavior preserved; this is a purely additive release. Existing addons recompile unchanged.

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

[1.0.10-alpha.26.1.2neoforge]: https://github.com/QianChang-official/Thunderbolt_lib/tree/Minecraft26.1.2neoforge
[1.0.11]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.11
[1.0.12-hotfix1]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.12-hotfix1
[1.0.10]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.10
[1.0.7]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.7
[1.0.8]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.8
[1.0.6]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.6
[1.0.5]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.5
[1.0.4]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.4
[1.0.3]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.3
[1.0.2]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.2
[1.0.1]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.1
[1.0.0]: https://github.com/QianChang-official/Thunderbolt_lib/releases/tag/v1.0.0
