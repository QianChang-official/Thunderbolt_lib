# Release Blockers

This file tracks release-gate items that must survive across sessions.

## Unified `1.0.10` / `26.1.2` Release Gate

Status: build validation passed for both lines; main-branch release docs refreshed; old standalone `v1.0.10` release replaced with one unified release carrying both renamed jars.

This pass is not a new semantic-version bump. It is a release-operations and documentation consolidation step so the existing stable `1.0.10` release line and the companion `26.1.2` port artifact can be described and published together.

### Required for the unified release refresh

- [x] Re-check local repository state against `Thunderbolt_lib`, `AE2-Lightning-Tech`, and `Applied-Energistics-2` upstreams.
- [x] Fast-forward the local `AE2-Lightning-Tech` `main` branch when it was found behind `origin/main`.
- [x] Confirm the isolated `Minecraft26.1.2neoforge` branch still matches `origin/Minecraft26.1.2neoforge`.
- [x] Rewrite main-branch Markdown docs so the `1.21.1` stable line and the `26.1.2` port line are both versioned explicitly.
- [x] Re-run `clean build` for `Thunderbolt_lib_neoforge_1.21.1`.
- [x] Re-run `clean build` for `Thunderbolt_lib_neoforge_26.1.2`.
- [x] Repackage the two jars under the unified public asset naming scheme.
- [x] Replace the old standalone `v1.0.10` release assets with a single unified release entry carrying both jars.

### Validation Notes

- `Thunderbolt_lib_neoforge_1.21.1`: local `./gradlew.bat clean build --no-daemon` passed again during this refresh.
- `Thunderbolt_lib_neoforge_26.1.2`: local `./gradlew.bat clean build --no-daemon` passed again during this refresh.
- `AE2-Lightning-Tech main` was updated from `1a0e1b4` to `79a3ee1` before the documentation rewrite, so the stable-line summary is based on the current upstream state.
- `Applied-Energistics-2` was cloned locally for contract review of the AE2-facing high-version bridge surface.
- GitHub release `v1.0.10` was deleted and recreated with `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` plus `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha-26.1.2neoforge.jar`.

## 26.1.2 Port Release Gate

Status: build validation passed; branch remains isolated and compatible with the verified `AE2-Lightning-Tech` `port/26.1.2-neoforge` baseline.

### Required Before Publishing the Port Artifact Alongside `1.0.10`

- [x] Keep the migration in the dedicated `Minecraft26.1.2neoforge` branch / worktree.
- [x] Upgrade the toolchain to JDK 25 / Gradle 9 and align metadata to NeoForge `26.1.2.21-beta`.
- [x] Remove the remaining direct AE2 compile-time dependency from the bridge path.
- [x] Add runtime contract preflight so the high-version bridge fails closed on incompatible AppEng / AE2LT environments.
- [x] Re-run a full branch-local `clean build` and confirm the alpha jar is produced.

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
