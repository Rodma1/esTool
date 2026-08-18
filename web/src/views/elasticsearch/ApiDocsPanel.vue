<template>
  <div class="api-docs-section">
    <el-collapse v-model="activeDocs">
      <el-collapse-item title="📚 ES API 使用文档" name="docs">
        <div class="docs-content">
          <el-tabs type="border-card">
            <el-tab-pane label="常用 API">
              <div class="api-list">
                <el-table :data="commonApis" stripe style="width: 100%">
                  <el-table-column prop="method" label="方法" width="80">
                    <template slot-scope="scope">
                      <el-tag :type="getMethodType(scope.row.method)" size="small">{{ scope.row.method }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="endpoint" label="端点" width="300"></el-table-column>
                  <el-table-column prop="description" label="说明"></el-table-column>
                  <el-table-column label="操作" width="100">
                    <template slot-scope="scope">
                      <el-button size="mini" type="primary" @click="useApi(scope.row)">使用</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>

            <el-tab-pane label="查询示例">
              <div class="example-section">
                <h4>1. 匹配所有文档</h4>
                <pre class="dark-code-block">{
  "query": {
    "match_all": {}
  }
}</pre>

                <h4>2. 条件查询</h4>
                <pre class="dark-code-block">{
  "query": {
    "match": {
      "title": "关键词"
    },
    "range": {
      "createTime": {
        "gte": "now-1d",
        "lte": "now"
      }
    }
  }
}</pre>

                <h4>3. 聚合查询</h4>
                <pre class="dark-code-block">{
  "size": 0,
  "aggs": {
    "group_by_status": {
      "terms": {
        "field": "status.keyword"
      }
    }
  }
}</pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="索引操作">
              <div class="example-section">
                <h4>1. 创建索引</h4>
                <pre class="dark-code-block">PUT /my_index
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "title": { "type": "text" },
      "createTime": { "type": "date" }
    }
  }
}</pre>

                <h4>2. 查看映射</h4>
                <pre class="dark-code-block">GET /my_index/_mapping</pre>

                <h4>3. 删除索引</h4>
                <pre class="dark-code-block">DELETE /my_index</pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="其他操作">
              <div class="example-section">
                <h4>1. 集群健康</h4>
                <pre class="dark-code-block">GET /_cluster/health</pre>

                <h4>2. 节点信息</h4>
                <pre class="dark-code-block">GET /_nodes</pre>

                <h4>3. 索引列表</h4>
                <pre class="dark-code-block">GET /_cat/indices?v&format=json</pre>

                <h4>4. 批量操作</h4>
                <pre class="dark-code-block">POST /_bulk
{ "index": { "_index": "test", "_id": "1" } }
{ "title": "文档 1" }
{ "index": { "_index": "test", "_id": "2" } }
{ "title": "文档 2" }</pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
export default {
  name: 'ApiDocsPanel',
  data() {
    return {
      activeDocs: [],
      commonApis: [
        { method: 'GET', endpoint: '/_cluster/health', description: '查看集群健康状态' },
        { method: 'GET', endpoint: '/_cat/indices?v&format=json', description: '查看所有索引' },
        { method: 'GET', endpoint: '/{index}/_search', description: '搜索文档' },
        { method: 'GET', endpoint: '/{index}/_mapping', description: '查看索引映射' },
        { method: 'POST', endpoint: '/{index}/_search', description: '复杂查询' },
        { method: 'PUT', endpoint: '/{index}', description: '创建索引' },
        { method: 'PUT', endpoint: '/{index}/_mapping', description: '更新映射' },
        { method: 'DELETE', endpoint: '/{index}', description: '删除索引' },
        { method: 'POST', endpoint: '/{index}/_doc', description: '创建文档' },
        { method: 'POST', endpoint: '/{index}/_delete_by_query', description: '按条件删除' },
        { method: 'POST', endpoint: '/_aliases', description: '别名操作' },
        { method: 'GET', endpoint: '/_nodes', description: '查看节点信息' },
      ]
    }
  },
  methods: {
    getMethodType(method) {
      const typeMap = { GET: 'success', POST: 'warning', PUT: 'primary', DELETE: 'danger', HEAD: 'info', PATCH: '' };
      return typeMap[method] || '';
    },
    useApi(api) {
      this.$emit('use-api', api);
    }
  }
}
</script>

<style lang="scss" scoped>
.api-docs-section {
  margin-top: 30px;
  border-top: 1px solid var(--card-border);
  padding-top: 20px;
}

.docs-content {
  padding: 10px;
}

.api-list {
  max-height: 500px;
  overflow-y: auto;
}

.example-section h4 {
  margin: 20px 0 10px;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}

.example-section h4:first-child {
  margin-top: 10px;
}

.dark-code-block {
  background: #1e1e2d;
  color: #abb2bf;
  padding: 15px;
  border-radius: var(--radius-sm);
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  overflow-x: auto;
  border-left: 3px solid var(--primary);
}
</style>
