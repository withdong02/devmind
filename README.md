# DevMind

> **AI-Powered Developer Assistant** | **AI 开发者助手**

[English](#english) | [中文](#中文)

---

## English

### Overview

DevMind is a full-stack AI developer assistant built with **Java 17 + Spring Boot 3 + Spring AI** on the backend and **React + TypeScript + Vite** on the frontend. It demonstrates production-grade patterns including MCP (Model Context Protocol), Skills system, RAG pipeline, multi-type memory, context engineering, multi-agent orchestration, and harness engineering.

### Architecture

```
┌─────────────────────────────────────────────────────┐
│              React + TypeScript Frontend              │
├─────────────────────────────────────────────────────┤
│                 REST API + SSE                        │
├──────────┬──────────┬──────────┬────────────────────┤
│  Skills  │  Agents  │ Context  │      Harness        │
│  System  │  System  │ Engine   │  (Hooks/Profiles)   │
├──────────┴──────────┴──────────┴────────────────────┤
│          Memory System (4 types) + RAG Pipeline       │
├─────────────────────────────────────────────────────┤
│     MCP Server / Client  |  Spring AI (LLM)          │
├─────────────────────────────────────────────────────┤
│  PostgreSQL + pgvector  |  Redis  |  File System      │
└─────────────────────────────────────────────────────┘
```

### Key Features

| Feature | Description |
|---------|-------------|
| **MCP** | MCP Server exposes tools (code analysis, git ops, file system) to Claude Desktop/Cursor; MCP Client connects external MCP services |
| **Skills** | 6 built-in skills: Code Review, Bug Analysis, Doc Generation, Task Planner, Git Assistant, Code Search. Auto-routing via keyword + LLM |
| **RAG** | AST-aware code chunking (JavaParser), pgvector embeddings, hybrid retrieval (semantic + keyword with RRF fusion) |
| **Memory** | 4 types: Short-term (Redis), Long-term (pgvector), Episodic (task records), Semantic (facts). LLM-based auto-consolidation |
| **Context Engineering** | Token budget management, priority scoring (semantic + time decay + importance), greedy knapsack assembly |
| **Multi-Agent** | Orchestrator + 4 specialists (Code, Research, Planning, Documentation). LLM-based task decomposition |
| **Harness** | Hook chain execution, YAML profiles (default/safe-mode), guardrails, rate limiting, audit logging |

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x, Spring AI 1.x |
| Database | PostgreSQL 16 + pgvector |
| Cache | Redis 7 |
| LLM | Xiaomi MiMo API (OpenAI-compatible) / Ollama / any OpenAI-compatible provider |
| Frontend | React 19, TypeScript, Vite, shadcn/ui, Monaco Editor |
| Build | Maven (multi-module), npm |
| Deploy | Docker Compose |

### Project Structure

```
devmind/
├── devmind-core/          # Domain interfaces (zero dependencies)
├── devmind-common/        # Shared DTOs, exceptions, utilities
├── devmind-memory/        # Memory system (4 types)
├── devmind-rag/           # RAG pipeline (load → chunk → embed → retrieve)
├── devmind-mcp-server/    # MCP Server (expose tools)
├── devmind-mcp-client/    # MCP Client (connect external servers)
├── devmind-skills/        # Skill implementations
├── devmind-context/       # Context engineering engine
├── devmind-agents/        # Multi-agent orchestration
├── devmind-harness/       # Hook system, profiles, audit
├── devmind-api/           # Spring Boot application entry
└── devmind-frontend/      # React frontend
```

### Quick Start

#### Prerequisites

| Requirement | Setup |
|-------------|-------|
| WSL2 | `wsl --install -d Ubuntu` in PowerShell, then restart |
| Docker Desktop | Install from https://www.docker.com/products/docker-desktop/, enable WSL2 integration in Settings → Resources → WSL Integration |
| Java 17+ | Only needed for local dev mode |
| Maven 3.9+ | Only needed for local dev mode |
| Node.js 18+ | Only needed for local dev mode |

#### Option A: Docker Full Stack (Recommended)

```bash
# 1. Clone
git clone https://github.com/withdong02/devmind.git
cd devmind

# 2. Configure API key
cp .env.example .env
# Edit .env, set MIMO_API_KEY=your-key-here

# 3. Start everything
docker-compose up -d
```

Access:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

#### Option B: Local Development

```bash
# 1. Start infrastructure only
docker-compose up -d postgres redis

# 2. Set environment variable
export MIMO_API_KEY=your-key-here

# 3. Build and run backend
mvn clean install -DskipTests
cd devmind-api && mvn spring-boot:run

# 4. Run frontend (another terminal)
cd devmind-frontend && npm install && npm run dev
```

Access:
- Frontend: http://localhost:5173 (hot reload)
- Backend API: http://localhost:8080

#### Modifying the Project

**Backend (Java):**

Code changes take effect after rebuild. No hot reload by default.

```bash
# Quick recompile (skip tests)
mvn compile -DskipTests

# Full rebuild
mvn clean install -DskipTests

# Restart backend
cd devmind-api && mvn spring-boot:run
```

**Frontend (React/TypeScript):**

Vite dev server supports hot reload — save a file and the browser updates automatically.

```bash
cd devmind-frontend
npm run dev
```

#### Rebuilding After Changes

**Docker mode — rebuild and restart:**

```bash
# Rebuild backend image and restart
docker-compose up -d --build backend

# Rebuild frontend image and restart
docker-compose up -d --build frontend

# Rebuild everything
docker-compose up -d --build
```

**Local mode — just recompile and restart:**

```bash
# Backend: recompile module that changed, then restart
mvn compile -pl devmind-api -am -DskipTests
cd devmind-api && mvn spring-boot:run

# Frontend: just save the file, Vite hot-reloads
```

#### Useful Commands

```bash
# View running containers
docker-compose ps

# View backend logs (debug startup errors)
docker-compose logs -f backend

# View frontend logs
docker-compose logs -f frontend

# Stop all services
docker-compose down

# Stop and remove volumes (reset database)
docker-compose down -v
```

### API Endpoints

| Module | Endpoint | Description |
|--------|----------|-------------|
| Chat | `POST /api/v1/chat/sessions` | Create session |
| Chat | `POST /api/v1/chat/sessions/{id}/messages` | Send message |
| Chat | `GET /api/v1/chat/sessions/{id}/stream` | SSE streaming |
| Skills | `GET /api/v1/skills` | List skills |
| Skills | `POST /api/v1/skills/{id}/execute` | Execute skill |
| RAG | `POST /api/v1/rag/index` | Index directory |
| RAG | `POST /api/v1/rag/search` | Semantic search |
| Memory | `GET /api/v1/memory/stats` | Memory stats |
| Memory | `POST /api/v1/memory/search` | Search memories |
| Context | `POST /api/v1/context/build` | Build context |
| Agents | `GET /api/v1/agents` | List agents |
| Agents | `POST /api/v1/agents/orchestrate` | Orchestrate task |
| MCP | `GET /api/v1/mcp/tools` | List MCP tools |
| Harness | `GET /api/v1/harness/profiles` | List profiles |
| Harness | `GET /api/v1/harness/audit` | Audit logs |

---

## 中文

### 项目简介

DevMind 是一个全栈 AI 开发者助手，后端基于 **Java 17 + Spring Boot 3 + Spring AI**，前端使用 **React + TypeScript + Vite**。项目展示了 MCP（模型上下文协议）、Skills 系统、RAG 管道、多类型记忆、上下文工程、多 Agent 编排和 Harness 工程等前沿架构模式。

### 系统架构

```
┌─────────────────────────────────────────────────────┐
│            React + TypeScript 前端                     │
├─────────────────────────────────────────────────────┤
│               REST API + SSE 流式                      │
├──────────┬──────────┬──────────┬────────────────────┤
│  Skills  │  Agents  │ Context  │     Harness         │
│  技能系统 │  多Agent │ 上下文引擎│  (Hook/Profile)     │
├──────────┴──────────┴──────────┴────────────────────┤
│         记忆系统 (4种类型) + RAG 管道                    │
├─────────────────────────────────────────────────────┤
│    MCP Server/Client  |  Spring AI (LLM 调用)         │
├─────────────────────────────────────────────────────┤
│ PostgreSQL + pgvector  |  Redis  |  文件系统            │
└─────────────────────────────────────────────────────┘
```

### 核心特性

| 特性 | 说明 |
|------|------|
| **MCP** | MCP Server 暴露代码分析、Git 操作、文件系统等工具给 Claude Desktop/Cursor 使用；MCP Client 连接外部 MCP 服务扩展能力 |
| **Skills** | 6 个内置技能：代码审查、Bug 分析、文档生成、任务规划、Git 助手、代码搜索。支持关键词 + LLM 自动路由 |
| **RAG** | AST 感知的代码分块（JavaParser），pgvector 向量嵌入，混合检索（语义 + 关键词，RRF 融合） |
| **记忆** | 4 种类型：短期记忆（Redis）、长期记忆（pgvector）、情景记忆（任务记录）、语义记忆（事实提取）。LLM 自动整合 |
| **上下文工程** | Token 预算管理、优先级评分（语义相关性 + 时间衰减 + 重要性）、贪心背包算法组装 |
| **多 Agent** | 编排器 + 4 个专家 Agent（代码、搜索、规划、文档）。LLM 驱动任务分解与路由 |
| **Harness** | Hook 链执行、YAML Profile（default/safe-mode）、守卫规则、速率限制、审计日志 |

### 技术栈

| 层级 | 技术选型 |
|------|---------|
| 后端 | Java 17, Spring Boot 3.x, Spring AI 1.x |
| 数据库 | PostgreSQL 16 + pgvector |
| 缓存 | Redis 7 |
| LLM | 小米 MiMo API（OpenAI 兼容）/ Ollama / 任意 OpenAI 兼容服务 |
| 前端 | React 19, TypeScript, Vite, shadcn/ui, Monaco Editor |
| 构建 | Maven 多模块, npm |
| 部署 | Docker Compose 一键启动 |

### 模块说明

```
devmind/
├── devmind-core/          # 领域接口（零依赖，所有模块依赖它）
├── devmind-common/        # 共享 DTO、异常、工具类
├── devmind-memory/        # 记忆系统（短期/长期/情景/语义）
├── devmind-rag/           # RAG 管道（加载→分块→嵌入→检索）
├── devmind-mcp-server/    # MCP Server（暴露工具）
├── devmind-mcp-client/    # MCP Client（连接外部服务）
├── devmind-skills/        # 技能实现
├── devmind-context/       # 上下文工程引擎
├── devmind-agents/        # 多 Agent 编排
├── devmind-harness/       # Hook 系统、Profile、审计
├── devmind-api/           # Spring Boot 应用入口
└── devmind-frontend/      # React 前端
```

### 快速开始

#### 前置条件

| 依赖 | 安装方式 |
|------|---------|
| WSL2 | PowerShell 中执行 `wsl --install -d Ubuntu`，重启电脑 |
| Docker Desktop | 下载安装 https://www.docker.com/products/docker-desktop/，在 Settings → Resources → WSL Integration 中启用你的 Ubuntu 发行版 |
| Java 17+ | 仅本地开发模式需要 |
| Maven 3.9+ | 仅本地开发模式需要 |
| Node.js 18+ | 仅本地开发模式需要 |

#### 方式一：Docker 全栈（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/withdong02/devmind.git
cd devmind

# 2. 配置 API Key
cp .env.example .env
# 编辑 .env，写入 MIMO_API_KEY=你的key

# 3. 一键启动
docker-compose up -d
```

访问地址：
- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html

#### 方式二：本地开发

```bash
# 1. 只启动基础设施
docker-compose up -d postgres redis

# 2. 设置环境变量
export MIMO_API_KEY=你的key

# 3. 编译并启动后端
mvn clean install -DskipTests
cd devmind-api && mvn spring-boot:run

# 4. 启动前端（另开终端）
cd devmind-frontend && npm install && npm run dev
```

访问地址：
- 前端：http://localhost:5173（支持热更新）
- 后端 API：http://localhost:8080

#### 修改项目

**后端（Java）：**

修改代码后需要重新编译，不支持热更新。

```bash
# 快速重编译（跳过测试）
mvn compile -DskipTests

# 完整重建
mvn clean install -DskipTests

# 重启后端
cd devmind-api && mvn spring-boot:run
```

**前端（React/TypeScript）：**

Vite 开发服务器支持热更新 — 保存文件后浏览器自动刷新。

```bash
cd devmind-frontend
npm run dev
```

#### 修改后重新构建

**Docker 模式 — 重新构建镜像并重启：**

```bash
# 只重建后端
docker-compose up -d --build backend

# 只重建前端
docker-compose up -d --build frontend

# 全部重建
docker-compose up -d --build
```

**本地模式 — 重编译后重启：**

```bash
# 后端：重编译变更的模块，然后重启
mvn compile -pl devmind-api -am -DskipTests
cd devmind-api && mvn spring-boot:run

# 前端：直接保存文件，Vite 自动热更新
```

#### 常用命令

```bash
# 查看运行中的容器
docker-compose ps

# 查看后端日志（排查启动报错）
docker-compose logs -f backend

# 查看前端日志
docker-compose logs -f frontend

# 停止所有服务
docker-compose down

# 停止并删除数据卷（重置数据库）
docker-compose down -v
```

### API 端点

| 模块 | 接口 | 说明 |
|------|------|------|
| 聊天 | `POST /api/v1/chat/sessions` | 创建会话 |
| 聊天 | `POST /api/v1/chat/sessions/{id}/messages` | 发送消息 |
| 聊天 | `GET /api/v1/chat/sessions/{id}/stream` | SSE 流式响应 |
| 技能 | `GET /api/v1/skills` | 列出所有技能 |
| 技能 | `POST /api/v1/skills/{id}/execute` | 执行技能 |
| RAG | `POST /api/v1/rag/index` | 索引目录 |
| RAG | `POST /api/v1/rag/search` | 语义搜索 |
| 记忆 | `GET /api/v1/memory/stats` | 记忆统计 |
| 记忆 | `POST /api/v1/memory/search` | 搜索记忆 |
| 上下文 | `POST /api/v1/context/build` | 构建上下文 |
| Agent | `GET /api/v1/agents` | 列出 Agent |
| Agent | `POST /api/v1/agents/orchestrate` | 编排任务 |
| MCP | `GET /api/v1/mcp/tools` | MCP 工具列表 |
| Harness | `GET /api/v1/harness/profiles` | Profile 列表 |
| Harness | `GET /api/v1/harness/audit` | 审计日志 |

---

## License

MIT
