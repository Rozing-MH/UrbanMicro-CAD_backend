# UrbanMicro-CAD Backend

> 本文件继承全局 `D:\DevelopmentProducts\UrbanMicro-CAD\.claude\CLAUDE.md`，补充后端特有规则。

## 技术栈

- Spring Boot 3.3.7 + Java 21
- MyBatis-Plus 3.5.7 — ORM
- PostgreSQL — 主数据库
- H2 — 测试内存数据库
- Spring Security + JWT (jjwt 0.12.6) — 认证
- SpringDoc OpenAPI 2.6.0 — API 文档
- Spring Validation — 参数校验

## 目录职责

```
src/main/java/com/urbanmicrocad/
├── auth/          认证模块
│   ├── controller/  AuthController
│   ├── dto/         LoginRequest, LoginResponse, RegisterRequest
│   ├── entity/      SysUser
│   ├── mapper/      SysUserMapper
│   └── service/     AuthService
├── project/       工程管理模块
│   ├── controller/  ProjectController
│   ├── dto/         CreateProjectRequest, ProjectDTO, ProjectSnapshotDTO, SaveSnapshotRequest, UpdateProjectRequest
│   ├── entity/      Project, ProjectSnapshot
│   ├── mapper/      ProjectMapper, ProjectSnapshotMapper
│   └── service/     ProjectService
├── template/      断面模板模块
│   ├── controller/  TemplateController
│   ├── dto/         TemplateDTO
│   ├── entity/      ProjectTemplate
│   ├── mapper/      TemplateMapper
│   └── service/     TemplateService
├── report/        报表模块
│   ├── controller/  ReportController
│   ├── dto/         ExportReportRequest, ReportSummary
│   ├── entity/      EvaluationReport
│   ├── mapper/      EvaluationReportMapper
│   └── service/     ReportService
└── common/        通用模块
    ├── config/       CorsConfig, JacksonConfig, MybatisPlusConfig, OpenApiConfig, SecurityConfig, RequestBodySizeFilter
    ├── exception/    ApiException, ErrorCode, GlobalExceptionHandler
    ├── response/     ApiResponse
    └── security/     JwtAuthenticationFilter, JwtService, CurrentUser, CurrentUserService
```

## 编码规则

### 分层架构
- Controller：仅处理 HTTP 请求/响应，不含业务逻辑，方法 ≤ 50 行
- Service：核心业务逻辑，`@Transactional` 管理事务，方法 ≤ 80 行
- Mapper：MyBatis-Plus BaseMapper，不写 XML，复杂查询用 QueryWrapper

### DTO 规范
- 请求 DTO：`@Valid` 校验注解（`@NotNull`, `@NotBlank`, `@Size`）
- 响应 DTO：不暴露实体内部字段，按需映射
- 统一响应包装：`ApiResponse<T>`

### 异常处理
- 业务异常抛 `ApiException`（带 ErrorCode）
- 全局捕获：`GlobalExceptionHandler`
- 请求体过大：`RequestBodySizeFilter` + `SizeLimitedHttpServletRequest`

### 安全
- JWT Bearer Token 认证（`JwtAuthenticationFilter`）
- 敏感操作注入 `@CurrentUser`
- CORS 通过 `CorsProperties` 配置，生产环境不允许 `*`
- 请求体大小限制防 DoS

### JSON 字段
- 数据库 JSON 字段用 `@TableField(typeHandler = JsonNodeTypeHandler.class)`
- Jackson 配置：`JacksonConfig` 统一序列化行为

## 构建与验证

```bash
mvn compile          # 编译检查
mvn test             # 运行测试
mvn spring-boot:run  # 启动开发服务器
```

## API 文档

启动后访问：`http://localhost:8080/swagger-ui.html`（SpringDoc OpenAPI）

## 当前版本

基于变更日志最新版本号，每次变更 0.0.1 递增。
