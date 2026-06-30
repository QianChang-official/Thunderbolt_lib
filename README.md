# Thunderbolt_lib（闪枢库）

[English](README_en.md)

Thunderbolt_lib 是 [AE2 Lightning Tech](https://github.com/MOAKIEE/AE2-Lightning-Tech) 的 addon API 前置库与运行时桥接层，用于为下游模组提供稳定、低耦合、尽量向后兼容的 AE2LT 接入面。

本库本身不负责扩展 AE2LT 的游戏内容；它提供的是能力接口、事件镜像、配方构建器、运行时桥接、版本探测与插件入口，使 addon 可以在不硬依赖 AE2LT 内部实现细节的前提下接入闪电能量网络与相关系统。

> 运行时 `mod_id` 固定为 `ae2lt_api`
>
> 为什么保留 `ae2lt_api` 而不是改成 `thunderbolt_lib`：这个 `mod_id` 已经被下游 addon 的依赖声明、兼容判断和 capability 访问代码硬编码。如果改掉，会破坏现有 addon 生态的兼容性。因此仓库名、项目展示名、发布文件名统一使用 `Thunderbolt_lib`，运行时标识继续保持 `ae2lt_api`。
>
> Thunderbolt_lib 与 AE2LT 主模组 `ae2lt` 刻意保持不同命名空间
>
> README / Markdown 文档以后只在 `main` 分支维护

## 项目定位

Thunderbolt_lib 与 AE2LT / AE2 的关系如下：

- **Thunderbolt_lib**：面向 addon 的稳定 API 与运行时桥接层
- **AE2 Lightning Tech**：实际提供闪电能量机器、配方、频率系统与 first-party API 的主模组
- **Applied Energistics 2**：AE2LT 的核心依赖，上层网格与部分运行时合同来源

Thunderbolt_lib 持续保留自己的 Java 包与命名空间：

| 项目 | Thunderbolt_lib | AE2LT first-party API |
|---|---|---|
| Java 包 | `com.qianchang.ae2lt_api.api.*` | `com.moakiee.ae2lt.api.*` |
| 命名空间 | `ae2lt_api` | `ae2lt` |
| 运行时 mod id | `ae2lt_api` | `ae2lt` |
| 主要用途 | addon 稳定桥接层 | 主模组原生 API |

## 当前维护线

| 维护线 | 本地仓库 / 分支 | Thunderbolt_lib 版本 | 对齐 AE2LT | Minecraft | Loader | Java | 当前公开资产 |
|---|---|---|---|---|---|---|---|
| Forge 1.20.1 | `Thunderbolt_lib_forge_1.20.1` / `release/forge-1.20.1-v1.0.10` | `1.0.10-hotfix1-1.20.1forge` | `1.0.10-1.20.1forge` | `1.20.1` | Forge `47.4.20` | `17-21` | `Thunderbolt_lib_1.20.1_forge_1.0.10-hotfix1.jar` |
| NeoForge 1.21.1 | `Thunderbolt_lib_neoforge_1.21.1` / `main` | `1.0.12-hotfix1` | `1.0.12` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar` |
| NeoForge 26.1.2 | `Thunderbolt_lib_neoforge_26.1.2` / `Minecraft26.1.2neoforge` | `1.0.11-alpha.26.1.2neoforge` | `1.0.1alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar` |

## 最新推荐下载

- **需要 NeoForge 1.21.1 主线**：使用 `Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar`
- **需要 Forge 1.20.1 维护线**：从 **Thunderbolt_lib 1.0.11 Hotfix** 获取 `Thunderbolt_lib_1.20.1_forge_1.0.10-hotfix1.jar`
- **需要 NeoForge 26.1.2 维护线**：从 **Thunderbolt_lib 1.0.12** 获取 `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar`

这三条线同时维护，但**不是每次 Release 都会重新上传全部维护线资产**。

## Release asset 命名规则

公开 jar 命名固定为：

```text
Thunderbolt_lib_<minecraft版本号>_<forge还是neoforge>_<要对齐的AE2LT版本号名称>.jar
```

示例：

```text
Thunderbolt_lib_1.20.1_forge_1.0.10-hotfix1.jar
Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar
Thunderbolt_lib_1.21.1_neoforge_1.0.11-hotfix1.jar
Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar
```

特别规则：

- 如果内部 AE2LT target 是 `1.0.1alpha-26.1.2neoforge`
- 对外公开资产名必须是 `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha.jar`
- 不要写成 `Thunderbolt_lib_26.1.2_neoforge_1.0.1alpha-26.1.2neoforge.jar`

## Release 策略

Thunderbolt_lib 有多条维护线，但发布时遵循“**只上传本轮实际变化的维护线**”原则：

- 只有本轮代码、版本号、AE2LT target、问题修复或构建产物发生变化的维护线，才进入本次 Release
- 未变化的维护线继续从之前对应的 Release 获取
- 不要因为维护线仍然存在，就把旧资产复制到新 tag
- 不要因为本地重新构建过，就默认重新上传旧版本线

也就是说：

- `v1.0.11` 只代表本轮更新的 NeoForge 1.21.1 主线
- Forge 1.20.1 与 NeoForge 26.1.2 的现有可用资产继续从 `v1.0.10` 获取

## 三条维护线说明

### Forge 1.20.1

这条线把 Thunderbolt_lib 的 loader 适配回 Forge 1.20.1，保留相同 addon-facing API，同时把运行时桥接改成 Forge 侧实现：

- Forge capability token 与 `LazyOptional`
- `AttachCapabilitiesEvent<BlockEntity>` 动态附加 provider
- Forge 事件总线 collector-event mirror
- `FriendlyByteBuf` 序列化辅助

### NeoForge 1.21.1

这是当前默认维护主线，也是 README 权威入口所在分支：

- 当前版本 `1.0.12-hotfix1`
- 对齐 AE2LT `1.0.12`
- 已包含 AE2LT 1.0.11 公共 `PatternProviderUiProfile` 的反射桥接
- 已包含 1.0.12 事件桥接防御加固（仅在 listener 实际修改时才回写 mirrored amount）
- 继续维护公共频率桥、Collector event mirror、配方构建器与运行时桥接元数据

### NeoForge 26.1.2

这条线用于高版本移植与合同收敛：

- 当前版本 `1.0.11-alpha.26.1.2neoforge`
- 对齐 AE2LT `1.0.1alpha-26.1.2neoforge`
- 高版本 AE2 / AE2LT / AppEng 交互保持反射化
- 启动期执行合同预检，运行时桥接在合同漂移时 fail-closed

## 安装与依赖说明

Thunderbolt_lib 在运行时仍要求目标维护线对应的 AE2LT 主模组存在。

对于 addon 模组，建议在 `mods.toml` / `neoforge.mods.toml` 中同时声明：

- `ae2lt_api`：Thunderbolt_lib 公共 API
- `ae2lt`：AE2 Lightning Tech 主模组

### NeoForge 示例

```toml
[[dependencies.yourmodid]]
    modId = "ae2lt_api"
    type = "required"
    versionRange = "[1.0.12-hotfix1,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.yourmodid]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.12,)"
    ordering = "AFTER"
    side = "BOTH"
```

### Forge 1.20.1 示例

```toml
[[dependencies.yourmodid]]
    modId = "ae2lt_api"
    mandatory = true
    versionRange = "[1.0.10-hotfix1-1.20.1forge,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.yourmodid]]
    modId = "ae2lt"
    mandatory = true
    versionRange = "[1.0.10-1.20.1forge,)"
    ordering = "AFTER"
    side = "BOTH"
```

如果你面向 `26.1.2` 维护线开发，请把下限改成：

```toml
[[dependencies.yourmodid]]
    modId = "ae2lt_api"
    type = "required"
    versionRange = "[1.0.10-alpha.26.1.2neoforge,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.yourmodid]]
    modId = "ae2lt"
    type = "required"
    versionRange = "[1.0.0alpha-26.1.2neoforge,)"
    ordering = "AFTER"
    side = "BOTH"
```

## 开发者接入说明

下游 addon 通常通过 Thunderbolt_lib，而不是直接面向 AE2LT first-party API 开发，原因是：

- 提供更稳定的 addon-facing 公共面
- 提供 recipe builder、plugin loader、版本探测与运行时桥接门面
- 尽量把 AE2LT 内部结构变化封装在反射桥接层内

常见入口：

- `AE2LTAPI`
- `AE2LTCapabilities`
- `AE2LTNativeBridge`
- `AE2LTVersion`
- `AE2LTFrequencyApi`
- `AE2LTFrequencyBinding`
- `@AE2LTPlugin` / `IAE2LTPlugin`

## API 概览

### Lightning energy capability

- `ILightningEnergyHandler`
- `AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK`
- `AE2LTCapabilities.LIGHTNING_ENERGY_ITEM`
- `LightningEnergyTier`

### Runtime bridge / native contract helpers

- `AE2LTNativeBridge`
- `AE2LTVersion`
- `AE2LTBlockEntityIds`
- `AE2LTRecipeIds`

### Frequency APIs

- `AE2LTFrequencyBinding`
- `AE2LTFrequencyApi`
- `AE2LTFrequencyInfo`
- `AE2LTTransmitterInfo`
- `AE2LTFrequencySecurity`

### Pattern provider bridge

在 NeoForge 1.21.1 主线上，`AE2LTPatternProviderApi` 与 `AE2LTPatternProviderUiProfileInfo` 反射桥接 AE2LT `1.0.11` 公共 `PatternProviderUiProfile`。

### Recipe builders

- `LightningAssemblyRecipeBuilder`
- `LightningTransformRecipeBuilder`
- `LightningSimulationRecipeBuilder`
- `OverloadProcessingRecipeBuilder`
- `CrystalCatalyzerRecipeBuilder`
- `LightningStrikeRecipeBuilder`

### Events and plugin entrypoints

- `LightningCollectedEvent`
- `AE2LTAPI`
- `@AE2LTPlugin`
- `IAE2LTPlugin`

## 运行时桥接与 graceful degrade

Thunderbolt_lib 的设计目标之一是把运行时耦合控制在桥接层内：

- 当 AE2LT 公开事件合同缺失或漂移时，collector mirror event 会 fail-closed
- 当高版本 AE2 / AE2LT / AppEng 网格合同不满足时，相关 capability bridge 会停用，而不是半坏运行
- 反射式桥接失效时，其它不依赖该合同的公共面仍尽量保持可用

但需要注意：

- **这不代表“只装 Thunderbolt_lib 不装 AE2LT”是受支持的玩家安装形态**
- Thunderbolt_lib 仍然把 AE2LT 作为对应维护线的运行时依赖

## 已知限制

- 维护线之间的内部 `mod_version` 与公开发布资产名不一定完全相同，公开下载请以 Release asset 名为准
- `ae2lt:crystal_catalyzer` 走 FE，不在闪电能量 capability bridge 覆盖范围内
- 高版本移植线存在更多运行时合同风险，因此其桥接策略比稳定线更严格

## 版本兼容策略

- Thunderbolt_lib 跟随 AE2LT 维护线演进，但尽量保持 addon-facing API 稳定
- 能以反射桥接处理的兼容变化，优先不把 AE2LT 类型暴露进 Thunderbolt_lib 公共签名
- 需要版本门槛时，通过 `AE2LTVersion` 与对应 bridge 的 runtime availability 检查做 feature gate
- 新增维护线或发布策略变更时，只更新 `main` 分支文档，不再把版本分支 README 当作长期入口

## Release tag / title 历史备注

由于 GitHub tag 一旦创建就不宜重写（会破坏已有引用），Thunderbolt_lib 的历史 Release 可能出现以下情况：

- tag 名可能因历史原因与公开 Release title 不完全一致
- Release title 和资产文件名按实际 AE2LT target 版本修正（如 `Thunderbolt_lib 1.0.12`、`Thunderbolt_lib_1.21.1_neoforge_1.0.12.jar`）

**用户下载时，请以 Release title、Release notes 中的资产名和 SHA256 为准，不要仅凭 tag 名判断 AE2LT target 版本。**

## 文档维护策略

从现在开始：

- README / Markdown 文档只在 `main` 分支维护
- `README.md` 是中文主文档，也是 GitHub 仓库首页默认显示入口
- `README_en.md` 是英文文档
- `README_zh_CN.md` 只保留为兼容跳转入口
- `Minecraft26.1.2neoforge` 与 `release/forge-1.20.1-v1.0.10` 分支中的 README 不再作为权威文档入口

如果你需要查看：

- 安装说明
- 兼容矩阵
- Release asset 命名规则
- 维护线状态
- API 文档入口

请始终以 `main` 分支 README 为准。

## 许可证 / 免责声明

- **代码许可证**：GNU LGPL v3.0（见 [LICENSE](LICENSE)）
- **项目说明与名称声明**：见 [DISCLAIMER.md](DISCLAIMER.md)
- **版本记录**：见 [CHANGELOG.md](CHANGELOG.md)
