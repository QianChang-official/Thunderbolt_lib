# Thunderbolt_lib / AE2LT / AE2 对照说明

## 1. 当前定位

### AE2（Applied Energistics 2）

AE2 是底层宿主生态，负责提供 ME 网络、存储、节点、动作源等基础合同。Thunderbolt_lib 不直接复刻 AE2 功能，但必须持续关注 AE2 对外合同是否发生影响桥接层的变化。

### AE2-Lightning-Tech（AE2LT）

AE2LT 是主模组仓库，负责完整游戏内容实现，包括：

- 方块、方块实体、菜单、渲染、网络同步
- 闪电能量系统、ME 网络扩展、机器逻辑、仪式逻辑
- 资源、数据包、配方、兼容层与配置

### Thunderbolt_lib

Thunderbolt_lib 是 AE2LT addon 开发用的 API 前置库与运行时桥接层，主要负责：

- 稳定公开 API
- 运行时兼容桥接
- 配方 JSON 构建器
- 插件发现入口
- 下游 addon 的低耦合集成面

它不负责复刻 AE2LT 主模组完整内容，而是尽量把“适合给 addon 公开”的部分稳定下来。

## 2. 当前发布矩阵

| 线别 | Thunderbolt_lib 版本 | 分支 | 对齐 AE2LT | Minecraft | Loader | Java | 对外发布文件名 |
|------|----------------------|------|------------|-----------|--------|------|----------------|
| 稳定线 | `1.0.10` | `main` | `1.0.10` | `1.21.1` | NeoForge `21.1.x` | `21` | `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar` |
| 移植线 | `1.0.10-alpha.26.1.2neoforge` | `Minecraft26.1.2neoforge` | `1.0.0alpha-26.1.2neoforge` | `26.1.2` | NeoForge `26.1.2.21-beta` | `25` | `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha-26.1.2neoforge.jar` |

## 3. 本次上游同步结论（2026-05-20）

本次整理主线文档前，已对本地仓库做了一轮核查与更新：

- `Thunderbolt_lib main`：与 `origin/main` 一致。
- `Thunderbolt_lib Minecraft26.1.2neoforge`：与 `origin/Minecraft26.1.2neoforge` 一致。
- `AE2-Lightning-Tech main`：本地原先落后 `origin/main` 24 个提交，现已快进拉到 `79a3ee1 (1.0.10)`。
- `AE2-Lightning-Tech port/26.1.2-neoforge`：本地工作树与远端 `889c402 (1.0.0alpha-26.1.2neoforge)` 对齐。
- `Applied-Energistics-2`：此前本地缺失，现已补齐克隆，用于核对高版本 AppEng 合同。

换句话说，这次文档、构建和发布整理，不是在旧基线上“盲修”，而是建立在已同步后的上游状态之上。

## 4. 稳定线（1.21.1 / NeoForge 21.1.x）更新总览

下面把 `main` 分支这条稳定线已经做过的更新，按版本号重新整理一遍。

### `1.0.10`

- 对齐 AE2LT `1.0.10`
- 复核 AE2LT `1.0.9` / `1.0.10` 无线频率改造，确认现有桥接仍兼容
- 新增公共绑频宿主 / 菜单宿主桥接接口
- 新增共享绑频界面的 fail-closed 打开请求
- 加强 `PluginLoader`，避免重复发现、重复服务项、损坏 `ServiceLoader` 元数据导致异常扩散

### `1.0.8`

- 对齐 AE2LT `1.0.8`
- 新增 `AE2LTFrequencyApi` 及其相关快照类型
- 以反射方式桥接 AE2LT 公共无线频率 API，避免把 AE2LT 类型硬编码进 Thunderbolt_lib 的公开方法签名

### `1.0.7`

- 把 `LightningCollectedEvent` 改为镜像 AE2LT 原生公开事件
- 不再由库侧抢先截获闪电实体 tick
- 取消/改值继续回写到 AE2LT 原生 collector 流程
- 这一版做了真实运行时联调验证，而不只是静态编译通过

### `1.0.6`

- 新增 `AE2LTFrequencyBinding`
- 为 AE2LT 内部频率绑定宿主提供反射式辅助接口
- 可以读取/设置/清除频率 ID，读取连接状态与频道统计

### `1.0.5`

- 新增频率绑定系统的可用性探测
- 新增频率绑定相关版本门槛判断
- 对热路径反射查询做缓存，减少重复查找 `Method` / `Field`

### `1.0.4`

- 新增 `AE2LTVersion`
- 新增当前 API 版本、目标 AE2LT 版本、已加载 AE2LT 版本等门面方法
- 新增 capability id 辅助查询
- `ILightningEnergyHandler` 增加与 AE2LT 原生命名风格对齐的默认别名

### `1.0.3`

- 新增 `AE2LTBlockEntityIds` 与 `AE2LTRecipeIds`
- 新增 `AE2LTNativeBridge`
- `LightningEnergyTier` 新增 `CODEC` / `STREAM_CODEC` / `fromOrdinal(int)`
- `LightningCollectedEvent` 新增 `isNaturalWeather()`

