# ES Tool 前端项目

ES 可视化管理工具前端，基于 Vue 2 + Element UI 构建。

## 技术栈

- **框架**: Vue 2.6 + Vue Router 3 + Vuex 3
- **UI 库**: Element UI 2.15
- **样式**: SCSS + CSS 变量
- **构建工具**: Vue CLI 5

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器 (localhost:8080)
npm run serve

# 生产构建
npm run build

# 代码检查
npm run lint
```

## 项目结构

```
src/
├── assets/                 # 静态资源
├── components/             # 公共组件
├── router/                 # 路由配置
├── store/                  # Vuex 状态管理
├── styles/                 # 全局样式
│   ├── variables.scss      # 全局变量（颜色、间距、阴影）
│   └── global.scss         # 全局工具类
├── utils/                  # 工具函数
├── views/                  # 页面视图
│   ├── elasticsearch/      # ES 功能模块
│   │   ├── ElasticIndicesView.vue
│   │   ├── DocumentView.vue
│   │   ├── AliasesView.vue
│   │   ├── TemplateView.vue
│   │   ├── TaskView.vue
│   │   ├── AnalyzeView.vue
│   │   ├── CurlView.vue
│   │   └── ApiDocsPanel.vue
│   ├── MenuView.vue        # 主布局（侧边栏+顶部栏）
│   ├── HomeView.vue        # 首页 - ES 连接仪表盘
│   ├── ElasticsearchView.vue  # ES 主页面
│   └── AboutView.vue
├── App.vue
└── main.js
```

## 2025-04-27 前端页面改造记录

### 改造目标
将原本简陋的页面升级为现代深色风格、专业运维工具感的界面，提升视觉体验和使用效率。

### 主要改动

#### 1. 全局主题体系

新增 `src/styles/variables.scss` 和 `src/styles/global.scss`，建立统一的设计令牌：

- **颜色系统**: 侧边栏深色背景 `#1e1e2d`、内容区浅灰 `#f5f8fa`、卡片白色、主色 `#409EFF`
- **间距系统**: 4px / 8px / 16px / 24px / 32px 阶梯
- **阴影与圆角**: 统一卡片阴影 `0 0.5rem 1.5rem rgba(0,0,0,0.05)`、圆角 8px
- **工具类**: `.app-card`、`.app-toolbar`、`.app-table`、`.page-wrapper`、`.status-dot`

`vue.config.js` 配置 `css.loaderOptions.scss.additionalData` 自动注入变量到所有组件。

#### 2. 布局架构重构

原布局为单一顶部水平导航，改造为 **顶部+侧边混合布局**：

- **左侧深色侧边栏**: 默认折叠（64px 只显示图标），可展开到 200px
  - 品牌区 `ES Tool`
  - 菜单项带 Element UI 图标（首页、ES 操作、关于我）
  - 菜单项圆角、hover/active 动画过渡
- **顶部白色状态栏**: 高度 48px
  - 左侧: 侧边栏折叠/展开切换按钮
  - 右侧: ES 连接状态徽标 + 用户下拉
- **内容区**: 浅灰背景 `#f5f8fa`，内边距 16px

#### 3. 首页改造成连接仪表盘

`HomeView.vue` 重写为 ES 集群连接仪表盘：

- 响应式卡片网格展示已配置的 ES 连接
- 每张卡片显示: 协议、主机:端口、版本、用户
- 状态标签: `已配置` / `未验证`
- 操作: **快速连接**（写入 Vuex 并跳转到 ES 操作页）
- 空状态: 引导用户去配置连接

#### 4. ES 连接配置折叠

`ElasticsearchView.vue` 连接表单区域支持折叠/展开：

- 折叠状态显示一行摘要: `协议 host:port version 已连接`
- 从首页快速连接后自动折叠
- 连接测试成功后自动折叠
- 点击"编辑连接"展开完整表单

连接表单改为三列网格布局，更清爽。版本信息以状态栏形式展示。

#### 5. 文档操作模块重新设计

`DocumentView.vue` 作为最高频页面，做了大量实用改进：

**查询面板:**
- 条件分组折叠: 基础查询(默认展开) / 时间筛选 / 查询字段 / 更新字段
- 索引选择器显示总数，支持一键全选/清空
- 动态字段紧凑行内布局
- **清空条件** 按钮一键重置
- **查询历史** 自动保存最近 5 次到 localStorage，可一键恢复

**结果面板:**
- 左右面板独立固定滚动，互不干扰
- 统计卡片 + 操作栏固定在顶部，不随结果滚动
- 表格/卡片视图切换:
  - **表格视图(默认)**: 紧凑行展示索引、ID、前3个字段摘要，极大减少垂直空间
  - 表格行可展开查看完整 JSON
  - **卡片视图**: 保留 JSON 卡片体验，默认折叠深度 0
