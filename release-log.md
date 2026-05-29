# Release Log

## v1.0.0.4ab7d368-beta (2026-05-29)

基于 v1.0.0.493043bc-beta 的功能增强版本，主要引入 Token 三态模型与手动刷新功能。

### 新增功能

- **Token 三态模型** — 新增 `revoked` 字段，区分三种 Token 状态：
  - **激活**（isActive=true, revoked=false）：正常登录使用
  - **禁用**（isActive=false, revoked=false）：登录提示"Token 已禁用，请联系管理员"，管理列表中仍可见且可重新启用
  - **吊销**（isActive=false, revoked=true）：登录提示"Token 已被吊销，请联系管理员重新生成"，从管理列表隐藏，用户名可被复用
- **Token 启用/禁用开关** — 管理列表新增开关列，可一键切换普通用户 Token 的启用状态（Admin Token 不可操作）
- **手动刷新 Token** — 管理员可为已绑定邮箱的 Token 手动刷新，刷新后自动吊销该 Token 的所有 Session，新 Token 通过邮件发送至用户邮箱
- **用户名复用** — 吊销后的 Token 用户名可被新 Token 重新使用（仅当不存在同名且未吊销的 Token）
- **重复用户名拦截优化** — 用户名重复校验从数据库唯一约束改为代码逻辑判断，返回明确的 400 错误提示

### 安全改进

- 禁用/吊销 Token 时级联吊销所有 Session，用户立即退出登录
- 刷新 Token 时级联吊销所有 Session，旧 Token 立即失效
- 前后端统一使用 HttpOnly Session Cookie 认证（SameSite=Lax，Secure 条件启用）

### 破坏性变更

**数据库 schema 变更**：`tokens` 表新增 `revoked` 列，且 `name` 字段的唯一约束从数据库层面移除，改为代码逻辑校验。

**升级前必须手动执行以下 SQL**（使用 H2 Console 或数据库工具连接 `data/picbed.mv.db`）：

```sql
-- 1. 查找 tokens 表的所有 UNIQUE 约束
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_NAME = 'TOKENS' AND CONSTRAINT_TYPE = 'UNIQUE';

-- 2. 删除 name 字段的 UNIQUE 约束（保留主键和 token_hash 的唯一约束）
--    将 CONSTRAINT_93 替换为上一步查询到的 CONSTRAINT_NAME
ALTER TABLE IF EXISTS tokens DROP CONSTRAINT IF EXISTS CONSTRAINT_93;
```

> **注意**：`CONSTRAINT_TYPE` 必须是 `UNIQUE` 才能删除，切勿删除主键（PRIMARY KEY）约束。

### 变更文件

| 文件 | 变更类型 |
|------|----------|
| `schema.sql` | 移除 tokens.name UNIQUE 约束，新增 revoked 列 |
| `entity/Token.java` | 新增 revoked 字段，name 移除 unique=true |
| `repository/TokenRepository.java` | 新增 revoked 条件查询方法 |
| `service/TokenService.java` | 三态验证、toggleActive、refreshToken、用户名复用检查 |
| `service/EmailService.java` | 新增 sendTokenRefreshed 邮件通知 |
| `controller/TokenController.java` | 新增 active 切换、refresh 接口 |
| `controller/AuthController.java` | 登录区分禁用/吊销错误消息 |
| `interceptor/TokenInterceptor.java` | 删除（由 SessionInterceptor 替代） |
| `interceptor/SessionInterceptor.java` | 新增，Session Cookie 认证 |
| `config/CorsConfig.java` | 切换为 allowedOrigins + allowCredentials |
| `api/index.js` | 新增 login/logout/getSession/toggleTokenActive/adminRefreshToken |
| `stores/token.js` | 改为 Session 认证模式 |
| `views/LoginView.vue` | 新增登录页 |
| `router/index.js` | 新增 /login 路由，Session 状态守卫 |
| `components/admin/TokenManagePanel.vue` | 启用开关、刷新按钮、吊销后隐藏 |
| `i18n/zh-CN.json` / `en-US.json` | 新增登录、Token 状态相关翻译 |

