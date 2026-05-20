# 产品变更记录

本文档记录产品需求文档的所有变更历史，遵循语义化版本规范。

版本号计算公式：versionCode = MAJOR × 10000 + MINOR × 100 + PATCH

---

---

## [3.2.0] - 2026-05-20

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**关于页面重构 + 开源致谢功能**

1. **重构**：设置页"关于"分组改为仅显示"关于 DuoSchedule"导航入口，点击进入独立的关于页面
2. **新增**：AboutScreen 关于页面，包含应用图标/名称/版本号，以及四个导航条目：更新日志、检查更新、用户协议和隐私政策、开源致谢
3. **新增**：ChangelogScreen 更新日志页面，展示版本更新记录列表，每条记录包含版本号、日期、变更类型标签和更新内容摘要
4. **新增**：LegalScreen 用户协议和隐私政策页面，展示用户协议（MIT License 开源许可、使用规范、免责声明）和隐私政策（数据存储、数据收集、网络访问、通知权限、第三方库）
5. **新增**：AcknowledgmentsScreen 开源致谢页面，按许可证类型分组展示所有开源依赖库（MIT License: jsoup；Apache License 2.0: Kotlin Stdlib、Jetpack Compose、AndroidX 系列、OkHttp、AndroidLiquidGlass、Shapes、Capsule）
6. **迁移**："预测式返回"开关从"关于"分组移至"外观与显示"分组末尾，更符合功能分类逻辑

### 修改文件

- `app/src/main/java/com/duoschedule/ui/settings/AboutScreen.kt`（新增关于页面）
- `app/src/main/java/com/duoschedule/ui/settings/ChangelogScreen.kt`（新增更新日志页面）
- `app/src/main/java/com/duoschedule/ui/settings/LegalScreen.kt`（新增法律信息页面）
- `app/src/main/java/com/duoschedule/ui/settings/AcknowledgmentsScreen.kt`（新增开源致谢页面）
- `app/src/main/java/com/duoschedule/ui/settings/SettingsScreen.kt`（重构关于分组，移动预测式返回开关位置）
- `app/src/main/java/com/duoschedule/ui/navigation/Navigation.kt`（添加 settings/about、settings/changelog、settings/legal、settings/acknowledgments 路由）
- `app/build.gradle.kts`（versionCode 30101→30200，versionName 3.1.1→3.2.0）
- `Product-Spec.md`（版本号 3.1.1→3.2.0，添加关于页面和开源致谢功能描述）
- `Product-Spec-CHANGELOG.md`（添加变更记录）

---

---

## [3.1.1] - 2026-05-20

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复应用内更新下载和安装功能多个严重问题**

1. **修复**：`file_paths.xml` 缺少 `<external-files-path>` 配置，APK 下载到 `context.getExternalFilesDir("apk")` 目录但 FileProvider 只配置了 `<cache-path>`，导致 `FileProvider.getUriForFile()` 抛出 `IllegalArgumentException`，安装按钮点击无反应
2. **修复**：`ApkInstaller.installApk()` 异常被 `try-catch` 静默吞掉，用户点击安装失败时看不到任何错误提示，现在返回 `Result<Unit>` 并在 ViewModel 中将错误展示给用户
3. **修复**：`UpdateViewModel.checkForUpdate()` 检测到缓存 APK 时未验证版本号，旧版 APK 直接显示为"下载完成"状态，现在使用 `PackageManager.getPackageArchiveInfo()` 读取缓存 APK 的 versionCode 与更新版本号比对，版本不匹配时删除旧文件并重新下载
4. **修复**：`ApkDownloader` 缺少下载内容验证，服务器返回 HTML 错误页面时也会被当作 APK 保存，现在校验 Content-Type（拒绝 text/html/text/plain）、最小文件大小（>500KB）、APK 文件头魔数（PK\x03\x04）
5. **修复**：`ApkDownloader` 当服务器不返回 Content-Length 时（`contentLength = -1`）进度回调永远不触发，UI 停留在 0%，现在每下载 64KB 报告一次进度（percent=-1 表示未知总大小）
6. **优化**：`UpdateScreen` 下载进度条支持未知总大小模式，显示不确定进度条 + "X MB 已下载" 文案

### 修改文件

- `res/xml/file_paths.xml`（添加 `<external-files-path name="apk_files" path="apk/" />`）
- `data/update/ApkInstaller.kt`（返回 `Result<Unit>` 替代静默吞异常，区分 IllegalArgumentException/ActivityNotFoundException 通用异常）
- `data/update/ApkDownloader.kt`（新增 Content-Type 校验、最小文件大小校验、APK 魔数校验、未知 contentLength 进度报告）
- `ui/update/UpdateViewModel.kt`（缓存 APK 版本号验证、安装失败错误反馈、APK 不存在错误反馈）
- `ui/update/UpdateScreen.kt`（下载进度条支持未知总大小模式）
- `app/build.gradle.kts`（versionCode 30100→30101，versionName 3.1.0→3.1.1）
- `Product-Spec.md`（版本号 3.1.0→3.1.1）
- `Product-Spec-CHANGELOG.md`（添加变更记录）

---

## [3.1.0] - 2026-05-20

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**重新梳理 Gitee 仓库与自动打包 APK 工作流**

1. **新增**：`scripts/cleanup_gitee_update_repo.py` 脚本，用于清理 Gitee `su-zijie21/duoschedule-update` 仓库中除 `update.json` 以外的所有文件（该仓库错误上传了项目源代码）
2. **重构**：`update_repo.py` 移除 `USE_GITEE` 环境变量依赖，改为自动检测 `GITEE_TOKEN` 是否存在来决定是否更新 Gitee `update.json`
3. **新增**：`update_repo.py` 支持 `GITEE_RELEASE_SUCCESS` 环境变量，Gitee Release 创建失败时 Gitee `update.json` 的 downloadUrl 自动回退到 GitHub Release URL，避免用户获取到 404 下载链接
4. **重构**：`release.yml` Gitee Release 步骤输出创建结果（`success=true/false`），传递给 `update_repo.py` 步骤
5. **优化**：ServerChan 通知在 Gitee Release 失败时提示 downloadUrl 已回退到 GitHub
6. **清理**：移除项目根目录残留的 `update.json` 文件，`.gitignore` 添加 `update.json` 条目

### 修改文件

- `scripts/cleanup_gitee_update_repo.py`（新增 Gitee 更新仓库清理脚本）
- `scripts/update_repo.py`（移除 USE_GITEE 依赖，新增 GITEE_RELEASE_SUCCESS 联动逻辑）
- `.github/workflows/release.yml`（Gitee Release 输出结果，传递给 update_repo.py，移除 USE_GITEE，ServerChan 通知增强）
- `.gitignore`（添加 update.json 条目）
- `app/build.gradle.kts`（versionCode 30003→30100，versionName 3.0.3→3.1.0）
- `Product-Spec.md`（版本号 3.0.3→3.1.0）
- `Product-Spec-CHANGELOG.md`（添加变更记录）

---

## [3.0.3] - 2026-05-20

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复 Gitee API 认证方式错误 + jsDelivr CDN 缓存刷新**

1. **修复**：update_repo.py 对 Gitee API 使用了 GitHub 的 `Authorization: token` 头认证，但 Gitee API 需要使用 `access_token` 查询参数和请求体字段认证，导致 Gitee update.json 更新一直失败
2. **新增**：update_repo.py 更新 GitHub update.json 后自动刷新 jsDelivr CDN 缓存（访问 purge.jsdelivr.net），避免 app 从 CDN 获取到过时的更新信息
3. **重构**：update_repo.py 拆分为 `update_github()` 和 `update_gitee()` 两个独立函数，分别使用各自平台正确的认证方式
4. **修复**：Gitee Release 仓库名大小写错误，代码中写的是 `DuoSchedule`（大写），实际 Gitee 仓库名为 `duoschedule`（全小写），导致 downloadUrl 指向不存在的仓库

### 修改文件

- `scripts/update_repo.py`（拆分 GitHub/Gitee 更新函数，修复 Gitee 认证方式，新增 jsDelivr CDN 缓存刷新，修复 Gitee 仓库名大小写）
- `.github/workflows/release.yml`（Gitee Release API URL 仓库名 DuoSchedule → duoschedule）
- `app/build.gradle.kts`（versionCode 30002→30003，versionName 3.0.2→3.0.3）
- `Product-Spec.md`（版本号 3.0.2→3.0.3）
- `Product-Spec-CHANGELOG.md`（添加变更记录）

---

## [3.0.2] - 2026-05-20

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复应用内更新检查失败的问题**

1. **修复**：AppUpdateChecker.kt 中 Gitee 更新检查 URL 使用了错误的用户名 `yang-jk`，导致 Gitee 更新源返回 404，修正为正确的 Gitee 用户名 `su-zijie21`
2. **修复**：update_repo.py 只更新 Gitee 的 update.json，未更新 GitHub 的 `yang-jk/duoschedule-update` 仓库，导致 GitHub CDN（jsDelivr）上的 update.json 严重过时（仍显示版本 1.0.0），app 从 GitHub CDN 获取到的最新版本号远低于当前安装版本，判定为无需更新
3. **新增**：update_repo.py 添加 GitHub 更新逻辑，发布时同时更新 GitHub 和 Gitee 两个平台的 update.json，确保双服务器故障转移机制正常工作

### 修改文件

- `app/src/main/java/com/duoschedule/data/update/AppUpdateChecker.kt`（Gitee URL 用户名 yang-jk → su-zijie21）
- `scripts/update_repo.py`（新增 GitHub update.json 更新逻辑，双平台同步更新）
- `app/build.gradle.kts`（versionCode 30001→30002，versionName 3.0.1→3.0.2）
- `Product-Spec.md`（版本号 3.0.1→3.0.2）
- `Product-Spec-CHANGELOG.md`（添加变更记录）

---

## [3.0.1] - 2026-05-20

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复 GitHub Actions 构建流程 + 添加 Gitee 镜像发布**

1. **修复**：修正 GitHub Actions 工作流中的版本号验证公式，与实际使用的公式 `MAJOR × 10000 + MINOR × 100 + PATCH` 保持一致
2. **修复**：修复 GitHub Actions 构建时密钥库路径错误，解决 `validateSigningRelease` 任务失败问题
3. **新增**：添加 Gitee 镜像支持，应用发布时自动上传 APK 到 Gitee Release 并更新 `update.json`，国内用户可从 Gitee 快速获取更新
4. **新增**：应用内更新检查支持双服务器故障转移（优先 Gitee，失败时自动切换到 GitHub CDN）
5. **优化**：ServerChan 通知增加 Gitee Release 状态追踪

### 修改文件

- `app/build.gradle.kts`（versionCode 116003→30001，versionName 1.16.3→3.0.1）
- `.github/workflows/release.yml`（修正版本号验证公式；修复密钥库路径；新增 Gitee Release 步骤；通知增加 Gitee 状态）
- `scripts/update_repo.py`（重写为支持 Gitee API 更新 update.json）
- `app/src/main/java/com/duoschedule/data/update/AppUpdateChecker.kt`（添加双服务器故障转移机制）
- `Product-Spec-CHANGELOG.md`（统一版本号计算公式，添加变更记录）

---

---

## [3.0.0] - 2026-05-20

### 变更类型：重大变更

### 状态：已实现

### 变更内容

**统一版本号计算规则 + 修复 GitHub Actions 构建签名错误**

1. **BREAKING**：版本号计算公式统一为 `versionCode = MAJOR × 10000 + MINOR × 100 + PATCH`，确保 versionCode 与 versionName 视觉对应关系直观（如 3.0.0 → 30000，12.12.12 → 121212）
2. **修复**：修正 GitHub Actions 工作流中的版本号验证公式，与实际使用的公式保持一致
3. **修复**：修复 GitHub Actions 构建时密钥库路径错误（`app/app/keystore` → `app/keystore`），解决 `validateSigningRelease` 任务失败问题
4. **新增**：添加 Gitee 镜像支持，应用发布时自动更新 Gitee `update.json`（需要配置 `USE_GITEE=true` 和 `GITEE_TOKEN`），国内用户可从 Gitee 快速获取更新；应用内更新检查支持双服务器故障转移（优先 Gitee，失败时自动切换到 GitHub CDN）

### 修改文件

- `app/build.gradle.kts`（versionCode 更新为 30000，versionName 更新为 3.0.0）
- `.github/workflows/release.yml`（修正版本号验证错误消息中的公式；修复密钥库路径配置）
- `scripts/update_repo.py`（添加 Gitee 镜像支持）
- `app/src/main/java/com/duoschedule/data/update/AppUpdateChecker.kt`（添加双服务器故障转移机制，优先 Gitee）
- `Product-Spec-CHANGELOG.md`（统一版本号计算公式，添加本次变更记录）

---

---

## [1.16.3] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复应用内更新安装 APK 崩溃 + 优化更新日志和 ServerChan 通知格式**

1. **修复**：AndroidManifest.xml 添加 REQUEST_INSTALL_PACKAGES 权限声明，修复安装 APK 时 SecurityException 崩溃
2. **优化**：changelog 提取去除 Markdown 特征（加粗标记、标题符号、变更类型/内容标签），输出纯文本
3. **优化**：ServerChan 通知使用 Markdown 兼容换行（行尾双空格），确保正确分行显示

### 修改文件

- `AndroidManifest.xml`（新增 REQUEST_INSTALL_PACKAGES 权限声明）
- `.github/workflows/release.yml`（优化 changelog 提取和 ServerChan 通知格式）
- `app/build.gradle.kts`（versionCode 116001→116003，versionName 1.16.1→1.16.3）
- `Product-Spec.md`（版本号 1.16.1→1.16.3）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

---

## [1.16.0] - 2026-05-19

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**重写 GitHub Actions 发布工作流与版本号计算规则**

