<template>
  <div>
    <!-- 工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title">任务列表</h3>
        <el-button type="primary" icon="el-icon-refresh" @click="refreshList">查询</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      ref="multipleTable"
      :data="tableData"
      tooltip-effect="dark"
      stripe
      highlight-current-row
      class="app-table"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="task_id" label="任务Id" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="type" label="类型" min-width="120"></el-table-column>
      <el-table-column prop="start_time" label="启动时间" min-width="160"></el-table-column>
      <el-table-column prop="running_time" label="运行时间" min-width="120"></el-table-column>
      <el-table-column prop="ip" label="地址" min-width="140"></el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-video-pause" @click="stopTask(scope.row)">停止任务</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="text" icon="el-icon-view" @click="taskInfo(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 任务详情抽屉 -->
    <el-drawer title="任务详情" :visible.sync="drawerVisible" direction="rtl" size="40%">
      <div style="padding: 20px">
        <pre class="json-pre">{{ formattedInfo }}</pre>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  props: { connectParam: Object },
  data() {
    return {
      tableData: [],
      multipleSelection: [],
      operationCategory: "TASK",
      drawerVisible: false,
      info: {}
    }
  },
  computed: {
    formattedInfo() {
      return JSON.stringify(this.info, null, 2);
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.multipleSelection = val.map(el => el.index);
    },
    refreshList() {
      this.fetchData();
    },
    getParams(operationType) {
      return { ...this.connectParam, operationCategory: this.operationCategory, operationType };
    },
    async fetchData() {
      try {
        const params = this.getParams("LIST");
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.tableData = response.data.data || [];
      } catch (error) {
        console.log(error);
      }
    },
    stopTask(row) {
      this.$confirm('此操作将取消任务，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const params = this.getParams("PUT");
        params.taskId = row.task_id;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        this.refreshList();
      }).catch(() => {});
    },
    async taskInfo(row) {
      try {
        const params = this.getParams("INFO");
        params.taskId = row.task_id;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.info = response.data.data;
        this.drawerVisible = true;
      } catch (error) {
        console.log(error);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.json-pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: var(--radius-sm);
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 70vh;
  overflow-y: auto;
}
</style>
