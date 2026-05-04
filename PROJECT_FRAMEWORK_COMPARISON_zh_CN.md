# Thunderbolt_lib / AE2LT 项目对照说明

## 1. 当前定位

### AE2-Lightning-Tech

主模组仓库，负责完整游戏内容实现，包括：

- 方块、方块实体、机器、菜单、渲染、网络同步
- 闪电能量、ME 网络、配方执行、闪击仪式
- 资源、数据包、兼容层与配置

### Thunderbolt_lib

当前仓库现在定位为 AE2LT 的 addon API 与运行时桥接库，不复刻主模组内容，主要负责：

- 公开 API
- 运行时兼容桥接
- 配方 JSON 构建器
- 插件发现入口
- 第三方 addon 的低耦合集成层

## 2. 本次调整(1.0.4)

AE2LT 1.0.4 相比 1.0.3 没有改变 first-party API 包 `com.moakiee.ae2lt.api`、配方 schema 或 5 个并网机器 ID；主模组侧本次主要是内部性能优化与 bug fix。本次 1.0.4 对齐升级仍以**低风险加法接口**为主,做了下面几件事:

- 版本从 `1.0.3` 升级为 `1.0.4`
- 构建产物改为 `Thunderbolt_lib-1.0.4.jar`
- 新增 `api/bridge/AE2LTVersion`,用于运行时查询已加载 AE2LT 版本、判断已核对兼容范围和比较版本号
- `AE2LTAPI` 增加 `getApiVersion()`、`getTargetAE2LTVersion()`、`getLoadedAE2LTVersion()`、`isAE2LTVersionAtLeast(...)`
- `AE2LTNativeBridge` 增加 AE2LT 自带 / Thunderbolt_lib 两侧 lightning capability 的 `ResourceLocation` 查询方法
- `ILightningEnergyHandler` 增加与 AE2LT 自带 `ILightningEnergyHandler` 命名一致的默认别名:`getStored`、`getCapacity`、`insert`、`extract`
- `LightningEnergyTier` 增加 `fromSerializedName(String)` 别名,对齐 AE2LT 自带 `LightningTier`
- `AE2LTCapabilities.API_VERSION` 同步升级到 `1.0.4`
- `neoforge.mods.toml` 的 AE2LT 运行时依赖范围收紧为 `1.0.4+`
- 中英文 README 与项目对照说明同步更新

### 2.1 之前的调整(1.0.3)

AE2LT 自身在 1.0.2 引入了 first-party API 包 `com.moakiee.ae2lt.api`(命名空间 `ae2lt`),并在 1.0.3 修复了若干联机崩服与渲染 / 闪电烧物问题。Thunderbolt_lib 1.0.3 对齐升级做了下面几件事:

- 版本从 `1.0.2` 升级为 `1.0.3`
- 构建产物改为 `Thunderbolt_lib-1.0.3.jar`
- 新增 `api/ids/AE2LTBlockEntityIds`(6 个 BE id 常量 + `LIGHTNING_GRID_MEMBERS` 列表)
- 新增 `api/ids/AE2LTRecipeIds`(6 个配方类型常量)
- 新增 `api/bridge/AE2LTNativeBridge`,在运行时探测 AE2LT 自带 API 是否存在,并暴露两个命名空间(`ae2lt` / `ae2lt_api`)
- `LightningEnergyTier` 增加 `CODEC`(Mojang)与 `STREAM_CODEC`(`RegistryFriendlyByteBuf`),并新增 `fromOrdinal(int)`,与 AE2LT 自带 `LightningTier` 的线格式互通
- `LightningCollectedEvent` 增加 `isNaturalWeather()` 与对应 5 参构造,旧 4 参构造保留并默认 `false`
- 收电桥接(`AE2LTLightningCollectorEventBridge`)向新事件构造透传 `naturalWeather` 信息
- `AE2LTCapabilities.API_VERSION` 同步升级到 `1.0.3`

### 2.2 之前的调整(1.0.1 / 1.0.2)

在原有跟进 AE2LT 1.0.0 能力面的基础上,1.0.1 / 1.0.2 针对 AE2LT 1.0.2 的实际接口做了精确对齐:

- 版本从 `1.0.0` 升级为 `1.0.1`,随后再升级为 `1.0.2`(内容与 1.0.1 完全一致,仅版本号变更)
- 构建产物改为 `Thunderbolt_lib-1.0.1.jar` 与 `Thunderbolt_lib-1.0.2.jar`
- `CrystalCatalyzerRecipeBuilder` 增加 `mode("dust")` 和 tag 输出能力,以匹配 AE2LT 1.0.2 的 `crystal_catalyzer/dust/*.json` 配方
- `AE2LTReflection.BRIDGED_BLOCK_ENTITY_IDS` 修正为 5 个并网机器(原来错误地包含了 `crystal_catalyzer`,该机器实际只用 FE)
- `AE2LTCapabilities.API_VERSION` 同步升级
- 中英文 README 与项目对照说明同步更新

