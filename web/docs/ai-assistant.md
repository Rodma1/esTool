# ES Tool AI 智能助手功能详解

> 让自然语言成为操作 Elasticsearch 的入口

## 背景与动机

在日常使用 Elasticsearch 的过程中，开发者经常面临两个痛点：

1. **查询门槛高**：ES 的 DSL 语法虽然强大，但要写出一个准确的布尔查询、聚合或时间范围过滤，需要记忆大量字段名和语法细节。对于非核心开发人员，这个门槛更高。
2. **索引语义不透明**：一个集群可能有几百个索引，`log-2024.01`、`order-prod-idx`、`user_behavior_v2`……新成员很难一眼看出每个索引的业务含义和字段用途。

基于以上痛点，我们为 ES Tool 引入了 **AI 智能助手**模块。用户只需像聊天一样输入一句话，AI 就能自动分析意图、生成正确的 ES DSL、执行查询并返回自然语言总结。

---

## 功能概览

### 1. ES 问答（Chat）

纯对话模式，AI 只回答不执行。适用于：

- 询问 ES 概念：`term 和 match 有什么区别？`
- 调优建议：`如何优化聚合查询性能？`
- 语法参考：`multi_match 的 type 有哪些取值？`

### 2. 智能查询（Query & Execute）

对话式执行模式。用户输入自然语言需求，AI 自动完成以下闭环：

```
用户输入 → AI 生成 DSL → 安全校验 → 执行查询 → 自然语言总结 → 保存历史
```

**示例对话：**

> **用户**：查询 news 索引最近 7 天发布、标题包含"中国"的文章
>
> **AI**：
> - 生成 DSL：
>   ```json
>   {
>     "query": {
>       "bool": {
>         "must": [
>           { "range": { "publishTime": { "gte": "now-7d/d" } } },
>           { "match": { "title": "中国" } }
>         ]
>       }
>     }
>   }
>   ```
> - 执行结果：命中 128 条
> - 总结：最近 7 天内共有 128 篇新闻标题包含"中国"，其中 2024-04-25 发布量最高。

### 3. 知识库（Knowledge Base）

解决"AI 怎么知道查哪个索引"的问题。

系统维护一份 `esKnowledgeBase.json`，记录业务语义映射：

```json
[
  {
    "businessName": "新闻数据",
    "indices": ["news-2024", "news-2025"],
    "description": "包含新闻标题、正文、作者、发布时间",
    "fields": {
      "title": { "type": "text", "description": "新闻标题", "analyzed": true },
      "publishTime": { "type": "date", "description": "发布时间", "analyzed": false }
    }
  }
]
```

**AI 生成草稿**：一键扫描集群所有索引的 mapping 结构，调用 LLM 自动生成知识库草稿，人工审核后保存。

### 4. 现有页面增强

- **DocumentView**：新增「AI 查询」按钮，一键生成查询条件并填充表单
- **CurlView**：新增「AI 生成请求体」按钮，描述需求即可生成 JSON body

### 5. 历史沉淀（History）

每次成功的「自然语言 → DSL → 执行」链路自动保存到专用索引 `.es-tool-ai-history`：

- **模式复用**：新用户提问前，自动检索历史上相似的成功查询，作为 few-shot 示例喂给 LLM，提升准确率
- **热查询推荐**：前端展示「大家都在问」，一键复用高频查询
- **会话关联**：同一 session 内的多轮对话共享上下文，支持增量指令（"再加上时间过滤"）

---

## 技术架构

```
┌──────────────────────────────────────────────────────────────┐
│  前端 (Vue 2)                                                 │
│  ├── AIAssistantView.vue      # AI 助手主页面                 │
│  ├── DocumentView.vue         # + AI 查询按钮                 │
│  ├── CurlView.vue             # + AI 生成请求体               │
│  └── AIFillModal.vue          # 弹窗式 AI 查询                │
├──────────────────────────────────────────────────────────────┤
│  后端 (Spring Boot)                                           │
│  ├── AIAssistantController    # /api/ai/*                     │
│  ├── AIAssistantService       # 核心编排                      │
│  │   ├── chat()               # 问答模式                      │
│   │   ├── generateQuery()     # 生成 DSL                      │
│   │   ├── executeQuery()      # 生成并执行                    │
│   │   └── generateKbDraft()   # 生成知识库草稿                │
│   ├── LLMClient               # HTTP 调用 LLM API             │
│   ├── IndexCatalogService     # 索引元数据缓存                │
│   ├── KnowledgeBaseService    # 知识库读写                    │
│   ├── AIHistoryService        # 历史记录存储（用 ES 存 ES）   │
│   └── PromptTemplate          # Prompt 模板                   │
├──────────────────────────────────────────────────────────────┤
│  外部依赖                                                     │
│  └── OpenAI / Claude / 中转站  # 兼容 OpenAI API 格式         │
└──────────────────────────────────────────────────────────────┘
```

---

## 核心流程详解

### 智能查询执行链路