1. **BREAKING**：版本号计算公式从 `MAJOR × 10000 + MINOR × 1000 + PATCH` 改为 `MAJOR × 100000 + MINOR × 1000 + PATCH`，使 versionCode 与 versionName 视觉对应关系更直观（如 2.06.06 → 206006）
2. **新增**：工作流自动从 versionName 计算 versionCode，并校验与 build.gradle.kts 中声明的一致，不一致时构建失败
3. **优化**：将版本信息提取和 changelog 提取拆分为独立步骤，changelog 提取失败时使用默认文案兜底
4. **优化**：GitHub Release 步骤和更新仓库步骤添加 step id，支持状态追踪
5. **新增**：更新仓库步骤添加 `continue-on-error: true`，失败不阻塞后续通知
6. **增强**：ServerChan 通知改为汇总模式，分别报告 APK 构建、Release 创建、更新仓库三个步骤的独立状态（✅成功/❌失败/⏭️跳过），包含版本号（versionName + versionCode）

### 修改文件

- `.github/workflows/release.yml`（重写工作流：新增 versionCode 校验、changelog 独立提取、ServerChan 汇总通知）
- `app/build.gradle.kts`（versionCode 25000→116000，versionName 1.15.0→1.16.0）
- `Product-Spec.md`（版本号 1.15.0→1.16.0，更新版本号计算公式）
- `Product-Spec-CHANGELOG.md`（更新版本号计算公式 + 新增变更记录）

---

## [1.15.0] - 2026-05-19

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**GitHub Actions CI/CD 自动打包发布**

1. **新增**：`.github/workflows/release.yml` GitHub Actions 工作流，当推送 `v*` 格式 tag 时自动触发
2. **新增**：自动构建 release APK（签名、混淆），重命名为 `DuoSchedule-{版本号}.apk`
3. **新增**：自动创建 GitHub Release，附带 APK 文件
4. **新增**：自动更新 `yang-jk/duoschedule-update` 仓库的 `update.json`，使 app 内更新检查生效
5. **修改**：`app/build.gradle.kts` 新增签名配置，支持从环境变量（CI）或 `key.properties`（本地）读取 keystore 信息

### 修改文件

- `.github/workflows/release.yml`（新增 GitHub Actions 工作流）
- `app/build.gradle.kts`（新增 signingConfigs 和环境变量读取逻辑）
- `Product-Spec.md`（版本号 1.14.6→1.15.0）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.6] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复导入课表后返回空白页面**

1. **问题**：导入课表确认后，用户看到空白页面而非成功对话框。`onConfirm` 回调在导入成功时立即导航到首页并清空 `pendingImportData`，导致 ImportPreviewScreen 被移出组合树，SuccessDialog 永远无法显示；`pendingImportData = null` 使导入预览页面走入空分支（无任何 UI 渲染），用户看到空白页面。此外，应用导出导入时 `importedTarget` 被设为 `null`，即使 SuccessDialog 能显示，"查看课表"按钮也不会执行任何导航
2. **修复**：将 `onConfirm` 回调改为空实现，不再触发导航和清空数据；将应用导出导入的 `importedTarget` 从 `null` 改为 `personATarget`，确保"查看课表"按钮能正确导航

### 修改文件

- `ui/navigation/Navigation.kt`（onConfirm 回调改为空实现）
- `ui/settings/ImportPreviewScreen.kt`（importedTarget 赋值修复）
- `app/build.gradle.kts`（versionCode 24005→24006，versionName 1.14.5→1.14.6）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.5] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复课表左右滑动切换周数概率性无响应**

1. **问题**：左右滑动切换周数时，有概率划不动，需要多划几次才能加载。`onHorizontalDrag` 中的 `swipeOffset.snapTo()` 通过 `scope.launch` 在协程中执行，而上一次滑动的 `animateTo` 动画可能仍在运行，`Animatable` 在动画期间 `snapTo` 会被忽略或延迟
2. **修复**：在 `onDragStart` 时立即调用 `swipeOffset.snapTo(swipeOffset.value)` 取消正在运行的动画，确保新拖拽手势即时控制偏移

### 修改文件

- `ui/schedule/ScheduleScreen.kt`（onDragStart 添加动画取消逻辑）
- `app/build.gradle.kts`（versionCode 24004→24005，versionName 1.14.4→1.14.5）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.4] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复CSV导入课程时Person B节次计算使用了Person A的课程时间配置**

1. **问题**：`CsvExporter.importFromCsv()` 只使用 `settingsAMap["课程时间"]`（Person A的课节时间）来计算所有课程的节次。当Person A和Person B有不同的课节时间配置时（例如A有5节课从08:15开始，B有6节课从08:20开始），Person B的课程节次号计算错误。
2. **修复**：将 `parseCourseLine` 的 `periodTimes` 参数拆分为 `periodTimesA` 和 `periodTimesB`，根据课程所属人员选择对应的课节时间配置来计算节次；新增 `determinedPersonType` 变量提前确定人员类型，用于选择正确的 `effectivePeriodTimes`；添加调试日志以便排查导入问题。

### 修改文件

- `data/importexport/CsvExporter.kt`（parseCourseLine签名改为periodTimesA/periodTimesB，节次计算使用人员对应的时间配置，新增TAG和Log调试日志，importFromCsv传入两组periodTimes）
- `app/build.gradle.kts`（versionCode 24003→24004，versionName 1.14.3→1.14.4）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.3] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复CSV导入课程时节次计算错误，所有非自定义时间课程显示在第一节**

1. **问题**：`CsvExporter.parseCourseLine()` 中，非自定义时间课程的 `startPeriod` 和 `endPeriod` 被硬编码为 `(1, 1)`，导致导入的课程无论实际时间如何，都在课表上显示在第一节
2. **修复**：新增 `getPeriodFromTime()` 和 `parsePeriodTimeRanges()` 辅助方法，根据课程实际开始/结束时间和课表时间配置（periodTimes）反算节次；`parseCourseLine` 新增 `periodTimes` 参数；`importFromCsv` 中对应用导出文件使用 settingsA 的课程时间配置传入，对模板文件使用默认 periodTimes

### 修改文件

- `data/importexport/CsvExporter.kt`（新增 getPeriodFromTime/parsePeriodTimeRanges 方法，parseCourseLine 新增 periodTimes 参数，替换硬编码(1,1)为计算值，importFromCsv 传入 effectivePeriodTimes）
- `app/build.gradle.kts`（versionCode 24002→24003，versionName 1.14.2→1.14.3）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.2] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复主页右上角周数显示被挤压成多行**

1. **问题**：主页 HeaderSection 中，外层 Row 使用 `Arrangement.SpaceBetween` 布局日期文本和周数信息，当两者自然宽度之和超过可用宽度时，右侧周数 Row 被压缩，内部 Text 缺少 `maxLines` 限制导致自动换行，"ta第X周" 被挤成三行
2. **修复**：移除外层 Row 的 `Arrangement.SpaceBetween`，为日期 Text 添加 `Modifier.weight(1f, fill = false)` 使周数 Row 优先按自然宽度测量不被压缩；为日期 Text 和周数 Text 均添加 `maxLines = 1` 防止换行

### 修改文件

- `ui/main/MainScreen.kt`（HeaderSection 外层 Row 移除 SpaceBetween，日期 Text 添加 weight+maxLines，周数 Text 添加 maxLines）
- `app/build.gradle.kts`（versionCode 24001→24002，versionName 1.14.1→1.14.2）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.1] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复设置二级菜单快速返回时页面转场动画卡顿/涩手**

1. **问题**：层级转场动画使用弹簧曲线（`AppleSpring.Gentle`/`Snappy`），弹簧物理计算在起始帧产生极大位移变化（高刚度导致速度尖峰），且弹簧振荡与中断后的 pop 动画方向冲突，用户感知为"卡一秒才有动画"和"涩手不流畅"
2. **修复**：将所有层级转场动画（`iosSlideEnter/Exit/PopEnter/PopExit`）从弹簧曲线改为 `tween` 曲线。enter 动画使用 `tween(350ms, FastOutSlowInEasing)`，pop 动画使用 `tween(300ms, FastOutSlowInEasing)`，确保动画启动即有位移、无速度尖峰、快速收敛无振荡

### 修改文件

- `ui/theme/AppleAnimationKit.kt`（iosSlideEnter/Exit 改用 tween(350ms)、iosSlidePopEnter/PopExit 改用 tween(300ms)，移除 snappyIntOffsetSpring/iosSlideTween/iosFadeTween 辅助函数）
- `app/build.gradle.kts`（versionCode 24000→24001，versionName 1.14.0→1.14.1）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.14.0] - 2026-05-19

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**数据管理页面新增备份管理功能 + 导出分享视角对调**

1. **新增**：DataManagementScreen 新增"备份管理"区域，展示导入前自动创建的备份文件列表（最多保留5份），每条备份显示可读时间戳（从文件名解析，如 backup_pre_import_20260519_103947.csv → "2026-05-19 10:39:47"）和文件大小
2. **新增**：备份条目支持"恢复"操作（通过 FileProvider 将备份文件转为 Uri，调用 importFromCsv 进入导入预览流程）和"删除"操作（调用 deleteBackupFile 后刷新列表）
3. **新增**：无备份时显示"暂无备份（导入课程时自动创建）"提示文字
4. **优化**：ImportPreviewScreen 成功对话框消息新增"已自动备份原有数据"提示，让用户感知备份已创建
5. **优化**：导出课程数据时自动对调"我"和"Ta"的视角（CSV版本升至5.0），使接收方打开文件时"我的名称"显示的是接收方自己的名字，无需手动调整人员分配；备份文件保持原始视角（CSV版本4.0），自己恢复时"我"还是"我"
6. **优化**：导入预览界面根据CSV版本自动选择默认人员映射——v5+（已对调）默认A→A/B→B，v4（未对调）默认A→B/B→A
7. **新增**：ImportResult/ImportPreviewData 新增 exportVersion 字段，用于传递CSV导出版本号

### 修改文件

- `ui/settings/DataManagementScreen.kt`（新增备份管理 SettingsSection，包含 backupFiles 状态、LaunchedEffect 加载、恢复/删除按钮、空状态提示；新增 LiquidGlassButton/LiquidGlassButtonStyle 导入；提升 labelsSecondary 为可组合级变量；ImportPreviewData 构造添加 exportVersion）
- `ui/settings/ImportPreviewScreen.kt`（SuccessDialog 消息添加"\n已自动备份原有数据"；默认人员映射根据 exportVersion 动态选择：v5+用A→A/B→B，v4用A→B/B→A）
- `data/importexport/CsvExporter.kt`（exportToFile/exportToCsv 新增 swapPersons 参数；writeExportContent 支持人员对调逻辑；swapPersons=true 时 CSV 版本升至 5.0；importFromCsv 传递 exportVersion 到 ImportResult）
- `data/importexport/ImportExportModels.kt`（ImportResult/ImportPreviewData 新增 exportVersion 字段）
- `ui/settings/SettingsViewModel.kt`（exportToCacheFile/exportToCsv 传 swapPersons=true；createPreImportBackup 不传 swapPersons 保持原始视角；Navigation.kt 传递 exportVersion）
- `ui/navigation/Navigation.kt`（ImportPreviewData 构造添加 exportVersion）
- `app/build.gradle.kts`（versionCode 23002→24000，versionName 1.13.2→1.14.0）
- `Product-Spec.md`（功能9新增备份管理描述 + 版本号更新）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.13.2] - 2026-05-19

### 变更类型：功能优化

### 状态：已实现

### 变更内容

**共享元素动画一镜到底优化**

1. **问题**：共享元素（Container Transform）动画在容器展开后，编辑页内部组件逐个出现，产生"容器先展开，内容再分别蹦出来"的割裂感，破坏一镜到底的连续性
2. **优化**：共享元素转场时使用 `EnterTransition.None` 替代 `fadeIn`，让内容直接显示而非淡入，配合 `clipInOverlayDuringTransition` 将内容裁剪在容器变形边界内，实现"揭幕"式一镜到底效果；所有 sharedElement 添加 `renderInOverlayDuringTransition = true`，确保共享元素渲染在叠加层不被父容器裁剪；非共享元素转场保持原有淡入淡出行为
3. **修复**：SettingsViewModel 缺少 `java.io.File` 导入导致编译失败

### 修改文件

- `ui/schedule/ScheduleScreen.kt`（AnimatedContent transitionSpec 条件化：共享元素转场用 EnterTransition.None，普通转场用 fadeIn；"+"按钮和课程卡片 sharedElement 添加 renderInOverlayDuringTransition 和 clipInOverlayDuringTransition）
- `ui/edit/CourseEditScreen.kt`（编辑页根容器 sharedElement 添加 renderInOverlayDuringTransition 和 clipInOverlayDuringTransition）
- `ui/settings/SettingsViewModel.kt`（添加缺失的 java.io.File 导入）
- `app/build.gradle.kts`（versionCode 23001→23002，versionName 1.13.1→1.13.2）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.13.1] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复导入课程确认无响应及双人分享课表人员分配错误**

1. **问题**：ImportPreviewScreen 中课程列表的 LazyColumn items key 仅基于课程名、星期、开始时间，同一人同一门课在不同周次范围（如1-8周和9-12周）时 key 重复，导致 `IllegalArgumentException: Key was already used` 运行时崩溃
2. **修复**：LazyColumn key 改为包含人员前缀（A_/B_）、课程名、星期、开始/结束时间、开始/结束周、周次类型，确保完全唯一
3. **问题**：AppExportConfirmDialog 的 personATarget/personBTarget 初始值为 null，导致确认按钮的 `confirmEnabled` 条件 `isAssignmentValid` 始终为 false，用户点击"确认导入"后无法继续
4. **修复**：personATarget 默认值改为 PersonType.PERSON_B，personBTarget 默认值改为 PersonType.PERSON_A，默认建议视角转换（导出方"我"→导入方"Ta"），确认按钮立即可点击
5. **问题**：双人课表分享时，接收方导入后课表人员未做视角转换，导出方的"我"直接映射为导入方的"我"，导致课表导入反了
6. **修复**：默认人员分配改为视角转换（导出方 PERSON_A → 导入方 PERSON_B，导出方 PERSON_B → 导入方 PERSON_A），用户仍可在确认对话框中手动调整
7. **问题**：SuccessDialog 中 importedTarget 硬编码为 PersonType.PERSON_B，应用导出文件导入成功后显示的人员目标不正确
8. **修复**：importedTarget 改为 null（应用导出文件同时导入双人课表，不应硬编码为单个人物）

