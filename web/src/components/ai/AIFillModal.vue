<template>
  <el-dialog
    title="AI 智能查询"
    :visible.sync="visible"
    width="700px"
    :close-on-click-modal="false"
    custom-class="ai-fill-modal"
  >
    <div class="ai-fill-content">
      <div class="ai-input-section">
        <el-input
          v-model="userInput"
          type="textarea"
          :rows="3"
          placeholder="描述你想查询的内容，例如：查询最近 7 天 status=active 的订单数据"
          resize="none"
        ></el-input>
        <div class="ai-input-actions">
          <el-button size="small" @click="visible = false">取消</el-button>
          <el-button size="small" type="primary" icon="el-icon-magic-stick" :loading="isLoading" @click="generateQuery">
            生成查询
          </el-button>
        </div>
      </div>

      <div v-if="result" class="ai-result-section">
        <div class="result-block">
          <div class="result-label">索引</div>
          <el-tag type="primary" size="small">{{ result.indexPattern || '-' }}</el-tag>
        </div>

        <div class="result-block">
          <div class="result-label">说明</div>
          <div class="result-text">{{ result.explanation || '-' }}</div>
        </div>

        <div class="result-block">
          <div class="result-label">生成的 DSL</div>
          <pre class="result-code">{{ result.dsl }}</pre>
        </div>

        <div v-if="executionResult" class="result-block">
          <div class="result-label">执行结果</div>
          <json-viewer
            :value="executionResult"
            :expand-depth="2"
            copyable
            boxed
          ></json-viewer>
        </div>

        <div class="result-actions">
          <el-button size="small" icon="el-icon-document-copy" @click="copyDsl">复制 DSL</el-button>
          <el-button size="small" type="primary" icon="el-icon-video-play" :loading="isExecuting" @click="executeQuery">执行查询</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import JsonViewer from 'vue-json-viewer';

export default {
  name: 'AIFillModal',
  components: { JsonViewer },
  props: {
    connectParam: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      visible: false,
      userInput: '',
      isLoading: false,
      isExecuting: false,
      result: null,
      executionResult: null
    };
  },
  methods: {
    open() {
      this.visible = true;
      this.userInput = '';
      this.result = null;
      this.executionResult = null;
    },
    async generateQuery() {
      const input = this.userInput.trim();
      if (!input) {
        this.$message.warning('请输入查询描述');
        return;
      }
      this.isLoading = true;
      this.result = null;
      this.executionResult = null;

      try {
        const response = await this.axios.post('/api/ai/generate-query', {
          userInput: input,
          connectParam: this.connectParam
        });

        if (response.data && response.data.code === 200) {
          this.result = response.data.data;
        } else {
          this.$message.error(response.data.message || '生成失败');
        }
      } catch (error) {
        this.$message.error('生成异常：' + (error.message || '网络错误'));
      } finally {
        this.isLoading = false;
      }
    },
    async executeQuery() {
      if (!this.result || !this.result.dsl) {
        this.$message.warning('没有可执行的 DSL');
        return;
      }
      this.isExecuting = true;
      try {
        const response = await this.axios.post('/api/ai/execute-query', {
          userInput: this.userInput,
          connectParam: this.connectParam
        });

        if (response.data && response.data.code === 200) {
          const data = response.data.data;
          this.executionResult = data.executionResult;
          this.$message.success('查询执行成功');
        } else {
          this.$message.error(response.data.message || '执行失败');
        }
      } catch (error) {
        this.$message.error('执行异常：' + (error.message || '网络错误'));
      } finally {
        this.isExecuting = false;
      }
    },
    copyDsl() {
      if (!this.result || !this.result.dsl) return;
      const el = document.createElement('textarea');
      el.value = this.result.dsl;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      this.$message.success('已复制 DSL');
    }
  }
};
</script>

<style lang="scss" scoped>
.ai-fill-modal ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

.ai-fill-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.ai-result-section {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.result-block {
  margin-bottom: 14px;
}

.result-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.result-text {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.6;
}

.result-code {
  margin: 0;
  padding: 12px;
  background: #1e1e2d;
  color: #abb2bf;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  border-radius: 6px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}
</style>
