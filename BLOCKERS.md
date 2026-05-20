# Release Blockers

This file tracks release-gate items that must survive across sessions.

## 1.0.10-alpha.26.1.2neoforge Compatibility Gate

Status: isolated build validation passed; branch ready for continued 26.1.2 integration testing.

This line tracks the isolated `Thunderbolt_lib_neoforge_26.1.2` environment for the
AE2LT `port/26.1.2-neoforge` branch. The goal is to keep the addon-facing API stable
while failing closed when the high-version AppEng / AE2LT runtime contract drifts.

### Required Before Branch Promotion

- [x] Move the migration work out of `Thunderbolt_lib_neoforge_1.21.1` and into an isolated `Thunderbolt_lib_neoforge_26.1.2` directory.
- [x] Switch the branch toolchain to JDK 25 + Gradle 9.2.1 + NeoForge `26.1.2.21-beta`.
- [x] Review AE2LT `origin/port/26.1.2-neoforge` for public event / frequency API drift.
- [x] Remove the remaining direct AE2 compile-time dependency from the branch and keep AppEng access reflective.
- [x] Add runtime preflight for the verified AppEng / AE2LT grid bridge contract so capability registration fails closed when incompatible.
- [x] Run `./gradlew.bat clean build --no-daemon` successfully in the isolated directory.

### Validation Notes

- Validation scope: isolated-branch source comparison against AE2LT `origin/port/26.1.2-neoforge`, JDK 25 environment verification, and local Gradle build validation in `Thunderbolt_lib_neoforge_26.1.2`.
- The verified AE2LT port exposes the same public `LightningCollectedEvent` and `com.moakiee.ae2lt.api.frequency.*` contracts consumed by Thunderbolt_lib.
- The verified AE2LT port still uses `IActionHost#getActionableNode()`, `grid.getStorageService().getInventory()`, `getCachedInventory()`, and `IActionSource.ofMachine(...)`, so the reflective grid bridge was updated to match that contract more closely.
- Local validation passed with `./gradlew.bat clean build --no-daemon`; produced `build/libs/Thunderbolt_lib-1.0.10-alpha.26.1.2neoforge.jar`.

## 1.0.10 Release Gate

Status: build validation passed; release unblocked for the AE2LT 1.0.9 / 1.0.10 alignment scope.

This line tracks the AE2LT 1.0.9 / 1.0.10 wireless-frequency changes. Thunderbolt_lib
keeps the public frequency bridge reflective, extends it with public menu-host / UI
helpers, and does not hard-link AE2LT's public frequency contracts into the library's
public method signatures.

### Required Before 1.0.10 Version Bump

- [x] Compare AE2LT `origin/main` after `1.0.7` and identify public API / wireless-frequency changes relevant to Thunderbolt_lib.
- [x] Verify AE2LT's public `com.moakiee.ae2lt.api.frequency` package remains compatible through the `1.0.9` / `1.0.10` line.
- [x] Add Thunderbolt_lib-side helpers for the public frequency menu-host / shared-screen surface without hard-linking AE2LT classes.
- [x] Harden plugin discovery against duplicate `ServiceLoader` entries and malformed service metadata.
- [x] Bump `mod_version`, `AE2LTCapabilities.API_VERSION`, `AE2LTVersion`, README target-version text, runtime metadata, and changelog entries to `1.0.10`.
- [x] Run full Gradle `build` and confirm the release jar is produced.

### Validation Notes

- Validation scope: AE2LT source comparison against `origin/main`, Thunderbolt_lib `clean build`, repeat `build`, and release-jar inspection.
- Reviewed AE2LT `origin/main` at `mod_version=1.0.10`; the public `com.moakiee.ae2lt.api.frequency` package added in `1.0.8` remains source-compatible, while most `1.0.9` / `1.0.10` work is internal wireless-connection logic.
- Local validation passed with `./gradlew.bat clean build --no-daemon` and a repeat `./gradlew.bat build --no-daemon`; produced `build/libs/Thunderbolt_lib-1.0.10.jar`.
- Runtime GameTest verification was not repeated for this release because the library change stays in reflective bridge code, metadata, and plugin bootstrap hardening; no collector-flow interception or capability registration logic changed.

## 1.0.8 Release Gate

Status: build validation passed; release unblocked for the API alignment scope.

