**当前版本**：3.2.4

版本号计算公式：versionCode = MAJOR × 10000 + MINOR × 100 + PATCH

**最后更新**：2026-05-20

---

## 新增功能

### 关于页面重构

设置页"关于"分组改为仅显示"关于 DuoSchedule"导航入口，点击进入独立的关于页面，包含：

- **更新日志**：展示版本更新记录列表，每条记录包含版本号、日期、变更类型标签和更新内容摘要
- **检查更新**：查看是否有新版本
- **用户协议和隐私政策**：展示用户协议（MIT License 开源许可、使用规范、免责声明）和隐私政策（数据存储、数据收集、网络访问、通知权限、第三方库）
- **开源致谢**：按许可证类型分组展示所有开源依赖库

### 开源致谢功能

致谢页面展示核心开源依赖库：

- Kotlin — JetBrains — Kotlin 编程语言
- Hilt — Google — 依赖注入框架
- OkHttp — Square — HTTP 客户端
- jsoup — Jonathan Hedley — Java HTML 解析器
- AndroidLiquidGlass — kyant0 — Compose 液态玻璃效果
- Shapes — kyant0 — iOS 风格平滑圆角形状
- Capsule — kyant0 — Compose 连续圆角矩形

### 设置页布局优化

- "预测式返回"开关从"关于"分组移至"外观与显示"分组末尾，更符合功能分类逻辑
