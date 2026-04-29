# 双人课程表 (DuoSchedule)

一款面向情侣的双人课程表应用，可同时显示两个人的当前课程状态，方便了解对方日程安排。

## 🌟 功能特性

- 💑 **双人课程管理**：支持在本地存储和管理两个人的独立课程表
- 📱 **实时状态显示**：主页实时显示两个人当前正在上的课程
- 📅 **今日课程列表**：显示两个人今天的完整课程列表，支持时间线视图
- 🔔 **课程提醒**：支持课前提醒和自动静音模式，不错过重要课程
- 🎨 **精美界面**：采用毛玻璃设计语言，支持深色模式自动切换
- 📊 **桌面组件**：支持 MIUI 和原生 Android 桌面组件，快速查看日程
- 📥 **课程导入**：支持从教务系统导入课程，支持 CSV 模板导入
- 🔄 **数据同步**：支持课程数据的导入导出，方便数据迁移

## 🛠️ 技术栈

- **语言**：Kotlin 2.0
- **UI 框架**：Jetpack Compose 1.6
- **架构模式**：MVVM + Repository
- **依赖注入**：Hilt
- **本地数据库**：Room
- **数据存储**：DataStore
- **通知**：WorkManager + AlarmManager

## 📋 系统要求

- Android 8.0 (API 26) 及以上版本
- 推荐 Android 12+ 以获得最佳毛玻璃效果体验

## 🚀 快速开始

### 构建项目

```bash
# 克隆项目
git clone https://github.com/yang-jk/DuoSchedule.git

# 进入项目目录
cd DuoSchedule

# 使用 Android Studio 打开并同步 Gradle
# 或使用命令行构建
./gradlew assembleDebug
```

### 运行项目

1. 连接 Android 设备或启动模拟器
2. 在 Android Studio 中点击 Run 按钮
3. 或使用命令：`./gradlew installDebug`

## 📁 项目结构

```
app/
├── src/main/
│   ├── java/com/duoschedule/
│   │   ├── data/          # 数据层（Repository、Dao、Model）
│   │   ├── di/            # 依赖注入配置
│   │   ├── ui/            # UI 层（Compose 屏幕和组件）
│   │   ├── notification/  # 通知和定时任务
│   │   ├── widget/        # 桌面小组件
│   │   └── util/          # 工具类
│   └── res/               # 资源文件（布局、样式、图片）
└── build.gradle.kts       # 模块构建配置
```

## 📱 界面预览

### 主要屏幕

| 主屏幕 | 课程表 | 设置 |
|--------|--------|------|
| 显示当前课程和今日安排 | 周视图课程表 | 个性化设置选项 |

## 📝 使用说明

1. **添加课程**：点击课程表中的空白区域或使用添加按钮
2. **编辑课程**：长按课程卡片进行编辑或删除
3. **导入课程**：在设置中选择导入功能，支持教务系统和 CSV 导入
4. **设置提醒**：在课程编辑页面设置课前提醒时间
5. **切换主题**：在设置中切换浅色/深色模式

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 贡献指南

1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送到分支：`git push origin feature/your-feature`
5. 创建 Pull Request

## 🙏 致谢

感谢所有为项目做出贡献的开发者！

---

**DuoSchedule** - 让情侣间的课程安排更清晰 💕