<<<<<<< Updated upstream
# 课程管理系统 Android 应用

这是一个基于 Android 的课程管理系统客户端应用，使用 Jetpack Compose 构建现代化的用户界面。

## 🚀 功能特性

### 用户认证
- 用户登录和注册
- 支持学生、教师、管理员三种角色
- 会话管理

### 学生功能
- 浏览课程列表
- 查看我的课程
- 查看个人课表
- 选课和退课

### 教师功能
- 查看我的教学班
- 查看个人课表

### 管理员功能
- 课程管理
- 用户管理
- 系统统计

## 🛠️ 技术栈

- **UI框架**: Jetpack Compose
- **架构模式**: MVVM + Repository
- **依赖注入**: Hilt
- **网络请求**: Retrofit + OkHttp
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Navigation Compose
- **状态管理**: StateFlow

## 📱 项目结构

```
app/src/main/java/com/example/coursescheduleapp/
├── model/                 # 数据模型
├── network/              # 网络层
│   ├── ApiService.kt     # API接口
│   └── NetworkModule.kt  # 网络配置
├── repository/           # 数据仓库层
├── viewmodel/           # 视图模型
├── ui/                  # 用户界面
│   ├── LoginActivity.kt # 登录界面
│   └── screens/         # 各个屏幕
└── CourseScheduleApplication.kt # 应用入口
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK 34
- Kotlin 1.9.0 或更高版本

### 构建步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd CourseScheduleApp
   ```

2. **配置后端地址**
   在 `NetworkModule.kt` 中修改 `BASE_URL`：
   ```kotlin
//   private const val BASE_URL = "http://your-backend-url:8080/"
   ```

3. **构建应用**
   ```bash
   ./gradlew build
   ```

4. **运行应用**
   - 在 Android Studio 中打开项目
   - 连接设备或启动模拟器
   - 点击运行按钮

## 🔧 配置说明

### 网络配置
- 默认后端地址：`http://10.0.2.2:8080/` (Android模拟器访问本机)
- 真机测试时请修改为实际的后端地址

### 权限配置
应用需要以下权限：
- `INTERNET`: 网络访问
- `ACCESS_NETWORK_STATE`: 网络状态检查

## 📋 API 接口

应用支持以下主要API接口：

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 课程相关
- `GET /api/courses` - 获取所有课程
- `GET /api/courses/{id}` - 获取课程详情
- `GET /api/courses/classes` - 获取教学班列表

### 选课相关
- `POST /api/selections` - 选课
- `DELETE /api/selections` - 退课
- `GET /api/selections/my-courses/student/{studentId}` - 获取我的课程

### 课表相关
- `GET /api/schedules` - 获取所有课表
- `GET /api/schedules/teacher/{teacherId}` - 获取教师课表

## 🎨 UI 设计

应用采用 Material Design 3 设计规范，主要特点：
- 现代化的卡片式布局
- 响应式设计
- 深色/浅色主题支持
- 流畅的动画效果

## 🔄 状态管理

使用 StateFlow 进行状态管理：
- `AuthState`: 认证状态
- `CoursesState`: 课程列表状态
- `MyCoursesState`: 我的课程状态
- `ScheduleState`: 课表状态

## 🧪 测试

### 单元测试
```bash
./gradlew test
```

### 集成测试
```bash
./gradlew connectedAndroidTest
```

## 📝 开发指南

### 添加新功能
1. 在 `model/` 中定义数据模型
2. 在 `network/ApiService.kt` 中添加API接口
3. 在 `repository/` 中创建Repository
4. 在 `viewmodel/` 中创建ViewModel
5. 在 `ui/screens/` 中创建UI界面

### 代码规范
- 使用 Kotlin 编码规范
- 遵循 MVVM 架构模式
- 使用 Hilt 进行依赖注入
- 使用 StateFlow 进行状态管理

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件
=======
# 课程管理系统 Android 应用

这是一个基于 Android 的课程管理系统客户端应用，使用 Jetpack Compose 构建现代化的用户界面。

## 🚀 功能特性

### 用户认证
- 用户登录和注册
- 支持学生、教师、管理员三种角色
- 会话管理

### 学生功能
- 浏览课程列表
- 查看我的课程
- 查看个人课表
- 选课和退课

### 教师功能
- 查看我的教学班
- 查看个人课表

### 管理员功能
- 课程管理
- 用户管理
- 系统统计

## 🛠️ 技术栈

- **UI框架**: Jetpack Compose
- **架构模式**: MVVM + Repository
- **依赖注入**: Hilt
- **网络请求**: Retrofit + OkHttp
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Navigation Compose
- **状态管理**: StateFlow

## 📱 项目结构

```
app/src/main/java/com/example/coursescheduleapp/
├── model/                 # 数据模型
├── network/              # 网络层
│   ├── ApiService.kt     # API接口
│   └── NetworkModule.kt  # 网络配置
├── repository/           # 数据仓库层
├── viewmodel/           # 视图模型
├── ui/                  # 用户界面
│   ├── LoginActivity.kt # 登录界面
│   └── screens/         # 各个屏幕
└── CourseScheduleApplication.kt # 应用入口
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK 34
- Kotlin 1.9.0 或更高版本

### 构建步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd CourseScheduleApp
   ```

2. **配置后端地址**
   在 `NetworkModule.kt` 中修改 `BASE_URL`：
   ```kotlin
   private const val BASE_URL = "http://your-backend-url:8080/"
   ```

3. **构建应用**
   ```bash
   ./gradlew build
   ```

4. **运行应用**
   - 在 Android Studio 中打开项目
   - 连接设备或启动模拟器
   - 点击运行按钮

## 🔧 配置说明

### 网络配置
- 默认后端地址：`http://10.0.2.2:8080/` (Android模拟器访问本机)
- 真机测试时请修改为实际的后端地址

### 权限配置
应用需要以下权限：
- `INTERNET`: 网络访问
- `ACCESS_NETWORK_STATE`: 网络状态检查

## 📋 API 接口

应用支持以下主要API接口：

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 课程相关
- `GET /api/courses` - 获取所有课程
- `GET /api/courses/{id}` - 获取课程详情
- `GET /api/courses/classes` - 获取教学班列表

### 选课相关
- `POST /api/selections` - 选课
- `DELETE /api/selections` - 退课
- `GET /api/selections/my-courses/student/{studentId}` - 获取我的课程

### 课表相关
- `GET /api/schedules` - 获取所有课表
- `GET /api/schedules/teacher/{teacherId}` - 获取教师课表

## 🎨 UI 设计

应用采用 Material Design 3 设计规范，主要特点：
- 现代化的卡片式布局
- 响应式设计
- 深色/浅色主题支持
- 流畅的动画效果

## 🔄 状态管理

使用 StateFlow 进行状态管理：
- `AuthState`: 认证状态
- `CoursesState`: 课程列表状态
- `MyCoursesState`: 我的课程状态
- `ScheduleState`: 课表状态

## 🧪 测试

### 单元测试
```bash
./gradlew test
```

### 集成测试
```bash
./gradlew connectedAndroidTest
```

## 📝 开发指南

### 添加新功能
1. 在 `model/` 中定义数据模型
2. 在 `network/ApiService.kt` 中添加API接口
3. 在 `repository/` 中创建Repository
4. 在 `viewmodel/` 中创建ViewModel
5. 在 `ui/screens/` 中创建UI界面

### 代码规范
- 使用 Kotlin 编码规范
- 遵循 MVVM 架构模式
- 使用 Hilt 进行依赖注入
- 使用 StateFlow 进行状态管理

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件
>>>>>>> Stashed changes
- 创建 Pull Request 