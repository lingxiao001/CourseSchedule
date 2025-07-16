# 课程排课系统时序图文档

本目录包含了课程排课系统各个功能模块的详细时序图和功能描述。

## 文档结构

### 1. 系统功能描述.md
包含系统所有功能模块的详细文字描述，涵盖：
- **通用模块**：用户登录/注册、权限认证、个人信息修改
- **管理员后台**：用户管理、课程管理、教学资源管理、智能排课、数据看板
- **教师工作台**：我的课表、教学班管理
- **学生中心**：在线选课、我的课表

### 2. 时序图文件

#### 2.1 通用模块时序图.puml
展示了系统基础功能的交互流程：
- 用户注册流程：信息验证、密码加密、数据保存
- 用户登录流程：身份认证、JWT令牌生成、角色跳转
- 权限认证流程：令牌验证、权限检查、资源访问控制
- 个人信息修改流程：信息展示、数据更新、验证反馈

#### 2.2 管理员后台时序图.puml
详细描述了管理员操作的各种功能：
- **用户管理**：学生/教师信息的增删改查、权限验证
- **课程管理**：课程信息维护、教学班开设、冲突检查
- **教学资源管理**：教室信息维护、资源配置
- **智能排课**：自动排课算法、手动微调、课表发布
- **数据看板**：系统统计、报表生成、数据导出

#### 2.3 教师工作台时序图.puml
展现了教师日常使用的核心功能：
- **我的课表**：按周/日查看、课表导出、今日课程提醒
- **教学班管理**：学生名单查看、信息查询统计、名单导出
- **课程信息查看**：授课详情、时间安排、资料上传

#### 2.4 学生中心时序图.puml
描述了学生选课和课表管理的完整流程：
- **在线选课**：课程浏览、搜索筛选、详情查看、选课/退选
- **我的课表**：个人课表查看、多视图模式、导出分享、提醒设置

### 3. 类图文件

#### 3.1 通用模块类图.puml
展示了系统基础架构的类结构：
- **实体层**：User、Student、Teacher实体类及其关系
- **DTO层**：LoginDTO、AuthResponseDTO、UserCreateDTO等数据传输对象
- **控制器层**：AuthController用户认证控制器
- **服务层**：AuthService、UserService认证和用户管理服务
- **仓库层**：UserRepository、StudentRepository、TeacherRepository数据访问接口
- **配置层**：SecurityConfig安全配置
- **异常处理**：GlobalExceptionHandler全局异常处理

#### 3.2 管理员后台类图.puml
详细展现管理员功能模块的类架构：
- **实体层**：Course、TeachingClass、Classroom、ClassSchedule等核心实体
- **DTO层**：CourseDTO、TeachingClassDTO、ClassroomDTO、ScheduleDTO、ConflictDTO等
- **控制器层**：AdminUserController、CourseController、ClassroomController、IntelligentScheduleController、StatsController
- **服务层**：CourseService、TeacherClassService、ClassroomService、BasicIntelligentSchedulingService
- **仓库层**：CourseRepository、TeachingClassRepository、ClassroomRepository、CourseScheduleRepository
- **枚举类**：SchedulingMode排课模式枚举

#### 3.3 教师工作台类图.puml
展现教师功能模块的完整类结构：
- **实体层**：Teacher、TeachingClass、ClassSchedule、CourseSelection等教师相关实体
- **DTO层**：ScheduleDTO、TeachingClassDTO、StudentDTO、CourseInfoDTO、WeeklyScheduleDTO
- **控制器层**：ScheduleController、TeacherController、CourseMaterialController
- **服务层**：ScheduleService、TeacherClassService、CourseMaterialService、NotificationService
- **仓库层**：CourseScheduleRepository、CourseSelectionRepository、CourseMaterialRepository、NotificationRepository
- **异常处理**：ConflictException、PermissionDeniedException