### 修改文件

- `ui/settings/ImportPreviewScreen.kt`（LazyColumn key 添加人员前缀、personATarget/personBTarget 默认值改为视角转换、importedTarget 硬编码修复）
- `app/build.gradle.kts`（versionCode 23000→23001，versionName 1.13.0→1.13.1）
- `Product-Spec.md`（更新功能9描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.13.0] - 2026-05-19

### 变更类型：功能优化

### 状态：已实现

### 变更内容

**浅色模式课程卡片配色优化**

1. **问题**：浅色模式下课程卡片配色过于饱和刺眼（使用 Material Design 500 级高饱和色如 #2196F3、#4CAF50），与浅色背景形成强烈视觉冲突；卡片文字使用黑色（labelsPrimary），在饱和色背景上可读性差。深色模式使用 200-300 级柔和色彩视觉效果和谐，浅色模式品质不对等
2. **优化**：替换 CourseColorPaletteLight 为低饱和度、中等明度的柔和色调（16色），参考 Apple Calendar 浅色模式配色风格；课程卡片文字统一改为白色，确保在彩色背景上的可读性；降低浅色模式渐变 alpha（0.92→0.82），使色彩更通透柔和

### 修改文件

- `ui/theme/DesignTokens.kt`（替换 CourseColorPaletteLight 为柔和色调）
- `ui/schedule/ScheduleScreen.kt`（CourseOverlayCard 文字颜色改为白色、渐变 alpha 浅色模式降低）
- `app/build.gradle.kts`（versionCode 22006→23000，versionName 1.12.6→1.13.0）
- `Product-Spec.md`（更新功能6配色描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.12.6] - 2026-05-19

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复设置二级菜单快速返回时页面转场动画卡顿**

1. **问题**：当用户打开设置的二级菜单（如课表设置、显示设置、通知设置等）后快速按返回键时，页面转场动画出现明显卡顿/跳帧。根本原因是层级转场使用 `AppleSpring.Gentle`（dampingRatio=0.75, stiffness=400）弹簧曲线，该弹簧振荡时间长、收敛慢（约 500-700ms 才稳定）。快速返回时弹簧残余振荡与 pop 动画方向冲突产生视觉跳跃
2. **修复**：将层级转场 enter 动画（`iosSlideEnter/Exit`）从 `AppleSpring.Gentle` 改为 `AppleSpring.Snappy`（dampingRatio=1.0, stiffness=900），减少振荡并加快收敛；将层级转场 pop 动画（`iosSlidePopEnter/PopExit`）从弹簧曲线改为 `tween(durationMillis = 250, easing = FastOutSlowInEasing)`，确保动画快速收敛无振荡

### 修改文件

- `ui/theme/AppleAnimationKit.kt`（iosSlideEnter/Exit 改用 Snappy 弹簧、iosSlidePopEnter/PopExit 改用 tween 曲线）
- `app/build.gradle.kts`（versionCode 22005→22006，versionName 1.12.5→1.12.6）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.12.5] - 2026-05-18

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复课表周切换滑动动画在"我的课表"和"Ta的课表"之间表现不一致**

1. **问题**：相邻周网格仅在 `prevCourseSlotMap.isNotEmpty()` / `nextCourseSlotMap.isNotEmpty()` 时才渲染，导致 Ta 的课表在相邻周无课程时滑动动画看起来空白或断裂；动画使用 `tween` 时间曲线而非弹簧物理曲线；状态切换可能产生视觉闪烁
2. **修复**：移除 `isNotEmpty()` 条件，始终渲染相邻周网格（即使无课程也显示空网格）；将 `tween(250/200, FastOutSlowInEasing)` 替换为 `spring(dampingRatio = 1.0f, stiffness = 500f)`（AppleSpring.Decelerate）弹簧物理曲线；使用 `Snapshot.withMutableSnapshot` 确保 `selectedWeek` 变更和 `swipeOffset.snapTo(0f)` 在同一帧原子提交

### 修改文件

- `ui/schedule/ScheduleScreen.kt`（移除相邻周 isNotEmpty 条件、tween→spring 动画升级、Snapshot.withMutableSnapshot 原子状态切换）
- `app/build.gradle.kts`（versionCode 22004→22005，versionName 1.12.4→1.12.5）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.12.4] - 2026-05-18

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复 TodayCourseCardItem 和 FreeTimeSummaryChip 的 onDrawSurface 填充透明度过低导致卡片不可见**

1. **问题**：`TodayCourseCardItem` 和 `FreeTimeSummaryChip` 的 `onDrawSurface` 使用极低透明度的单层填充（深色 `Color.White.copy(alpha = 0.06f)` / 浅色 `Color.Black.copy(alpha = 0.03f)`），导致卡片在背景上几乎不可见
2. **修复**：将单层填充替换为与 `CurrentCourseCard` 一致的多层液态玻璃填充（Layer1_Tint + Layer2_Base ColorDodge + GlassEffect），使用较低 alpha 值实现更轻的玻璃效果；添加 `BlendMode.ColorDodge` 导入

### 修改文件

- `ui/main/components/TodayScheduleTimeline.kt`（TodayCourseCardItem 和 FreeTimeSummaryChip 的 onDrawSurface 多层玻璃填充 + 添加 BlendMode 导入）
- `app/build.gradle.kts`（versionCode 22003→22004，versionName 1.12.3→1.12.4）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.12.3] - 2026-05-18

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复底栏样式和预测式返回开关不一致**

1. **问题**：底栏（LiquidBottomTabs）第二个 Row（不可见着色层）缺少 `drawBackdrop` 调用，导致选中标签的玻璃效果（vibrancy/blur/lens/highlight）缺失，与 AndroidLiquidGlass-2.0.0-alpha03 参考实现不一致
2. **修复**：在第二个 Row 的 Modifier 链中补充 `drawBackdrop(backdrop, ...)` 调用，添加 vibrancy、blur、lens 和 highlight 效果，与参考实现保持一致
3. **问题**：首次安装打开应用时，预测式返回开关显示为关闭状态，但实际仍有预测式返回动画效果。原因是注册的 `PRIORITY_DEFAULT` 非动画回调与 Navigation 组件的 `PRIORITY_DEFAULT` 动画回调同优先级，系统优先使用动画回调导致预测式返回动画仍显示
4. **修复**：将禁用预测式返回时的回调优先级从 `PRIORITY_DEFAULT`（0）提升为 `PRIORITY_DEFAULT + 1`（1），确保非动画回调优先于 Navigation 的动画回调被分发，从而正确禁用预测式返回动画

---

## [1.12.2] - 2026-05-18

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复 MainViewModel 启动崩溃及通知重复调度**

1. **问题**：MainViewModel 的 `init` 块在 `personATodayCourses` 和 `personBTodayCourses` 属性初始化之前启动协程，由于 `Dispatchers.Main.immediate` 会立即执行协程体，导致访问未初始化的 StateFlow 引发 NullPointerException 崩溃
2. **修复**：将 `init` 块移到 `personBTodayCourses` 属性定义之后，确保协程启动时所有依赖的 StateFlow 已初始化
3. **问题**：应用启动时 `app_start`、`enter_foreground`、`BOOT_COMPLETED` 三个触发源几乎同时调用 `scheduleReminderNotifications()`，导致通知被重复调度 3 次
4. **修复**：在 `DuoScheduleApp` 中添加 5 秒防抖机制，避免短时间内重复调度通知

---

## [1.12.1] - 2026-05-18

### 变更类型：Bug修复

### 状态：已实现

### 变更内容

**修复数据库迁移崩溃（MIGRATION_6_7 缺少索引创建）**

1. **问题**：从数据库版本 6 升级到 7 时，MIGRATION_6_7 迁移为空，但 Course 实体新增了 `index_courses_dayOfWeek_personType_startHour_startMinute` 索引，导致 Room 验证失败，应用启动时崩溃（`IllegalStateException: Migration didn't properly handle: courses`）
2. **修复**：在 MIGRATION_6_7 中添加 `CREATE INDEX IF NOT EXISTS` 语句，创建缺失的复合索引

---

## [1.12.0] - 2026-05-18

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**主页结构与视觉优化：信息架构重构 + 视觉语言统一 + 间距节奏优化**

1. **Header 精简（Task 1）**：
   - 移除问候语（"早上好"等），日期文本从 headlineMedium + Bold 升级为 headlineLarge + Bold
   - 周次胶囊中分隔符从 "|" 替换为 "·"
   - Header 内部 padding top 从 Spacing.md 缩减为 Spacing.sm

2. **当前状态区融合与信息优化（Task 2）**：
   - 移除 CurrentCourseSection 中的"当前状态"标题和包裹 Column
   - Header 与 CurrentCourseCard 间距调整为 Spacing.xl（24dp）
   - CurrentCourseCard 与 TodayScheduleSection 间距调整为 Spacing.lg（16dp）
   - CurrentCourseCard 有课状态：移除进度条、剩余时间徽章和节次文字
   - 新增下节课预告（"↗ 下节: 线性代数 · 10:00"）
   - 空闲状态：显示下节课名称和开始时间（替代仅显示"下节 10:00"）

3. **共同空闲时间摘要化（Task 3）**：
   - 移除主页独立的 FreeTimeSection 区域（"共同空闲"标题 + FreeTimeCard 卡片）
   - 移除 FreeTimeCard、EmptyFreeTimeContent、FreeTimeMiniChip 组件
   - 新增 FreeTimeSummaryChip 组件，以紧凑芯片形式内嵌在今日课程列表顶部
   - 芯片显示"🕐 N个空闲时段 · 最近 HH:MM-HH:MM"，点击打开 AllFreeTimeSlotsSheet
   - 无空闲时段时不显示芯片，单人模式下不显示芯片
   - AllFreeTimeSlotsSheet 和 FreeTimeDetailSheet 改为 public，从 ScheduleList 中调用

4. **今日课程时间列移除与时间显示增强（Task 4）**：
   - 移除 TodayScheduleTimeline 中的 TIMELINE_WIDTH 常量和时间列 Box
   - 移除 DualColumnTimeSlotRow 和 SingleColumnTimeSlotRow 中的时间列渲染
   - 时间范围右对齐显示在课程名同行（13sp + Medium + 次级文字色）
   - 简化 calculateTimeSlots 为 calculateCourseInfos

5. **课程卡片信息增强（Task 5）**：
   - 移除 TodayCourseCardItem 中的 4dp 左侧颜色条
   - 在卡片顶部添加人物名标签（彩色圆点 + 人物名），风格与 CurrentCourseCard 一致
   - 在地点同行添加教师信息显示（如"教室A101 · 张老师"）
   - 移除 TodayCourseCardItem 中的进度条
   - 进行中课程：圆点指示器添加脉冲动画，卡片背景添加微妙人物色高亮（alpha 0.04）
   - 已结束课程：文字颜色降级为 labelsTertiary

6. **BOTH 模式单列合并布局（Task 6）**：
   - 重构 ScheduleList，BOTH 模式下将两人课程按开始时间合并排序为单列
   - SELF_ONLY 和 TA_ONLY 模式也使用单列布局

7. **卡片视觉风格统一（Task 7）**：
   - EmptyScheduleCard 从 shadow(4dp) + background(layer1Tint) 替换为 drawBackdrop 毛玻璃效果
   - FreeTimeSummaryChip 使用与 TodayCourseCardItem 一致的 drawBackdrop 毛玻璃效果

8. **间距节奏优化（Task 8）**：
   - Section 标题与内容间距从 Spacing.sm (8dp) 缩减为 Spacing.xs (4dp)
   - 间距层次：Header 紧凑 → Header 与状态区间距大 → 内容区间距适中 → Section 内间距紧

9. **空闲状态视觉增强（Task 9）**：
   - CurrentCourseCard 中"空闲中"咖啡图标添加人物色半透明背景圆形容器（36dp）

10. **垂直分割线优化（Task 10）**：
    - CurrentCourseCard 双人模式垂直分割线从 1dp 实线改为渐变淡出效果

### 修改文件

- `ui/main/MainScreen.kt`（Header 精简、移除 FreeTimeSection、CurrentCourseCard 间距调整、EmptyScheduleCard 玻璃拟态、间距优化）
- `ui/main/components/CurrentCourseCard.kt`（移除进度条/剩余时间/节次文字、新增下节课预告、空闲状态圆形背景、分割线渐变）
- `ui/main/components/TodayScheduleTimeline.kt`（移除时间列、新增 FreeTimeSummaryChip、人物名标签、教师信息、脉冲动画、单列合并布局）
- `ui/main/components/FreeTimeSection.kt`（移除 FreeTimeCard/EmptyFreeTimeContent/FreeTimeMiniChip、AllFreeTimeSlotsSheet/FreeTimeDetailSheet 改为 public）
- `app/build.gradle.kts`（versionCode 21000→22000，versionName 1.11.0→1.12.0）
- `Product-Spec.md`（更新功能2/3/4描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.11.0] - 2026-05-18

### 变更类型：性能优化

### 状态：已实现

### 优化内容

**MainScreen 状态收集拆分 + ViewModel Flow 链优化**

1. **状态收集拆分**：将 MainScreen 顶层的 12+ 个 `collectAsState()` 调用拆分到各子组件内部，每个子组件只订阅自己需要的 StateFlow：
   - HeaderSection：收集 personAName、personBName、personACurrentWeek、personBCurrentWeek
   - CurrentCourseSection：收集 personACurrentCourse、personBCurrentCourse
   - FreeTimeSection：收集 freeTimeSlots
   - TodayScheduleSection：收集 personATodayCourses、personBTodayCourses、displayMode、currentHour、currentMinute、personAPeriodTimes、personBPeriodTimes、personAName、personBName
   - MainScreen 顶层仅保留 singleModeEnabled（用于条件渲染控制）

2. **移除冗余状态**：移除 MainScreen 中的本地 `currentTime` 状态和 `currentHour`/`currentMinute` 变量，改用 ViewModel 暴露的 `currentHour`/`currentMinute` StateFlow，子组件按需收集

3. **时间更新迁移**：将 MainScreen 的 `LaunchedEffect` 时间更新循环迁移到 ViewModel 的 `init` 块，时间更新不再依赖 Compose 组合生命周期

