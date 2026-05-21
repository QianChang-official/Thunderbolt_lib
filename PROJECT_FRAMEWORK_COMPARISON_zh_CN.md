# Thunderbolt_lib / AE2LT / AE2 对照说明

## 1. 三个仓库当前分工

### AE2（Applied Energistics 2）

AE2 是底层宿主生态，提供 ME 网络、存储、节点、动作源等基础合同。Thunderbolt_lib 不复刻 AE2 功能，但运行时桥接必须始终跟随 AE2 的公开合同变化。

### AE2-Lightning-Tech（AE2LT）

AE2LT 是主模组仓库，负责游戏内容本体：方块、方块实体、配方、菜单、渲染、闪电能量系统以及与 AE2 的整合。

### Thunderbolt_lib

Thunderbolt_lib 是给 AE2LT addon 开发者使用的 API 前置库与运行时桥接层，主要负责：

- 稳定公开 API
- capability / 事件兼容桥
- 配方 JSON 构建器
- 插件发现入口
- 下游 addon 的低耦合集成面

它的目标不是复制 AE2LT 内容，而是把“适合开放给 addon 的部分”稳定下来。

## 2. 当前发布矩阵

| 线别 | 仓库 / 分支 | Thunderbolt_lib 版本 | 对齐 AE2LT | Minecraft | Loader | Java | 对外发布文件名 |
|------|-------------|----------------------|------------|-----------|--------|------|----------------|
| 当前 Forge 移植线 | `Thunderbolt_lib_forge_1.20.1` / `release/forge-1.20.1-v1.0.10` | `1.0.10-1.20.1forge` | `1.0.10-1.20.1forge` | `1.20.1` | Forge `47.4.20` | `17-21` | `Thunderbolt_lib_1.20.1_forge_1.0.10.jar` |
| 稳定 NeoForge 线 | `Thunderbolt_lib_neoforge_1.21.1` / `main` | `1.0.10` | `1.0.10` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` |
| 高版本移植线 | `Thunderbolt_lib_neoforge_26.1.2` / `Minecraft26.1.2neoforge` | `1.0.10-alpha.26.1.2neoforge` | `1.0.0alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar` |

## 3. 这次 1.20.1 Forge 移植的核心变化

当前仓库这条线的重点不是“新增 API”，而是把既有 Thunderbolt_lib 公共面安全地移植到 Forge 1.20.1：

- 构建侧切换到 ForgeGradle 6。
- Java 基线回到 17。
- mappings 改成官方 `1.20.1`。
- capability 接入改成 Forge `Capability` / `LazyOptional` 模式。
- AE2LT 方块实体桥接改成 `AttachCapabilitiesEvent<BlockEntity>` 动态附加 provider。
- 事件桥接改为 Forge 事件总线。
- `LightningEnergyTier` 在网络层使用 `FriendlyByteBuf` 读写辅助。
- `ResourceLocation.fromNamespaceAndPath(...)` 回退为 `new ResourceLocation(...)` 以匹配 1.20.1 API。

## 4. 本次运行时修正点

联装验证里抓到的真实运行时问题是：

- 位置：`AE2LTReflection.shouldAttachBridge()`
- 现象：Forge 运行环境里访问 `BuiltInRegistries.BLOCK_ENTITY_TYPE` 触发字段重映射错误
- 处理：改为 `ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType())`

这次修正的意义在于：问题已经不再停留在“能编译”，而是推进到了真正的联装运行时兼容收尾。

## 5. 为什么仍然保留 `ae2lt_api` 这个 mod_id

运行时标识继续保留 `ae2lt_api`，是为了避免打断：

- 现有 addon 的依赖声明
- 已发布模组里写死的兼容判断
- capability 查询代码

因此当前策略是：

- 仓库名、项目展示名、对外发布文件名统一使用 `Thunderbolt_lib`
- 运行时 mod id 继续保持 `ae2lt_api`

## 6. 当前稳定下来的核心能力

- `AE2LTAPI`：统一静态门面
- `ILightningEnergyHandler` / `LightningEnergyTier`：闪电能量能力接口与等级枚举
- `AE2LTCapabilities`：库侧 capability 入口与 API 版本号
- `AE2LTBlockEntityIds` / `AE2LTRecipeIds`：冻结资源 ID 常量
- `AE2LTNativeBridge`：AE2LT 原生 API 探测与命名空间辅助
- `AE2LTVersion`：运行时版本比较与功能门槛判断
- `AE2LTFrequencyBinding`：内部频率绑定宿主反射桥
- `AE2LTFrequencyApi`：公共频率 API 反射桥
- `LightningCollectedEvent`：镜像 AE2LT 原生 collector 事件
- 各类 AE2LT 配方构建器

## 7. 当前发布策略

- 三条线都应先本地 `clean build` 通过，再上传 Release。
- 对外发布文件名统一采用 `Thunderbolt_lib_<minecraft版本号>_<forge还是neoforge>_<AE2LT版本号>.jar`。
- 当前 `v1.0.10` 应统一挂载三条线的 jar：
  - `Thunderbolt_lib_1.20.1_forge_1.0.10.jar`
  - `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar`
  - `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha.jar`
- 如果内部版本号重复携带了 Minecraft / loader 后缀，上传前要先短化对外文件名。

## 8. 已知约束

- 真正完整的运行时兼容，仍需要在最终 Minecraft 实例里与 AE2 / AE2LT 联装复核。
- 三条线虽然共享 addon-facing API 思路，但底层 loader 与运行时合同不同，不能强行把所有实现细节无脑共用。
- Thunderbolt_lib 的公开 API 与 AE2LT 自带 first-party API 仍然是两套独立命名空间，不做隐式自动转换。
