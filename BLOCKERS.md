# Release Blockers

This file tracks release-gate items that should survive across sessions.

## Forge 1.20.1 Port Release Gate

Status: release-ready after documentation cleanup, branch publication, build validation, and unified release asset refresh.

### Required for the Forge `1.20.1` publish pass

- [x] Keep the Forge migration isolated in `Thunderbolt_lib_forge_1.20.1`.
- [x] Port the build to ForgeGradle `6.x`, Java `17`, Forge `47.4.20`, and official `1.20.1` mappings.
- [x] Rework loader-facing capability and event integration from NeoForge APIs to Forge APIs.
- [x] Fix the runtime registry lookup in `AE2LTReflection.shouldAttachBridge()` by using `ForgeRegistries.BLOCK_ENTITY_TYPES`.
- [x] Refresh root Markdown docs so they describe the Forge `1.20.1` line and the unified three-asset release layout.
- [x] Create and push a dedicated GitHub branch for the current Forge port snapshot.
- [x] Run local `clean build` and confirm the Forge jar is produced.
- [x] Upload the normalized Forge jar to GitHub release `v1.0.10` and refresh the release notes.

### Validation Notes

- Expected Forge build artifact: `build/libs/Thunderbolt_lib-1.0.10-1.20.1forge.jar`.
- Expected public Forge asset name: `Thunderbolt_lib_1.20.1_forge_1.0.10.jar`.
- Unified `v1.0.10` should carry all currently maintained Thunderbolt_lib assets together.

## Unified `v1.0.10` Release Layout

Status: release notes and assets should describe all maintained lines together.

### Expected public assets

- `Thunderbolt_lib_1.20.1_forge_1.0.10.jar`
- `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar`
- `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar`

### Notes

- If an old asset name still repeats the Minecraft / loader suffix, replace it with the normalized public filename.
- If in-place asset editing fails, it is acceptable to recreate the release and re-upload all three local jars.