#### 3.4 学生中心类图.puml
描述学生功能模块的类架构设计：
- **实体层**：Student、CourseSelection、TeachingClass、Course、ClassSchedule等学生相关实体
- **DTO层**：SelectionDTO、MyCourseDTO、CourseAvailableDTO、StudentScheduleDTO、CourseScheduleItem等
- **控制器层**：SelectionController、StudentCourseController、StudentScheduleController、StudentReminderController
- **服务层**：SelectionService、StudentCourseService、StudentScheduleService、ReminderService
- **仓库层**：CourseSelectionRepository、StudentCourseRepository、ReminderRepository
- **异常处理**：SelectionException、CourseFullException、TimeConflictException

## 技术特点

### 1. 系统架构
- 采用分层架构：控制器 -> 服务层 -> 数据访问层
- 前后端分离设计，RESTful API接口
- JWT token身份认证和权限控制

### 2. 核心功能
- **智能排课算法**：基于约束满足，考虑时间、教室、教师等多重约束
- **权限管理**：基于角色的访问控制（RBAC）
- **数据一致性**：通过事务和约束检查确保数据完整性
- **实时通知**：课程提醒、选课通知等消息推送

### 3. 用户体验
- 多种视图模式（周视图、日视图、月视图）
- 数据导出功能（PDF、Excel等格式）
- 搜索和筛选功能
- 响应式设计支持移动端

## 使用说明

### 时序图使用说明
1. 所有时序图使用PlantUML格式编写
2. 可以使用PlantUML工具或在线编辑器查看图形化的时序图
3. 每个时序图都包含了详细的错误处理和异常情况
4. 时序图中的API接口设计可以作为系统开发的参考

### 类图使用说明
1. 所有类图采用分层架构设计，清晰展示各层之间的依赖关系
2. 实体层展示数据库映射的JPA实体类
3. DTO层定义前后端数据传输格式
4. 控制器层处理HTTP请求和响应
5. 服务层实现业务逻辑
6. 仓库层提供数据访问抽象
7. 异常处理层定义业务异常类型

### 查看建议
1. 推荐使用Visual Studio Code + PlantUML插件查看
2. 也可使用在线PlantUML编辑器：http://www.plantuml.com/plantuml/uml/
3. 类图较为复杂，建议按模块分别查看

## 数据流说明

系统的数据流遵循以下模式：
1. **用户操作** → 前端界面接收
2. **前端请求** → 后端控制器处理
3. **业务逻辑** → 服务层执行
4. **数据操作** → 数据库交互
5. **结果返回** → 逐层返回到用户界面

每个操作都包含完整的错误处理机制，确保系统的稳定性和用户体验。

## 文档总结

本目录包含了课程排课系统的完整设计文档：

### 📋 功能描述文档（1个）
- **系统功能描述.md**：详细的功能需求和业务流程说明

### 🔄 时序图文档（4个）
- **1_通用模块时序图.puml**：用户认证和权限管理流程
- **2_管理员后台时序图.puml**：管理员操作的完整流程
- **3_教师工作台时序图.puml**：教师日常工作流程
- **4_学生中心时序图.puml**：学生选课和课表管理流程

### 🏗️ 类图文档（4个）
- **5_通用模块类图.puml**：系统基础架构和认证模块
- **6_管理员后台类图.puml**：管理员功能的类结构设计
- **7_教师工作台类图.puml**：教师功能的类架构设计
- **8_学生中心类图.puml**：学生功能的类结构设计

### 🎯 设计亮点

1. **分层架构清晰**：严格按照Controller-Service-Repository模式设计
2. **职责分离明确**：每个模块功能独立，接口明确
3. **异常处理完善**：包含详细的业务异常和错误处理机制
4. **数据传输规范**：使用DTO对象规范前后端数据交互
5. **扩展性良好**：采用接口抽象，便于功能扩展和维护
6. **安全性考虑**：包含权限验证和数据安全机制

这些文档为系统的开发、测试和维护提供了完整的技术参考，确保开发团队对系统架构有统一的理解。 