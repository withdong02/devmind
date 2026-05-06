# DevMind

> AI 开发者助手 | AI-Powered Developer Assistant

[中文](#中文) | [English](#english)

---

## 中文

### 简介

全栈 AI 开发者助手。后端 **Java 17 + Spring Boot 3 + Spring AI**，前端 **React + TypeScript + Vite**。

**核心模块：**
- **MCP** — Server 暴露工具给 Claude Desktop/Cursor；Client 连接外部 MCP 服务
- **Skills** — 6 个内置技能（代码审查、Bug 分析、文档生成、任务规划、Git 助手、代码搜索），自动路由
- **RAG** — AST 感知代码分块（JavaParser），pgvector 向量嵌入，混合检索（语义 + 关键词，RRF 融合）
- **记忆** — 4 种类型（短期/长期/情景/语义），LLM 自动整合
- **上下文工程** — Token 预算管理、优先级评分、贪心背包算法组装
- **多 Agent** — 编排器 + 4 个专家（代码、搜索、规划、文档）
- **Harness** — Hook 链、YAML Profile、守卫规则、速率限制、审计日志

**技术栈：** Java 17, Spring Boot 3.x, Spring AI 1.x, PostgreSQL 16 + pgvector, Redis 7, React 19, Vite, Docker Compose

### 快速开始

**前置条件：** [Docker Desktop](https://www.docker.com/products/docker-desktop/)，启用 WSL2 集成

```bash
git clone https://github.com/withdong02/devmind.git && cd devmind
cp .env.example .env   # 编辑：设置 MIMO_API_KEY
docker-compose up -d
```

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 后端 API | http://localhost:8080 |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |

### 修改后重新构建

```bash
docker-compose up -d --build           # 全部重建
docker-compose up -d --build backend   # 只重建后端
docker-compose up -d --build frontend  # 只重建前端
docker-compose logs -f backend         # 查看日志
docker-compose down                    # 停止所有服务
```

---

## English

### Overview

Full-stack AI developer assistant. Backend: **Java 17 + Spring Boot 3 + Spring AI**. Frontend: **React + TypeScript + Vite**.

**Core modules:**
- **MCP** — Server exposes tools to Claude Desktop/Cursor; Client connects external MCP services
- **Skills** — 6 built-in skills (Code Review, Bug Analysis, DocGen, Task Planner, Git, Code Search) with auto-routing
- **RAG** — AST-aware code chunking (JavaParser), pgvector embeddings, hybrid retrieval (semantic + keyword, RRF)
- **Memory** — 4 types (short-term/long-term/episodic/semantic), LLM auto-consolidation
- **Context Engineering** — Token budget management, priority scoring, greedy knapsack assembly
- **Multi-Agent** — Orchestrator + 4 specialists (Code, Research, Planning, Documentation)
- **Harness** — Hook chains, YAML profiles, guardrails, rate limiting, audit logging

**Tech stack:** Java 17, Spring Boot 3.x, Spring AI 1.x, PostgreSQL 16 + pgvector, Redis 7, React 19, Vite, Docker Compose

### Quick Start

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) with WSL2 enabled

```bash
git clone https://github.com/withdong02/devmind.git && cd devmind
cp .env.example .env   # edit: set MIMO_API_KEY
docker-compose up -d
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

### Rebuild After Changes

```bash
docker-compose up -d --build           # rebuild all
docker-compose up -d --build backend   # backend only
docker-compose up -d --build frontend  # frontend only
docker-compose logs -f backend         # view logs
docker-compose down                    # stop all
```

---

## Project Structure

```
devmind/
├── devmind-core/          # 领域接口（零依赖）
├── devmind-common/        # 共享 DTO、异常、工具类
├── devmind-memory/        # 记忆系统（4 种类型）
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

## License

MIT
