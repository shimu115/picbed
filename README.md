# PicBed

基于阿里云 OSS 的轻量自托管图床服务。前端通过预签名 URL 直传 OSS，文件不经过服务器。写操作（上传/删除/管理）需要 Token 认证，查看公开访问无需鉴权。

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-4fc08d.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.9-409eff.svg)](https://element-plus.org/)

## 功能特性

- **OSS 直传** — 浏览器通过预签名 URL 直接上传到 OSS，不消耗服务器带宽
- **Token 认证** — 上传、删除、管理等写操作需携带 `X-Auth-Token`，查看公开访问无需登录
- **一键初始化** — 首次启动自动生成主密钥，访问 `/setup` 页面即可创建首个 Token
- **图片画廊** — 公开的瀑布流画廊，支持预览、复制链接、查看尺寸信息
- **批量管理** — 多选批量删除、分页图片列表、Token 生命周期管理（创建/吊销）
- **自定义域名** — 支持配置 CDN 加速域名
- **国际化** — 中英文一键切换，语言偏好持久化存储
- **接口限流** — 基于 IP 的令牌桶限流，上传 10 次/分钟、管理 30 次/分钟、公开 60 次/分钟
- **零外部依赖数据库** — 内嵌 H2 文件数据库，无需安装 MySQL/PostgreSQL

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.3.5、Java 17、Maven |
| 数据库 | H2（文件模式、MySQL 兼容） |
| ORM | Spring Data JPA + Hibernate |
| 对象存储 | 阿里云 OSS（aliyun-sdk-oss 3.18.1） |
| 接口限流 | Bucket4j 8.10.1 |
| 前端框架 | Vue 3.5、Vite 6、Element Plus 2.9 |
| 状态管理 | Pinia 2.3 |
| 路由 | Vue Router 4.5 |
| 国际化 | vue-i18n 9.14 |

## 环境要求

- **Java 17+** 和 **Maven 3.8+**（后端）
- **Node.js 18+** 和 **npm**（前端）
- **阿里云 OSS 存储桶**，需具备：
  - 已创建的存储桶（建议公共读）
  - RAM 用户，拥有 `PutObject`、`DeleteObject`、`GetObject` 权限
  - 存储桶已配置 CORS 规则（浏览器直传必需）

## 快速开始

### 1. 克隆项目并配置环境变量

```bash
git clone <仓库地址> && cd picbed
```

设置 OSS 凭证：

```bash
# Linux / macOS
export ALIYUN_OSS_ACCESS_KEY_ID="你的AccessKey ID"
export ALIYUN_OSS_ACCESS_KEY_SECRET="你的AccessKey Secret"
export ALIYUN_OSS_BUCKET_NAME="你的Bucket名称"
export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"    # 可选，默认为示例值
export ALIYUN_OSS_CUSTOM_DOMAIN="cdn.example.com"            # 可选，CDN 加速域名

# Windows (PowerShell)
$env:ALIYUN_OSS_ACCESS_KEY_ID="你的AccessKey ID"
$env:ALIYUN_OSS_ACCESS_KEY_SECRET="你的AccessKey Secret"
$env:ALIYUN_OSS_BUCKET_NAME="你的Bucket名称"
```

### 2. 构建并启动后端

```bash
cd picbed-server
mvn package -DskipTests
java -jar target/picbed-server-1.0.0.jar
```

首次启动时，服务器会自动生成主密钥并打印在控制台，同时保存到 `data/security.txt` 文件中。

### 3. 构建并启动前端

```bash
cd picbed-web
npm install
npm run dev        # 开发服务器运行在 http://localhost:5173（/api 代理到 :8080）
```

生产构建：

```bash
npm run build      # 输出到 dist/
```

将 `dist/` 目录通过 Nginx 托管，与 Spring Boot JAR 配合部署即可。

### 4. 初始化系统

1. 打开 `http://localhost:5173`（或你的部署地址）
2. 页面会自动跳转到 `/setup` 初始化页面
3. 输入服务器控制台打印的主密钥（或查看 `data/security.txt` 文件）
4. 为你的首个 Token 命名并创建
5. **立即复制生成的 Token**——关闭后将无法再次查看
6. 设置完成，可以开始上传图片了

## 配置项

所有配置位于 `picbed-server/src/main/resources/application.yml`。

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|---------|--------|------|
| `aliyun.oss.endpoint` | `ALIYUN_OSS_ENDPOINT` | `oss-cn-hangzhou.aliyuncs.com` | OSS 地域节点 |
| `aliyun.oss.access-key-id` | `ALIYUN_OSS_ACCESS_KEY_ID` | （空） | RAM AccessKey ID |
| `aliyun.oss.access-key-secret` | `ALIYUN_OSS_ACCESS_KEY_SECRET` | （空） | RAM AccessKey Secret |
| `aliyun.oss.bucket-name` | `ALIYUN_OSS_BUCKET_NAME` | （空） | OSS 存储桶名称 |
| `aliyun.oss.custom-domain` | `ALIYUN_OSS_CUSTOM_DOMAIN` | （空） | 自定义 CDN 域名，如 `cdn.example.com` |
| `app.upload.max-file-size` | — | `20971520`（20 MB） | 上传文件大小上限（字节） |

**数据库：** H2 文件数据库，存储在 `data/picbed.mv.db`，用户名和密码均为 `picbed`。可按需在 `application.yml` 中修改。

**H2 控制台：** 开发环境下可通过 `/h2-console` 访问，JDBC URL 为 `jdbc:h2:file:./data/picbed`。

## 项目结构

```
picbed/
├── picbed-server/                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/picbed/
│       ├── PicBedApplication.java  # 启动入口
│       ├── config/                 # CORS / OSS / 限流 / WebMvc / 初始化密钥
│       ├── controller/             # TokenController / ImageController / UploadController
│       ├── dto/                    # 请求/响应 DTO + Result<T> 统一封装
│       ├── entity/                 # JPA 实体：Token / ImageInfo
│       ├── exception/              # GlobalExceptionHandler + 自定义异常
│       ├── interceptor/            # TokenInterceptor（X-Auth-Token 校验）
│       ├── repository/             # Spring Data JPA 仓库接口
│       ├── service/                # 业务逻辑：TokenService / ImageService / OssService
│       └── util/                   # TokenUtil（哈希）/ OssUtil（Key 生成 + URL 拼接）
│
└── picbed-web/                     # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/                    # Axios 实例 + 所有 API 函数
        ├── components/             # admin/ / common/ / gallery/ / layout/ / upload/
        ├── composables/            # useOssUpload.js（OSS 直传逻辑）
        ├── i18n/                   # zh-CN.json / en-US.json + vue-i18n 配置
        ├── router/                 # 4 个路由 + 导航守卫
        ├── stores/                 # Pinia：token.js / upload.js
        └── views/                  # Gallery / Upload / Management / Setup
```

## API 文档

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/public/status` | 查询系统是否已初始化 |
| `GET` | `/api/public/images?page=0&size=20` | 分页获取图片列表（按时间倒序） |
| `GET` | `/api/public/images/{id}` | 获取单张图片详情 |

### 初始化接口（仅限首次，需主密钥）

| 方法 | 路径 | 请求头 | 说明 |
|------|------|--------|------|
| `POST` | `/api/setup/token` | `X-Setup-Token` | 使用主密钥创建首个认证 Token |

### 受保护接口（需 X-Auth-Token 请求头）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/upload/signature` | 获取 OSS 预签名上传 URL（5 分钟有效） |
| `POST` | `/api/admin/images` | OSS 上传完成后保存图片元数据 |
| `DELETE` | `/api/admin/images/{id}` | 删除图片（同时删除数据库记录和 OSS 对象） |
| `DELETE` | `/api/admin/images/batch` | 批量删除图片 |
| `GET` | `/api/admin/tokens` | 列出所有 Token |
| `POST` | `/api/admin/tokens` | 创建新 Token |
| `DELETE` | `/api/admin/tokens/{id}` | 吊销 Token |

所有接口统一返回格式：

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```

## 架构设计

### 上传流程

```
浏览器                       后端服务器                      阿里云 OSS
  │                            │                               │
  │── POST /upload/signature ──►│                               │
  │   (X-Auth-Token)           │                               │
  │◄── {putUrl, accessUrl} ───│                               │
  │                            │                               │
  │──────── PUT 文件（预签名 URL）──────────────────────────────►│
  │◄─────── 200 OK ───────────────────────────────────────────│
  │                            │                               │
  │── POST /admin/images ────►│                               │
  │   （保存元数据）             │                               │
  │◄── 200 OK ───────────────│                               │
```

### 查看流程

```
浏览器                       后端服务器                      阿里云 OSS
  │                            │                               │
  │── GET /public/images ────►│                               │
  │◄── [图片列表] ────────────│                               │
  │                            │                               │
  │──────── GET {ossUrl}（img src）───────────────────────────►│
  │◄─────── 图片二进制数据 ────────────────────────────────────│
```

## 安全设计

- **Token 存储：** 数据库仅存 SHA-256 哈希值，原始 Token 仅在创建时返回一次
- **OSS 凭证：** 仅服务端持有，通过环境变量注入，绝不暴露给客户端
- **预签名 URL：** 5 分钟有效期，由服务端使用 RAM 凭证生成
- **接口限流：** Bucket4j 基于 IP 的令牌桶算法，上传 10 次/分钟、管理 30 次/分钟、公开 60 次/分钟
- **初始化保护：** 主密钥在首次启动时自动生成，存储于本地 `data/security.txt`

### OSS 存储桶建议配置

- **访问权限：** 公共读、私有写
- **CORS：** 允许你的域名发起 `PUT` 请求，允许头设为 `*`
- **防盗链：** 配置 Referer 白名单，仅允许你自己的域名
- **RAM 权限：** 最小权限原则——仅授予目标 Bucket 的 `PutObject`、`DeleteObject`、`GetObject`

## Docker 部署

### 1. 配置环境变量

在项目根目录创建 `.env` 文件：

```bash
# 必填：阿里云 OSS 凭证
ALIYUN_OSS_ACCESS_KEY_ID=你的AccessKey ID
ALIYUN_OSS_ACCESS_KEY_SECRET=你的AccessKey Secret
ALIYUN_OSS_BUCKET_NAME=你的Bucket名称

# 可选：OSS 地域节点（默认值见下方）
ALIYUN_OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com

# 可选：CDN 加速域名
ALIYUN_OSS_CUSTOM_DOMAIN=cdn.example.com

# 可选：邮件通知（不填则不启用邮箱验证功能）
SMTP_HOST=smtp.example.com
SMTP_PORT=465
SMTP_USERNAME=your-email@example.com
SMTP_PASSWORD=your-smtp-password
```

| 变量 | 必填 | 默认值 | 说明 |
|------|:---:|--------|------|
| `ALIYUN_OSS_ACCESS_KEY_ID` | 是 | — | RAM AccessKey ID |
| `ALIYUN_OSS_ACCESS_KEY_SECRET` | 是 | — | RAM AccessKey Secret |
| `ALIYUN_OSS_BUCKET_NAME` | 是 | — | OSS 存储桶名称 |
| `ALIYUN_OSS_ENDPOINT` | 否 | `oss-cn-beijing.aliyuncs.com` | OSS 地域节点 |
| `ALIYUN_OSS_CUSTOM_DOMAIN` | 否 | — | CDN 加速域名 |
| `SMTP_HOST` | 否 | — | SMTP 服务器地址 |
| `SMTP_PORT` | 否 | `465` | SMTP 端口 |
| `SMTP_USERNAME` | 否 | — | SMTP 用户名 |
| `SMTP_PASSWORD` | 否 | — | SMTP 密码 |

> `.env` 已加入 `.gitignore`，不会被提交到仓库。

### 2. 构建并启动

```bash
# Windows
build.bat

# Linux / macOS
chmod +x build.sh start.sh
./build.sh
```

构建脚本会自动用临时容器编译后端 JAR 和前端 dist，然后构建运行时镜像并启动。仅需 Docker，无需本地安装 Java/Maven/Node.js。

### 3. 手动启动 / 停止

```bash
docker-compose up -d      # 启动
docker-compose down       # 停止
```

## 手动部署

### 后端

```bash
cd picbed-server
mvn package -DskipTests
# 将 target/picbed-server-1.0.0.jar 部署到服务器，配合 Nginx 反向代理运行
```

### 前端

```bash
cd picbed-web
npm run build
# 将 dist/ 目录通过 Nginx 托管，/api 路径反向代理到后端
```

### Nginx 配置参考

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header X-Forwarded-For $remote_addr;
    }

    location / {
        root /path/to/picbed-web/dist;
        try_files $uri $uri/ /index.html;
    }
}
```

## 开源协议

MIT © 2026 [shimu115](https://github.com/shimu115)

## 开发说明

本项目整体由 AI 辅助生成，开发工具为 [Claude Code](https://claude.ai/code)（Anthropic），底层模型为 DeepSeek V4 Pro。项目架构设计、后端接口、前端页面及绝大部分业务逻辑均由 AI 根据需求描述自动编写，少部分细节（如布局调整、交互优化、Bug 修复等）由人工手动修改完善。

## 致谢

- [Claude Code](https://claude.ai/code) — AI 编程助手
- [DeepSeek](https://deepseek.com/) — 底层大语言模型
- [Element Plus](https://element-plus.org/) — Vue 3 组件库
- [Bucket4j](https://bucket4j.com/) — 令牌桶限流库
- [阿里云 OSS](https://www.aliyun.com/product/oss) — 对象存储服务
