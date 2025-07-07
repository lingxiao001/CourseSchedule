# 智能排课系统

这是一个前后端分离的智能排课系统，旨在为高校管理员、教师和学生提供一个高效、便捷的课程管理和查询平台。

## ✨ 功能特性

系统分为三个主要用户角色：管理员、教师和学生。

### 👨‍💼 管理员
- **用户管理**: 增、删、改、查系统中的所有用户（教师、学生）。
- **课程管理**: 管理课程信息，包括添加新课程、修改现有课程等。
- **教室管理**: 管理可用于排课的教室资源。
- **智能排课**: 系统核心功能，可以一键自动为所有课程安排上课时间、地点和教师。
- **手动排课**: 对自动排课结果进行微调，或手动安排特定课程。
- **数据统计**: 查看关于课程、学生选课情况等的统计数据。

### 👨‍🏫 教师
- **我的课程**: 查看个人所教授的课程列表。
- **教学班管理**: 管理教学班级，查看学生名单。
- **我的课表**: 查看个人的授课时间表。

### 🎓 学生
- **在线选课**: 浏览可选课程列表并进行在线选课。
- **我的课程**: 查看已选课程及详情。
- **我的课表**: 查看个人课表。

## 🛠️ 技术栈

本项目采用前后端分离架构。

### 后端 (`CourseSchedule`)
- **核心框架**: Spring Boot 3.4.5
- **持久层**: Spring Data JPA (Hibernate)
- **数据库**: MySQL
- **安全**: Spring Security
- **构建工具**: Maven
- **编程语言**: Java 21

### 前端 (`vue01`)
- **核心框架**: Vue.js 3
- **路由**: Vue Router
- **状态管理**: Pinia
- **UI 库**: Element Plus
- **HTTP客户端**: Axios
- **构建工具**: Vue CLI

## 🚀 快速开始

### 环境准备
- Java 21+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 后端启动
1.  **数据库配置**: 打开 `CourseSchedule/src/main/resources/application.properties` 文件，修改 `spring.datasource.url`, `spring.datasource.username`, 和 `spring.datasource.password` 以匹配您的本地MySQL配置。
2.  **创建数据库**: 在您的MySQL中创建一个名为 `courseschedule` 的数据库。
3.  **运行项目**:
    ```bash
    cd CourseSchedule
    mvn spring-boot:run
    ```
    后端服务将启动在 `http://localhost:8080`。

### 前端启动
1.  **安装依赖**:
    ```bash
    cd vue01
    npm install
    ```
2.  **启动开发服务器**:
    ```bash
    npm run serve
    ```
    前端应用将启动在 `http://localhost:8081` (或其它可用端口)。
3.  **访问应用**: 打开浏览器访问前端地址即可开始使用。

## 📁 项目结构

```
.
├── CourseSchedule/     # Spring Boot 后端项目
│   ├── src/main/java/  # Java 源代码
│   └── pom.xml         # Maven 配置文件
├── vue01/              # Vue.js 前端项目
│   ├── src/            # 前端源代码
│   └── package.json    # npm 依赖配置文件
└── README.md           # 项目说明文件
``` 