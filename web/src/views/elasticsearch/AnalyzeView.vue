<template>
  <div>
    <!-- 工具栏 -->
    <div class="app-toolbar">
      <div class="app-toolbar-left">
        <h3 class="page-title">分词分析</h3>
      </div>
    </div>

    <!-- 查询表单 -->
    <el-form :model="formData" label-width="80px">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8">
          <el-form-item label="索引名">
            <el-select
              v-model="formData.indexName"
              filterable
              clearable
              placeholder="选择索引"
              style="width: 100%"
            >
              <el-option
                v-for="item in indexNames"
                :key="item"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
            <el-button type="text" size="small" icon="el-icon-refresh" @click="getIndexNames">刷新</el-button>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-form-item label="字段">
            <el-input v-model="formData.filed" placeholder="请输入字段名"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="16" :md="12">
          <el-form-item label="分词器">
            <el-select
              v-model="formData.analyzer"
              filterable
              default-first-option
              clearable
              allow-create
              placeholder="请选择或输入分词器"
              style="width: 100%"
            >
              <el-option
                v-for="(value, key) in analyzersMap"
                :key="key"
                :label="key"
                :value="value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :md="12">
          <el-form-item label-width="0">
            <el-button-group>
              <el-button type="text" icon="el-icon-refresh" @click="fetchAnalyzerOperations">刷新分词器</el-button>
              <el-button type="text" icon="el-icon-document" @click="toggleAnalyzerList">
                {{ isAnalyzerListVisible ? '隐藏列表' : '显示列表' }}
              </el-button>
            </el-button-group>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 分词器列表 -->
      <el-table
        v-if="isAnalyzerListVisible && analyzers.length > 0"
        :data="analyzers"
        stripe
        class="app-table"
        style="width: 100%; margin-bottom: 20px"
      >
        <el-table-column label="组件" prop="component"></el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="版本" prop="version"></el-table-column>
      </el-table>

      <el-form-item label="文本">
        <el-input
          type="textarea"
          v-model="formData.document"
          placeholder="请输入待分词的文本"
          :rows="4"
        ></el-input>
      </el-form-item>

      <el-form-item label-width="80px">
        <el-button type="primary" icon="el-icon-scissors" @click="analyzeText">分词</el-button>
      </el-form-item>
    </el-form>

    <!-- 分词结果 -->
    <el-table
      v-if="analysisResult.length > 0"
      :data="analysisResult"
      stripe
      class="app-table"
      style="width: 100%"
    >
      <el-table-column label="分词结果">
        <template slot-scope="scope">
          <el-tag size="medium">{{ scope.row }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 错误提示 -->
    <el-alert v-if="errorMessage" type="error" :title="errorMessage" show-icon closable @close="errorMessage = ''"></el-alert>
  </div>
</template>

<script>
export default {
  props: { connectParam: Object },
  data() {
    return {
      formData: {
        indexName: '',
        analyzer: '',
        document: '',
        filed: ''
      },
      analyzers: [],
      analysisResult: [],
      operationCategory: "ANALYZE",
      errorMessage: '',
      isAnalyzerListVisible: false,
      analyzersMap: {},
      indexNames: []
    };
  },
  methods: {
    getParams(operationType) {
      return { ...this.connectParam, operationCategory: this.operationCategory, operationType };
    },
    async getIndexNames() {
      const params = this.getParams("INDEX_LIST");
      params.operationCategory = "INDEX";
      try {
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        const values = response.data.data;
        this.indexNames = Array.isArray(values) ? values.map(item => item.index) : [];
      } catch (error) {
        this.indexNames = [];
      }
    },
    async toggleAnalyzerList() {
      this.isAnalyzerListVisible = !this.isAnalyzerListVisible;
      if (this.isAnalyzerListVisible) {
        this.errorMessage = '';
        try {
          const params = this.getParams("PLUGINS");
          const response = await this.axios.post('/api/elasticsearch/operation', params);
          this.analyzers = Array.isArray(response.data.data)
            ? [...response.data.data]
            : [...Object.keys(response.data.data)];
        } catch (error) {
          this.errorMessage = '无法获取分词器列表。';
        }
      }
    },
    async fetchAnalyzerOperations() {
      this.errorMessage = '';
      try {
        const params = this.getParams("ANALYZERS");
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.analyzersMap = response.data.data || {};
      } catch (error) {
        this.errorMessage = '无法获取分词器列表。';
      }
    },
    async analyzeText() {
      this.errorMessage = '';
      try {
        const params = this.getParams("ANALYZE");
        params.indexName = this.formData.indexName;
        params.document = this.formData.document;
        params.analyzer = this.formData.analyzer;
        params.field = this.formData.filed;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.analysisResult = response.data.data || [];
      } catch (error) {
        this.errorMessage = '分词失败，请检查输入的索引和文本。';
      }
    }
  },
  mounted() {
    this.fetchAnalyzerOperations();
    this.getIndexNames();
  }
};
</script>
