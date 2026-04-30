# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个 Elasticsearch 可视化管理工具，采用前后端分离架构。使用了工厂模式 + 策略模式 + 建造者模式来支持多版本 Elasticsearch (7.x/8.x/9.x) 的兼容操作。

- 后端: Spring Boot + MyBatis-Plus
- 前端: Vue 2.x + Element UI

## 代码架构

### 后端模块结构

```
backend/
├── es-tool-common/       # 公共模块 - 配置、工具类、领域模型
├── es-tool-start/        # 启动模块 - Controller、Service 入口
├── es-tool-7/            # ES 7.x 版本特定实现
├── es-tool-8/            # ES 8.x 版本特定实现
└── es-tool-9/            # ES 9.x 版本特定实现
```

**核心设计模式**:
1. **策略模式**: 每个 ES 操作（索引、文档、别名等）都实现 `ElasticsearchOperationStrategy` 接口
2. **工厂模式**: 根据版本号（7/8/9）和操作类型创建对应的策略实现
3. **版本隔离**: 不同 ES 版本的客户端配置和操作实现完全隔离在各自模块

**关键文件**:
- `ElasticsearchService.java`: 服务入口，根据版本号选择对应工厂和策略
- `ElasticsearchController.java`: REST API 控制器 (`/elasticsearch/operation`, `/elasticsearch/httpOperation`)
- `ElasticsearchOperation*StrategyFactory.java`: 各版本的策略工厂

### 前端结构

```
web/src/
├── views/elasticsearch/  # ES 各功能视图
│   ├── ElasticIndicesView.vue    # 索引管理
│   ├── DocumentView.vue          # 文档操作
│   ├── AliasesView.vue           # 别名管理
│   ├── TemplateView.vue          # 模板管理
│   ├── CurlView.vue              # HTTP 调试
│   ├── AnalyzeView.vue           # 分词分析
│   └── TaskView.vue              # 任务管理
└── views/MenuView.vue     # 主菜单页面
```

## 常用命令

### 后端开发

```bash
# 编译打包 (在 backend/ 目录)
mvn clean install

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=ElasticsearchTest

# 启动应用 (进入 es-tool-start 模块)
cd es-tool-start
mvn spring-boot:run
# 或
java -jar target/es-tool-start-*.jar
```

### 前端开发

```bash
# 安装依赖
cd web/
npm install

# 启动开发服务器 (localhost:8080)
npm run serve

# 构建生产版本 (输出到 dist/ 目录)
npm run build

# 代码检查
npm run lint
```

## 配置说明

### 后端配置

**配置文件**: `backend/es-tool-start/src/main/resources/application.yml`
- 服务端口: `8089`
- Web 容器: Undertow (替代 Tomcat)

**ES 连接配置**: `backend/data/esConnectParam.json`
- 支持多集群配置
- 格式: JSON 数组，包含 scheme, hostName, port, userName, password, version

### 前端代理配置

前端开发服务器通过 `/api/` 前缀代理到后端 `http://localhost:8089/`

## 开发要点

1. **版本扩展**: 新增 ES 版本支持时，在 `backend/` 下新建对应版本模块，实现策略接口和工厂
2. **新增操作**: 在各版本模块中添加新的 `*OperationStrategy` 实现类，并在工厂中注册
3. **连接管理**: 每个请求创建独立的 ES 客户端并在 finally 块中关闭，确保线程安全
4. **HTTP 模式**: 支持通过 HTTP 直接发送请求绕过客户端版本限制 (`httpOperation` 接口)
