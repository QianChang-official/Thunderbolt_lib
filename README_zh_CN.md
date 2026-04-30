# Thunderbolt_lib

[English](README.md)

`Thunderbolt_lib` 是 [AE2-Lightning-Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech) 的 addon API 与运行时桥接库。

> 运行时 `mod_id` 仍然保留为 `ae2lt_api`。
> 对标版本：AE2 Lightning Tech 1.0.0+、Minecraft 1.21.1、NeoForge 21.1.x。

## 提供的能力

- 闪电能量 Capability API：`ILightningEnergyHandler`
- AE2LT 闪电联网机器的运行时桥接
- 避雷收电拦截事件：`LightningCollectedEvent`
- 当前 AE2LT 机器与仪式配方的构建器
- `@AE2LTPlugin`、`IAE2LTPlugin`、`ServiceLoader` 插件加载
- 静态辅助门面：`AE2LTAPI`

## 当前命名策略

- 仓库 / 项目名：`Thunderbolt_lib`
- 构建 Jar 名：`Thunderbolt_lib-1.0.0.jar`
- 运行时模组 id：`ae2lt_api`

保留 `mod_id = ae2lt_api` 的原因是避免现有 addon 的 `neoforge.mods.toml` 依赖声明、Capability 查询和兼容代码直接失效。

## 当前配方覆盖

| 构建器 | 配方类型 |
|--------|---------|
| `LightningAssemblyRecipeBuilder` | `ae2lt:lightning_assembly` |
| `LightningTransformRecipeBuilder` | `ae2lt:lightning_transform` |
| `LightningSimulationRecipeBuilder` | `ae2lt:lightning_simulation` |
| `OverloadProcessingRecipeBuilder` | `ae2lt:overload_processing` |
| `CrystalCatalyzerRecipeBuilder` | `ae2lt:crystal_catalyzer` |
| `LightningStrikeRecipeBuilder` | `ae2lt:lightning_strike` |

## 依赖示例

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

## 构建产物

```bash
./gradlew build
```

```text
build/libs/Thunderbolt_lib-1.0.0.jar
```

## 免责声明

本名称仅用于非商业社区用途。如名称被任何权利人认为涉嫌侵权或不适宜继续使用，请联系维护者，收到通知后会第一时间更名或修改。

完整说明见：[DISCLAIMER.md](DISCLAIMER.md)