4. **Flow 去重**：为 `personACurrentCourse`、`personBCurrentCourse`、`freeTimeSlots` 三个 Flow 链添加 `.distinctUntilChanged()`，在 `@Immutable` data class 的 `equals()` 配合下，防止数据未变时的重复发射

5. **智能时间调度**：ViewModel 时间更新从固定 60 秒间隔改为智能调度：
   - 上课中：保持 60 秒更新间隔（进度条需要频繁更新）
   - 空闲时：计算到下节课开始的时间，在该时刻更新（最长 30 分钟）
   - 无课程时：保持 60 秒间隔
   - 减少非上课时段的不必要 Flow 发射和重组

### 修改文件

- `ui/main/MainScreen.kt`（拆分状态收集到子组件、移除冗余状态和时间更新 LaunchedEffect）
- `ui/main/MainViewModel.kt`（暴露 currentHour/currentMinute、init 时间更新循环、distinctUntilChanged、智能调度 calculateNextUpdateDelay）
- `app/build.gradle.kts`（versionCode 20002→21000，versionName 1.10.2→1.11.0）
- `Product-Spec.md`（更新功能15描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.10.2] - 2026-05-18

### 变更类型：性能优化

### 状态：已实现

### 优化内容

**derivedStateOf 与 LazyColumn key 优化**

1. **CourseNameSection filteredHistory derivedStateOf**：将 `courseHistory.filter { ... }.take(3)` 用 `remember { derivedStateOf { ... } }` 包裹，避免每次重组都重新计算过滤结果，仅在 `courseHistory` 或 `name` 变化时才重新计算。

2. **TeacherInputRow filteredTeachers derivedStateOf**：将 `teacherHistory.filter { ... }.take(3)` 用 `remember { derivedStateOf { ... } }` 包裹，避免每次重组都重新计算过滤结果，仅在 `teacherHistory` 或 `teacher` 变化时才重新计算。

3. **WeekSelectorDropdown LazyColumn key**：为 `items((1..totalWeeks).toList())` 添加 `key = { week -> week }` 参数，帮助 Compose 识别列表项身份，减少不必要的重组。

4. **FreeTimeSection LazyColumn key**：为 `items(slots)` 添加 `key = { slot -> "${slot.startHour}_${slot.startMinute}_${slot.endHour}_${slot.endMinute}" }` 参数，使用时间字段组合作为唯一标识。

5. **ImportPreviewScreen LazyColumn key**：为 `items(coursesForPersonA)` 和 `items(coursesForPersonB)` 添加 `key = { item -> "${item.data.name}_${item.data.dayOfWeek}_${item.data.startHour}_${item.data.startMinute}" }` 参数，使用课程数据字段组合作为唯一标识。

**缓存 Compose 作用域内的对象**

6. **glassmorphismGradientBrush remember**：将 `Glassmorphism.kt` 中 `glassmorphismGradientBrush` 的 Brush 创建用 `remember(darkTheme)` 包裹，避免每次重组重新创建 Brush 对象。

7. **CourseOverlayCard courseColor/gradientBrush remember**：将 `ScheduleScreen.kt` 中 `CourseOverlayCard` 的 `getCourseColor` 和 `Brush.verticalGradient` 用 `remember` 包裹，以 `course.name`、`darkTheme`、`courseColor`、`alpha` 为 key，避免每次重组重新计算颜色和渐变。

8. **GlassTextField cursorBrush remember**：将 `LiquidGlass.kt` 中 `GlassTextField` 的 `cursorBrush` 用 `remember(darkTheme)` 包裹，避免每次重组重新创建光标渐变 Brush。

9. **HeaderSection DateTimeFormatter remember**：将 `MainScreen.kt` 中 `HeaderSection` 的 `DateTimeFormatter.ofPattern` 和 `today.format()` 用 `remember` 包裹，避免每次重组重新创建格式化器和格式化字符串。

10. **EaseInOutCubic 顶层常量**：将 `ScheduleScreen.kt` 中 Composable 内的 `CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)` 提取为顶层 `private val EaseInOutCubic`，避免每次重组重新创建。

11. **ContainerTransformSpring/MicroTween 顶层常量**：将 `ScheduleScreen.kt` 和 `CourseEditScreen.kt` 中 `animateDpAsState` 的 `spring(dampingRatio = 0.9f, stiffness = 600f)` 提取为 `private val ContainerTransformSpring: SpringSpec<Dp>`，`animateFloatAsState` 的 `tween(AnimationDuration.Micro, easing = FastOutSlowInEasing)` 提取为 `private val MicroTween: TweenSpec<Float>`，避免每次重组重新创建动画规格。

12. **PROGRESSIVE_BLUR_SHADER 顶层常量**：将 `Components.kt` 中 `ScrollTopBlurOverlay` 的 AGSL shader 源码字符串提取为 `private const val PROGRESSIVE_BLUR_SHADER`，避免每次重组重新分配字符串。

13. **glowEffect dp.toPx() 缓存**：将 `Glassmorphism.kt` 中 `glowEffect` 的 `20.dp.toPx()` 从 `drawBehind` 内部移至 Composable 作用域使用 `LocalDensity.current` 计算，避免每帧 draw 调用时重复转换。

**稳定化 Lambda 参数**

14. **MainScreen onDisplayModeChange/onCourseClick remember**：将 `MainScreen.kt` 中 `TodayScheduleSection` 的 `onDisplayModeChange` 和 `onCourseClick` lambda 用 `remember` 包裹，避免每次重组创建新 lambda 实例导致子组件不必要的重组。

15. **ScheduleScreen onCourseClick remember**：将 `ScheduleScreen.kt` 中 `WeeklyScheduleGrid` 的 `onCourseClick` lambda 用 `remember` 包裹，避免每次重组创建新 lambda 实例。

16. **EmptyAction/EmptyBiAction/EmptyTriAction 顶层常量**：将 `ScheduleScreen.kt` 中 prev/next week grids 传入的空 lambda `{ }`、`{ _, _ -> }`、`{ _, _, _ -> }` 提取为顶层 `private val EmptyAction`、`EmptyBiAction`、`EmptyTriAction`，避免每次重组创建新 lambda 实例。

17. **SettingsScreen onCheckedChange/onOptionSelected remember**：将 `SettingsScreen.kt` 中单人模式、预测式返回的 `onCheckedChange` 和主题模式的 `onOptionSelected` lambda 用 `remember` 包裹，避免每次重组创建新 lambda 实例。

### 修改文件

- `CourseEditScreen.kt`（CourseNameSection filteredHistory 添加 derivedStateOf，TeacherInputRow filteredTeachers 添加 derivedStateOf，ContainerTransformSpring/MicroTween 顶层常量）
- `ScheduleScreen.kt`（WeekSelectorDropdown LazyColumn items 添加 key 参数，courseColor/gradientBrush remember，EaseInOutCubic/ContainerTransformSpring/MicroTween/EmptyAction/EmptyBiAction/EmptyTriAction 顶层常量，onCourseClick remember）
- `FreeTimeSection.kt`（AllFreeTimeSlotsSheet LazyColumn items 添加 key 参数）
- `ImportPreviewScreen.kt`（AppExportPreviewContent LazyColumn items 添加 key 参数）
- `Glassmorphism.kt`（glassmorphismGradientBrush remember，glowEffect dp.toPx() 缓存）
- `LiquidGlass.kt`（GlassTextField cursorBrush remember）
- `Components.kt`（PROGRESSIVE_BLUR_SHADER 顶层常量）
- `MainScreen.kt`（HeaderSection DateTimeFormatter remember，onDisplayModeChange/onCourseClick remember）
- `SettingsScreen.kt`（onCheckedChange/onOptionSelected remember）
- `app/build.gradle.kts`（versionCode 20001→20002，versionName 1.10.1→1.10.2）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.10.1] - 2026-05-18

### 变更类型：性能优化

### 状态：已实现

### 优化内容

**毛玻璃效果降级与底栏优化**

1. **ScrollTopBlurOverlay 低端设备降级**：新增 `ActivityManager.isLowRamDevice` 检测，低端设备跳过 AGSL shader + blur 渲染路径，改用简单渐变遮罩（与 API < 33 降级方案一致），减少低端设备 GPU 负载。

2. **LiquidBottomTabs 隐藏按压层优化**：移除隐藏按压层（alpha=0f 的 Row）上独立的 `drawBackdrop` 调用（vibrancy + blur + lens），保留 `layerBackdrop` 供指示器组合背景使用，将 drawBackdrop 层数从 3 层减少到 2 层，降低 GPU 渲染开销。

3. **EmptyScheduleCard 简化**：将 `drawBackdrop + vibrancy + blur + lens` 全套毛玻璃效果替换为 `shadow(4.dp) + background(layer1Tint.copy(alpha = 0.6f))` 轻量方案，移除不必要的 GPU 密集型渲染。

### 修改文件

- `Components.kt`（ScrollTopBlurOverlay 新增 isLowRamDevice 检测与渐变降级）
- `LiquidBottomTabs.kt`（移除隐藏按压层的 drawBackdrop）
- `MainScreen.kt`（EmptyScheduleCard 替换为轻量背景+阴影）
- `app/build.gradle.kts`（versionCode 20000→20001，versionName 1.10.0→1.10.1）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.10.0] - 2026-05-18

### 变更类型：性能优化

### 状态：已实现

### 优化内容

**启动流程优化 + 数据库索引优化**

1. **ComposeWarmup 简化**：移除 NavHost 预热、delay(500) 阻塞调用和大量重量级组件预热（动画、LazyColumn、Card、Brush、Navigation、Schedule、Course、Screen 等），仅保留轻量级 Text/Column/Row/Box 预热。使用 Choreographer.postFrameCallback 在下一帧后自动释放组合，避免不必要的延迟阻塞启动。

2. **DuoScheduleApp 预加载去重**：移除 onCreate 中第二个 applicationScope.launch 块里重复的 `database.courseDao().getAllCourses()` 和 `preloadSettings()` 调用（preloadViewModels 已包含这些操作），减少启动时重复数据加载。

3. **PerformanceMonitor 线程安全**：将 `startupMetrics` 从 `mutableMapOf` 改为 `ConcurrentHashMap`，修复多线程并发读写潜在的线程安全问题。

4. **Course 数据库复合索引**：新增 `Index(value = ["dayOfWeek", "personType", "startHour", "startMinute"])` 复合索引，优化按星期+人员+时间查询课程的性能（课表网格渲染、当前课程检测等高频查询场景）。

5. **数据库迁移**：版本 6→7，新增 MIGRATION_6_7（空迁移，索引由 Room 自动创建）。

### 修改文件

- `ComposeWarmup.kt`（简化预热内容，移除 delay，使用 Choreographer 释放）
- `DuoScheduleApp.kt`（移除重复的 database preload 和 preloadSettings 调用）
- `PerformanceMonitor.kt`（startupMetrics 改用 ConcurrentHashMap）
- `Course.kt`（新增复合索引）
- `AppDatabase.kt`（版本 6→7，新增 MIGRATION_6_7）
- `DatabaseModule.kt`（注册 MIGRATION_6_7）
- `app/build.gradle.kts`（versionCode 19008→20000，versionName 1.9.8→1.10.0）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.10.0] - 2026-05-18

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**Compose 稳定性优化：为数据类添加 @Immutable/@Stable 注解并移除 Course 可变缓存**

- 为以下数据类添加 `@Immutable` 注解，帮助 Compose 编译器识别类型稳定性，跳过不必要的重组：
  - `CurrentCourseState`（ui/model/CurrentCourseState.kt）
  - `FreeTimeSlot`（ui/model/FreeTimeSlot.kt）
  - `CourseSlotInfo`（ui/schedule/ScheduleScreen.kt）
  - `CourseLayoutInfo`（ui/schedule/ScheduleScreen.kt）
  - `EmptySlotPosition`（ui/schedule/ScheduleScreen.kt）
  - `EditTarget`（ui/schedule/ScheduleScreen.kt）
  - `CellBounds`（ui/schedule/CourseContextMenu.kt）
  - `Course`（data/model/Course.kt）
- 为 `ContextMenuItem` 添加 `@Stable` 注解（包含 lambda 参数，不适合 @Immutable）
- 将 `Course` 的可变缓存 `cachedCustomWeeks` 移至外部 `CourseWeekCache` 对象，使 `Course` 数据类真正不可变，符合 `@Immutable` 契约
- `CourseWeekCache` 提供 get/put/clear 方法，通过 Course.id 作为键管理缓存

### 修改文件

- `ui/model/CurrentCourseState.kt`（添加 @Immutable 注解）
- `ui/model/FreeTimeSlot.kt`（添加 @Immutable 注解）
- `ui/schedule/ScheduleScreen.kt`（为 EditTarget、CourseSlotInfo、CourseLayoutInfo、EmptySlotPosition 添加 @Immutable 注解）
- `ui/schedule/CourseContextMenu.kt`（为 ContextMenuItem 添加 @Stable 注解，为 CellBounds 添加 @Immutable 注解）
- `data/model/Course.kt`（添加 @Immutable 注解，移除 cachedCustomWeeks 字段，新增 CourseWeekCache 对象，更新 isInWeek 方法使用外部缓存）
- `app/build.gradle.kts`（versionCode 19008→20000，versionName 1.9.8→1.10.0）
- `Product-Spec.md`（更新功能15描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.8] - 2026-05-18

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

**修复设置-课表设置-当前周次无法更改**

- 问题：用户在课表设置中手动修改当前周次后，值立即被自动计算值覆盖，导致无法手动更改
- 根因：系统中存在3处自动覆盖逻辑，当存储的当前周次与根据开学时间计算的周次不一致时，会强制用计算值覆盖用户的手动值：
  1. MainViewModel 的 combine 逻辑（personACurrentWeek/personBCurrentWeek StateFlow）
  2. ScheduleViewModel 的 combine 逻辑（personACurrentWeek/personBCurrentWeek StateFlow）
  3. DuoScheduleApp.updateCurrentWeekIfNeeded()（应用启动/回到前台时）
