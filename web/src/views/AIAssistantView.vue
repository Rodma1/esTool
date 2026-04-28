<template>
  <div class="page-wrapper ai-assistant-page">
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title"><i class="el-icon-magic-stick"></i> AI 智能助手</h3>
        <el-radio-group v-model="currentMode" size="small">
          <el-radio-button label="chat">ES 问答</el-radio-button>
          <el-radio-button label="query">智能查询</el-radio-button>
        </el-radio-group>
      </div>
      <div class="app-toolbar-right">
        <el-button size="small" icon="el-icon-notebook-2" @click="openKbDialog">知识库</el-button>
        <el-button size="small" icon="el-icon-brush" @click="clearChat">清空对话</el-button>
      </div>
    </div>

    <!-- 知识库管理弹窗 -->
    <el-dialog title="知识库管理" :visible.sync="kbDialogVisible" width="800px" :close-on-click-modal="false">
      <div class="kb-actions">
        <el-button size="small" icon="el-icon-refresh" @click="loadKnowledgeBase">刷新</el-button>
        <el-button size="small" type="primary" icon="el-icon-magic-stick" :loading="kbGenerating" @click="generateKbDraft">AI 生成草稿</el-button>
        <el-button size="small" type="success" icon="el-icon-check" :loading="kbSaving" @click="saveKnowledgeBase">保存</el-button>
      </div>
      <el-input
        v-model="kbJson"
        type="textarea"
        :rows="20"
        placeholder="知识库 JSON，格式：[{ businessName, indices, description, fields }]"
        class="kb-textarea"
      ></el-input>
    </el-dialog>

    <div class="chat-container app-card">
      <!-- 消息列表 -->
      <div class="chat-messages" ref="messageContainer">
        <div v-if="messages.length === 0" class="chat-empty">
          <el-empty description="开始你的 AI 对话">
            <div class="empty-tips">
              <p>ES 问答：询问 Elasticsearch 概念、语法、调优建议</p>
              <p>智能查询：用自然语言描述需求，AI 自动生成并执行查询</p>
            </div>
            <div v-if="hotQueries.length > 0" class="hot-queries">
              <div class="hot-queries-title"><i class="el-icon-s-flag"></i> 大家都在问</div>
              <div class="hot-query-list">
                <el-tag
                  v-for="(q, idx) in hotQueries"
                  :key="idx"
                  size="small"
                  type="info"
                  class="hot-query-tag"
                  @click="useHotQuery(q.query)"
                >
                  {{ q.query }}
                </el-tag>
              </div>
            </div>
          </el-empty>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-item', msg.role === 'user' ? 'message-user' : 'message-assistant']"
        >
          <div class="message-avatar">
            <i :class="msg.role === 'user' ? 'el-icon-user-solid' : 'el-icon-magic-stick'"></i>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(msg.content)"></div>
            <div v-if="msg.dsl" class="message-dsl">
              <div class="dsl-header">
                <span>生成的 DSL</span>
                <el-button size="mini" type="text" @click="copyText(msg.dsl)">复制</el-button>
              </div>
              <pre class="dsl-code">{{ msg.dsl }}</pre>
            </div>
            <div v-if="msg.executionResult" class="message-result">
              <div class="result-header">执行结果</div>
              <json-viewer
                :value="msg.executionResult"
                :expand-depth="2"
                copyable
                boxed
              ></json-viewer>
            </div>
          </div>
        </div>

        <!-- AI 正在思考 -->
        <div v-if="isLoading" class="message-item message-assistant">
          <div class="message-avatar">
            <i class="el-icon-magic-stick"></i>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
              <span class="typing-text">AI 正在思考...</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <el-input
          v-model="userInput"
          type="textarea"
          :rows="3"
          :placeholder="inputPlaceholder"
          resize="none"
          @keydown.enter.native.prevent="handleEnter"
        ></el-input>
        <div class="input-actions">
          <el-button type="primary" icon="el-icon-s-promotion" :loading="isLoading" @click="sendMessage">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer';

