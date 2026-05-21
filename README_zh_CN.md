# Thunderbolt_lib

[English](README.md)

Thunderbolt_lib（闪枢库）是 AE2 Lightning Tech（AE2LT）的 addon API 前置库与运行时桥接层。本仓库当前维护的是 **Forge 1.20.1** 移植线，用来承接 `AE2LT 1.0.10-1.20.1forge` 的对外 API、运行时兼容桥以及发布产物整理。

> 当前仓库线别：AE2LT `1.0.10-1.20.1forge` / Minecraft `1.20.1` / Forge `47.4.20` / Java `17+`
>
> 运行时 `mod_id` 继续保持 `ae2lt_api`
>
> 对外发布文件统一命名：`Thunderbolt_lib_<minecraft版本号>_<forge还是neoforge>_<AE2LT版本号>.jar`
>
> GitHub `v1.0.10` 继续作为当前三条 Thunderbolt_lib 线的统一 Release 入口

## 发布矩阵

| 线别 | 仓库 / 分支 | Thunderbolt_lib 版本 | 对齐 AE2LT | Minecraft | Loader | Java | 对外发布文件名 |
|------|-------------|----------------------|------------|-----------|--------|------|----------------|
| 当前 Forge 移植线 | `Thunderbolt_lib_forge_1.20.1` / `release/forge-1.20.1-v1.0.10` | `1.0.10-1.20.1forge` | `1.0.10-1.20.1forge` | `1.20.1` | Forge `47.4.20` | `17-21` | `Thunderbolt_lib_1.20.1_forge_1.0.10.jar` |
| 稳定 NeoForge 线 | `Thunderbolt_lib_neoforge_1.21.1` / `main` | `1.0.10` | `1.0.10` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` |
| 高版本移植线 | `Thunderbolt_lib_neoforge_26.1.2` / `Minecraft26.1.2neoforge` | `1.0.10-alpha.26.1.2neoforge` | `1.0.0alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar` |

## 这个 1.20.1 Forge 仓库实际做了什么

这条线不是单纯改版本号，而是把 Thunderbolt_lib 的运行时接入面回落到 Forge 1.20.1：

- 构建侧改为 ForgeGradle 6、Java 17、官方 mappings。
- capability 接入改成 Forge `Capability` + `LazyOptional` 模式。
- AE2LT 方块实体桥接改成 `AttachCapabilitiesEvent<BlockEntity>` 动态附加 provider。
- 事件总线改成 Forge 版 collector-event mirror 与兼容 bootstrap。
- `LightningEnergyTier` 在 1.20.1 线改用 `FriendlyByteBuf` 读写辅助，保持与 AE2LT 线协议一致。
- `AE2LTReflection.shouldAttachBridge()` 改成走 `ForgeRegistries.BLOCK_ENTITY_TYPES`，避免联装运行时触发 `BuiltInRegistries.BLOCK_ENTITY_TYPE` 的字段重映射错误。

同时，面向 addon 的公共能力仍然保持不变，仍可继续使用：

- `ILightningEnergyHandler`
- `AE2LTCapabilities`
- `AE2LTAPI`
- `AE2LTNativeBridge`
- `AE2LTVersion`
- `AE2LTFrequencyBinding`
- `AE2LTFrequencyApi`
- `LightningCollectedEvent`
- 配方构建器
- `@AE2LTPlugin` / `IAE2LTPlugin`

## 运行时桥接覆盖范围

`AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK` 仍然桥接 AE2LT 公布的 5 个并网闪电机器：

| BlockEntity id | 作用 |
|----------------|------|
| `ae2lt:lightning_collector` | 收集自然或人工闪电 |
| `ae2lt:lightning_simulation_room` | 模拟闪电配方 |
| `ae2lt:lightning_assembly_chamber` | 闪电组装 |
| `ae2lt:overload_processing_factory` | 高负载闪电加工 |
| `ae2lt:tesla_coil` | 释放闪电能量 |

`ae2lt:crystal_catalyzer` 仍明确不在桥接列表内，因为它走的是 FE 逻辑。

## 与 AE2LT 自带 first-party API 的关系

Thunderbolt_lib 仍然刻意保持独立命名空间：

| | Thunderbolt_lib | AE2LT first-party API |
|--|-----------------|-----------------------|
| Java 包 | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| 命名空间 | `ae2lt_api` | `ae2lt` |
| 运行时 mod id | `ae2lt_api` | `ae2lt` |
| 主要用途 | 面向 addon 的稳定桥接层 | 主模组原生 API |

保留 `mod_id = ae2lt_api`，是为了不打断现有 addon 的依赖声明与 capability 查询代码。

## 拓展模组依赖声明示例（`mods.toml`）

```toml
[[dependencies.your_mod_id]]
    modId = "ae2lt_api"
    mandatory = true
    versionRange = "[1.0.10-1.20.1forge,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.your_mod_id]]
    modId = "ae2lt"
    mandatory = true
    versionRange = "[1.0.10-1.20.1forge,)"
    ordering = "AFTER"
    side = "BOTH"
```

## 构建与产物

构建命令：

```powershell
.\gradlew.bat clean build --no-daemon
```

本地构建产物：

```text
build/libs/Thunderbolt_lib-1.0.10-1.20.1forge.jar
```

建议对外发布文件名：

```text
Thunderbolt_lib_1.20.1_forge_1.0.10.jar
Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar
Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar
```

## GitHub Release 约定

当前 `v1.0.10` Release 应同时挂载三条线的 jar：

- `Thunderbolt_lib_1.20.1_forge_1.0.10.jar`
- `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar`
- `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar`

如果内部版本号里重复携带了 Minecraft / loader 后缀，上传前应先把对外文件名缩短。

## 版本说明

详细版本变更见 [CHANGELOG.md](CHANGELOG.md)。当前重点版本为：

- `1.0.10-1.20.1forge`：Forge 1.20.1 移植线，包含构建迁移、Forge capability / event 适配，以及 `shouldAttachBridge()` 的运行时注册表修正。
- `1.0.10`：NeoForge 1.21.1 稳定线。
- `1.0.10-alpha.26.1.2neoforge`：NeoForge 26.1.2 隔离移植线。

## 说明

本项目名称仅用于社区非商业用途。完整说明见 [DISCLAIMER.md](DISCLAIMER.md)。

