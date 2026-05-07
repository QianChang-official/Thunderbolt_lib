# Thunderbolt_lib

[English](README.md)

Thunderbolt_lib（闪枢库）是 AE2 Lightning Tech（AE2LT）的 API 前置库，为希望与 AE2LT 闪电能量网络互操作的下游模组提供稳定、最小、且长期保持向后兼容的开发者接入面。

本前置库本身不注册任何方块、物品或配方，也不向 AE2LT 内部资源注入新内容。它仅暴露编写"AE2LT 拓展模组"所需的接口、能力、事件、配方构建器与桥接工具，使生态中的其它模组能够按公共契约接入闪电能量系统，而无需依赖 AE2LT 主模组的内部实现细节。

运行时 mod_id 为 `ae2lt_api`，与 AE2LT 主模组（`ae2lt`）刻意保持不同命名空间，便于在 `mods.toml` 中分别声明依赖，也便于在出现命名冲突或迁移时由 API 一侧承接桥接职责。

适用于 Minecraft 1.21.1 / NeoForge 21.1.x / Java 21。

## 功能

### 能力接口

通过 NeoForge 能力系统暴露一组方块/物品能力（`AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK` 与 `AE2LTCapabilities.LIGHTNING_ENERGY_ITEM`）。下游模组在自己的 BlockEntity 或 Item 上实现 `ILightningEnergyHandler` 接口并完成注册，即可参与闪电能量网络。

该接口提供 `getLightningStored`、`getLightningCapacity`、`insertLightning`、`extractLightning`、`canInsert`、`canExtract` 等读写原语，并附带 `isEmpty`、`isFull` 等便捷判断。所有能量量级 `long` 表示，可承载从单台机器到全网格规模的计量需求。

### 能量等级

`LightningEnergyTier` 定义了高压（`HIGH_VOLTAGE`）与超高压（`EXTREME_HIGH_VOLTAGE`）两档，并提供 Mojang Codec 与 StreamCodec 实例，用于在 NBT、网络包以及配方 JSON 中安全地序列化与反序列化能量等级，避免下游模组手写枚举字面量带来的拼写漂移与跨端不一致。

### 运行时桥接

`AE2LTNativeBridge` 是一组运行时探测工具，下游模组可在加载早期通过它判断当前是否真的有 AE2LT 主模组在运行，并以 `ResourceLocation` 的形式安全引用主模组中的方块实体类型与配方类型。这一层的存在使"AE2LT 不在场也能优雅降级"的可选依赖模式成为可能，避免硬依赖与 `ClassNotFoundException`。

### 配方构建器

`LightningAssemblyRecipeBuilder` 提供链式 API（`create`、`input`、`inputTag`、`result`、`totalEnergy`、`lightningCost`、`lightningTier`、`toJson`），用于在数据生成阶段为闪电组装腔产出符合主模组 schema 的配方 JSON。

每个配方最多支持 9 个输入栈，输入既可指向具体物品也可指向物品标签，结果项与所需总 FE 能量、单次闪电消耗量、所需电压等级均可显式配置。

### 事件总线

`LightningCollectedEvent` 在闪电能量被收集时由主模组在 NeoForge 事件总线上派发。事件可被取消，并暴露收集器位置、收集到的能量量级、所属世界以及当前是否处于自然天气状态等上下文，便于下游模组在风暴、机械模拟、玩家挑战等场景下做条件性介入。

### ID 常量

`AE2LTBlockEntityIds` 与 `AE2LTRecipeIds` 集中收录了主模组对外暴露的方块实体类型 ID 与配方类型 ID，作为 `ResourceLocation` 常量提供，避免下游模组手写字面量造成的拼写漂移，并使主模组未来重命名内部资源时可由本前置库统一修补。

### 插件加载

通过 `IAE2LTPlugin` 接口与 `@AE2LTPlugin` 注解，下游模组可以以"插件"身份在主模组初始化的固定时机被回调，统一接入注册流程，而无需自行监听 NeoForge 生命周期事件或推算正确的 ordering 关系。

### 静态门面

`AE2LTAPI` 作为整个 API 的静态访问入口，集中暴露 API 版本号、主要能力句柄与桥接工具，便于在自家代码中以 `import static` 方式快速引用，也便于在 IDE 中通过单一入口浏览整个公共面。

## 拓展模组依赖声明

下游拓展模组应在 `META-INF/neoforge.mods.toml` 中以 `ae2lt_api` 与 `ae2lt` 两个独立 modId 分别声明依赖。前者保证开发期与运行期能够找到本前置库并使用其公共类型；后者用于声明对 AE2LT 主模组的运行期可选/必需依赖。示例：

```toml
[[dependencies.yourmodid]]
    modId = "ae2lt_api"
    type = "required"
    versionRange = "[1.0.0,)"
    ordering = "AFTER"
    side = "BOTH"

[[dependencies.yourmodid]]
    modId = "ae2lt"
    type = "optional"
    versionRange = "[1.0.0,)"
    ordering = "AFTER"
    side = "BOTH"
```

`versionRange` 中的下限请按下游模组实际兼容的最低 API 版本填写。

## 许可证

本项目使用多许可证结构：

- **代码**：MIT License
