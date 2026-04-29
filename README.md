# 双人课程表 (DuoSchedule)

一款面向情侣的双人课程表应用，可同时显示两个人的当前课程状态，方便了解对方日程安排。

## 功能特性

- 💑 **双人课程管理**：支持在本地存储和管理两个人的独立课程表
- 📱 **实时状态显示**：主页实时显示两个人当前正在上的课程
- 📅 **今日课程列表**：显示两个人今天的完整课程列表
- 🔔 **课程提醒**：支持课前提醒和自动静音模式
- 🎨 **精美界面**：采用毛玻璃设计语言，支持深色模式
- 📊 **桌面组件**：支持 MIUI 和原生 Android 桌面组件

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **架构模式**：MVVM
- **依赖注入**：Hilt
- **本地数据库**：Room
- **数据存储**：DataStore

## 系统要求

- Android 8.0 (API 26) 及以上版本
- 推荐 Android 12+ 以获得最佳体验

## 构建说明

1. 克隆项目到本地
2. 使用 Android Studio 打开项目
3. 同步 Gradle 依赖
4. 运行 `./gradlew assembleDebug` 构建调试版本

## 项目结构

```
app/
├── src/main/
│   ├── java/com/duoschedule/
│   │   ├── data/          # 数据层
│   │   ├── ui/            # UI 层
│   │   ├── notification/  # 通知相关
│   │   └── widget/        # 桌面组件
│   └── res/               # 资源文件
└── build.gradle.kts
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
