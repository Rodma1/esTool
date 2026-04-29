<template>
  <div class="page-wrapper">
    <div class="app-card">
      <!-- 连接配置区 -->
      <div class="connection-wrapper">
        <!-- 折叠状态：显示摘要 -->
        <div v-show="!isConnectionExpanded" class="connection-summary">
          <div class="summary-left">
            <i class="el-icon-link"></i>
            <span v-if="form.hostName" class="summary-text">
              <el-tag size="small" type="success" effect="plain">{{ form.scheme || 'http' }}</el-tag>
              <strong>{{ form.hostName }}:{{ form.port }}</strong>
              <el-tag v-if="form.version" size="small" type="info">v{{ form.version }}</el-tag>
              <el-tag v-if="versionInfo.number !== '-'" size="small" type="success">✓ 已连接</el-tag>
            </span>
            <span v-else class="summary-text text-muted">未配置 ES 连接，请先配置连接信息</span>
          </div>
          <div class="summary-right">
            <el-button type="text" icon="el-icon-edit" @click="isConnectionExpanded = true">编辑连接</el-button>
          </div>
        </div>

        <!-- 展开状态：完整表单 -->
        <div v-show="isConnectionExpanded" class="connection-section">
          <div class="section-header">
            <div class="section-title">
              <i class="el-icon-link"></i> ES 连接配置
            </div>
            <el-button type="text" icon="el-icon-arrow-up" @click="isConnectionExpanded = false">收起</el-button>
          </div>

          <!-- 已保存连接选择 -->
          <el-row :gutter="16" style="margin-bottom: 16px">
            <el-col :span="12">
              <el-form-item label="选择地址" label-width="80px">
                <el-select
                  v-model="selectHostName"
                  clearable
                  placeholder="选择已保存的连接"
                  style="width: 100%"
                  @change="selectConnectParam"
                >
                  <el-option
                    v-for="item in connectForm"
                    :key="item.hostName + ':' + item.port + ':' + item.version"
                    :label="item.hostName + ':' + item.port + ' (v' + item.version + ')'"
                    :value="item.hostName + ':' + item.port + ':' + item.version"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" style="text-align: right">
              <el-button type="primary" icon="el-icon-connection" :plain="true" @click="linkTest">
                连接测试
              </el-button>
            </el-col>
          </el-row>

          <!-- 连接表单 -->
          <el-form ref="form" :model="form" label-width="80px">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="协议">
                  <el-select v-model="form.scheme" clearable placeholder="请选择协议" style="width: 100%">
                    <el-option label="http" value="http"></el-option>
                    <el-option label="https" value="https"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="IP地址">
                  <el-input v-model="form.hostName" placeholder="请输入 IP 地址"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="端口">
                  <el-input v-model="form.port" placeholder="请输入端口"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="用户名">
                  <el-input v-model="form.userName" placeholder="请输入用户名"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="密码">
                  <el-input v-model="form.password" placeholder="请输入密码" show-password></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="版本">
                  <el-input v-model="form.version" placeholder="如: 7, 8, 9"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <!-- 版本信息状态栏 -->
          <div v-if="versionInfo.number !== '-'" class="status-bar">
            <div>
              <span class="label">ES 版本:</span>
              <span class="value">{{ versionInfo.number }}</span>
            </div>
            <div>
              <span class="label">安装类型:</span>
              <span class="value">{{ versionInfo.buildType }}</span>
            </div>
            <div>
              <span class="label">Lucene 版本:</span>
              <span class="value">{{ versionInfo.luceneVersion }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能 Tab -->
      <el-tabs v-model="activeName" class="app-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="索引操作" name="first">
          <ElasticIndicesView :connectParam="form" />
        </el-tab-pane>
        <el-tab-pane label="文档操作" name="second">
          <DocumentView :connectParam="form" />
        </el-tab-pane>
        <el-tab-pane label="别名操作" name="third">
          <AliasesView :connectParam="form" />
        </el-tab-pane>
        <el-tab-pane label="模板列表" name="fourth">
          <TemplateView :connectParam="form" />
        </el-tab-pane>
        <el-tab-pane label="任务列表" name="fifty">
          <TaskView :connectParam="form" />
        </el-tab-pane>
        <el-tab-pane label="分析器" name="sixth">
          <AnalyzeView :connectParam="form" />
        </el-tab-pane>
        <el-tab-pane label="HTTP 调试" name="seventh">
          <CurlView :connectParam="form" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import ElasticIndicesView from "./elasticsearch/ElasticIndicesView.vue";
import DocumentView from "./elasticsearch/DocumentView.vue"
import AliasesView from "./elasticsearch/AliasesView.vue"
import TemplateView from "./elasticsearch/TemplateView.vue"
import TaskView from "@/views/elasticsearch/TaskView.vue";
import AnalyzeView from "@/views/elasticsearch/AnalyzeView.vue";
import CurlView from "@/views/elasticsearch/CurlView.vue";

export default {
  components: { CurlView, TaskView, ElasticIndicesView, DocumentView, AliasesView, TemplateView, AnalyzeView },
  data() {
    return {
      form: {
        scheme: '',
        hostName: '',
        userName: '',
        password: '',
        port: '',
        version: '',
        operationCategory: 'INFO',
      },
      connectForm: [],
      versionInfo: {
        "number": "-",
        "buildType": "-",
        "luceneVersion": "-"
      },
      activeName: '',
      selectHostName: '',
      isConnectionExpanded: true,
    }
  },
  computed: {
    activeConnection() {
      return this.$store.state.activeConnection;
    }
  },
  watch: {
    activeConnection: {
      immediate: true,
      handler(conn) {
        if (conn) {
          this.form = { ...this.form, ...conn };
          this.selectHostName = conn.hostName + ':' + conn.port + ':' + conn.version;
          if (conn.versionNumber && conn.versionNumber !== '-') {
            this.versionInfo = {
              number: conn.versionNumber,
              buildType: conn.buildType || '-',
              luceneVersion: conn.luceneVersion || '-'
            };
          }
          this.isConnectionExpanded = false;
        }
      }
    }
  },
  methods: {
    async linkTest() {
      try {
        const params = { ...this.form, operationCategory: "INFO" };
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        if (response.data.code !== 200) {
          this.$message({ message: response.data.message, type: 'error' });
          return;
        }
        this.$message({ message: '连接成功', type: 'success' });
        this.versionInfo = response.data.data;
        this.activeName = '';
        this.$store.dispatch('setActiveConnection', {
          ...this.form,
          versionNumber: this.versionInfo.number,
          buildType: this.versionInfo.buildType,
          luceneVersion: this.versionInfo.luceneVersion
        });
        this.isConnectionExpanded = false;
      } catch (error) {
        this.$message.error('连接失败');
      }
    },
    handleTabClick(tab) {
      this.activeName = tab.name;
    },
    async getConnectForm() {
      try {
        const response = await this.axios.get('/api/elasticsearch/connectParam');
        this.connectForm = response.data.data || [];
        this.$store.dispatch('setConnections', this.connectForm);
      } catch (error) {
        console.error(error);
      }
    },
    selectConnectParam() {
      const found = this.connectForm.find(
        item => item.hostName + ':' + item.port + ':' + item.version === this.selectHostName
      );
      if (found) {
        this.form = { ...this.form, ...found };
      }
    },
  },
  mounted() {
    this.getConnectForm();
  }
}
</script>

<style lang="scss" scoped>
.connection-wrapper {
  margin-bottom: 24px;
}

.connection-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: var(--radius);

  .summary-left {
    display: flex;
    align-items: center;
    gap: 10px;

    i {
      font-size: 18px;
      color: var(--primary);
    }

    .summary-text {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
    }
  }

  .summary-right {
    .el-button {
      font-size: 13px;
    }
  }
}

.connection-section {
  background: #f8f9fa;
  border-radius: var(--radius);
  padding: 20px;
  animation: fadeIn 0.3s ease;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);

    i {
      margin-right: 6px;
      color: var(--primary);
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.app-tabs {
  ::v-deep .el-tabs__header {
    margin-bottom: 20px;
  }

  ::v-deep .el-tabs__nav-wrap::after {
    height: 1px;
    background-color: var(--card-border);
  }

  ::v-deep .el-tabs__item {
    font-weight: 500;
    color: var(--text-secondary);

    &.is-active {
      color: var(--primary);
    }
  }
}
</style>
