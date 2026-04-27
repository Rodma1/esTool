<template>
  <div>
    <!-- 工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title">索引管理</h3>
        <el-button-group>
          <el-button type="primary" icon="el-icon-refresh" @click="refreshList">查询</el-button>
          <el-button type="primary" icon="el-icon-plus" plain @click="dialogVisible = true">创建索引</el-button>
          <el-button type="danger" icon="el-icon-delete" plain @click="deleteIndex">删除索引</el-button>
          <el-button icon="el-icon-connection" plain @click="aliasDialogVisible = true">关联别名</el-button>
          <el-button icon="el-icon-document-add" plain @click="createMappingDialogVisible = true">创建映射</el-button>
        </el-button-group>
      </div>
      <div class="app-toolbar-right">
        <el-input
          v-model="searchQuery"
          placeholder="请输入索引名进行检索"
          prefix-icon="el-icon-search"
          clearable
          style="width: 280px"
        ></el-input>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      ref="multipleTable"
      :data="filteredTableData"
      tooltip-effect="dark"
      stripe
      highlight-current-row
      class="app-table"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="index" label="索引名" min-width="140"></el-table-column>
      <el-table-column prop="docsCount" label="文档数" min-width="100"></el-table-column>
      <el-table-column prop="storeSize" label="存储大小" min-width="120"></el-table-column>
      <el-table-column prop="priStoreSize" label="主分片存储" min-width="120"></el-table-column>
      <el-table-column prop="health" label="健康状态" min-width="100">
        <template slot-scope="scope">
          <el-tag
            :type="getHealthType(scope.row.health)"
            effect="dark"
            size="mini"
          >
            {{ scope.row.health }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="pri" label="主分片" min-width="80"></el-table-column>
      <el-table-column prop="rep" label="副分片" min-width="80"></el-table-column>
      <el-table-column prop="docsDeleted" label="删除文档数" min-width="110"></el-table-column>
      <el-table-column prop="status" label="状态" min-width="90"></el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button type="text" icon="el-icon-view" @click="viewMapping(scope.row.index)">查看映射</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 映射详情对话框 -->
    <el-dialog title="索引映射详情" :visible.sync="mappingDialogVisible" width="60%">
      <el-scrollbar style="max-height: 100%">
        <json-viewer
          :key="jsonViewerKey"
          :value="mappingData"
          copyable
          boxed
          :expand-depth="expandDepth"
        ></json-viewer>
      </el-scrollbar>
      <span slot="footer" class="dialog-footer">
        <el-button @click="expandAll">展开所有</el-button>
        <el-button @click="collapseAll">折叠所有</el-button>
        <el-button type="primary" @click="mappingDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 创建索引对话框 -->
    <el-dialog title="创建索引" :visible.sync="dialogVisible" width="500px">
      <el-form ref="form" :model="createIndexFrom" label-width="80px">
        <el-form-item label="索引名称">
          <el-input v-model="createIndexFrom.indexName" placeholder="请输入索引名称"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="createIndex">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 关联别名对话框 -->
    <el-dialog title="关联别名" :visible.sync="aliasDialogVisible" width="500px">
      <el-form ref="form" :model="associationAliasFrom" label-width="80px">
        <el-form-item label="别名名称">
          <el-input v-model="associationAliasFrom.alias" placeholder="请输入别名"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="aliasDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="associationAlias">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 创建映射对话框 -->
    <el-dialog title="创建映射" :visible.sync="createMappingDialogVisible" width="600px">
      <el-form ref="createMappingForm" :model="createMappingData" label-width="100px">
        <el-form-item label="索引名称">
          <el-input v-model="createMappingData.indexName" placeholder="请输入索引名称"></el-input>
        </el-form-item>
        <el-form-item label="映射内容">
          <el-input
            type="textarea"
            v-model="createMappingData.mapping"
            placeholder="请输入映射内容（JSON 格式）"
            :rows="10"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createMappingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMapping">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer';
import 'vue-json-viewer/style.css';
import { handleApiMessage } from '@/utils/messageUtil.js';

export default {
  components: { JsonViewer },
  props: { connectParam: Object },
  data() {
    return {
      searchQuery: '',
      tableData: [{
        index: '', docsCount: '', storeSize: '', priStoreSize: '',
        health: '', uuid: '', pri: '', rep: '', docsDeleted: '', status: '',
      }],
      mappingDialogVisible: false,
      mappingData: {},
      multipleSelection: [],
      jsonViewerKey: 0,
      expandDepth: 4,
      operationCategory: "INDEX",
      dialogVisible: false,
      aliasDialogVisible: false,
      createMappingDialogVisible: false,
      createIndexFrom: { indexName: '' },
      associationAliasFrom: { alias: '' },
      createMappingData: { indexName: '', mapping: '' },
    }
  },
  computed: {
    filteredTableData() {
      return this.tableData.filter(item =>
        item.index.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
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
      const params = { ...this.connectParam, operationCategory: this.operationCategory, operationType };
      return params;
    },
    async fetchData() {
      try {
        const params = this.getParams("INDEX_LIST");
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.tableData = response.data.data || [];
      } catch (error) {
        console.log(error);
      }
    },
    async createIndex() {
      try {
        const params = this.getParams("CREATE");
        params.indexName = this.createIndexFrom.indexName;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        await this.fetchData();
        this.createIndexFrom.indexName = '';
      } catch (error) {
        console.log(error);
      }
      this.dialogVisible = false;
    },
    async associationAlias() {
      try {
        const params = this.getParams("INSERT");
        params.indices = this.multipleSelection;
        params.operationCategory = "ALIAS";
        params.alias = this.associationAliasFrom.alias;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        this.associationAliasFrom.alias = '';
      } catch (error) {
        console.log(error);
      }
      this.aliasDialogVisible = false;
    },
    deleteIndex() {
      this.$confirm('此操作将永久删除选中的索引, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const params = this.getParams("BATCH_DELETE");
        params.indices = this.multipleSelection;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.$message({ message: response.data.message, type: 'success' });
        await this.fetchData();
      }).catch(() => {});
    },
    async viewMapping(indexName) {
      try {
        const params = this.getParams("QUERY");
        params.operationCategory = "MAPPING";
        params.operationType = "QUERY";
        params.indexName = indexName;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.mappingData = response.data.data;
        this.mappingDialogVisible = true;
      } catch (error) {
        this.$message.error('获取索引映射失败，请检查后端接口');
      }
    },
    expandAll() {
      this.expandDepth = Infinity;
      this.refreshJsonViewer();
    },
    collapseAll() {
      this.expandDepth = 0;
      this.refreshJsonViewer();
    },
    refreshJsonViewer() {
      this.jsonViewerKey += 1;
    },
    async submitMapping() {
      try {
        const parsedMapping = JSON.parse(this.createMappingData.mapping);
        const params = this.getParams("CREATE");
        params.operationCategory = "MAPPING";
        params.indexName = this.createMappingData.indexName;
        params.mapping = JSON.stringify(parsedMapping);
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        const success = await handleApiMessage(response, '映射创建成功');
        if (!success) return;
        this.createMappingDialogVisible = false;
        this.createMappingData.indexName = '';
        this.createMappingData.mapping = '';
      } catch (error) {
        if (error instanceof SyntaxError) {
          this.$message.error('映射内容必须是合法的 JSON 格式');
        } else {
          this.$message.error('创建映射失败，请检查后端接口');
        }
      }
    },
    getHealthType(health) {
      const map = { green: 'success', yellow: 'warning', red: 'danger' };
      return map[health] || 'info';
    }
  }
}
</script>
