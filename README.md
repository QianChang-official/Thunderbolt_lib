# AE2LT Addon Framework

[中文文档](README_zh_CN.md)

A framework API mod for building addons for [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech) — an [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) addon that introduces a lightning energy system, advanced machines, and overloaded network components.

> Requires AE2 Lightning Tech · Built for Minecraft 1.21.1 / NeoForge

## About

AE2LT Addon Framework provides a stable, versioned API surface so that third-party mods can integrate with AE2 Lightning Tech without depending on its internals. In NeoForge terms, it is best treated as an **API / library mod**: a normal mod JAR loaded at runtime as a required dependency, while also serving as a developer-facing library at compile time. It exposes:

- **NeoForge Capabilities** — query and transfer lightning energy (`ILightningEnergyHandler`) from any block or item
- **Events** — hook into the lightning collection pipeline (`LightningCollectedEvent`)
- **Recipe Builders** — generate JSON for all five AE2LT machine recipe types via code
- **Plugin System** — register your addon with `@AE2LTPlugin` + `IAE2LTPlugin` using Java `ServiceLoader`
- **Static API entry point** — `AE2LTAPI` singleton for capability access without boilerplate

## API Overview

### Lightning Energy Capability

```java
// Query a block's lightning energy handler
AE2LTAPI.getInstance().getLightningHandler(level, pos, direction).ifPresent(handler -> {
    long hv = handler.getLightningStored(LightningEnergyTier.HIGH_VOLTAGE);
    long ehv = handler.getLightningStored(LightningEnergyTier.EXTREME_HIGH_VOLTAGE);
    handler.insertLightning(LightningEnergyTier.HIGH_VOLTAGE, 100, false);
});
```

### Registering a Plugin

1. Implement `IAE2LTPlugin` and annotate with `@AE2LTPlugin`:

```java
@AE2LTPlugin
public class MyAddonPlugin implements IAE2LTPlugin {
    @Override
    public void onInitialize(AE2LTApiContext ctx) {
        if (ctx.isAE2LTLoaded()) {
            // register recipe data-gen, custom capabilities, etc.
        }
    }
}
```

2. Register in `src/main/resources/META-INF/services/com.qianchang.ae2lt_api.api.plugin.IAE2LTPlugin`:

```
com.example.myaddon.MyAddonPlugin
```

### Recipe Builders

All five AE2LT machine recipe types are covered:

```java
// Lightning Assembly
JsonObject recipe = LightningAssemblyRecipeBuilder.create()
    .input("minecraft:iron_ingot", 4)
    .result("mymod:overload_plate", 1)
    .totalEnergy(1000)
    .lightningCost(8)
    .lightningTier(LightningEnergyTier.HIGH_VOLTAGE)
    .toJson();

// Crystal Catalyzer
JsonObject recipe2 = CrystalCatalyzerRecipeBuilder.create()
    .catalyst("ae2:certus_quartz_crystal", 1)
    .output("ae2lt:overload_crystal", 2)
    .energyPerCycle(64)
    .toJson();
```

Builders available:
| Builder | Recipe type |
|---------|-------------|
| `LightningAssemblyRecipeBuilder` | `ae2lt:lightning_assembly` |
| `LightningTransformRecipeBuilder` | `ae2lt:lightning_transform` |
| `LightningSimulationRecipeBuilder` | `ae2lt:lightning_simulation` |
| `OverloadProcessingRecipeBuilder` | `ae2lt:overload_processing` |
| `CrystalCatalyzerRecipeBuilder` | `ae2lt:crystal_catalyzer` |

### Events

```java
@SubscribeEvent
public static void onLightningCollected(LightningCollectedEvent event) {
    event.setHvAmount(event.getHvAmount() * 2); // double all HV yield
    // event.setCanceled(true);  // cancel collection entirely
}
```

## Getting Started

### Dependency Setup (Gradle)

Add this mod as a `compileOnly` dependency in your addon's build.gradle:

```groovy
repositories {
    // Option A: place ae2lt_api-*.jar in your libs/ folder
    // Option B: publish to local maven and reference via mavenLocal()
}

dependencies {
    compileOnly fileTree(dir: 'libs', include: 'ae2lt_api-*.jar')
    // or: compileOnly "com.qianchang:ae2lt_api:0.3.2-snapshot"
}
```

> The framework JAR is available in the [Releases](../../releases) section.

### Runtime Dependency

Declare the dependency in your `neoforge.mods.toml`:

```toml
[[dependencies.your_mod_id]]
    modId = "ae2lt_api"
    type = "required"
    versionRange = "[0.3.2,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[0.3,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Building

```bash
# Requires Java 21 and the AE2LT jar in libs/ for full builds
./gradlew build
```

The output jar is placed in `build/libs/ae2lt_api-<version>.jar`, currently `build/libs/ae2lt_api-0.3.2-snapshot.jar`.

## Compatibility

| Dependency | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| AE2 Lightning Tech | ≥ 0.3 |
| Applied Energistics 2 | Any 1.21.1 release |

## Issues

Found a bug or missing API surface? Please open an issue with your Minecraft / NeoForge / AE2LT / AE2LT Addon Framework versions and a clear description.

## License

AE2LT Addon Framework is licensed under the [MIT License](LICENSE).

## Credits

Developed by **QianChang**.

Built on top of [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech) by MOAKIEE, CystrySU, gjmhmm8, _leng, TedXenon, MHanHanBing and the [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) team.