- 结果内实时关键词过滤
- JSON 卡片左侧彩色边框条，不同索引用不同颜色
- 单条删除、复制、展开/折叠 JSON

#### 6. 所有子页面统一卡片化

所有 ES 功能子页面统一应用 `.app-card` + `.app-toolbar` 模式：

- `ElasticIndicesView`: 操作栏 + 表格，health 状态彩色 tag
- `AliasesView`: 写入索引状态严格判断（true/'true'/1/'1' 才为"是"）
- `TemplateView`: 操作栏 + 表格 + 详情抽屉
- `TaskView`: 操作栏 + 表格，合并操作列
- `AnalyzeView`: 表单紧凑布局
- `CurlView`: 请求/响应分区，ApiDocsPanel 代码块深色主题

#### 7. Vuex 状态扩展

`store/index.js` 新增全局状态：

- `sidebarCollapsed`: 侧边栏折叠状态（默认 true）
- `activeConnection`: 当前选中的 ES 连接（首页和 ES 页共享）
- `connections`: 连接列表

### 布局空间优化

改造过程中针对"页面变小"的反馈做了以下紧凑化调整：

| 项目 | 原值 | 现值 |
|------|------|------|
| 侧边栏宽度(展开) | 240px | 200px |
| 侧边栏默认状态 | 展开 | 折叠(64px) |
| 顶部栏高度 | 60px | 48px |
| 页面内边距 | 24px | 16px |
| 卡片内边距 | 20px | 16px |
| 工具栏间距 | 20px | 14px |
| 列间距(gutter) | 24px | 16px |

### 响应式

- 侧边栏折叠/展开支持响应式
- 表格列在小屏幕自动隐藏/调整
- DocumentView 在中等屏幕以下改为上下堆叠布局

## 2025-04-27 AI 智能助手模块

基于大语言模型（LLM）的 ES 智能查询与问答系统，让用户可以用自然语言操作 Elasticsearch。

### 功能概览

1. **ES 问答模式**：解答 Elasticsearch 概念、语法、调优等问题
2. **智能查询模式**：输入自然语言，AI 自动生成 ES DSL 并执行，返回结果和解释
3. **知识库**：建立索引-业务语义映射，让 AI 知道「新闻」对应哪些索引
4. **现有页面增强**：DocumentView 和 CurlView 均支持 AI 辅助生成查询
5. **历史沉淀**：成功查询自动保存到 ES 索引 `.es-tool-ai-history`，支持热查询推荐和模式复用

### 项目结构更新

```
src/
├── views/
│   └── AIAssistantView.vue       # AI 助手主页面
├── components/ai/
│   └── AIFillModal.vue           # DocumentView AI 查询弹窗
```

后端新增 `com.chen.controller.ai`、`com.chen.service.ai`、`com.chen.model.ai` 包。

### 配置说明

**后端 `application.yml`**：
```yaml
ai:
  llm:
    provider: openai
    api-key: ${AI_API_KEY:}              # 建议通过环境变量注入
    base-url: https://api.openai.com/v1  # 可替换为代理或兼容 API
    model: gpt-4o-mini
    timeout: 60
    max-tokens: 4096
```

**知识库文件**：`backend/data/esKnowledgeBase.json`
- 支持 AI 自动生成草稿 + 人工审核
- 格式：`[{ businessName, indices, description, fields }]`

### API 接口

| 接口 | 说明 |
|------|------|
| `POST /api/ai/chat` | AI 通用对话 |
| `POST /api/ai/generate-query` | 自然语言生成 DSL |
| `POST /api/ai/execute-query` | 生成 DSL 并执行 |
| `POST /api/ai/indices` | 获取索引目录 |
| `GET/POST /api/ai/knowledge-base` | 知识库读写 |
| `POST /api/ai/generate-kb-draft` | AI 生成知识库草稿 |
| `POST /api/ai/hot-queries` | 热查询推荐 |

### 安全说明

- AI 默认只生成查询类 DSL（search / count / aggregate）
- 执行前校验拒绝 `delete` / `update` / `index` 等写入操作
- API Key 建议通过环境变量注入，避免硬编码

## 代理配置

前端开发服务器通过 `/api/` 前缀代理到后端 `http://localhost:8089/`，配置在 `vue.config.js` 中。

## 参考文档

- [Element UI 组件文档](https://element.eleme.cn/#/zh-CN/component/installation)
- [Vue CLI 配置参考](https://cli.vuejs.org/config/)