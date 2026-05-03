# Thunderbolt_lib

[English](README.md)

`Thunderbolt_lib` 是 [AE2-Lightning-Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech) 的 addon API 与运行时桥接库。

> 运行时 `mod_id` 仍然保留为 `ae2lt_api`。
> 对标版本：AE2 Lightning Tech 1.0.0+（已按 1.0.3 配方 Schema 对齐），Minecraft 1.21.1，NeoForge 21.1.x。
> 最新版本:**1.0.3** —— 详见 [CHANGELOG.md](CHANGELOG.md)。

## 提供的能力

- 闪电能量 Capability API:`ILightningEnergyHandler`
- AE2LT 闪电联网机器的运行时桥接(共 5 个并网方块实体,见下方)
- 避雷收电拦截事件:`LightningCollectedEvent`(1.0.3 起新增 `isNaturalWeather()`)
- 当前 AE2LT 机器与仪式配方的构建器
- `@AE2LTPlugin`、`IAE2LTPlugin`、`ServiceLoader` 插件加载
- 静态辅助门面:`AE2LTAPI`
- 冻结 ID 常量:`AE2LTBlockEntityIds`、`AE2LTRecipeIds`(1.0.3 起新增)
- Codec 工具:`LightningEnergyTier.CODEC` / `STREAM_CODEC`(1.0.3 起新增)
- 原生 API 探测:`AE2LTNativeBridge`(1.0.3 起新增)

## 运行时桥接覆盖

`AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK` 桥接的是 AE2LT 1.0.2+ 公开暴露的 5 个并网机器:

| 方块实体 id | 角色 |
|------------|------|
| `ae2lt:lightning_collector` | 收集自然 / 人造闪电 |
| `ae2lt:lightning_simulation_room` | 模拟雷击,用于配方制造 |
| `ae2lt:lightning_assembly_chamber` | 闪电 + 物品输入合成 |
| `ae2lt:overload_processing_factory` | 高负载闪电处理 |
| `ae2lt:tesla_coil` | 闪电能量释放 |

`ae2lt:crystal_catalyzer` 仅使用 FE,不属于闪电能量桥接范围,故有意排除。

同样这五个 ID 已经以 `AE2LTBlockEntityIds.LIGHTNING_GRID_MEMBERS`(以及 `LIGHTNING_COLLECTOR` 等单项常量)的形式公开,用于在 addon 代码里进行迭代或匹配,不必再硬编码字符串。

## 与 AE2LT 1.0.3 自带 API 的关系

AE2LT 1.0.2 / 1.0.3 引入了自己的 first-party API 包 `com.moakiee.ae2lt.api`,使用 `ae2lt` 命名空间。两个命名空间是有意分开的:

| | 本库(this repo) | AE2LT 自带 API |
|--|------------------|----------------|
| Java 包 | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| 命名空间 | `ae2lt_api` | `ae2lt` |
| Capability id | `ae2lt_api:lightning_energy` | `ae2lt:lightning_energy` |
| 等级枚举 | `LightningEnergyTier` | `LightningTier` |
| 配方构建器 | 有 | 无 |
| 插件加载器 | 有 | 无 |
| 独立可用(未装 AE2LT) | 是 | 否 |

对绝大多数 addon 来说,本库依然是更合适的选择:在没有装 AE2LT 时也可以编译 / 加载;暴露了 AE2LT 自身没有的配方构建器与插件加载器;并且在多个 Thunderbolt_lib 发布之间二进制稳定。可调用 `AE2LTNativeBridge.isNativeApiAvailable()` 在运行时探测 AE2LT 自带 API 是否存在。

## 当前命名策略

- 仓库 / 项目名:`Thunderbolt_lib`
- 构建 Jar 名:`Thunderbolt_lib-1.0.3.jar`
- 运行时模组 id:`ae2lt_api`

保留 `mod_id = ae2lt_api` 的原因是避免现有 addon 的 `neoforge.mods.toml` 依赖声明、Capability 查询和兼容代码直接失效。

## 当前配方覆盖

| 构建器 | 配方类型 | 备注 |
|--------|---------|------|
| `LightningAssemblyRecipeBuilder` | `ae2lt:lightning_assembly` | 多输入 + 闪电等级 + 总能量 |
| `LightningTransformRecipeBuilder` | `ae2lt:lightning_transform` | 简单输入 → 结果 |
| `LightningSimulationRecipeBuilder` | `ae2lt:lightning_simulation` | 多输入 + 闪电等级 + 总能量 |
| `OverloadProcessingRecipeBuilder` | `ae2lt:overload_processing` | 物品 + 可选输入流体 + 多结果 |
| `CrystalCatalyzerRecipeBuilder` | `ae2lt:crystal_catalyzer` | 催化槽,物品或 tag 输出,支持 `dust` 模式 |
| `LightningStrikeRecipeBuilder` | `ae2lt:lightning_strike` | 多方块仪式,由闪电触发 |

`CrystalCatalyzerRecipeBuilder` 已对齐 AE2LT 1.0.2 的 `crystal_catalyzer/dust/*.json` 文件:调用 `dustMode()`(或 `mode("dust")`)再配合 `outputTag(tagId, count)` 即可输出 tag 解析后的物品堆。

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
build/libs/Thunderbolt_lib-1.0.3.jar
```

## 版本说明

本项目跟随 AE2 Lightning Tech 的发布节奏。详见 [CHANGELOG.md](CHANGELOG.md)。

- `1.0.3` —— 新增冻结 ID 常量、等级枚举的 Mojang/Stream Codec、AE2LT 自带 API 探测桥,以及 `LightningCollectedEvent` 的 `naturalWeather` 标识。对齐 AE2LT 1.0.3 的 first-party API 包。
- `1.0.2` —— 跟进 AE2LT 1.0.2 发布版本号,内容与 1.0.1 完全一致。
- `1.0.1` —— 按 AE2LT 1.0.2 配方 Schema 对齐 API(Crystal Catalyzer dust 模式 + tag 输出,桥接列表修正为 5 个 BE)。
- `1.0.0` —— Thunderbolt_lib 初次发布,对齐 AE2LT 1.0.0。

## 免责声明

本名称仅用于非商业社区用途。如名称被任何权利人认为涉嫌侵权或不适宜继续使用,请联系维护者,收到通知后会第一时间更名或修改。

完整说明见:[DISCLAIMER.md](DISCLAIMER.md)
