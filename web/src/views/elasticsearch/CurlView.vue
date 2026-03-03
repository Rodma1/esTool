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
            placeholder="输入 API 端点，如：/_cluster/health"
            class="endpoint-input"
            clearable
        >
          <template slot="prepend">
            {{ baseUrl }}
          </template>
        </el-input>

        <el-button type="primary" @click="executeRequest" :loading="isLoading" class="send-btn">
          发送
        </el-button>
      </div>
    </div>

    <!-- 请求体区域 (POST/PUT/PATCH) -->
    <div class="request-body-section" v-if="showRequestBody">
      <div class="section-header">
        <span class="section-title">请求体</span>
        <el-radio-group v-model="bodyType" size="small">
          <el-radio-button label="json">JSON</el-radio-button>
          <el-radio-button label="text">文本</el-radio-button>
        </el-radio-group>
      </div>
      <el-input
          type="textarea"
          v-model="requestForm.body"
          :rows="8"
          placeholder='请输入 JSON 格式的请求体'
          class="body-textarea"
      ></el-input>
    </div>

    <!-- 响应结果区域 -->
    <div class="response-section">
      <div class="section-header">
        <span class="section-title">响应结果</span>
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
      <el-empty description="点击发送按钮执行请求" v-if="!responseData && !responseText"/>
    </div>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer';

export default {
  components: {
    JsonViewer,
  },
  props: {
    connectParam: Object,
  },
  data() {
    return {
      requestForm: {
        method: 'GET',
        endpoint: '',
        body: '',
      },
      isLoading: false,
      responseData: null,
      responseText: '',
      rawResponse: '',
      responseStatus: '',
      responseTime: '',
      bodyType: 'json',
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
    },
    responseTimeTag() {
      if (!this.responseTime) return 'info';
      const time = parseInt(this.responseTime);
      if (time < 100) return 'success';
      if (time < 500) return 'warning';
      return 'danger';
    }
  },
  methods: {
    getParams(operationType) {
      const params = {
        ...this.connectParam,
        operationCategory: this.operationCategory,
        operationType: operationType,
        method: this.requestForm.method,
        endpoint: this.requestForm.endpoint,
        body: this.requestForm.body,
      };
      return params;
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

          // 尝试解析 JSON
          try {
            if (typeof result === 'string') {
              this.responseData = JSON.parse(result);
            } else {
              this.responseData = result;
            }
            this.responseText = JSON.stringify(this.responseData, null, 2);
          } catch (e) {
            // 不是 JSON 格式，按文本处理
            this.responseText = result;
            this.rawResponse = result;
          }

          this.rawResponse = this.rawResponse || JSON.stringify(result, null, 2);
          this.responseStatus = response.data.code || '200';

          this.$message({
            message: '请求成功',
            type: 'success'
          });
        } else {
          this.$message.error(response.data.message || '请求失败');
        }
      } catch (error) {
        console.log(error);
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

      const el = document.createElement('textarea');
      el.value = content;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
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

    resetForm() {
      this.requestForm = {
        method: 'GET',
        endpoint: '',
        body: '',
      };
      this.responseData = null;
      this.responseText = '';
      this.rawResponse = '';
      this.responseStatus = '';
      this.responseTime = '';
    }
  },
}
</script>

<style scoped>.curl-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
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
</style>