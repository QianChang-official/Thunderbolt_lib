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

## 2. 本次调整

这次在原有跟进 AE2LT 1.0.0 能力面的基础上，又做了命名与版本整理：

- 版本从 `0.4.0-snapshot` 调整为 `1.0.0`
- 构建产物改为 `Thunderbolt_lib-1.0.0.jar`
- 项目 / 仓库显示名改为 `Thunderbolt_lib`
- 运行时 `mod_id` 继续保留为 `ae2lt_api`
- 新增免责声明，说明名称仅用于非商业用途，如有侵权异议可联系即改

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

- `AE2LTAPI`：方块 / 物品的闪电能力查询与读写辅助
- `AE2LTRecipeTypes`：统一 recipe id 常量
- `LightningStrikeRecipeBuilder`：补齐主模组现有 `lightning_strike` 配方构建器
- `internal.compat`：AE2LT 机器运行时桥接层
- `LightningCollectedEvent`：在 collector 收电流程里真实可拦截
- 中英文 README 与独立免责声明同步更新

## 5. 建议继续测试的重点

- `Thunderbolt_lib-1.0.0.jar` 与 `ae2lt-1.0.0.jar` 同时加载是否稳定
- `AE2LTAPI.getLightningHandler(...)` 是否仍能正确桥接 AE2LT 机器
- `LightningCollectedEvent` 改值 / 取消后是否真正影响收电结果
- `LightningStrikeRecipeBuilder` 生成的 JSON 是否能被 AE2LT 正确识别
- 旧 addon 若依赖 `ae2lt_api`，在只改 Jar 名与显示名的情况下是否保持兼容