### `1.0.2`

- 仅版本号跟进 AE2LT `1.0.2`
- 功能内容与 `1.0.1` 等价

### `1.0.1`

- `CrystalCatalyzerRecipeBuilder` 补齐 `dust` 模式与 tag 输出
- 修正被桥接的并网机器列表为真正的 5 个方块实体
- 明确把 `crystal_catalyzer` 从闪电能量桥接列表里移除

### `1.0.0`

- 项目正式公开发布
- 仓库 / 展示名更名为 `Thunderbolt_lib`
- 运行时 `mod_id` 刻意保留为 `ae2lt_api`

## 5. 26.1.2 移植线更新总览

`Minecraft26.1.2neoforge` 不是对稳定线做“小补丁”，而是一条隔离出来的高版本移植线。目前主版本号写作：

### `1.0.10-alpha.26.1.2neoforge`

这条线目前已经完成的主要改动如下：

- 将 `26.1.2` 迁移工作隔离到独立 worktree / 分支，避免污染稳定线
- 升级到 JDK `25`
- 升级到 Gradle `9.2.1`
- 使用 NeoForge `26.1.2.21-beta`
- 把需要迁移的 ID 用法从 `ResourceLocation` 调整到 `Identifier`
- 去掉剩余对 AE2 编译期直接依赖的部分，改为全反射访问 AppEng 网格接口
- 让网格电量读取逻辑对齐 AE2LT 26.1.2 自己的 `GridLightningEnergyHandler`
  - 读：`getStorageService().getCachedInventory().get(...)`
  - 写：`getInventory().insert/extract(...)`
- 网格节点查询优先走 `IActionHost#getActionableNode()`，仅在兼容旧合同场景才回退到 `getMainNode()`
- 新增高版本运行时合同启动预检；如果关键 AppEng / AE2LT 接口漂移，直接停用 capability bridge，而不是带着半坏状态继续运行
- 让 `AE2LTVersion` 能识别 `1.0.0alpha-26.1.2neoforge` 这类移植分支版本串，并继续正确判断 first-party API / public frequency 功能门槛

这条线的目标不是“立刻把稳定线 merge 过去”，而是先保证：在高版本环境里，库能以更诚实、更保守的方式工作——能桥接就桥接，不确定就 fail-closed。

## 6. 为什么仍然保留 `ae2lt_api` 这个 mod_id

如果把运行时 `mod_id` 一起改掉，会直接影响：

- `neoforge.mods.toml` 依赖声明
- 现有 addon 对 `ae2lt_api` 的依赖
- 可能写死 `ae2lt_api` 的兼容判断与 capability 访问代码

因此当前方案是：

- 仓库名、项目展示名、发布文件名统一使用 `Thunderbolt_lib`
- 运行时标识继续保持 `ae2lt_api`

这样能在不打断生态兼容面的前提下，完成命名与发布整理。

## 7. 当前已经稳定下来的核心能力

- `AE2LTAPI`：统一静态门面
- `ILightningEnergyHandler` / `LightningEnergyTier`：闪电能量能力接口与等级枚举
- `AE2LTCapabilities`：库侧 capability 入口与 API 版本号
- `AE2LTBlockEntityIds` / `AE2LTRecipeIds`：冻结资源 ID 常量
- `AE2LTNativeBridge`：AE2LT 原生 API 探测与命名空间辅助
- `AE2LTVersion`：运行时版本比较与功能门槛判断
- `AE2LTFrequencyBinding`：内部频率绑定宿主反射桥
- `AE2LTFrequencyApi`：公共频率 API 反射桥
- `LightningCollectedEvent`：镜像 AE2LT 原生 collector 事件
- 一整套配方构建器：`lightning_assembly` / `lightning_transform` / `lightning_simulation` / `overload_processing` / `crystal_catalyzer` / `lightning_strike`

## 8. 当前发布与验证策略

- 两条线都必须先本地 `clean build` 通过，再谈 Release
- 对外发布文件名统一采用 `Thunderbolt_lib_<minecraft版本号>_<forge还是neoforge>_<AE2LT版本号>.jar`
- 当前推荐的两个文件名分别是：
  - `Thunderbolt_lib_1.21.1_neoforge_1.0.10.jar`
  - `Thunderbolt_lib_26.1.2_neoforge_1.0.0alpha-26.1.2neoforge.jar`
- `main` 分支文档负责做“双线总索引”
- `Minecraft26.1.2neoforge` 分支继续保留移植专属实现与分支内 changelog

## 9. 已知约束

- 真正的整包联机 / 客户端运行期兼容，仍然需要在最终 Minecraft 实例里与 AE2LT / AE2 一起加载验证
- 稳定线与移植线虽然共享 addon-facing API 思路，但底层运行时合同并不完全相同，因此不能把所有实现细节强行无脑共用
- Thunderbolt_lib 的公开 API 与 AE2LT 自带 first-party API 依然是两套独立命名空间，不做隐式自动转换
