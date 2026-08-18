<template>
  <div>
    <!-- 工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title">模板管理</h3>
        <el-button-group>
          <el-button type="primary" icon="el-icon-refresh" @click="refreshList">查询</el-button>
          <el-button type="primary" icon="el-icon-plus" plain @click="dialogVisible = true">创建模板</el-button>
        </el-button-group>
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
      <el-table-column type="expand">
        <template slot-scope="scope">
          <el-form label-position="left" inline class="demo-table-expand">
            <el-form-item label="关联模板">
              <span>{{ scope.row.composedOf }}</span>
            </el-form-item>
          </el-form>
        </template>
      </el-table-column>

      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="name" label="模板名" min-width="200"></el-table-column>
      <el-table-column prop="indexPatterns" label="匹配规则" min-width="250"></el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-view" @click="getTemplateInfo(scope.row)">查看详情</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="text" class="text-danger" icon="el-icon-delete" @click="deleteTemplate(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 详情抽屉 -->
    <el-drawer title="模板详情" :visible.sync="drawer" :with-header="true" size="40%">
      <div style="padding: 20px">
        <div style="margin-bottom: 16px; text-align: right">
          <el-button type="text" icon="el-icon-document-copy" @click="copyJson(templateInfo)">复制</el-button>
        </div>
        <json-viewer :value="templateInfo" :expanded="true"></json-viewer>
      </div>
    </el-drawer>

    <!-- 创建模板对话框 -->
    <el-dialog title="创建模板" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="createTemplateFrom" label-width="80px">
        <el-form-item label="模板名称">
          <el-input v-model="createTemplateFrom.indexTemplate" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input
            type="textarea"
            v-model="createTemplateFrom.indexTemplateContent"
            placeholder="请输入模板内容（JSON 格式）"
            :rows="8"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="createTemplate">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  props: { connectParam: Object },
  data() {
    return {
      tableData: [],
      multipleSelection: [],
      operationCategory: "TEMPLATE",
      dialogVisible: false,
      drawer: false,
      templateInfo: {},
      createTemplateFrom: {
        indexTemplate: '',
        indexTemplateContent: ''
      }
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
    async createTemplate() {
      try {
        const params = this.getParams("PUT");
        params.indexTemplate = this.createTemplateFrom.indexTemplate;
        params.indexTemplateContent = this.createTemplateFrom.indexTemplateContent;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        this.dialogVisible = false;
        this.refreshList();
      } catch (error) {
        console.log(error);
      }
    },
    deleteTemplate(row) {
      this.$confirm(`此操作将删除模板 ${row.name}，是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const params = this.getParams("DELETE");
        params.indexTemplate = row.name;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        this.refreshList();
      }).catch(() => {});
    },
    async getTemplateInfo(row) {
      try {
        const params = this.getParams("INFO");
        params.indexTemplate = row.name;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.templateInfo = response.data.data;
        this.drawer = true;
      } catch (error) {
        console.log(error);
      }
    },
    copyJson(item) {
      const el = document.createElement('textarea');
      el.value = JSON.stringify(item, null, 2);
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      this.$message.success('JSON 已复制到剪贴板');
    }
  }
}
</script>

<style lang="scss" scoped>
.demo-table-expand {
  font-size: 0;

  label {
    width: 90px;
    color: #99a9bf;
  }

  .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 50%;
  }
}
</style>