This line tracks the AE2LT 1.0.8 public wireless frequency API. Thunderbolt_lib
does not hard-link AE2LT's new frequency classes; the new helper surface is
reflective and read-only.

### Required Before 1.0.8 Version Bump

- [x] Compare AE2LT main after `1.0.8` and identify public API changes.
- [x] Add a Thunderbolt_lib-side bridge for the new public frequency query surface.
- [x] Keep mutation/UI frequency contracts out of Thunderbolt_lib public signatures unless hard-linking becomes necessary.
- [x] Bump `mod_version`, `AE2LTCapabilities.API_VERSION`, `AE2LTVersion`, README target-version text, runtime metadata, and changelog entries to `1.0.8`.
- [x] Run `compileJava`.
- [x] Run full `build`.

### Validation Notes

- Validation scope: static source comparison against AE2LT `origin/main` after `1.0.8`, `compileJava`, and full Gradle `build`.
- The AE2LT public frequency API files are unchanged between the `1.0.8` commit and current `origin/main`.
- Runtime GameTest verification was not repeated for this release because the library change is an additive reflective query bridge; no collector-flow or capability-registration code changed.

## 1.0.7 Release Gate

Status: 运行时验证已通过; 1.0.7 release unblocked.

The previous freeze is lifted. This file now records the release gate that was satisfied before bumping `mod_version`, `AE2LTCapabilities.API_VERSION`, `AE2LTVersion`, README target-version text, and release metadata to `1.0.7`.

### Required Before 1.0.7 Version Bump

- [x] Run integrated runtime verification with AE2LT + Thunderbolt_lib loaded together.
- [x] Confirm natural lightning captured by the Lightning Collector yields EHV.
- [x] Confirm non-natural / tagged lightning captured by the Lightning Collector yields HV.
- [x] Confirm natural lightning still advances crystal cultivation and related collector side effects.
- [x] Confirm natural-lightning-gated lightning strike / ritual recipes still work through AE2LT's native flow.
- [x] Confirm logs no longer show AE2LT's natural-lightning interception warning after the bridge rewrite.

## Validation Notes

- Validation scope: GameTest integration verification + client startup compatibility + log scan.
- 2026-05-14 local `runGameTestServer` validation passed with Thunderbolt_lib loaded: natural EHV storage, artificial HV storage, natural cultivation/rod side effects, and a natural-only ritual path all succeeded.
- Client startup compatibility check reached normal runtime / integrated-server activity with Thunderbolt_lib loaded.
- Server/client log inspection found no reappearance of AE2LT's natural-lightning interception warning and no `AE2LT compatibility bridge failed to initialize` messages.
- The surrounding AE2LT dev environment still emits unrelated loot/recipe parse errors for missing `mekanism_extras`, `extendedae`, `neoecoae`, and `minecraft:nether_quartz` recipe inputs. Treat that as separate environment cleanup, not a blocker for this collector-bridge hotfix.
- Reproducing the local runtime validation required a temporary AE2LT metadata-only override to `mod_version=1.0.6` because Thunderbolt_lib `1.0.6` declares `ae2lt >= 1.0.6`. That override was used only for validation and should not be committed as part of the AE2LT source changes.

## Completed Preconditions

- [x] Replace tick-time lightning interception with mirroring of AE2LT's public `com.moakiee.ae2lt.api.event.LightningCollectedEvent`.
- [x] Add compatibility-bridge initialization guards and fail-closed logging.
- [x] Cache reflected `Method` / `Field` lookups in `AE2LTReflection`.
- [x] Pre-resolve the native event contract into a cached bridge contract during initialization so the hot path no longer repeats method lookup by name.
- [x] Document the degradation scope when library-side `LightningCollectedEvent` mirroring is disabled.
- [x] Lift the `1.0.7` version freeze after runtime verification passes.

## Follow-Up Candidates

These are not current release blockers, but they should stay visible while the 1.0.7 line is open.

- [ ] Evaluate replacing bridge-side `Method.invoke(...)` calls with `MethodHandle` to remove the per-event varargs `Object[]` allocation.
- [ ] Only fold the `MethodHandle` optimization into `1.0.7` if the patch stays small, low-risk, and does not delay runtime verification; otherwise defer it to the next patch release.
