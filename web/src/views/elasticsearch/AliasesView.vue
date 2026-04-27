<template>
  <div>
    <!-- 工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title">别名管理</h3>
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
      <el-table-column prop="alias" label="别名" min-width="180"></el-table-column>
      <el-table-column prop="index" label="索引名" min-width="180"></el-table-column>
      <el-table-column prop="isWriteIndex" label="写入索引" min-width="120">
        <template slot-scope="scope">
          <el-tag :type="isWriteIndexTrue(scope.row.isWriteIndex) ? 'success' : 'info'" size="mini">
            {{ isWriteIndexTrue(scope.row.isWriteIndex) ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-delete" @click="deleteAlias(scope.row)">删除</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="text" icon="el-icon-edit" @click="updateAliasWriteIndex(scope.row)">设为写入索引</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  props: { connectParam: Object },
  data() {
    return {
      tableData: [],
      multipleSelection: [],
      operationCategory: "ALIAS",
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
    async updateAliasWriteIndex(row) {
      try {
        const params = this.getParams("WRITE_INDEX");
        params.indexName = row.index;
        params.alias = row.alias;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        this.refreshList();
      } catch (error) {
        console.log(error);
      }
    },
    isWriteIndexTrue(val) {
      if (val === true || val === 'true' || val === 1 || val === '1') return true;
      return false;
    },
    deleteAlias(row) {
      this.$confirm(`此操作将取消别名 ${row.alias} 和索引 ${row.index} 的关联, 是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const params = this.getParams("DISASSOCIATION");
        params.alias = row.alias;
        params.indices = [row.index];
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        this.refreshList();
      }).catch(() => {});
    },
  }
}
</script>