- 修复：在 DataStore 中新增"手动覆盖标记"（PERSON_A/B_MANUAL_WEEK_OVERRIDE），区分用户手动设置和自动计算的当前周次：
  - 用户手动设置当前周次时，标记为 true，自动计算逻辑不再覆盖
  - 用户修改开学时间或总周数时，标记重置为 false，恢复自动计算
  - MainViewModel、ScheduleViewModel 的 combine 逻辑在覆盖前检查标记
  - DuoScheduleApp.updateCurrentWeekIfNeeded() 在覆盖前检查标记

### 修改文件

- `SettingsDataStore.kt`（新增 PERSON_A/B_MANUAL_WEEK_OVERRIDE 偏好键 + getManualWeekOverride/setManualWeekOverride 方法）
- `CourseRepository.kt`（新增 getManualWeekOverride/setManualWeekOverride 透传方法）
- `SettingsViewModel.kt`（setPersonCurrentWeek 设置手动覆盖为 true，setPersonSemesterStart/setPersonTotalWeeks 重置手动覆盖为 false）
- `MainViewModel.kt`（combine 逻辑检查手动覆盖标记，有覆盖时使用存储值而非计算值）
- `ScheduleViewModel.kt`（combine 逻辑检查手动覆盖标记，有覆盖时使用存储值而非计算值）
- `DuoScheduleApp.kt`（updateCurrentWeekIfNeeded 检查手动覆盖标记，有覆盖时跳过自动更新）
- `app/build.gradle.kts`（versionCode 19007→19008，versionName 1.9.7→1.9.8）
- `Product-Spec.md`（更新功能12描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.7] - 2026-05-18

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

**修复共享元素转场圆角动画过冲导致崩溃**

- 问题：共享元素转场动画中，`animateDpAsState` 配合 `spring(dampingRatio=0.9f)` 欠阻尼弹簧，在圆角值从大向小动画时产生过冲（overshoot），导致 `cornerRadius` 瞬间变为负值，传给 `ContinuousRoundedRectangle` 时抛出 `IllegalArgumentException: Corner size in Px can't be negative`
- 修复：在 `CourseEditScreen.kt` 和 `ScheduleScreen.kt` 中，将动画后的 `cornerRadius` 传入 `ContinuousRoundedRectangle` 之前添加 `.coerceAtLeast(0.dp)` 保护，确保圆角值不会为负

### 修改文件

- `ui/edit/CourseEditScreen.kt`（圆角值添加 coerceAtLeast(0.dp) 保护）
- `ui/schedule/ScheduleScreen.kt`（圆角值添加 coerceAtLeast(0.dp) 保护）
- `app/build.gradle.kts`（versionCode 19006→19007，versionName 1.9.6→1.9.7）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.6] - 2026-05-18

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**一镜到底动画优雅化优化**

- 新增 `AppleSpring.ContainerTransform` 弹簧预设（dampingRatio=0.9, stiffness=600），更果断少弹跳的容器变换曲线
- 重写 `AnimatedContent` 的 `transitionSpec`，从简单 `fadeIn/fadeOut` 改为交错时序：源端内容快速淡出（150ms），目标端内容延迟淡入（350ms + 150ms 延迟）
- 为 `sharedElement` 添加 `boundsTransform` 参数，使用 ContainerTransform 弹簧曲线控制容器变形动画
- 新增圆角动画：课程卡片退出时圆角从 medium 平滑过渡到 xxlarge，编辑页进入时圆角从 xxlarge 平滑过渡到 none，实现形状变形效果
- 利用 `sharedElement` 默认的 `renderInOverlayDuringTransition=true`，共享元素在转场时渲染在叠加层，不受父容器裁剪

### 修改文件

- `ui/theme/AppleAnimationKit.kt`（新增 ContainerTransform 弹簧预设）
- `ui/schedule/ScheduleScreen.kt`（重写 transitionSpec、添加 boundsTransform、添加圆角动画）
- `ui/edit/CourseEditScreen.kt`（添加 boundsTransform、添加圆角动画和 clip）
- `app/build.gradle.kts`（versionCode 19005→19006，versionName 1.9.5→1.9.6）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.5] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

**修复预测式返回开关逻辑反转及启动时状态未生效**

- 问题1：开启预测式返回时反而没有预测式返回效果，关闭时反而有（逻辑反转）
- 问题2：首次打开 app 时，预测式返回开关是关着的，但实际仍有预测式返回效果，只有手动重新开关后才生效
- 根因1：`updatePredictiveBack()` 中 `if (enabled)` 注册自定义回调覆盖了系统预测式返回，改为 `if (!enabled)` 注册回调禁用预测式返回，开启时不注册回调让系统自然处理
- 根因2：三层默认值均为 `true` 导致启动时序问题——DataStore 默认值 `preferences[PREDICTIVE_BACK_ENABLED] ?: true`、StateFlow 初始值 `stateIn(..., true)`、`repeatOnLifecycle` 首次收集发射初始值 `true` → `updatePredictiveBack(true)` → 不注册回调 → 系统默认有预测式返回。DataStore 异步加载真实值 `false` 后才触发更新，但此时用户已可见预测式返回效果
- 修复1：DataStore 默认值从 `true` 改为 `false`（实验性功能默认关闭）
- 修复2：StateFlow 初始值从 `true` 改为 `false`
- 修复3：`onCreate` 中 `super.onCreate()` 之后立即调用 `updatePredictiveBack(false)`，在 DataStore 加载之前就禁用预测式返回
- 修复4：将预测式返回状态管理从 Compose `LaunchedEffect` 移到 Activity 生命周期级别，使用 `lifecycleScope.launch` + `repeatOnLifecycle(STARTED)` 收集 Flow

### 修改文件

- `SettingsDataStore.kt`（默认值 `true` → `false`）
- `SettingsViewModel.kt`（StateFlow 初始值 `true` → `false`）
- `MainActivity.kt`（修复开关逻辑 + onCreate 立即禁用 + repeatOnLifecycle 收集）
- `app/build.gradle.kts`（versionCode 19004→19005，versionName 1.9.4→1.9.5）
- `Product-Spec.md`（更新功能14描述）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.4] - 2026-05-18

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

**修复今日空闲时间颜色使用了 Ta 的颜色而非我的颜色**

- 问题：空闲时间卡片、详情弹窗、全部时段弹窗使用了 `getPersonBColor()`（橙色，Ta 的颜色），应使用 `getPersonAColor()`（蓝色，我的颜色）
- 根因：1.4.0 版本 PersonType 语义修正时遗漏了 FreeTimeSection.kt 中的 3 处颜色引用
- 修复：FreeTimeCard、FreeTimeDetailSheet、AllFreeTimeSlotsSheet 中的 `getPersonBColor()` 全部改为 `getPersonAColor()`

### 修改文件

- `FreeTimeSection.kt`（3 处 `getPersonBColor()` → `getPersonAColor()`）
- `app/build.gradle.kts`（versionCode 19003→19004，versionName 1.9.3→1.9.4）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.3] - 2026-05-18

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**今日课程卡片右下角时间显示优化**

- 课程卡片右下角时间从只显示结束时间（如"09:30"）改为显示起始和结束时间（如"08:00-09:30"），无论是节数显示的课程还是自定义时间的课程，统一显示完整时间范围
- 自定义时间课程的 periodText 不再重复显示时间（原 periodText 返回 `course.getTimeString()`，现改为返回空字符串，因为右下角已显示完整时间范围，避免信息重复）

### 修改文件

- `TodayScheduleTimeline.kt`（`getEndTimeString()` → `getTimeString()`，自定义时间课程 `getPeriodText` 返回空字符串）
- `app/build.gradle.kts`（versionCode 19002→19003，versionName 1.9.2→1.9.3）
- `Product-Spec.md`（更新功能3描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.2] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

**修复预测式返回开关逻辑反转及启动时状态未生效**

- 问题1：开启预测式返回时反而没有预测式返回效果，关闭时反而有（逻辑反转）
- 问题2：首次打开 app 时，预测式返回开关是关着的，但实际仍有预测式返回效果，只有手动重新开关后才生效
- 根因1：`updatePredictiveBack()` 中 `if (enabled)` 注册自定义回调覆盖了系统预测式返回，改为 `if (!enabled)` 注册回调禁用预测式返回，开启时不注册回调让系统自然处理
- 根因2：原实现使用 Compose `LaunchedEffect` 驱动 `updatePredictiveBack`，但 `stateIn` 初始值为 `true`，导致首次组合时以 `true` 调用（不注册回调），DataStore 异步加载真实值 `false` 后 `LaunchedEffect` 可能因时序问题未重新触发
- 修复：将预测式返回状态管理从 Compose `LaunchedEffect` 移到 Activity 生命周期级别，使用 `lifecycleScope.launch` + `repeatOnLifecycle(STARTED)` 直接收集 ViewModel Flow，确保 `onStart` 时即能拿到真实值并正确应用

### 修改文件

- `MainActivity.kt`（修复预测式返回开关逻辑 + 将状态管理移至 Activity 生命周期级别）
- `app/build.gradle.kts`（versionCode 19001→19002，versionName 1.9.1→1.9.2）
- `Product-Spec.md`（更新功能14描述）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.1] - 2026-05-18

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**修复底栏选中指示器垂直偏移和常态下底部黑色残留**

- 选中指示器（胶囊高亮）在底栏中偏上约4dp，且常态下底部出现黑色残留
- 根因：与 AndroidLiquidGlass 官方教程对比发现3处差异导致问题
- 修复1：`BoxWithConstraints` 的 `contentAlignment` 从 `TopStart` 改为 `CenterStart`，56dp指示器在64dp空间内自然居中（上下各4dp）
- 修复2：将 `.navigationBarsPadding()` 从3个子元素移到 `BoxWithConstraints` 容器上，避免子元素各自添加导航栏内边距导致布局错位
- 修复3：移除 `BoxWithConstraints` 的固定高度（`64dp + navBarHeight`），改为自适应内容高度
- 修复4：指示器Box的padding从 `.padding(start=4.dp, end=4.dp, top=4.dp)` 恢复为 `.padding(horizontal = 4f.dp)`，由CenterStart对齐自动处理垂直居中

### 修改文件

- `LiquidBottomTabs.kt`（重构布局对齐方式，参照官方教程）
- `app/build.gradle.kts`（versionCode 19000→19001，versionName 1.9.0→1.9.1）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.9.0] - 2026-05-18

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**顶部模糊效果对齐底栏毛玻璃风格**

- `drawPlainBackdrop` 改为 `drawBackdrop`，与底栏使用相同的 backdrop API
- 新增 `vibrancy()` 效果，增加模糊区域饱和度，让色彩更丰富
- 显式禁用 highlight 和 shadow（`highlight = { null }`，`shadow = { null }`），避免默认高光和阴影
- 新增 `onDrawSurface` 绘制半透明底色（`backgroundColor.copy(alpha = 0.5f)`），让模糊区域有毛玻璃质感
- blurHeight 默认值 96dp → 120dp，增大模糊区域
- blurRadius 默认值 40dp → 25dp，降低模糊半径
- 保留 AnimatedVisibility + fadeIn/fadeOut 包裹和 scrollOffset 参数控制
- 保留 API < 33 的降级方案（渐变遮罩）

### 修改文件

- `ui/theme/Components.kt`（ScrollTopBlurOverlay 改用 drawBackdrop + vibrancy + onDrawSurface）
- `app/build.gradle.kts`（versionCode 18000→19000，versionName 1.8.0→1.9.0）
- `Product-Spec.md`（更新功能 21 描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.8.0] - 2026-05-18

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**顶部渐进模糊效果迭代**

- 增大模糊区域：blurHeight 64dp → 96dp，blurRadius 20dp → 40dp
- 使用 backdrop 2.0.0-alpha03 的 `runtimeShaderEffect`（AGSL 着色器）实现渐进模糊，从顶部完全不透明渐变到底部完全透明
- 仅在上滑时（scrollOffset > 0）显示模糊效果，滚动回顶部时淡出隐藏，带淡入/淡出动画
- 移除课表页（ScheduleScreen）的模糊效果
- 主页（MainScreen）保留模糊效果

### 修改文件

- `ui/theme/Components.kt`（ScrollTopBlurOverlay 增加 scrollOffset 参数和 AnimatedVisibility 动画）
- `ui/schedule/ScheduleScreen.kt`（移除模糊相关代码）
- `ui/main/MainScreen.kt`（传入滚动偏移量）
- `ui/settings/SettingsScreen.kt`（传入滚动偏移量）
- `ui/settings/DisplaySettingsScreen.kt`（传入滚动偏移量）
- `ui/settings/NotificationSettingsScreen.kt`（传入滚动偏移量）
- `ui/settings/ScheduleSettingsScreen.kt`（传入滚动偏移量）
- `ui/settings/PeriodTimesSettingsScreen.kt`（传入滚动偏移量）
- `ui/settings/DataManagementScreen.kt`（传入滚动偏移量）
- `ui/edit/CourseEditScreen.kt`（传入滚动偏移量）
- `app/build.gradle.kts`（backdrop 1.0.6 → 2.0.0-alpha03，versionCode 17005→18000，versionName 1.7.5→1.8.0）
- `Product-Spec.md`（更新功能 21 描述）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.7.5] - 2026-05-18

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**修复底栏选中指示器垂直偏移问题**

- 选中指示器（胶囊高亮）在底栏中偏上约4dp，未与背景栏内容区域（图标+文字）垂直对齐
- 原因：背景栏高度64dp内含4dp全方向padding，内容区域从顶部4dp处开始；但选中指示器Box高度56dp只有水平padding，在TopStart对齐下从容器最顶部开始，导致偏上4dp
- 修复：将指示器Box的`.padding(horizontal = 4f.dp)`改为`.padding(horizontal = 4f.dp, top = 4.dp)`

### 修改文件

- `LiquidBottomTabs.kt`（选中指示器添加top padding）
- `app/build.gradle.kts`（versionCode 17004→17005，versionName 1.7.4→1.7.5）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.7.4] - 2026-05-18

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**为 8 个页面的 Scaffold 添加 edge-to-edge 支持，使内容延伸到导航栏后面**