### 已知限制

- 升级时需手动删除数据库 UNIQUE 约束（见上方破坏性变更说明）
- 刷新 Token 后旧 Session 立即失效，用户需重新登录

---

## v1.0.0.493043bc-beta (2026-05-28)

首个公开测试版本。

### 核心功能

- **OSS 直传** — 浏览器通过预签名 URL 直接上传至阿里云 OSS，文件不经过服务器
- **Token 认证** — 上传、删除、管理等写操作需携带 `X-Auth-Token`，公开查看无需鉴权
- **一键初始化** — 首次启动自动生成主密钥，访问 `/setup` 页面即可创建首个管理员 Token
- **图片画廊** — 公开瀑布流画廊，支持预览、复制链接、下载、按文件名/用户名搜索
- **批量管理** — 多选批量删除、分页图片列表、发布状态切换、图片预览
- **Token 管理** — 生命周期管理（创建/吊销/刷新），角色权限控制（ADMIN/USER），Token 泄露安全警告
- **邮箱验证** — 邮件验证码校验，支持 SMTP 邮件通知（Token 创建、刷新、安全警告）
- **自定义域名** — 支持配置 CDN 加速域名
- **国际化** — 中英文一键切换，语言偏好持久化存储
- **接口限流** — 基于 IP 的令牌桶限流，上传 10 次/分钟、管理 30 次/分钟、公开 60 次/分钟
- **零外部依赖数据库** — 内嵌 H2 文件数据库，无需安装 MySQL/PostgreSQL

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.3.5 |
| 运行时 | Java | 17 |
| 构建 | Maven | 3.9+ |
| 数据库 | H2 (文件模式) | — |
| ORM | Spring Data JPA + Hibernate | — |
| 对象存储 | Aliyun OSS SDK | 3.18.1 |
| 限流 | Bucket4j | 8.10.1 |
| 前端框架 | Vue 3 | 3.5 |
| 构建工具 | Vite | 6.0 |
| UI 组件库 | Element Plus | 2.9 |
| 状态管理 | Pinia | 2.3 |
| 路由 | Vue Router | 4.5 |
| 国际化 | vue-i18n | 9.14 |

### 安全设计

- Token 数据库仅存 SHA-256 哈希，原始值仅在创建时返回一次
- OSS 凭证仅服务端持有，通过环境变量注入，不暴露给客户端
- 预签名上传 URL 5 分钟有效
- 基于 IP 的令牌桶限流
- 首次初始化主密钥自动生成，存储于本地 `data/security.txt`
- RAM 最小权限原则（PutObject + DeleteObject + GetObject）

### 部署方式

- **Docker Compose** — 提供完整的前后端容器化部署，支持 `.env` 环境变量配置。内置 Nginx 仅代理前端构建产物（`dist/` 静态文件），监听 HTTP 5173 端口
- **构建脚本** — 提供 `build.bat` / `build.sh`，使用临时容器编译后端 JAR 和前端 dist，无需本地安装 Maven/Node.js
- **HTTPS** — 如需启用 HTTPS，请自行部署 Nginx 并配置 SSL 证书，反向代理至容器端口

### 已知限制

- 单文件上传上限默认未开启限制，管理员可在后台设置中动态调整上传大小限制
- 仅支持阿里云 OSS 作为对象存储
- 不支持第三方 OAuth 登录
- 不支持多用户注册（Token 由管理员统一管理）

### 开发说明

本项目由 [Claude Code](https://claude.ai/code)（Anthropic）辅助生成，底层模型为 DeepSeek V4 Pro。项目架构设计、后端接口、前端页面及绝大部分业务逻辑均由 AI 根据需求描述自动编写，部分细节（布局调整、交互优化、Bug 修复等）由人工手动修改完善。
