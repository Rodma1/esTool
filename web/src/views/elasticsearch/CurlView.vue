<template>
  <div class="curl-container">
    <!-- 请求配置区域 -->
    <div class="request-section">
      <div class="request-method-bar">
        <el-select v-model="requestForm.method" placeholder="选择方法" class="method-select">
          <el-option label="GET" value="GET"></el-option>
          <el-option label="POST" value="POST"></el-option>
          <el-option label="PUT" value="PUT"></el-option>
          <el-option label="DELETE" value="DELETE"></el-option>
          <el-option label="HEAD" value="HEAD"></el-option>
          <el-option label="PATCH" value="PATCH"></el-option>
        </el-select>

        <el-input
          v-model="requestForm.endpoint"
          placeholder="输入 API 端点，如：/_cat/indices?v"
          class="endpoint-input"
          clearable
        >
          <template slot="prepend">{{ baseUrl }}</template>
        </el-input>

        <el-button type="primary" icon="el-icon-s-promotion" @click="executeRequest" :loading="isLoading" class="send-btn">
          发送
        </el-button>
      </div>
    </div>

    <!-- 请求体区域 -->
    <div v-if="showRequestBody" class="request-body-section">
      <div class="section-header">
        <span class="section-title"><i class="el-icon-document"></i> 请求体</span>
        <div class="header-actions">
          <el-radio-group v-model="bodyType" size="small">
            <el-radio-button label="json">JSON</el-radio-button>
            <el-radio-button label="text">文本</el-radio-button>
          </el-radio-group>
          <el-button size="mini" @click="formatJson" v-if="bodyType === 'json'" title="格式化 JSON">
            <i class="el-icon-rank"></i> 格式化
          </el-button>
          <el-button size="mini" type="success" @click="compressJson" v-if="bodyType === 'json'" title="压缩 JSON">
            <i class="el-icon-zoom-in"></i> 压缩
          </el-button>
          <el-button size="mini" type="warning" @click="copyBody" title="复制请求体">
            <i class="el-icon-document-copy"></i> 复制
          </el-button>
          <el-button size="mini" type="danger" @click="clearBody" title="清空">
            <i class="el-icon-delete"></i> 清空
          </el-button>
          <el-button size="mini" type="primary" icon="el-icon-magic-stick" @click="aiGenerateBody">AI 生成</el-button>
        </div>
      </div>
      <el-input
        type="textarea"
        v-model="requestForm.body"
        :rows="12"
        :placeholder="getPlaceholder()"
        class="body-textarea"
      ></el-input>
    </div>

    <!-- AI 生成弹窗 -->
    <el-dialog title="AI 生成请求体" :visible.sync="aiDialogVisible" width="600px">
      <el-input
        v-model="aiInput"
        type="textarea"
        :rows="4"
        placeholder="描述你需要的请求体，例如：按状态聚合统计文档数量"
        resize="none"
      ></el-input>
      <div slot="footer">
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiLoading" @click="confirmAiGenerate">生成</el-button>
      </div>
    </el-dialog>

    <!-- 响应结果区域 -->
    <div class="response-section">
      <div class="section-header">
        <span class="section-title"><i class="el-icon-message"></i> 响应结果</span>
        <div class="response-actions">
          <el-tag :type="responseStatusTag" size="small" v-if="responseStatus">
            状态码：{{ responseStatus }}
          </el-tag>
          <el-tag type="info" size="small" v-if="responseTime">
            耗时：{{ responseTime }}ms
          </el-tag>
          <el-button size="small" icon="el-icon-document-copy" @click="copyResponse">复制</el-button>
          <el-button size="small" icon="el-icon-download" @click="downloadResponse">下载</el-button>
          <el-radio-group v-model="responseViewType" size="small">
            <el-radio-button label="json">JSON</el-radio-button>
            <el-radio-button label="text">文本</el-radio-button>
            <el-radio-button label="raw">原始</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- JSON 视图 -->
      <json-viewer
        v-if="responseViewType === 'json' && responseData"
        :value="responseData"
        :expand-depth="5"
        copyable
        boxed
        style="background: #f5f7fa; padding: 10px; border-radius: 4px;"
      ></json-viewer>

      <!-- 文本视图 -->
      <el-input
        v-else-if="responseViewType === 'text'"
        type="textarea"
        :value="responseText"
        :rows="15"
        readonly
        class="response-textarea"
      ></el-input>

      <!-- 原始视图 -->
      <pre v-else-if="responseViewType === 'raw'" class="raw-response">{{ rawResponse }}</pre>

      <!-- 空状态 -->
      <el-empty description="点击发送按钮执行请求" v-if="!responseData && !responseText"></el-empty>
    </div>

    <!-- API 文档组件 -->
    <ApiDocsPanel @use-api="handleUseApi" />
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer';
import ApiDocsPanel from './ApiDocsPanel.vue';