```
用户输入
  │
  ▼
┌─────────────────┐
│ 本地预筛选       │ ← 关键词匹配索引名/字段名/知识库（零 token 成本）
│ - 相关索引 Top 5 │
│ - 知识库条目     │
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ 历史模式检索     │ ← 搜索 .es-tool-ai-history 中相似成功案例
│ - few-shot 示例  │
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ Prompt 组装      │ ← 系统角色 + 索引上下文 + 知识库 + few-shot + 用户问题
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ LLM 调用         │ ← 要求返回严格 JSON：{dsl, indexPattern, explanation}
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ DSL 解析与安全校验│ ← 拒绝 delete/update/index 等写入操作
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ ES 执行          │ ← 复用现有 httpOperation 接口
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ 结果总结（可选） │ ← 结果 < 5KB 时传给 LLM 做自然语言总结
└─────────────────┘
  │
  ▼
┌─────────────────┐
│ 保存历史         │ ← 异步写入 .es-tool-ai-history
└─────────────────┘
```

### 成本控制：本地预筛选 + 两级上下文

如果每次请求都把全量索引丢给 LLM，token 开销会随集群规模线性增长。

**优化策略**：

1. **索引目录缓存**：后端启动时拉取 `_cat/indices` + `_mapping`，内存中缓存轻量元数据 `{name, alias, docCount, fieldSummary}`
2. **本地关键词匹配**：用户输入后，先在后端做零成本的 keyword 匹配（索引名、别名、字段名、知识库描述），只选出 Top-5 最相关的索引传给 LLM
3. **Token 估算**：200 个索引的轻量目录约 500 tokens，预筛选后通常只发 300 tokens，gpt-4o-mini 单次成本约 $0.0001，可忽略

---

## 配置说明

### 后端 `application.yml`

```yaml
ai:
  llm:
    provider: openai          # openai / claude / custom
    api-key: ${AI_API_KEY:}   # 强烈建议通过环境变量注入
    base-url: https://api.openai.com/v1  # 可替换为代理或兼容 API
    model: gpt-4o-mini        # 默认模型
    timeout: 60               # 请求超时（秒）
    max-tokens: 4096          # 最大返回 token 数
```

**环境变量方式启动**：
```bash
export AI_API_KEY=sk-your-key
java -jar es-tool-start.jar
```

**中转站配置示例**：
```yaml
ai:
  llm:
    base-url: http://10.18.121.79:4000/v1
    model: deepseek-v3.2
```

### 知识库文件

`backend/data/esKnowledgeBase.json`

- 首次启动时自动创建空文件
- 支持通过前端「知识库管理」界面编辑
- 支持 AI 自动生成草稿 + 人工审核后保存

---

## 安全设计

### 1. 只读优先

AI 默认只生成 `search` / `count` / `aggregate` 查询。执行前通过字符串校验拒绝包含 `"delete"`、`"update"`、`"index"` 等关键词的 DSL。

### 2. API Key 安全

- 支持通过 `${AI_API_KEY}` 环境变量注入
- 不记录到日志，不返回给前端

### 3. 超时与降级

- LLM 调用设置 60 秒超时
- 前端 AI 接口设置 5 分钟超时（`axios` 拦截器自动识别 `/api/ai/` 路径）
- LLM 返回不可解析 JSON 时，展示原始响应并提示用户

### 4. 结果截断

ES 返回数据量 > 5KB 时，跳过 LLM 总结，直接展示原始 JSON，避免过多 token 消耗。

---

## 使用示例

### 示例一：基础查询

> **用户**：查询 order 索引中 status=2 且 createTime 在 2024-04-01 到 2024-04-30 之间的订单
>
> **AI 输出**：
> ```json
> {
>   "dsl": {
>     "query": {
>       "bool": {
>         "must": [
>           { "term": { "status": "2" } },
>           { "range": { "createTime": { "gte": "2024-04-01", "lte": "2024-04-30" } } }
>         ]
>       }
>     }
>   },
>   "indexPattern": "order",
>   "explanation": "查询 order 索引中 status 为 2 且创建时间在 2024 年 4 月内的订单"
> }
> ```

### 示例二：聚合统计

> **用户**：按 province 字段聚合统计用户数量
>
> **AI 输出**：
> ```json
> {
>   "dsl": {
>     "aggs": {
>       "by_province": {
>         "terms": { "field": "province" }
>       }
>     },
>     "size": 0
>   },
>   "indexPattern": "user",
>   "explanation": "按省份分组统计用户数量"
> }
> ```

### 示例三：多轮对话修正

> **用户**：查询昨天的新闻
> **AI**：查询 `news-*`，返回 56 条
>
> **用户**：只看标题包含"科技"的
> **AI**：在前次 DSL 基础上追加 `must: { match: { title: "科技" } }`，返回 12 条

---

## 总结

ES Tool AI 智能助手的核心价值是**降低 Elasticsearch 的使用门槛**。通过自然语言交互，让不熟悉 DSL 语法的业务人员也能快速查询数据；同时通过知识库和历史沉淀，让 AI 越来越"懂"你的业务场景。

未来可扩展方向：
- 接入本地模型（Ollama），实现完全私有化部署
- 支持多 Agent 协作（查询 Agent + 分析 Agent + 可视化 Agent）
- 根据历史查询自动生成 Dashboard 推荐
