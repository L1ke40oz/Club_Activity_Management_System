# 校园社团活动管理系统

一个面向高校的社团与活动管理平台，覆盖社团创建与审批、成员管理、活动发布与报名签到、社团评价、站内信通知、数据统计以及基于大模型的智能分析报告。前端使用 Vue 3 + Element Plus，后端使用 Spring Boot 3 + MyBatis-Plus + MySQL。

## 功能特性

- **用户与权限**：注册 / 登录 / 登出，基于 Session 的鉴权，区分普通用户与系统管理员两种角色。
- **社团管理**：社团的创建申请与管理员审批、信息编辑、逻辑解散，公开浏览社团列表与详情。
- **成员管理**：申请加入、审批 / 拒绝、退出社团、职务变更（社长 / 副社长 / 部长 / 成员）。
- **活动管理**：活动申请、管理员审批 / 驳回、结束活动、编辑与逻辑删除。
- **报名签到**：活动报名、单人签到、按学号批量签到、查看报名名单与个人报名记录。
- **社团评价**：对社团进行 1–5 星评分与文字评价。
- **站内信**：系统通知的接收、未读数统计、单条 / 全部标记已读。
- **数据统计**：全局概览、社团维度、活动维度、成员活跃度多维统计，前端以 ECharts 图表展示。
- **智能分析**：调用大模型生成社团 / 全校运营分析报告，支持 SSE 流式输出与 AI 对话。

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios、ECharts、marked |
| 后端 | Spring Boot 3.4、MyBatis-Plus 3.5、Spring Security Crypto（BCrypt）、MySQL 8 |
| 鉴权 | HttpSession + Cookie（JSESSIONID） |
| AI | 兼容 OpenAI 格式的 Chat Completions 接口（DeepSeek / 通义千问 / OpenAI 等） |

## 目录结构

```
Club_Activity_Management_System/
├── frontend/              # Vue 3 前端
│   ├── src/
│   │   ├── api/           # 各模块接口封装
│   │   ├── views/         # 页面组件
│   │   ├── router/        # 路由
│   │   ├── stores/        # Pinia 状态
│   │   └── utils/         # axios 封装、SSE 客户端
│   └── vite.config.js     # 开发代理配置（/api -> localhost:8080）
├── backend/               # Spring Boot 后端
│   └── src/main/
│       ├── java/          # controller / service / mapper / entity
│       └── resources/
│           ├── application.yml   # 配置（凭证通过环境变量注入）
│           ├── schema.sql        # 建表脚本
│           ├── init-data.sql     # 数据重置脚本（可选）
│           └── add-more-data.sql # 追加示例数据（可选）
├── docs/
│   └── API.md             # 后端 API 文档
└── .env.example           # 后端环境变量示例
```

## 环境要求

- **Node.js** ≥ 18（建议 20+）
- **JDK** 17
- **MySQL** 8.x
- **Maven**：无需单独安装，后端自带 `mvnw` 包装器

## 快速开始

克隆仓库：

```bash
git clone https://github.com/L1ke40oz/Club_Activity_Management_System.git
cd Club_Activity_Management_System
```

### 1. 准备数据库

启动 MySQL，执行建表脚本创建库和表（脚本首行会自动 `CREATE DATABASE campus_club_db`）：

```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

> 首次启动后端时，`DataInitializer` 会自动写入测试数据（1 个管理员 + 15 个用户 + 20 个社团 + 若干活动与报名记录），**无需**手动导入 `init-data.sql`。
> `init-data.sql` 用于清空并重置数据，`add-more-data.sql` 用于追加更多示例社团与活动，二者均为可选。

### 2. 配置并启动后端

后端的数据库密码与 AI Key 通过环境变量注入，仓库中不含真实凭证。`application.yml` 已为每一项设置了本地默认值，**只需覆盖与本机不同的项**（通常是数据库密码）。参考 `.env.example`。

**Windows PowerShell：**

```powershell
cd backend
$env:DB_PASSWORD = "你的MySQL密码"
$env:AI_API_KEY  = "你的大模型APIKey"   # 不使用智能分析可跳过
./mvnw.cmd spring-boot:run
```

**macOS / Linux：**

```bash
cd backend
export DB_PASSWORD="你的MySQL密码"
export AI_API_KEY="你的大模型APIKey"     # 不使用智能分析可跳过
./mvnw spring-boot:run
```

后端启动后监听 `http://localhost:8080`。控制台出现 `测试数据初始化完成！` 即表示数据就绪。

> 如果只想快速跑起来且本机 MySQL 密码就是默认值，可直接修改 `application.yml` 中对应的默认值；但请勿把真实密码提交回仓库。

可选的环境变量（均有默认值，见 `.env.example`）：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 数据库地址 / 端口 / 库名 | `127.0.0.1` / `3306` / `campus_club_db` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 / 密码 | `root` / `root` |
| `AI_API_URL` | 大模型接口地址（OpenAI 兼容） | DeepSeek 官方地址 |
| `AI_API_KEY` | 大模型 API Key | `your-api-key-here` |
| `AI_MODEL` | 模型名称 | `deepseek-chat` |

### 3. 启动前端

新开一个终端：

```bash
cd frontend
npm install
npm run dev
```

浏览器打开终端提示的地址（默认 `http://localhost:5173`）。Vite 已配置代理，前端 `/api` 的请求会转发到后端 `http://localhost:8080`，无需处理跨域。

### 4. 登录体验

| 学号 | 密码 | 角色 |
|------|------|------|
| `admin` | `admin123` | 系统管理员 |
| `2024001` ~ `2024015` | `123456` | 普通用户 |

管理员可审批社团 / 活动、生成智能分析报告、查看数据统计；普通用户可加入社团、报名活动、评价社团等。

## 生产构建

前端打包：

```bash
cd frontend
npm run build          # 产物输出到 frontend/dist
npm run preview        # 本地预览打包结果
```

后端打包为可执行 Jar：

```bash
cd backend
./mvnw clean package   # 产物在 backend/target/*.jar
java -jar target/ClubManagementSystem-0.0.1-SNAPSHOT.jar
```

## API 文档

完整接口说明见 [docs/API.md](docs/API.md)。所有接口以统一格式返回：

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

除登录、注册及少量公开浏览接口外，其余接口均需登录（携带 Cookie 中的 `JSESSIONID`），前端已在 Axios 中开启 `withCredentials`。

## 常见问题

- **后端启动报连接数据库失败**：检查 MySQL 是否启动、`DB_PASSWORD` 是否正确、`campus_club_db` 是否已由 `schema.sql` 创建。
- **智能分析 / AI 对话无响应**：确认已设置有效的 `AI_API_KEY`，且 `AI_API_URL` 可访问；未配置时其他功能不受影响。
- **前端请求 401**：说明未登录或会话过期，重新登录即可。
- **端口被占用**：后端端口在 `application.yml` 的 `server.port` 修改；前端端口在 `vite.config.js` 的 `server.port` 修改（改动前端端口不影响代理）。

## 许可

本项目为课程设计作品，仅供学习交流使用。
