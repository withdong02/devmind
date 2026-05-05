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

**Prerequisites:** Docker Desktop, Java 17+, Maven 3.9+

```bash
# 1. Clone
git clone https://github.com/withdong02/devmind.git
cd devmind

# 2. Set API key
cp .env.example .env
# Edit .env and set MIMO_API_KEY

# 3. Start infrastructure
docker-compose up -d postgres redis

# 4. Run backend
mvn clean install -DskipTests
cd devmind-api && mvn spring-boot:run

# 5. Run frontend (separate terminal)
cd devmind-frontend && npm install && npm run dev
```

- Frontend: http://localhost:5173 (dev) or http://localhost:3000 (Docker)
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

**Full Docker deployment:**

```bash
docker-compose up -d
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

**前置条件：** Docker Desktop、Java 17+、Maven 3.9+

```bash
# 1. 克隆
git clone https://github.com/withdong02/devmind.git
cd devmind

# 2. 配置 API Key
cp .env.example .env
# 编辑 .env 设置 MIMO_API_KEY

# 3. 启动基础设施
docker-compose up -d postgres redis

# 4. 运行后端
mvn clean install -DskipTests
cd devmind-api && mvn spring-boot:run

# 5. 运行前端（另开终端）
cd devmind-frontend && npm install && npm run dev
```

- 前端：http://localhost:5173（开发模式）或 http://localhost:3000（Docker）
- 后端 API：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html

**Docker 全栈部署：**

```bash
docker-compose up -d
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