export default {
  name: 'AIAssistantView',
  components: { JsonViewer },
  data() {
    return {
      currentMode: 'chat',
      userInput: '',
      isLoading: false,
      messages: [],
      hotQueries: [],
      kbDialogVisible: false,
      kbJson: '',
      kbGenerating: false,
      kbSaving: false
    };
  },
  computed: {
    inputPlaceholder() {
      return this.currentMode === 'chat'
        ? '输入 ES 相关问题，例如：term 查询和 match 查询有什么区别？'
        : '描述你想查询的内容，例如：查询 news 索引最近 7 天发布的数据';
    },
    activeConnection() {
      return this.$store.state.activeConnection;
    }
  },
  watch: {
    messages() {
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    }
  },
  mounted() {
    this.loadHotQueries();
  },
  methods: {
    handleEnter(e) {
      if (!e.shiftKey) {
        this.sendMessage();
      }
    },
    async sendMessage() {
      const input = this.userInput.trim();
      if (!input || this.isLoading) return;

      this.messages.push({
        role: 'user',
        content: input
      });
      this.userInput = '';
      this.isLoading = true;

      try {
        const requestBody = {
          userInput: input,
          messages: this.messages
            .filter(m => m.role === 'user' || m.role === 'assistant')
            .map(m => ({ role: m.role, content: m.content })),
          connectParam: this.activeConnection
        };

        const endpoint = this.currentMode === 'chat' ? '/api/ai/chat' : '/api/ai/execute-query';
        const response = await this.axios.post(endpoint, requestBody);

        if (response.data && response.data.code === 200) {
          const data = response.data.data;
          this.messages.push({
            role: 'assistant',
            content: data.reply || data.explanation || 'AI 已生成查询并执行',
            dsl: data.dsl,
            executionResult: data.executionResult
          });
        } else {
          this.messages.push({
            role: 'assistant',
            content: '请求失败：' + (response.data.message || '未知错误')
          });
        }
      } catch (error) {
        this.messages.push({
          role: 'assistant',
          content: '请求异常：' + (error.message || '网络错误')
        });
      } finally {
        this.isLoading = false;
      }
    },
    clearChat() {
      this.messages = [];
    },
    scrollToBottom() {
      const container = this.$refs.messageContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },
    formatMessage(text) {
      if (!text) return '';
      // 简单处理：将换行转为 <br>，代码块用 <pre> 包裹
      let formatted = text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      // 处理 ```code``` 块
      formatted = formatted.replace(/```([\s\S]*?)```/g, '<pre class="code-block">$1</pre>');
      // 处理行内 `code`
      formatted = formatted.replace(/`([^`]+)`/g, '<code>$1</code>');
      // 普通换行
      formatted = formatted.replace(/\n/g, '<br>');
      return formatted;
    },
    copyText(text) {
      if (!text) return;
      const el = document.createElement('textarea');
      el.value = text;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      this.$message.success('已复制到剪贴板');
    },
    async loadHotQueries() {
      if (!this.activeConnection) return;
      try {
        const response = await this.axios.post('/api/ai/hot-queries', this.activeConnection);
        if (response.data && response.data.code === 200) {
          this.hotQueries = response.data.data || [];
        }
      } catch (error) {
        // 静默失败，不影响主功能
        console.log('加载热查询失败', error);
      }
    },
    useHotQuery(query) {
      this.userInput = query;
      this.sendMessage();
    },
    openKbDialog() {
      this.kbDialogVisible = true;
      this.loadKnowledgeBase();
    },
    async loadKnowledgeBase() {
      try {
        const response = await this.axios.get('/api/ai/knowledge-base');
        if (response.data && response.data.code === 200) {
          this.kbJson = JSON.stringify(response.data.data || [], null, 2);
        }
      } catch (error) {
        this.$message.error('加载知识库失败：' + (error.message || '网络错误'));
      }
    },
    async generateKbDraft() {
      if (!this.activeConnection) {
        this.$message.warning('请先连接 ES');
        return;
      }
      this.kbGenerating = true;
      try {
        const response = await this.axios.post('/api/ai/generate-kb-draft', this.activeConnection);
        if (response.data && response.data.code === 200) {
          this.kbJson = JSON.stringify(response.data.data || [], null, 2);
          this.$message.success('知识库草稿已生成，请检查并保存');
        } else {
          this.$message.error(response.data.message || '生成失败');
        }
      } catch (error) {
        this.$message.error('生成异常：' + (error.message || '网络错误'));
      } finally {
        this.kbGenerating = false;
      }
    },
    async saveKnowledgeBase() {
      let items;
      try {
        items = JSON.parse(this.kbJson);
        if (!Array.isArray(items)) {
          this.$message.warning('格式错误：必须是 JSON 数组');
          return;
        }
      } catch (e) {
        this.$message.error('JSON 格式错误：' + e.message);
        return;
      }
      this.kbSaving = true;
      try {
        const response = await this.axios.post('/api/ai/knowledge-base', items);
        if (response.data && response.data.code === 200) {
          this.$message.success('知识库已保存');
        } else {
          this.$message.error(response.data.message || '保存失败');
        }
      } catch (error) {
        this.$message.error('保存异常：' + (error.message || '网络错误'));
      } finally {
        this.kbSaving = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.ai-assistant-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 48px);
  padding: 16px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.chat-empty {
  padding-top: 60px;
}

.empty-tips {
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.8;
  margin-top: 8px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary-light);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}

.message-user .message-avatar {
  background: #e6f7ff;
  color: #1890ff;
}

.message-content {
  flex: 1;
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px 16px;
  max-width: 80%;
}

.message-user .message-content {
  background: #e6f7ff;
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  word-break: break-word;

  ::v-deep .code-block {
    background: #1e1e2d;
    color: #abb2bf;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 8px 0;
    font-family: 'Courier New', Courier, monospace;
    font-size: 13px;
    line-height: 1.5;
  }

  ::v-deep code {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Courier New', Courier, monospace;
    font-size: 13px;
    color: #d63384;
  }
}

.message-dsl {
  margin-top: 12px;
  border: 1px solid var(--card-border);
  border-radius: 6px;
  overflow: hidden;
}

.dsl-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f3f6f9;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.dsl-code {
  margin: 0;
  padding: 12px;
  background: #1e1e2d;
  color: #abb2bf;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-result {
  margin-top: 12px;
}

.result-header {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0;

  span:not(.typing-text) {
    width: 8px;
    height: 8px;
    background: var(--primary);
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }

  .typing-text {
    font-size: 13px;
    color: var(--text-secondary);
    margin-left: 8px;
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.chat-input-area {
  border-top: 1px solid var(--card-border);
  padding: 12px 16px;
  background: #fff;

  ::v-deep .el-textarea__inner {
    border-radius: 8px;
    resize: none;
  }
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.hot-queries {
  margin-top: 20px;
  text-align: center;
}

.hot-queries-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;

  i {
    color: var(--primary);
    margin-right: 4px;
  }
}

.hot-query-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  max-width: 500px;
  margin: 0 auto;
}

.hot-query-tag {
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: var(--primary);
    border-color: var(--primary);
    background: #f5f9ff;
  }
}

.kb-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.kb-textarea {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;

  ::v-deep .el-textarea__inner {
    background: #fafafa;
  }
}
</style>