### 2.3 之前的调整(1.0.0)

- 版本从 `0.4.0-snapshot` 调整为 `1.0.0`
- 构建产物改为 `Thunderbolt_lib-1.0.0.jar`
- 项目 / 仓库显示名改为 `Thunderbolt_lib`
- 运行时 `mod_id` 继续保留为 `ae2lt_api`
- 新增免责声明,说明名称仅用于非商业用途,如有侵权异议可联系即改

## 3. 为什么保留 ae2lt_api 这个 mod_id

如果把 `mod_id` 也一起改掉，会直接影响：

- `neoforge.mods.toml` 的依赖声明
- 现有 addon 对 `ae2lt_api` 的运行时依赖
- 可能写死 `ae2lt_api` 的兼容判断或能力访问代码

所以本次采用低风险方案：

- 对外文件名、仓库名、展示名改为 `Thunderbolt_lib`
- 运行时标识仍保持 `ae2lt_api`

这样既能完成品牌/命名调整，也不会把兼容面直接打断。

## 4. 目前已经实现的核心能力

- `AE2LTAPI`:方块 / 物品的闪电能力查询与读写辅助
- `AE2LTRecipeTypes`:统一 recipe id 常量
- `LightningStrikeRecipeBuilder`:补齐主模组现有 `lightning_strike` 配方构建器
- `CrystalCatalyzerRecipeBuilder`:支持普通模式与 `dust` 模式,输出可为 item id 或 tag id(对齐 1.0.2 schema)
- `internal.compat`:AE2LT 5 个并网机器的运行时桥接层(Lightning Collector / Simulation Room / Assembly Chamber / Overload Processing Factory / Tesla Coil)
- `LightningCollectedEvent`:在 collector 收电流程里真实可拦截,1.0.3 起携带 `naturalWeather` 标识
- `AE2LTBlockEntityIds` / `AE2LTRecipeIds`:1.0.3 起暴露的冻结 ID 常量
- `AE2LTNativeBridge`:1.0.3 起暴露的 AE2LT 自带 API 探测助手
- `AE2LTVersion`:1.0.4 起暴露的版本检测与版本比较助手
- `LightningEnergyTier.CODEC` / `STREAM_CODEC`:1.0.3 起暴露的 Mojang Codec 与网络 StreamCodec
- 中英文 README 与独立免责声明同步更新

## 5. 建议继续测试的重点

- `Thunderbolt_lib-1.0.4.jar` 与 `ae2lt-1.0.4.jar` 同时加载是否稳定
- `AE2LTAPI.getLightningHandler(...)` 是否仍能正确桥接 5 个并网机器
- `LightningCollectedEvent` 改值 / 取消后是否真正影响收电结果,且 `isNaturalWeather()` 在自然雷暴 vs 命令触发雷击时是否区分正确
- `LightningEnergyTier.STREAM_CODEC` 与 AE2LT `LightningTier.STREAM_CODEC` 之间能否做到 ordinal 级别互通
- `CrystalCatalyzerRecipeBuilder.dustMode()` 配合 `outputTag(...)` 生成的 JSON 是否能被 AE2LT 1.0.2+ 的 `crystal_catalyzer/dust/*` schema 正确识别
- `LightningStrikeRecipeBuilder` 生成的 JSON 是否能被 AE2LT 正确识别
- 旧 addon 若依赖 `ae2lt_api` 1.0.0 / 1.0.2 / 1.0.3,在升级到 1.0.4 后源码层面仍然兼容(API 表面继续保留既有签名)
- `AE2LTNativeBridge.isNativeApiAvailable()` 在 AE2LT 1.0.2+ 装载 / 缺席两种情况下分别返回 true / false

## 6. 已知约束

- 本仓库 `libs/ae2lt-1.0.2.jar` 仅包含资源(无 .class),开发期编译依赖完全由反射桥接覆盖,不需要类文件
- 真正的运行时联调需要在最终用户的 Minecraft 实例中,与完整版 AE2LT 1.0.4 一同加载
- AE2LT 1.0.4 自带的 first-party API(`com.moakiee.ae2lt.api`)与本库的 API(`com.qianchang.ae2lt_api.api`)是两套独立命名空间,不会自动转换;`AE2LTNativeBridge` 仅提供探测与 ID 查询,不做隐式桥接