- 所有 Scaffold 添加 `contentWindowInsets = WindowInsets(0)` 参数
- 将 `.padding(paddingValues)` 改为 `.padding(top = paddingValues.calculateTopPadding())`，仅保留顶部内边距
- 在 Column 内容底部添加 `Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))` 处理导航栏高度
- 对于使用 LazyColumn 的页面（CourseEditScreen），通过 contentPadding 的 bottom 添加导航栏高度

### 修改文件

- `DisplaySettingsScreen.kt`（Scaffold edge-to-edge 支持）
- `ScheduleSettingsScreen.kt`（Scaffold edge-to-edge 支持）
- `DataManagementScreen.kt`（Scaffold edge-to-edge 支持）
- `NotificationSettingsScreen.kt`（Scaffold edge-to-edge 支持）
- `PeriodTimesSettingsScreen.kt`（Scaffold edge-to-edge 支持）
- `CourseEditScreen.kt`（CourseEditScreen + CourseEditContent 两处 Scaffold edge-to-edge 支持）
- `ImportPreviewScreen.kt`（Scaffold edge-to-edge 支持）
- `UpdateScreen.kt`（Scaffold edge-to-edge 支持）
- `app/build.gradle.kts`（versionCode 17003→17004，versionName 1.7.3→1.7.4）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.7.3] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**为所有使用 ScrollTopBlurOverlay 的页面添加 layerBackdrop 支持**

- `ScrollTopBlurOverlay` 现在需要 `backdrop: Backdrop` 参数才能正确渲染模糊效果
- 为 9 个页面的可滚动内容添加 `layerBackdrop` 修饰符和 `rememberLayerBackdrop()` 状态
- 每个页面在 Box 容器前创建 `val scrollBackdrop = rememberLayerBackdrop()`
- 可滚动 Column/LazyColumn 添加 `.layerBackdrop(scrollBackdrop)` 修饰符
- `ScrollTopBlurOverlay()` 改为 `ScrollTopBlurOverlay(backdrop = scrollBackdrop)`

### 修改文件

- `MainScreen.kt`（添加 layerBackdrop 支持）
- `ScheduleScreen.kt`（WeeklyScheduleGrid 添加 layerBackdrop 支持）
- `SettingsScreen.kt`（添加 layerBackdrop 支持）
- `DisplaySettingsScreen.kt`（添加 layerBackdrop 支持）
- `NotificationSettingsScreen.kt`（添加 layerBackdrop 支持）
- `ScheduleSettingsScreen.kt`（添加 layerBackdrop 支持）
- `PeriodTimesSettingsScreen.kt`（添加 layerBackdrop 支持）
- `DataManagementScreen.kt`（添加 layerBackdrop 支持）
- `CourseEditScreen.kt`（CourseEditScreen + CourseEditContent 两处添加 layerBackdrop 支持）
- `app/build.gradle.kts`（versionCode 17002→17003，versionName 1.7.2→1.7.3）
- `Product-Spec.md`（更新版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

---

## [1.7.2] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**修复 API 35+ 设备导航栏不沉浸问题（enableEdgeToEdge 调用顺序）**

- `enableEdgeToEdge()` 从 `installSplashScreen()` + `super.onCreate()` 之后移到之前调用
- 原因：API 35+ 强制 edge-to-edge，`installSplashScreen()` 的 `postSplashScreenTheme` 会触发主题切换，若 `enableEdgeToEdge()` 尚未调用，导航栏恢复不透明
- `isNavigationBarContrastEnforced = false` 同步移到 `installSplashScreen()` 之前

### 修改文件

- `MainActivity.kt`（调整 `enableEdgeToEdge()` + `isNavigationBarContrastEnforced` 调用顺序）
- `app/build.gradle.kts`（versionCode 17001→17002，versionName 1.7.1→1.7.2）
- `Product-Spec.md`（更新功能19描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.7.1] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**修复小白条不透明白色遮挡问题**

- 修复 `values-v29/themes.xml`：补全与基础日间主题相同的全部属性（`colorPrimary`、`colorPrimaryDark`、`colorAccent`、`windowBackground`、`datePickerStyle`、`timePickerStyle`），以及 `DatePickerStyle`、`CalendarViewStyle`、`TimePickerStyle`、`Theme.DuoSchedule.Splash` 样式
- 修复 `values-night-v29/themes.xml`：补全与基础夜间主题相同的全部属性（`colorPrimary`、`colorPrimaryDark`、`colorAccent`、`windowBackground`、`datePickerStyle`、`timePickerStyle`），以及 `DatePickerStyle`、`CalendarViewStyle`、`TimePickerStyle`、`Theme.DuoSchedule.Splash` 样式
- `windowBackground` 缺失导致 API 29+ 设备回退为父主题默认白色，导航栏区域显示白色实体条 — 修复后导航栏区域底色与页面背景一致

### 修改文件

- `values-v29/themes.xml`（补全缺失属性 + 样式）
- `values-night-v29/themes.xml`（补全缺失属性 + 样式）
- `app/build.gradle.kts`（versionCode 17000→17001，versionName 1.7.0→1.7.1）
- `Product-Spec.md`（更新功能19描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.7.0] - 2026-05-17

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**移除 miuix-blur 依赖，简化顶部模糊效果实现**

- 移除 `miuix-blur-android:0.9.1` 依赖，减少外部库依赖
- 移除 `AndroidManifest.xml` 中 `tools:overrideLibrary="top.yukonga.miuix.kmp.blur"` 声明
- `ScrollTopBlurOverlay` 组件从 miuix-blur 的 `textureBlur` + `LayerBackdrop` 实现改为纯渐变遮罩实现，不再依赖 miuix-blur 的实时高斯模糊
- 所有 9 个页面移除 `rememberLayerBackdrop()` 变量和 `.layerBackdrop(scrollBackdrop)` 修饰符
- 所有 9 个页面 `ScrollTopBlurOverlay(backdrop = scrollBackdrop)` 改为 `ScrollTopBlurOverlay()`

### 修改文件

- `build.gradle.kts`（移除 miuix-blur 依赖 + 版本号 1.6.2→1.7.0，versionCode 16002→17000）
- `AndroidManifest.xml`（移除 uses-sdk overrideLibrary）
- `ui/theme/Components.kt`（ScrollTopBlurOverlay 移除 backdrop 参数和 textureBlur，改为渐变遮罩）
- `ui/main/MainScreen.kt`（移除 layerBackdrop）
- `ui/schedule/ScheduleScreen.kt`（移除 layerBackdrop）
- `ui/settings/SettingsScreen.kt`（移除 layerBackdrop）
- `ui/settings/DisplaySettingsScreen.kt`（移除 layerBackdrop）
- `ui/settings/NotificationSettingsScreen.kt`（移除 layerBackdrop）
- `ui/settings/ScheduleSettingsScreen.kt`（移除 layerBackdrop）
- `ui/settings/PeriodTimesSettingsScreen.kt`（移除 layerBackdrop）
- `ui/settings/DataManagementScreen.kt`（移除 layerBackdrop）
- `ui/edit/CourseEditScreen.kt`（移除 layerBackdrop）
- `Product-Spec.md`（更新功能 21 描述 + 版本号）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

---

## [1.6.2] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

**修复沉浸式小白条：导航栏区域不透明，看不到后面内容**

- 根因：Android 资源合并规则中，`values-v29/themes.xml` 完全替换 `values/themes.xml` 的同名 style，而非合并。v29 版本缺少 `android:navigationBarColor` 和 `android:statusBarColor` 透明设置，导致 Android 10+ 设备上导航栏回退到父主题默认值（不透明），小白条后面看不到应用内容
- 修复 `values-v29/themes.xml`：补充 `android:statusBarColor=transparent`、`android:navigationBarColor=transparent`、`android:windowLightStatusBar=true`
- 修复 `values-night-v29/themes.xml`：补充 `android:statusBarColor=transparent`、`android:navigationBarColor=transparent`、`android:windowLightStatusBar=false`
- 修复 `PeriodTimesSettingsScreen` 的 `BottomAppBar` 缺少 `navigationBarsPadding()`，手势导航设备上保存按钮可能被系统导航栏遮挡

### 修改文件

- `values-v29/themes.xml`（补充透明状态栏/导航栏颜色 + windowLightStatusBar）
- `values-night-v29/themes.xml`（补充透明状态栏/导航栏颜色 + windowLightStatusBar）
- `PeriodTimesSettingsScreen.kt`（BottomAppBar 添加 navigationBarsPadding）

---

## [1.6.1] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 变更内容

**修复沉浸式系统栏适配：底栏下方内容被遮挡**

- 修复 MainActivity 内容层缺少 `navigationBarsPadding()` 导致内容延伸到底栏和系统导航栏之间区域的问题
- 从 MainScreen 和 ScheduleScreen 移除冗余的 `navigationBarsPadding()`（由 MainActivity 统一处理）
- 修复 compileSdk 升级到 37 以兼容 miuix-blur-android:0.9.1
- 添加 `tools:overrideLibrary` 解决 miuix-blur minSdk 冲突

---

## [1.6.1] - 2026-05-17

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**功能 21：页面顶部高斯模糊效果**

所有可滚动页面顶部添加实时高斯模糊效果，使用 backdrop 库实现真正的毛玻璃效果。

- 新增可复用 `ScrollTopBlurOverlay` 组件（`ui/theme/Components.kt`），默认模糊高度 64dp，模糊半径 40dp
- 通过 `layerBackdrop` 捕获滚动内容，在顶部区域应用 `drawBackdrop` + `blur()` 实时高斯模糊
- 叠加半透明渐变遮罩让模糊边缘自然过渡到透明
- 自动适配深色/浅色模式（使用 `MaterialTheme.colorScheme.background`）
- 触摸事件穿透（`pointerInput(Unit) {}` 空实现），不影响内容交互
- 主页（MainScreen）应用顶部高斯模糊
- 课表页（ScheduleScreen/WeeklyScheduleGrid）应用顶部高斯模糊
- 设置主页（SettingsScreen）应用顶部高斯模糊
- 5 个设置子页面（DisplaySettingsScreen、NotificationSettingsScreen、ScheduleSettingsScreen、PeriodTimesSettingsScreen、DataManagementScreen）应用顶部高斯模糊
- 课程编辑页（CourseEditScreen）应用顶部高斯模糊

### 修改文件

