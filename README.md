# ES Tool 🛠️ - Elasticsearch 多版本可视化运维平台

[English](./README.en.md) | 中文

> 一站式管理 Elasticsearch **7.x / 8.x / 9.x** 集群：索引、文档、别名、模板、分词、HTTP 调试、数据迁移，内置 **AI 助手**帮你写 DSL。

## ✨ 项目亮点

- 🔄 **三版本兼容**: 一套界面同时操作 ES 7.x / 8.x / 9.x，基于 **工厂模式 + 策略模式 + 建造者模式** 实现版本无感切换
- 🤖 **AI 助手**: 自然语言转 DSL、流式对话 (SSE)、知识库管理、相似 query 推荐
- 🌐 **多集群管理**: 一个配置文件管理多套环境 (dev / staging / prod)
- 🚀 **HTTP 直通模式**: 高版本特性直接走 HTTP，无需等待 SDK 适配
- 🚚 **数据迁移**: 跨集群、跨索引数据搬运
- 🎓 **学习友好**: 真实多模式组合 + 多版本 SDK 兼容的 Spring Boot 项目示例

## 📦 功能矩阵

| 模块 | 能力 |
| --- | --- |
| 索引管理 | 创建 / 删除 / mapping 查看 / 批量操作 |
| 文档操作 | 增删改查、批量导入、条件检索 |
| 别名管理 | 别名绑定、切换、原子操作 |
| 模板管理 | Index Template / Component Template |
| 分词分析 | Analyzer 实时测试、对比 |
| HTTP 调试 | 原生 cURL 风格请求，绕过版本限制 |
| 数据迁移 | 跨集群 / 跨索引数据迁移 |
| AI 助手 | NL → DSL、流式对话、知识库、相似 query 推荐 |
| 任务管理 | 异步任务查看与跟踪 |

## 🏗️ 架构设计

```
backend/
├── es-tool-common/   # 公共模块：配置、工具类、领域模型
├── es-tool-start/    # 启动模块：Controller / Service 入口
├── es-tool-7/        # ES 7.x 策略实现
├── es-tool-8/        # ES 8.x 策略实现
└── es-tool-9/        # ES 9.x 策略实现
```

**核心设计模式**:

1. **策略模式** — 每个 ES 操作 (索引、文档、别名等) 实现 `ElasticsearchOperationStrategy` 接口
2. **工厂模式** — 根据版本号 + 操作类型创建对应策略实现
3. **版本隔离** — 不同 ES 版本客户端配置与实现完全隔离在各自模块

> 新增 ES 版本? 只需新建一个 `es-tool-X` 模块 + 工厂注册一行代码。

## 🚀 快速开始

### 环境要求

| 项 | 版本 |
| --- | --- |
| JDK | 1.8+ |
| Node.js | 16+ |
| Maven | 3.6+ |

### 后端

```bash
cd backend
mvn clean install
cd es-tool-start
mvn spring-boot:run   # 启动后访问 http://localhost:8089
```

### 前端

```bash
cd web
npm install
npm run serve   # 启动后访问 http://localhost:8080
```

### 集群连接配置

编辑 `backend/data/esConnectParam.json`:

```json
[
  {
    "scheme": "http",
    "hostName": "127.0.0.1",
    "port": 9200,
    "userName": "elastic",
    "password": "your-password",
    "version": "8"
  }
]
```

`version` 取值: `"7"` / `"8"` / `"9"`。

## 📸 截图

> 主菜单

![image-20260818172104017](image/image-20260818172104017.png)

> 索引管理 (可在 docs/ 下补充新截图)

## 🐳 Docker 部署

项目根目录提供 `docker-compose.yml`, 一键启动前后端:

```bash
docker-compose up -d
```

## 📚 适合谁用

- **运维** — 日常 ES 集群管理、跨版本运维
- **开发** — 调试 DSL、批量数据操作、写查询前验证分词
- **学习者** — 想看一个真实的多模式组合 + 多版本 SDK 兼容的 Spring Boot 项目

## 🤝 贡献

欢迎 Issue / PR。新版本支持、新操作策略、Bug 修复都很欢迎。

## 📜 License

MIT

---

> 作者：神的孩子都在歌唱  
> 博客：https://blog.csdn.net/weixin_46654114  
> 转载请注明来源。
