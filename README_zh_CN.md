# AE2LT 附属框架

[English](README.md)

一个为 [AE2 闪电科技](https://github.com/MOAKIEE/AE2-Lightning-Tech) 开发附属模组而生的框架 API 模组。AE2 闪电科技是 [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) 的附属，引入了闪电能源系统、进阶机器以及过载 ME 网络组件。

> 依赖 AE2 闪电科技 · 适用于 Minecraft 1.21.1 / NeoForge

## 关于

AE2LT 附属框架提供了一套稳定、有版本管理的 API 层，让第三方模组能够与 AE2 闪电科技集成，而无需依赖其内部实现。在 NeoForge 生态里，它更适合被视为一个 **API / library mod**：运行时它是一个正常加载的前置模组，编译期它又能作为开发库使用。框架暴露了以下能力：

- **NeoForge 能力（Capability）** — 查询和传输任意方块或物品的闪电能量（`ILightningEnergyHandler`）
- **事件** — 接入闪电收集流程（`LightningCollectedEvent`）
- **配方构建器** — 用代码生成 AE2LT 五种机器配方的 JSON
- **插件系统** — 通过 `@AE2LTPlugin` + `IAE2LTPlugin` 结合 Java `ServiceLoader` 注册你的附属
- **静态 API 入口** — `AE2LTAPI` 单例，免去能力查询的重复代码

## API 概览

### 闪电能量 Capability

```java
// 查询方块的闪电能量处理器
AE2LTAPI.getInstance().getLightningHandler(level, pos, direction).ifPresent(handler -> {
    long hv = handler.getLightningStored(LightningEnergyTier.HIGH_VOLTAGE);
    long ehv = handler.getLightningStored(LightningEnergyTier.EXTREME_HIGH_VOLTAGE);
    handler.insertLightning(LightningEnergyTier.HIGH_VOLTAGE, 100, false);
});
```

### 注册插件

1. 实现 `IAE2LTPlugin` 并加上 `@AE2LTPlugin` 注解：

```java
@AE2LTPlugin
public class MyAddonPlugin implements IAE2LTPlugin {
    @Override
    public void onInitialize(AE2LTApiContext ctx) {
        if (ctx.isAE2LTLoaded()) {
            // 注册配方数据生成、自定义 capability 等
        }
    }
}
```

2. 在 `src/main/resources/META-INF/services/com.qianchang.ae2lt_api.api.plugin.IAE2LTPlugin` 中注册：

```
com.example.myaddon.MyAddonPlugin
```

### 配方构建器

所有五种 AE2LT 机器配方类型均已覆盖：

```java
// 闪电装配室
JsonObject recipe = LightningAssemblyRecipeBuilder.create()
    .input("minecraft:iron_ingot", 4)
    .result("mymod:overload_plate", 1)
    .totalEnergy(1000)
    .lightningCost(8)
    .lightningTier(LightningEnergyTier.HIGH_VOLTAGE)
    .toJson();

// 水晶催化器
JsonObject recipe2 = CrystalCatalyzerRecipeBuilder.create()
    .catalyst("ae2:certus_quartz_crystal", 1)
    .output("ae2lt:overload_crystal", 2)
    .energyPerCycle(64)
    .toJson();
```

可用的构建器：
| 构建器 | 配方类型 |
|--------|---------|
| `LightningAssemblyRecipeBuilder` | `ae2lt:lightning_assembly` |
| `LightningTransformRecipeBuilder` | `ae2lt:lightning_transform` |
| `LightningSimulationRecipeBuilder` | `ae2lt:lightning_simulation` |
| `OverloadProcessingRecipeBuilder` | `ae2lt:overload_processing` |
| `CrystalCatalyzerRecipeBuilder` | `ae2lt:crystal_catalyzer` |

### 事件

```java
@SubscribeEvent
public static void onLightningCollected(LightningCollectedEvent event) {
    event.setHvAmount(event.getHvAmount() * 2); // 将所有高压闪电产量翻倍
    // event.setCanceled(true);  // 取消本次收集
}
```

## 快速开始

### 依赖配置（Gradle）

在你的附属模组 build.gradle 中添加本框架为 `compileOnly` 依赖：

```groovy
repositories {
    // 方式 A：将 ae2lt_api-*.jar 放到你项目的 libs/ 目录
    // 方式 B：发布到本地 maven 并通过 mavenLocal() 引用
}

dependencies {
    compileOnly fileTree(dir: 'libs', include: 'ae2lt_api-*.jar')
    // 或者: compileOnly "com.qianchang:ae2lt_api:0.3.2-snapshot"
}
```

> 框架 JAR 可在 [Releases](../../releases) 页面下载。

### 运行时依赖

在你的 `neoforge.mods.toml` 中声明依赖：

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

## 构建

```bash
# 需要 Java 21；将 AE2LT 的 jar 放入 libs/ 以支持完整构建
./gradlew build
```

输出 jar 位于 `build/libs/ae2lt_api-<版本号>.jar`，当前构建结果为 `build/libs/ae2lt_api-0.3.2-snapshot.jar`。

## 兼容性

| 依赖 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| AE2 闪电科技 | ≥ 0.3 |
| Applied Energistics 2 | 任意 1.21.1 版本 |

## 问题反馈

发现 bug 或希望扩展 API？欢迎在 issue 跟踪器中提交，请附上 Minecraft / NeoForge / AE2LT / 框架版本号与清晰描述。

## 许可证

AE2LT 附属框架以 [MIT 协议](LICENSE) 开源。

## 鸣谢

由 **QianChang** 开发。

基于 [AE2 闪电科技](https://github.com/MOAKIEE/AE2-Lightning-Tech)（作者：MOAKIEE、CystrySU、gjmhmm8、_leng、TedXenon、MHanHanBing）以及 [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) 构建。