export default {
  components: { JsonViewer, ApiDocsPanel },
  props: { connectParam: Object },
  data() {
    return {
      requestForm: { method: 'GET', endpoint: '', body: '' },
      isLoading: false,
      responseData: null,
      responseText: '',
      rawResponse: '',
      responseStatus: '',
      responseTime: '',
      bodyType: 'json',
      aiDialogVisible: false,
      aiInput: '',
      aiLoading: false,
      responseViewType: 'json',
      operationCategory: "HTTP",
    }
  },
  computed: {
    baseUrl() {
      const scheme = this.connectParam?.scheme || 'http';
      const hostName = this.connectParam?.hostName || 'localhost';
      const port = this.connectParam?.port || '9200';
      return `${scheme}://${hostName}:${port}`;
    },
    showRequestBody() {
      return ['POST', 'PUT', 'PATCH'].includes(this.requestForm.method);
    },
    responseStatusTag() {
      if (!this.responseStatus) return 'info';
      const status = parseInt(this.responseStatus);
      if (status >= 200 && status < 300) return 'success';
      if (status >= 400 && status < 500) return 'warning';
      if (status >= 500) return 'danger';
      return 'info';
    }
  },
  methods: {
    handleUseApi(api) {
      this.requestForm.method = api.method;
      this.requestForm.endpoint = api.endpoint.replace('{index}', 'your_index');
      if (api.method === 'POST' || api.method === 'PUT') {
        if (api.description.includes('搜索') || api.description.includes('查询')) {
          this.requestForm.body = JSON.stringify({ query: { match_all: {} } }, null, 2);
        } else if (api.description.includes('创建索引')) {
          this.requestForm.body = JSON.stringify({
            settings: { number_of_shards: 3, number_of_replicas: 1 },
            mappings: { properties: { title: { type: 'text' }, createTime: { type: 'date' } } }
          }, null, 2);
        }
      }
      this.$message.success('已加载 API 模板，请根据实际情况修改');
    },
    getPlaceholder() {
      return this.bodyType === 'json'
        ? `请输入 JSON 格式的请求体，例如：\n{\n  "query": {\n    "match_all": {}\n  },\n  "size": 10\n}`
        : '请输入请求体内容';
    },
    formatJson() {
      try {
        if (!this.requestForm.body.trim()) {
          this.$message.warning('请先输入 JSON 内容');
          return;
        }
        const parsed = JSON.parse(this.requestForm.body);
        this.requestForm.body = JSON.stringify(parsed, null, 2);
        this.$message.success('JSON 格式化成功');
      } catch (e) {
        this.$message.error('JSON 格式错误：' + e.message);
      }
    },
    compressJson() {
      try {
        if (!this.requestForm.body.trim()) {
          this.$message.warning('请先输入 JSON 内容');
          return;
        }
        const parsed = JSON.parse(this.requestForm.body);
        this.requestForm.body = JSON.stringify(parsed);
        this.$message.success('JSON 压缩成功');
      } catch (e) {
        this.$message.error('JSON 格式错误：' + e.message);
      }
    },
    clearBody() {
      this.requestForm.body = '';
      this.$message.success('已清空请求体');
    },
    copyBody() {
      if (!this.requestForm.body) {
        this.$message.warning('没有可复制的内容');
        return;
      }
      this.copyToClipboard(this.requestForm.body);
      this.$message.success('已复制到剪贴板');
    },
    getParams(operationType) {
      return {
        ...this.connectParam,
        operationCategory: this.operationCategory,
        operationType,
        method: this.requestForm.method,
        endpoint: this.requestForm.endpoint,
        body: this.requestForm.body,
      };
    },
    async executeRequest() {
      if (!this.requestForm.endpoint) {
        this.$message.warning('请输入 API 端点');
        return;
      }
      this.isLoading = true;
      this.responseData = null;
      this.responseText = '';
      this.rawResponse = '';
      this.responseStatus = '';
      this.responseTime = '';
      const startTime = Date.now();
      try {
        const params = this.getParams("EXECUTE");
        const response = await this.axios.post('/api/elasticsearch/httpOperation', params);
        const endTime = Date.now();
        this.responseTime = endTime - startTime;
        if (response.data && response.data.code === 200) {
          const result = response.data.data;
          try {
            this.responseData = typeof result === 'string' ? JSON.parse(result) : result;
            this.responseText = JSON.stringify(this.responseData, null, 2);
          } catch (e) {
            this.responseText = result;
            this.rawResponse = result;
          }
          this.rawResponse = this.rawResponse || JSON.stringify(result, null, 2);
          this.responseStatus = response.data.code || '200';
          this.$message({ message: '请求成功', type: 'success' });
        } else {
          this.$message.error(response.data.message || '请求失败');
        }
      } catch (error) {
        this.$message.error('请求失败：' + (error.message || '未知错误'));
        this.responseStatus = error.response?.status || 'ERROR';
      } finally {
        this.isLoading = false;
      }
    },
    copyResponse() {
      let content = '';
      if (this.responseViewType === 'json' && this.responseData) {
        content = JSON.stringify(this.responseData, null, 2);
      } else if (this.responseViewType === 'text') {
        content = this.responseText;
      } else {
        content = this.rawResponse;
      }
      this.copyToClipboard(content);
      this.$message.success('已复制到剪贴板');
    },
    downloadResponse() {
      let content = '';
      if (this.responseViewType === 'json' && this.responseData) {
        content = JSON.stringify(this.responseData, null, 2);
      } else if (this.responseViewType === 'text') {
        content = this.responseText;
      } else {
        content = this.rawResponse;
      }
      const blob = new Blob([content], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `es_response_${Date.now()}.json`;
      a.click();
      URL.revokeObjectURL(url);
      this.$message.success('已下载响应文件');
    },
    copyToClipboard(text) {
      const el = document.createElement('textarea');
      el.value = text;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
    },
    aiGenerateBody() {
      this.aiDialogVisible = true;
      this.aiInput = '';
    },
    async confirmAiGenerate() {
      const input = this.aiInput.trim();
      if (!input) {
        this.$message.warning('请输入描述');
        return;
      }
      this.aiLoading = true;
      try {
        const response = await this.axios.post('/api/ai/generate-query', {
          userInput: '请生成一个 Elasticsearch 请求体（JSON），用于：' + input,
          connectParam: this.connectParam
        });
        if (response.data && response.data.code === 200) {
          const data = response.data.data;
          if (data.dsl) {
            // 尝试解析 DSL 并格式化
            let dsl = data.dsl;
            try {
              const parsed = JSON.parse(dsl);
              dsl = JSON.stringify(parsed, null, 2);
            } catch (e) {
              // 保持原样
            }
            this.requestForm.body = dsl;
            this.$message.success('请求体已生成');
            this.aiDialogVisible = false;
          } else {
            this.$message.warning('AI 未生成有效请求体');
          }
        } else {
          this.$message.error(response.data.message || '生成失败');
        }
      } catch (error) {
        this.$message.error('生成异常：' + (error.message || '网络错误'));
      } finally {
        this.aiLoading = false;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.curl-container {
  padding: 4px;
}

.request-section {
  margin-bottom: 20px;
}

.request-method-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.method-select {
  width: 120px;
}

.endpoint-input {
  flex: 1;
}

.send-btn {
  width: 100px;
}

.request-body-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--card-border);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title {
  font-weight: bold;
  font-size: 14px;
  color: var(--text-primary);

  i {
    margin-right: 4px;
    color: var(--primary);
  }
}

.response-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.body-textarea,
.response-textarea {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
}

.response-section {
  margin-top: 20px;
}

.raw-response {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 600px;
  overflow-y: auto;
}

.body-textarea {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  background-color: #fafafa;
  border: 1px solid #dcdfe6;

  &:hover {
    border-color: #c0c4cc;
  }

  &:focus {
    border-color: #409EFF;
    background-color: #fff;
  }
}
</style>
