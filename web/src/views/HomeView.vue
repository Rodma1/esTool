<template>
  <div class="page-wrapper">
    <!-- 页面工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title"><i class="el-icon-s-data"></i> 集群连接</h3>
      </div>
      <div class="app-toolbar-right">
        <el-button type="primary" icon="el-icon-plus" @click="goToEsPage">新增连接</el-button>
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
      <el-button type="primary" @click="goToEsPage">去配置连接</el-button>
    </el-empty>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      connections: []
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
    goToEsPage() {
      this.$router.push('/elasticsearch')
    },
    removeConnection(index) {
      this.$confirm('确定删除该连接配置?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.connections.splice(index, 1)
        this.$store.dispatch('setConnections', this.connections)
        this.$message.success('已删除')
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
