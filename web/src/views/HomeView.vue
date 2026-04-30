<template>
  <div class="page-wrapper">
    <!-- 页面工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title"><i class="el-icon-s-data"></i> 集群连接</h3>
      </div>
      <div class="app-toolbar-right">
        <el-button type="primary" icon="el-icon-plus" @click="showAddDialog">新增连接</el-button>
      </div>
    </div>

    <!-- 连接卡片网格 -->
    <el-row v-if="connections.length > 0" :gutter="24">
      <el-col
        v-for="(conn, index) in connections"
        :key="index"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        style="margin-bottom: 24px"
      >
        <el-card class="connection-card" shadow="hover">
          <div class="card-header">
            <el-tag :type="getStatusType(conn)" effect="dark" size="mini">
              {{ getStatusText(conn) }}
            </el-tag>
          </div>
          <div class="card-body">
            <h4>{{ conn.hostName }}:{{ conn.port }}</h4>
            <p class="text-muted">
              <i class="el-icon-cpu"></i> 版本: {{ conn.version || '-' }}
            </p>
            <p class="text-muted">
              <i class="el-icon-link"></i> 协议: {{ conn.scheme || 'http' }}
            </p>
            <p v-if="conn.userName" class="text-muted">
              <i class="el-icon-user"></i> 用户: {{ conn.userName }}
            </p>
          </div>
          <div class="card-footer">
            <el-button type="primary" size="small" icon="el-icon-link" @click="quickConnect(conn)">
              快速连接
            </el-button>
            <el-button size="small" icon="el-icon-delete" type="text" class="text-danger" @click="removeConnection(index)">
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-else description="暂无 ES 连接配置">
      <el-button type="primary" @click="showAddDialog">去配置连接</el-button>
    </el-empty>

    <!-- 新增连接对话框 -->
    <el-dialog title="新增 ES 连接" :visible.sync="addDialogVisible" width="520px" @close="resetAddForm">
      <el-form ref="addForm" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="协议" prop="scheme">
          <el-select v-model="addForm.scheme" placeholder="请选择协议" style="width: 100%">
            <el-option label="http" value="http"></el-option>
            <el-option label="https" value="https"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="hostName">
          <el-input v-model="addForm.hostName" placeholder="请输入 IP 地址"></el-input>
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input v-model="addForm.port" placeholder="请输入端口号"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="addForm.userName" placeholder="请输入用户名（可选）"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="addForm.password" placeholder="请输入密码（可选）" show-password></el-input>
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-select v-model="addForm.version" placeholder="请选择 ES 版本" style="width: 100%">
            <el-option label="7.x" value="7"></el-option>
            <el-option label="8.x" value="8"></el-option>
            <el-option label="9.x" value="9"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAddConnection">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      connections: [],
      addDialogVisible: false,
      addLoading: false,
      addForm: {
        scheme: 'http',
        hostName: '',
        port: '',
        userName: '',
        password: '',
        version: ''
      },
      addRules: {
        hostName: [{ required: true, message: '请输入 IP 地址', trigger: 'blur' }],
        port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
        version: [{ required: true, message: '请选择 ES 版本', trigger: 'change' }],
        scheme: [{ required: true, message: '请选择协议', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getConnections()
  },
  methods: {
    async getConnections() {
      try {
        const response = await this.axios.get('/api/elasticsearch/connectParam')
        this.connections = response.data.data || []
        this.$store.dispatch('setConnections', this.connections)
      } catch (error) {
        this.$message.error('获取连接配置失败')
      }
    },
    async quickConnect(conn) {
      const loading = this.$loading({
        lock: true,
        text: `正在连接 ${conn.hostName}:${conn.port}...`,
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      try {
        const params = { ...conn, operationCategory: 'INFO' }
        const response = await this.axios.post('/api/elasticsearch/operation', params)
        if (response.data.code !== 200) {
          this.$message.error(response.data.message || '连接失败')
          return
        }
        const versionInfo = response.data.data
        const enrichedConn = {
          ...conn,
          versionNumber: versionInfo.number,
          buildType: versionInfo.buildType,
          luceneVersion: versionInfo.luceneVersion
        }
        this.$store.dispatch('setActiveConnection', enrichedConn)
        this.$message.success(`连接成功: ${conn.hostName}:${conn.port} (v${versionInfo.number})`)
        this.$router.push('/elasticsearch')
      } catch (error) {
        this.$message.error(`连接失败: ${error.message || '网络错误'}`)
      } finally {
        loading.close()
      }
    },
    showAddDialog() {
      this.addDialogVisible = true
    },
    resetAddForm() {
      this.addForm = { scheme: 'http', hostName: '', port: '', userName: '', password: '', version: '' }
      this.$nextTick(() => { this.$refs.addForm && this.$refs.addForm.clearValidate() })
    },
    handleAddConnection() {
      this.$refs.addForm.validate(async (valid) => {
        if (!valid) return
        this.addLoading = true
        try {
          const params = { ...this.addForm, port: Number(this.addForm.port) }
          const response = await this.axios.post('/api/elasticsearch/connectParam', params)
          if (response.data.code !== 200) {
            this.$message.error(response.data.message || '新增失败')
            return
          }
          this.$message.success('连接配置已保存')
          this.addDialogVisible = false
          this.getConnections()
        } catch (error) {
          this.$message.error('新增连接失败')
        } finally {
          this.addLoading = false
        }
      })
    },
    removeConnection(index) {
      const conn = this.connections[index]
      this.$confirm('确定删除该连接配置?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.axios.delete('/api/elasticsearch/connectParam', { data: conn })
          if (response.data.code !== 200) {
            this.$message.error(response.data.message || '删除失败')
            return
          }
          this.connections.splice(index, 1)
          this.$store.dispatch('setConnections', this.connections)
          this.$message.success('已删除')
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    getStatusType(conn) {
      return conn.version ? 'success' : 'info'
    },
    getStatusText(conn) {
      return conn.version ? '已配置' : '未验证'
    }
  }
}
</script>

<style lang="scss" scoped>
.connection-card {
  ::v-deep .el-card__body {
    padding: 20px;
  }

  .card-header {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
  }

  .card-body {
    h4 {
      margin: 0 0 12px 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      word-break: break-all;
    }

    p {
      margin: 6px 0;
      font-size: 13px;
      color: var(--text-secondary);

      i {
        margin-right: 4px;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid var(--card-border);
  }
}
</style>