- `ui/theme/Components.kt`（新增 ScrollTopBlurOverlay 组件，保留 ScrollTopGradientOverlay）
- `ui/main/MainScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/schedule/ScheduleScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/settings/SettingsScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/settings/DisplaySettingsScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/settings/NotificationSettingsScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/settings/ScheduleSettingsScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/settings/PeriodTimesSettingsScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/settings/DataManagementScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `ui/edit/CourseEditScreen.kt`（layerBackdrop + ScrollTopBlurOverlay）
- `Product-Spec.md`（更新功能 21 描述）
- `Product-Spec-CHANGELOG.md`（更新变更记录）

---

## [1.6.0] - 2026-05-17

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**沉浸式系统栏厂商适配（小米 HyperOS + OPPO ColorOS）**

根据小米 HyperOS 官方适配教程（全面屏手势提示线适配）和 OPPO ColorOS 官方适配教程（沉浸式状态栏适配），完善沉浸式系统栏适配。

- `enableEdgeToEdge()` 改用完全透明参数：`SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)`，状态栏和导航栏均无 scrim，确保小米设备手势提示线（小白条）背景完全透明
- 移除 `Theme.kt` 中冗余的 `SideEffect` 系统栏设置（`window.statusBarColor`、`window.navigationBarColor`、`insetsController.isAppearanceLightStatusBars`、`insetsController.isAppearanceLightNavigationBars`），与 `enableEdgeToEdge()` 职责重复
- MainActivity 统一为底部导航页面内容区域添加 `padding(bottom = LiquidBottomTabsSpec.Height)`（64dp），解决各页面底部内容被底部导航栏遮挡的问题
- MainScreen 底部 padding 从硬编码 `80.dp` 改为 `navigationBarsPadding()`，动态适配不同设备的导航栏高度
- ScheduleScreen 添加 `navigationBarsPadding()`，修复课表网格底部内容被遮挡；移除内层 Row 重复的 `statusBarsPadding()`
- SettingsScreen 底部 Spacer 从 `weight(1f)`（在 verticalScroll 中无效）改为固定高度 `Spacing.xl`
- Android 9+ 设备隐藏导航栏分割线：`navigationBarDividerColor` 设为透明（values-v29/themes.xml、values-night-v29/themes.xml）

### 修改文件

- `MainActivity.kt`（enableEdgeToEdge 透明参数 + 内容区域底部偏移）
- `Theme.kt`（移除冗余 SideEffect + 清理无用 import）
- `MainScreen.kt`（硬编码 80dp → navigationBarsPadding）
- `ScheduleScreen.kt`（添加 navigationBarsPadding + 移除重复 statusBarsPadding）
- `SettingsScreen.kt`（底部 Spacer 修复）
- `values-v29/themes.xml`（添加 navigationBarDividerColor=transparent）
- `values-night-v29/themes.xml`（添加 navigationBarDividerColor=transparent）
- `Product-Spec.md`（功能19描述更新 + 兼容性要求更新 + 版本号 1.5.0→1.6.0）
- `build.gradle.kts`（版本号 1.5.0→1.6.0，versionCode 15000→16000）

---

## [1.5.0] - 2026-05-17

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**功能 20：自定义时间事件**

支持添加时间不固定的事件（如考试、讲座、社团活动），不受课节时间约束。

- Course 数据模型新增 `isCustomTime: Boolean = false` 字段
- 课程编辑界面新增时间模式切换：按课节（默认）/ 自定义时间
- 自定义时间模式下使用时间选择器直接设置开始和结束时间，不再依赖课节
- 课表网格根据实际时间定位自定义时间事件的纵向位置和高度，支持跨越课节边界
- 主页今日课程列表、当前课程状态检测兼容自定义时间事件
- 通知系统（课前/上课中通知）兼容自定义时间课程
- 共同空闲时间计算考虑自定义时间事件
- CSV 导入导出兼容 isCustomTime 字段（导出版本 3.0→4.0，向后兼容旧版 CSV）
- 桌面小组件数据兼容自定义时间事件
- 数据库迁移：版本 5→6，新增 isCustomTime 列（默认 false）

**Bug 修复：课表无法显示自定义时间课程**

- 修复 `getPeriodFromTimeFast` 使用 `until`（不包含上限）导致结束时间正好等于课节边界时返回默认值1的问题，改为查找最后一个 startMinutes <= 给定时间的课节
- 修复 `buildCourseSlotMap` 中 `endPeriod < startPeriod` 时范围为空导致课程不被添加到 slot map 的问题，增加 `coerceAtLeast` 保护
- 修复 `onCourseLongPress` 中自定义时间课程 `startPeriod=0` 导致上下文菜单定位错误的问题

**Bug 修复：小组件自定义时间课程显示"第0节"**

- MyCoursesWidgetReceiver、TaCoursesWidgetReceiver、ScheduleWidgetReceiverMIUI 中自定义时间课程使用 `course.getTimeString()` 显示实际时间

### 修改文件

- `Course.kt`（新增 isCustomTime 字段）
- `AppDatabase.kt`（版本 5→6 + MIGRATION_5_6）
- `DatabaseModule.kt`（注册 MIGRATION_5_6）
- `CourseEditScreen.kt`（时间模式切换 UI + CustomTimePickerBottomSheet）
- `CourseEditViewModel.kt`（自定义时间逻辑 + CourseEditState 扩展 + setTimeMode + setCustomTime + saveCourse 自定义时间分支）
- `CustomTimePickerBottomSheet.kt`（新建：自定义时间选择器）
- `ScheduleScreen.kt`（自定义时间事件渲染：分数定位、calculateCustomTimePosition、CourseOverlayCard 浮点参数、时间字符串显示、parsedPeriodTimes 传递、getPeriodFromTimeFast 修复、buildCourseSlotMap 修复、onCourseLongPress 修复）
- `MainViewModel.kt`（getPeriodText 自定义时间显示）
- `TodayScheduleTimeline.kt`（getPeriodText 自定义时间显示）
- `CsvExporter.kt`（导出兼容 isCustomTime 列 + 导入解析兼容）
- `ImportExportModels.kt`（CourseImportData 新增 isCustomTime 字段）
- `MyCoursesWidgetReceiver.kt`（periodText isCustomTime 判断）
- `TaCoursesWidgetReceiver.kt`（periodText isCustomTime 判断）
- `ScheduleWidgetReceiverMIUI.kt`（periodText isCustomTime 判断）
- `build.gradle.kts`（版本号 1.4.0→1.5.0，versionCode 14000→15000）

---

## [1.4.1] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

- 预测式返回开关逻辑反转：开启预测式返回时反而没有预测式返回效果，关闭时反而有。原因是 `updatePredictiveBack()` 方法中 `if (enabled)` 注册了自定义回调覆盖了系统预测式返回，改为 `if (!enabled)` 注册自定义回调禁用预测式返回，开启时不注册回调让系统自然处理。

### 修改文件

- `MainActivity.kt`（修复预测式返回开关逻辑）

---

## [1.4.0] - 2026-05-17

### 变更类型：功能增强与Bug修复

### 状态：已实现

### 变更内容

**第一阶段：PersonType 语义修正**

将 PERSON_A 定义为"我"（当前用户），PERSON_B 定义为"Ta"（对方用户），修正原有语义混乱。

- SettingsDataStore: personAName 默认值从"Ta"改为"我"，personBName 默认值从"我"改为"Ta"
- 新增 DataStore 数据迁移：`migratePersonTypeIfNeeded()` 方法，交换已存储的 A/B 名称、开学日期、总周数、当前周次、总节数、节次时间，通过 `PERSON_TYPE_MIGRATION_DONE` 标记确保幂等性
- Room 数据库版本从 4 升级到 5，新增 `MIGRATION_4_5` 迁移，使用临时值 `PERSON_TEMP` 交换课程表中 personType 字段
- DesignTokens: PersonA 颜色从橙色(#FFB74D)改为蓝色(#4789FE)，PersonB 颜色从蓝色(#4789FE)改为橙色(#FFB74D)，Light/Dark 变体同步交换
- colors.xml: person_a_color/person_b_color 交换（浅色+深色）
- strings.xml: person_a/person_b 标签交换
- ViewModel 层：所有 personAName/personBName 默认值互换，导入默认目标改为 PERSON_A，CourseEditState 默认 personType 改为 PERSON_A
- UI 层：所有界面中 "我"/"我的" 对应 PERSON_A，"Ta"/"Ta的" 对应 PERSON_B，包括设置页、课表编辑页、导入预览页、主页、底部导航、今日课程时间轴、当前课程卡片等
- 小组件：MyCoursesWidget 改为 PERSON_A + person_a_color，TaCoursesWidget 改为 PERSON_B + person_b_color，Widget XML 默认颜色从 person_b_color 改为 person_a_color
- 通知系统：所有 PersonType.PERSON_B 改为 PersonType.PERSON_A
- 导入导出：CsvExporter 标记互换，导出版本升至 3.0，添加旧版兼容解析逻辑（旧版 section/名称映射交换，parsePersonType 添加 exportVersion 参数）
- 模型注释：PersonType.kt 和 TodayCourseDisplayMode.kt 添加语义注释

**第二阶段：通知可靠性修复**

修复通知和自动静音功能的多个可靠性问题。

- 提醒通知使用基于 courseId 的独立通知 ID（REMINDER_NOTIFICATION_ID_BASE + courseId），避免多课程提醒互相覆盖
- OngoingCourseReceiver 根据当前时间重新计算剩余分钟数，课程已结束时不启动 LiveUpdateService
- 通知文本全部中文化，移除英文混杂（"Course Service"→"课程服务"，"Xm"→"X分钟"，"ended"→"已结束"）
- 课程时长参数化：移除所有硬编码 45 分钟默认值，改为使用课程实际时长
- 删除/修改课程时调用 cancelAlarmsForCourse() 取消对应闹钟
- RingerModeManager: saveCurrentRingerMode() 在自动静音激活时不覆盖原始铃声模式，新增 originalRingerMode/originalInterruptionFilter 字段
- 关闭自动静音时取消所有已调度静音闹钟并恢复铃声模式
- scheduleAutoSilentTasks() 调度前先取消旧静音闹钟，课程已结束时跳过调度
- 连续课程检测支持重叠课程，找到结束时间最晚的连续课程
- SilentModeReceiver.scheduleSilentEndAlarm() 改为通过 AlarmScheduler 统一调度
- LiveUpdateService: isRunning 添加 @Volatile 和 ActivityManager 验证，serviceScope 改为 val
- Course.duration 添加 coerceAtLeast(0) 边界保护，duration <= 0 的课程不触发通知
- BootReceiver: 移除重复调度，添加 ACTION_MY_PACKAGE_REPLACED 广播监听
- NotificationRescheduleWorker.scheduleQuickCheck 间隔从 5 分钟改为 15 分钟
- RescheduleWorker 传递课程实际 totalMinutes 给 LiveUpdateService
- 设备重启后自动静音未过期时重新设置静音模式
- 清理死代码：删除 WORK_NAME_ONGOING 常量、cancelOngoingNotifications 重复方法、静音即时广播重复发送

### 修改文件

- `SettingsDataStore.kt`（默认名称交换 + 迁移方法）
- `AppDatabase.kt`（版本 4→5 + MIGRATION_4_5）
- `DatabaseModule.kt`（注册 MIGRATION_4_5）
- `DuoScheduleApp.kt`（迁移调用 + 日志标签互换 + 静音恢复）
- `DesignTokens.kt`（PersonA/PersonB 颜色交换）
- `colors.xml` / `values-night/colors.xml`（颜色交换）
- `strings.xml`（标签交换）
- `SettingsViewModel.kt` / `ScheduleViewModel.kt` / `MainViewModel.kt` / `CourseEditViewModel.kt`（默认值修正 + AlarmScheduler 注入）
- `SettingsScreen.kt` / `ScheduleSettingsScreen.kt` / `CourseEditScreen.kt` / `ImportPreviewScreen.kt` / `MainScreen.kt`（UI 标签修正）
- `BottomNavItem.kt` / `TodayScheduleTimeline.kt` / `CurrentCourseCard.kt` / `Navigation.kt`（映射修正）
- `MyCoursesWidgetReceiver.kt` / `TaCoursesWidgetReceiver.kt` / `ScheduleWidgetReceiverMIUI.kt`（小组件修正）
- `CourseNotificationManager.kt`（PersonType + 通知ID + 时长参数化 + 死代码清理）
- `LiveUpdateService.kt`（PersonType + 状态可靠性 + 时长参数化）
- `RescheduleWorker.kt`（PersonType + totalMinutes 传递）
- `SilentModeReceiver.kt`（PersonType + 连续课程检测 + AlarmScheduler 统一调度）
- `AlarmScheduler.kt`（cancelAllSilentAlarms 方法）
- `RingerModeManager.kt`（原始铃声模式保护）
- `OngoingCourseReceiver.kt`（剩余时间重新计算 + 时长校验）
- `PromotedNotificationBuilder.kt`（文本中文化 + 时长参数化）
- `PromotedNotificationStyle.kt`（移除默认值 45）
- `ReminderAlarmReceiver.kt` / `ReminderWorker.kt`（传递 courseId）
- `BootReceiver.kt`（移除重复调度 + MY_PACKAGE_REPLACED）
- `NotificationRescheduleWorker.kt`（间隔 5→15 分钟）
- `AndroidManifest.xml`（MY_PACKAGE_REPLACED intent-filter）
- `CsvExporter.kt`（标记互换 + 旧版兼容 + parsePersonType exportVersion 参数）
- `Course.kt`（duration 边界保护）
- `PersonType.kt` / `TodayCourseDisplayMode.kt`（语义注释）
- `PreStartServiceReceiver.kt`（使用 isServiceRunning(context)）
- `widget_today_courses.xml` / `values-night/widget_today_courses.xml`（默认颜色 person_a_color）
- `widget_course_item.xml` / `values-night/widget_course_item.xml`（默认颜色 person_a_color）
- `build.gradle.kts`（版本号 1.3.0→1.4.0）

---

## [1.3.0] - 2026-05-17

### 变更类型：功能增强

### 状态：已实现

### 变更内容

**OPPO 公平运行内存适配（id=13825）**
- 新增 `FairMemoryReceiver` 类，监听 OPPO 公平运行内存机制广播
- 监听 `itgsa.intent.action.TRIM` 内存预警广播，收到后主动释放内存缓存并回调系统
- 监听 `itgsa.intent.action.KILL` 内存查杀广播，收到后保存现场数据并回调系统（3秒内完成）
- 支持物理内存异常（notifyType=1000）和 Java 堆内存异常（notifyType=2000）两种通知类型
- 通过 Binder 回调机制将处理结果通知系统端
- 在 `DuoScheduleApp.onCreate()` 中动态注册接收器，使用独立 HandlerThread 处理

**Android 17 兼容性适配（id=13789）**
- 新增 `network_security_config.xml`，默认禁止明文 HTTP 流量，适配 usesClearTraffic 弃用计划
- 在 AndroidManifest.xml 中引用 `android:networkSecurityConfig`
- 确认 Adaptive Apps 大屏自适应无需额外适配（应用未设置 screenOrientation 限制）
- 确认 getStableInsets 废弃无需适配（应用使用 Compose WindowInsets API）

### 修改文件

- `FairMemoryReceiver.kt`（新增）
- `network_security_config.xml`（新增）
- `DuoScheduleApp.kt`
- `AndroidManifest.xml`
- `build.gradle.kts`

---

## [1.2.1] - 2026-05-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容

- 设置页开关组件首次进入时出现不必要动画。`LiquidToggle` 的 `checked` handler 中 `animateToValue` 改为 `snapToValue`，程序化状态变化不再触发动画；删除未使用的 `LocalToggleShouldAnimate` 定义；简化 `IOSSwitch`，移除失效的 `shouldAnimate` 机制。

### 修改文件

- `LiquidToggle.kt`
- `SettingsComponents.kt`

---

## [1.2.0] - 2026-02-23 ~ 2026-03-17

### 变更类型：功能增强与优化（综合更新）

### 状态：已实现（部分待开发）

本版本包含从初始版本发布后的所有功能增强、优化和 Bug 修复，按功能模块分类记录。

---

### 一、课表核心功能

**功能 1：双人课程表管理**
- 支持本地存储和管理两个人的独立课程表，支持复杂周期设置
- 周次选择改为点选框，根据学期总周数动态显示，支持滑动选择多周
- 上课时间从时间段选择改为课节选择，支持单节或多节
- 修复周数显示逻辑：选择单独一周时显示"第5周"而非"1周"，新增 `formatWeekRanges` 智能格式化

**功能 8/9：Ta的课表页面 / 我的课表页面**
- 独立页面展示完整周课表，支持切换周次、点击编辑、点击空白添加
- 底部导航栏顺序调整为：主页 → 我的课表 → Ta的课表 → 设置

**功能 10：课表参数设置**
- 支持两人分别设置开学时间、学期总周数、当前周数、课表节数和课表时间
- 修改开学时间或总周数时自动重新计算当前周次

**功能 11：非本周课程显示开关**
- 控制是否在课表中显示非本周课程，默认关闭

**功能 13：课表周数日期显示**
- 根据开学时间和当前周数自动计算每天日期，列头下方显示"月/日"
- 修复非周一开学的日期计算：使用 `TemporalAdjusters.previousOrSame(MONDAY)` 找到开学日期所在周的周一

**功能 14：课表左侧节数与时间显示**
- 左侧显示节数和对应时间，节数放大、时间缩小，减少左侧占用宽度

**功能 16：课表空白格子交互优化**
- 改为两步操作：第一次点击显示灰色半透明遮罩 + 加号图标，第二次点击进入添加界面
- 点击其他格子或区域自动取消遮罩

**功能 21：课表显示优化与Bug修复**
- 移除课程方框固定宽高比，使用固定最小高度
- 使用 LazyColumn 替代 Column 消除大间隙
- 实现完整时间冲突检测（同天、同人、时间重叠、周次冲突）
- 默认节数从12节调整为5节

**功能 24：周数选择器布局优化**
- 周数选择器与标题同行显示，移除左右箭头按钮，仅保留滑动切换

**功能 27/66/71：课表周切换滑动动画**
- 从无动画 → 淡入淡出 → 水平平移 → 完整滑动跟随手势重构
- 课表内容实时跟随手指移动，松手即切换到相邻周
- 硬边界处理（首尾周无法继续滑动，触觉反馈），切换动画 200ms

**功能 45：课表格子配色优化**
- 从高饱和度 Material Design 彩色改为清新糖果色系（18种色调），深色模式亮度增加 20%

**功能 48：课表格子文字样式优化**
- 课程名称从 16sp Medium 改为 14sp Bold，地点从 10sp 增大到 12sp

**功能 60：课表界面优化**
- 空格子从灰色块改为虚线边框样式，修复首次加载闪烁，滑动阈值从 100dp 降到 60dp

**功能 61：课表网格线分隔设置**
- 虚线边框开关 → 横线和竖线分隔，设置从"课表设置"移至"显示设置"
- 移除"Ta的虚线边框"独立设置，合并为单一"显示网格线分隔"开关

**功能 72：课表格子长按操作**
- 所有格子支持长按（500ms + 触觉反馈），有课格子：复制/编辑/删除，空白格子：粘贴/添加
- 菜单改为横向并排文字 + Liquid Glass 效果，在格子上方弹出
- 复制/粘贴支持内存剪贴板，粘贴时检测时间冲突

**功能 81：课程字体大小自定义设置**
- 课程名称可选 10-16sp（默认12sp），地点可选 9-14sp（默认11sp）

**功能 82：课表课程卡片内边距优化**
- 内边距从水平4dp/垂直3dp调整为水平2dp/垂直2dp

**功能 87：当前周次不会自动切换**
- 新增 `updateCurrentWeekIfNeeded()` 方法，应用启动、进入前台、修改设置时自动计算更新

**功能 91（课表）：课表页面标题行布局优化**
- 周次选择器移至标题行同行显示，移除日期范围显示

---

### 二、主页功能

**功能 2：当前课程状态显示**
- 实时显示两人当前课程名称和剩余时间

**功能 3：今日课程列表**
- 显示两人今天的完整课程列表

**功能 4：共同空闲时间显示优化**
- 过滤过去的空闲时间，只显示当前时间之后的时段
- 点击空闲时间卡片弹出 BottomSheet 查看所有空闲时间

**功能 31：当前课程卡片美化优化**
- 从并排双卡片改为上下堆叠布局，有课显示课程信息+进度条，无课显示咖啡杯图标+休息中
- 后续改为并排布局，两个卡片水平排列各占一半宽度
- 配色：Ta 蓝色(#4789FE)，我 黄色(#FFB74D)

**功能 32：UI Bug 修复**
- 修复空闲时间白色方块（GlassmorphismCard 高度问题），移除首页 FAB

**功能 37：主页布局优化**
- 当前课程并排布局，今日课程双列显示，移除下节课预告，紧凑样式

**功能 38：今日课程显示模式设置**
- 支持仅显示我的/Ta的/都显示，设置入口：设置 → 显示设置

**功能 39：今日课程信息显示优化**
- 课程名称和地点行数从1行改为2行，配合单列模式优化显示

**功能 41：今日课程显示逻辑优化**
- 左侧时间条 + 右侧课程信息横向布局，已结束课程灰色处理

**功能 44：今日课程时间轴显示优化**
- 统一72dp固定高度卡片，三行布局（名称+地点+节次），空闲占位卡片
- 切换按钮标签动态化，使用设置中的人员名称

**功能 46：今日课程高度动态适配**
- 移除固定400dp高度，改为 `heightIn(max = 400.dp)` 动态计算

**功能 79：空闲时段显示优化**
- 显示最近的空闲时段而非最晚的，`freeTimeSlots.last()` 改为 `freeTimeSlots.first()`

**功能 83：首页今日课程跨天更新修复**
- 添加日期变化检测，跨天后自动刷新星期数据和课程列表

**功能 85：后台通知优化**
- 新增 `NotificationRescheduleWorker` 每15分钟重新调度通知
- 添加应用生命周期监听，优化前台服务启动

**功能 86：主页今日课程最后一个卡片阴影被截断**
- 移除 ScheduleList 内部 `heightIn(max = 400.dp)` 和 `verticalScroll`，统一页面滚动

**功能 91（主页）：主页今日课程列表底部被底栏遮挡**
- 在滚动 Column 中添加 `padding(bottom = 80.dp)` 为底栏预留空间

---

### 三、通知功能

**功能 34：课前通知**
- 通知标题从固定"Upcoming Class"改为显示课程名称
- 简化为只通知自己的课程，移除显示模式配置

**功能 35：上课中岛通知**
- 简化为只显示自己的课程状态，移除双人同时显示逻辑

**功能 36：通知设置**
- 设置入口：设置主页面"通知设置"分类
- 移除"岛通知显示模式"设置项

**功能 49：Android 16 Live Updates 适配**
- 升级 `androidx.core:core-ktx` 到 1.15.0，添加 `POST_LIVE_UPDATES` 权限
- 使用官方 `setLiveUpdate(true)` API 替代反射调用

**功能 50：小米动态岛适配**
- 新增 `MiuiIslandHelper` 工具类，构建 JSON tickerData 实现动态岛显示
- 大岛区域显示课程名称和地点+剩余时间，小岛区域显示应用图标

**功能 51：Live Updates 显示优化**
- 移除进度条显示，通知内容简化为"地点 · 剩余 X 分钟"

**功能 52：Android 15 实况通知适配**
- 严格版本检查（仅 API 36+），新增独立实况通知渠道
- 状态栏显示剩余时间（`setShortCriticalText`），通知更新节流 500ms

**功能 53：通知调试区优化**
- 按钮防抖 + SnackBar 反馈，权限状态卡片，日志查看器（最多50条），仅 Debug 构建显示

**功能 54：通知渠道优化**
- 4个渠道合并为2个：课程提醒 + 上课状态

**功能 67：上课自动静音**
- 支持静音/振动/勿扰三种模式，课程开始时触发，结束时恢复
- 连续课程（10分钟内）保持静音，仅对"我的课程"生效
- 修复权限问题：添加 `ACCESS_NOTIFICATION_POLICY` 权限声明
- 修复恢复问题：增强静音状态持久化（记录是否自动静音、结束时间、课程ID），应用启动和进入前台时检查并恢复

---

### 四、底部导航栏

**功能 47：底部导航栏标签动态化**
- 标签根据用户设置的人员名称动态显示，如"小明的课表"

**功能 56：底部导航栏 iOS 26 风格优化**
- 选中项圆角矩形高亮背景（药丸形状），图标18dp/文字10sp

**功能 84（导航栏）：底部导航栏指示器样式和动画优化**
- 移除独立指示器滑块，选中项自身显示 Capsule 背景 + 毛玻璃效果
- 动画 400ms，CubicBezierEasing 缓动，按压时 scale 0.85

**功能 88：底部导航栏液态玻璃效果重构**
- 参考 AndroidLiquidGlass-kmp 项目重构，选中指示器按压时显示完整玻璃效果
- 支持拖拽切换 Tab（DampedDragAnimation），按压缩放动画，交互高亮效果

**功能 89（底栏）：深色模式底栏图标颜色修复 + 底栏宽度优化**
- 添加 `LocalLiquidBottomTabContentColor` 传递正确的图标颜色
- 添加水平 padding 36dp 优化宽度

**功能 90：液态玻璃高光效果毛刺修复**
- `blurRadius` 从 `width/2f` 改为 `width*1.5f`，`falloff` 从 1f 改为 0.6f

---

### 五、导航与性能

**功能 19：页面切换性能优化**
- Flow 订阅策略从 `WhileSubscribed(5000)` 改为 `Lazily`，添加导航动画

**功能 20：全面性能优化**
- Splash Screen、数据库预加载、索引优化、Compose 编译优化、PerformanceMonitor 性能监控

**功能 73：iOS 风格页面切换动画**
- 底部导航切换：滑动方向根据页面位置决定，250ms
- 页面跳转：新页面从右滑入，背景页左移30%，300ms

**功能 75/77：页面切换卡顿优化**
- Flow 订阅策略改为 `Eagerly`，增强 ComposeWarmup 预热实际页面组件
- 在 DuoScheduleApp 中预加载 Repository 关键数据（并发预取）
- 动画时长优化：页面切换 250ms，底栏 150ms

**功能 76：去除开屏页面**
- 移除 Splash Screen 主题和 `installSplashScreen()` 调用

---

### 六、设置功能

**功能 15：设置界面二级导航重构**
- 设置主页面改为分类入口列表：用户设置 → 课表设置 → 显示设置 → 数据管理

**功能 22：设置界面交互优化**
- 时间设置移入独立三级界面，显示设置新增课表外观设置（周六/周日/非本周/格子高度）
- 所有输入改为点击弹出对话框（TextInputDialog/NumberInputDialog/TimeRangeInputDialog/SliderInputDialog）

**功能 28：节次选择框样式优化**
- 选中项蓝色(#4789FE)放大字号，未选中项灰色，动画过渡 150-200ms

**功能 55/64/65：时间选择器重构**
- 从文本输入改为滚轮选择器，选中项蓝色高亮20sp/未选中灰色16sp
- 重构为 BottomSheet 形式，取消灰色背景框，标题动态显示课节和时长

**功能 57：设置项点击交互优化**
- 点击横条直接弹出选择弹窗，移除独立按钮，右侧显示当前值+箭头

**功能 63：新增课程界面布局优化**
- 教室地点和上课老师从垂直布局改为水平布局，历史建议改为 Chip 样式

**功能 74：时间选择器 BottomSheet 手势冲突修复**
- GlassBottomSheet 新增 `enableDismissOnSwipe` 参数，时间选择器禁用手势关闭

---

### 七、数据管理

**功能 5：桌面小组件**
- MIUI/HyperOS 适配：移除标准 Android 小组件，只保留 MIUI 专用小组件
- 使用独立进程(:widgetProvider)运行，支持 MIUI exposure 刷新模式，适配 vivo 规范
- 全面重构计划：多种尺寸(2×2空闲时间/4×2我的课程/4×2Ta课程)，iOS 风格设计，智能刷新（待开发）

**功能 6：课程数据导入**
- 支持本应用导出CSV、Wakeup备份文件导入
- 导入预览功能：显示解析课程列表，可勾选、冲突红色高亮、选择导入目标

**功能 17：课程数据导出**
- CSV 完整格式导出，支持仅Ta/仅我/双人三种范围

**功能 18：小米文件选择控件接入**
- HyperOS 3+ 使用小米文件选择控件SDK，其他系统降级使用 Android 原生文件选择器

**功能 26：CSV 导入模板**
- 7字段模板（课程名称/星期/开始节次/结束节次/地点/老师/周次），周次支持多种格式
- 模板下载入口：设置 → 数据管理

**功能 29：外部应用打开CSV文件导入**
- 支持微信/QQ/文件管理器打开CSV，自动检测编码（UTF-8/GBK）

**功能 70：数据导出导入功能优化**
- 修复人员名称和总周数导出错误，导出时显示范围选择对话框
- 导入自动识别文件类型（应用导出CSV vs 模板CSV），外部应用 Intent 处理

**功能 89（导入）：双人课表分享与导入优化**
- 导出使用真实姓名替代"我"/"Ta"，导入时显示身份识别预览界面
- 用户手动选择课表分配，冲突课程红色高亮（待开发）

---

### 八、UI 与视觉优化

**功能 7：信息岛适配**
- 适配 Android 原生信息岛功能，不支持时降级为普通小组件

**功能 33：共同空闲时间显示优化**
- 移除智能推荐，药丸形状卡片(22dp圆角)，点击弹窗显示详情

**功能 43：全局卡片圆角统一**
- 主卡片/按钮/输入框/Chip/底部弹窗 16dp，课程格子 8dp，进度条 2dp

**功能 58：深色/浅色模式切换后背景显示异常**
- 为 `rememberLayerBackdrop` 添加 `darkTheme` key 参数
- 所有页面 `containerColor = Color.Transparent` 改为 `MaterialTheme.colorScheme.background`

**统一全局蓝色为 #4789FE**
- PrimaryLight/Dark、PersonAColor、colors.xml 资源全部统一

**统一文档中"我"和"Ta"的顺序**
- 所有描述统一为"我"在前、"Ta"在后

---

### 九、待开发功能

**功能 40：默认启动页面设置**
- 支持选择主页/我的课表/Ta的课表作为启动页面

---

## [1.1.0] - 2026-02-23

### 变更类型：技术栈调整

### 状态：已实现

### 变更内容

- 开发框架：Flutter → Android原生（Kotlin）
- 原因：用户要求改为 Android 原生开发，便于后续适配信息岛功能
- 开发语言：Kotlin
- 本地存储：Room + DataStore
- 小组件：AppWidgetProvider
- 状态管理：ViewModel + Flow
- UI框架：Jetpack Compose + Material Design 3
- 依赖注入：Hilt

---

## [1.0.0] - 2026-02-23

### 变更类型：新增

### 状态：已实现

### 新增功能

- 双人课程表管理：支持本地存储和管理两个人的独立课程表，支持复杂周期设置
- 当前课程状态显示：实时显示两个人当前正在上的课程，包括课程名称和剩余时间
- 今日课程列表：显示两个人今天的完整课程列表
- 共同空闲时间：计算并显示两个人今天共同的空闲时间段
- 桌面小组件：提供桌面小组件，无需打开应用即可查看当前课程状态
- 课程数据导入：支持从Wakeup课程表、教务系统等多种来源导入课程数据
- 信息岛适配（后续版本）：适配Android原生信息岛功能

---

## 版本号规范

遵循语义化版本规范：`MAJOR.MINOR.PATCH`

- **MAJOR（主版本号）**：不兼容的 API 修改或重大功能变更
- **MINOR（次版本号）**：向下兼容的功能性新增
- **PATCH（修订号）**：向下兼容的问题修正

版本号计算公式：versionCode = MAJOR × 10000 + MINOR × 100 + PATCH

---

**文档版本**：3.1.0

**最后更新**：2026-05-20
