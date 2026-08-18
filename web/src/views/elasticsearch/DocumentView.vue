<template>
  <div>
    <el-row :gutter="16" class="doc-layout">
    <!-- 左侧查询面板 -->
    <el-col v-show="!leftCollapsed" :xs="24" :sm="24" :md="10" :lg="8">
      <div class="app-card query-panel">
        <div class="panel-title">
          <span><i class="el-icon-search"></i> 查询条件</span>
          <el-button type="text" size="mini" icon="el-icon-s-fold" @click="leftCollapsed = true" title="收起面板"></el-button>
        </div>

        <!-- 顶部操作栏 -->
        <div class="query-actions">
          <el-button type="primary" icon="el-icon-refresh" size="small" @click="refreshList">查询</el-button>
          <el-button icon="el-icon-refresh-left" size="small" @click="clearAllConditions">清空</el-button>
          <el-button type="success" icon="el-icon-magic-stick" size="small" @click="openAiModal">AI 查询</el-button>
          <el-dropdown trigger="click" @command="handleHistoryClick" v-if="queryHistory.length > 0">
            <el-button icon="el-icon-time" size="small">历史</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="(h, idx) in queryHistory" :key="idx" :command="h">{{ h.label }}</el-dropdown-item>
              <el-dropdown-item divided command="clear">清空历史</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <div class="query-form">
          <el-collapse v-model="activeCollapse" :accordion="false">
            <el-collapse-item title="基础查询" name="basic">
              <el-form :model="{}" label-width="80px">
                <el-form-item label="文档 ID">
                  <el-input v-model="documentId" placeholder="根据文章Id查询" clearable size="small"></el-input>
                </el-form-item>
                <el-form-item label="查询索引">
                  <el-select v-model="indices" allow-create filterable multiple placeholder="选择索引" style="width: 100%" size="small">
                    <el-option v-for="item in indexNames" :key="item" :label="item" :value="item"></el-option>
                  </el-select>
                  <div v-if="indexNames.length > 0" class="index-helper">
                    <el-tag size="mini" type="info">{{ indexNames.length }} 个</el-tag>
                    <el-button type="text" size="mini" @click="indices = [...indexNames]">全选</el-button>
                    <el-button type="text" size="mini" @click="indices = []">清空</el-button>
                  </div>
                </el-form-item>
                <el-form-item label="排序">
                  <el-row :gutter="8">
                    <el-col :span="10">
                      <el-select v-model="sortOrder" placeholder="排序" style="width: 100%" size="small">
                        <el-option label="升序" value="Asc"></el-option>
                        <el-option label="降序" value="Desc"></el-option>
                      </el-select>
                    </el-col>
                    <el-col :span="14">
                      <el-input v-model="sortField" placeholder="排序字段" size="small"></el-input>
                    </el-col>
                  </el-row>
                </el-form-item>
              </el-form>
            </el-collapse-item>

            <el-collapse-item title="时间筛选" name="time">
              <el-form :model="{}" label-width="80px">
                <el-form-item label="时间范围">
                  <el-date-picker v-model="timeSearch.times" type="datetimerange" range-separator="至"
                    start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd HH:mm:ss"
                    :default-time="['00:00:00', '23:59:59']" clearable style="width: 100%" size="small"
                  ></el-date-picker>
                </el-form-item>
                <el-form-item label="时间字段">
                  <el-input v-model="timeSearch.field" placeholder="时间排序字段" size="small"></el-input>
                </el-form-item>
              </el-form>
            </el-collapse-item>

            <el-collapse-item :title="'查询字段 (' + searchFields.length + ')'" name="searchFields">
              <div class="dynamic-fields">
                <div v-for="(domain, index) in searchFields" :key="'search-' + index" class="dynamic-field-row">
                  <el-input v-model="domain.key" placeholder="字段名" size="mini"></el-input>
                  <el-input v-model="domain.value" placeholder="字段值" size="mini"></el-input>
                  <el-button type="text" class="text-danger" icon="el-icon-delete" size="mini" @click="removeDomain(domain)"></el-button>
                </div>
                <el-button v-if="searchFields.length === 0" type="text" icon="el-icon-plus" size="mini" @click="addDomain">添加查询字段</el-button>
                <el-button v-else type="text" icon="el-icon-plus" size="mini" @click="addDomain">添加</el-button>
              </div>
            </el-collapse-item>

            <el-collapse-item :title="'更新字段 (' + updateFields.length + ')'" name="updateFields">
              <div class="dynamic-fields">
                <div v-for="(domain, index) in updateFields" :key="'update-' + index" class="dynamic-field-row">
                  <el-input v-model="domain.key" placeholder="字段名" size="mini"></el-input>
                  <el-input v-model.number="domain.value" placeholder="字段值" size="mini"></el-input>
                  <el-button type="text" class="text-danger" icon="el-icon-delete" size="mini" @click="removeUpdateDomain(domain)"></el-button>
                </div>
                <el-button v-if="updateFields.length === 0" type="text" icon="el-icon-plus" size="mini" @click="addUpdateDomain">添加更新字段</el-button>
                <el-button v-else type="text" icon="el-icon-plus" size="mini" @click="addUpdateDomain">添加</el-button>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-col>

    <!-- 右侧结果面板 -->
    <el-col :xs="24" :sm="24" :md="leftCollapsed ? 24 : 14" :lg="leftCollapsed ? 24 : 16">
      <div class="app-card result-panel">
        <!-- 头部：标题 + 视图切换 -->
        <div class="result-header">
          <div class="panel-title">
            <el-button v-if="leftCollapsed" type="text" size="mini" icon="el-icon-s-unfold" @click="leftCollapsed = false" title="展开查询面板" style="margin-right:6px"></el-button>
            <i class="el-icon-document"></i> 查询结果
          </div>
          <el-radio-group v-model="viewMode" size="mini">
            <el-radio-button label="table"><i class="el-icon-s-grid"></i> 表格</el-radio-button>
            <el-radio-button label="card"><i class="el-icon-s-order"></i> 卡片</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 统计卡片 -->
        <div class="result-stats">
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="stat-item stat-total">
                <div class="stat-icon"><i class="el-icon-s-data"></i></div>
                <div class="stat-info">
                  <div class="stat-label">文档总数</div>
                  <div class="stat-value">{{ count | formatNumber }}</div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item stat-hit">
                <div class="stat-icon"><i class="el-icon-s-flag"></i></div>
                <div class="stat-info">
                  <div class="stat-label">查询命中</div>
                  <div class="stat-value">{{ searchCount | formatNumber }}</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 结果操作栏 -->
        <div class="result-toolbar">
          <div class="result-toolbar-left">
            <el-checkbox v-model="isSelectAll" :indeterminate="isIndeterminate" @change="handleSelectAllChange">全选</el-checkbox>
            <el-tag v-if="selectedItems.length > 0" size="mini" type="primary">已选 {{ selectedItems.length }} 条</el-tag>
            <el-button v-if="selectedItems.length > 0" type="danger" size="mini" icon="el-icon-delete" plain @click="batchDelete">删除</el-button>
            <el-button v-if="selectedItems.length > 0" type="warning" size="mini" icon="el-icon-edit" plain @click="batchUpdate">更新</el-button>
          </div>
          <div class="result-toolbar-right">
            <el-input v-model="resultFilter" placeholder="结果内筛选" prefix-icon="el-icon-search" size="mini" clearable style="width: 140px"></el-input>
            <el-select v-model="pageSize" size="mini" style="width: 70px" @change="handleSizeChange">
              <el-option label="10" :value="10"></el-option>
              <el-option label="20" :value="20"></el-option>
              <el-option label="50" :value="50"></el-option>
              <el-option label="100" :value="100"></el-option>
            </el-select>
          </div>
        </div>

        <!-- 创建对话框 -->
        <el-dialog title="创建 JSON 数据" :visible.sync="createDialogVisible" width="500px" @close="resetCreateForm">
          <el-form label-width="80px">
            <el-form-item label="目标索引">
              <el-select v-model="selectIndex" clearable placeholder="请选择索引" style="width: 100%">
                <el-option v-for="item in indexNames" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="JSON 数据">
              <el-input type="textarea" v-model="newJson" placeholder="请输入 JSON 数据" :rows="10"></el-input>
            </el-form-item>
          </el-form>
          <span slot="footer" class="dialog-footer">
            <el-button @click="createDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="createJson">创建</el-button>
          </span>
        </el-dialog>

        <!-- 内容滚动区 -->
        <div class="result-content">
          <!-- 空状态 -->
          <el-empty v-if="filteredJsonData.length === 0 && !loading" :description="emptyText">
            <el-button v-if="!hasQueried" type="primary" icon="el-icon-refresh" size="small" @click="refreshList">开始查询</el-button>
            <el-button v-else type="primary" icon="el-icon-plus" size="small" @click="showCreateDialog">创建文档</el-button>
          </el-empty>

          <!-- 表格视图 -->
          <el-table
            v-else-if="viewMode === 'table'"
            :data="filteredJsonData"
            stripe
            highlight-current-row
            class="app-table"
            style="width: 100%"
            @selection-change="handleTableSelectionChange"
          >
            <el-table-column type="selection" width="45"></el-table-column>
            <el-table-column type="expand" width="45">
              <template slot-scope="scope">
                <div class="expand-json">
                  <json-viewer :value="scope.row.source" :expand-depth="5" copyable boxed></json-viewer>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="index" label="索引" min-width="140">
              <template slot-scope="scope">
                <el-tag size="mini" :color="getIndexColor(scope.row.index)" effect="dark">{{ scope.row.index }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="ID" min-width="140" show-overflow-tooltip></el-table-column>
            <el-table-column label="字段摘要" min-width="200" show-overflow-tooltip>
              <template slot-scope="scope">
                <span class="field-summary">{{ getFieldSummary(scope.row.source) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" icon="el-icon-view" @click="showDetail(scope.row)">查看</el-button>
                <el-button type="text" size="mini" icon="el-icon-document-copy" @click="copyJson(scope.row)">复制</el-button>
                <el-button type="text" class="text-danger" size="mini" icon="el-icon-delete" @click="deleteSingle(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 卡片视图 -->
          <div v-else class="json-list">
            <div
              v-for="item in filteredJsonData"
              :key="item.id"
              class="json-result-card"
              :class="{ 'is-selected': selectedItems.includes(item.id) }"
              :style="{ borderLeftColor: getIndexColor(item.index) }"
            >
              <div class="json-result-header">
                <div class="json-meta">
                  <el-checkbox v-model="selectedItems" :label="item.id"></el-checkbox>
                  <el-tag size="mini" :color="getIndexColor(item.index)" effect="dark">{{ item.index }}</el-tag>
                  <el-tag size="mini" type="info">{{ item.id }}</el-tag>
                </div>
                <div class="json-actions">
                  <el-tooltip content="查看">
                    <el-button type="text" size="mini" icon="el-icon-view" @click="showDetail(item)"></el-button>
                  </el-tooltip>
                  <el-tooltip content="展开/折叠">
                    <el-button type="text" size="mini"
                      :icon="expandedItems.includes(item.id) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
                      @click="toggleExpand(item.id)"
                    ></el-button>
                  </el-tooltip>
                  <el-tooltip content="复制">
                    <el-button type="text" size="mini" icon="el-icon-document-copy" @click="copyJson(item)"></el-button>
                  </el-tooltip>
                  <el-tooltip content="删除">
                    <el-button type="text" class="text-danger" size="mini" icon="el-icon-delete" @click="deleteSingle(item)"></el-button>
                  </el-tooltip>
                </div>
              </div>
              <json-viewer
                :value="item.source"
                :expand-depth="expandedItems.includes(item.id) ? 5 : 0"
              ></json-viewer>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="count > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="count"
          layout="total, sizes, prev, pager, next, jumper"
          class="result-pagination"
        ></el-pagination>
      </div>
    </el-col>

    <!-- 单条数据详情弹窗 -->
    <el-dialog
      :title="detailItem ? detailItem.index + ' / ' + detailItem.id : '文档详情'"
      :visible.sync="detailDialogVisible"
      width="70%"
      top="5vh"
      :close-on-click-modal="true"
    >
      <json-viewer
        v-if="detailItem"
        :value="detailItem.source"
        :expand-depth="10"
        copyable
        boxed
      ></json-viewer>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" icon="el-icon-document-copy" @click="copyJson(detailItem)">复制 JSON</el-button>
      </span>
    </el-dialog>

    <!-- AI 智能查询弹窗 -->
    <ai-fill-modal ref="aiFillModal" :connect-param="connectParam"></ai-fill-modal>
  </el-row>
  </div>
</template>

<script>
import AIFillModal from '@/components/ai/AIFillModal.vue';

const HISTORY_KEY = 'es_doc_query_history';
const HISTORY_MAX = 5;

export default {
  // eslint-disable-next-line vue/no-unused-components
  components: { AIFillModal },
  props: { connectParam: Object },
  filters: {
    formatNumber(val) {
      if (!val && val !== 0) return '-';
      return val.toLocaleString();
    }
  },
  data() {
    return {
      jsonData: [],
      createDialogVisible: false,
      newJson: '',
      currentPage: 1,
      pageSize: 10,
      selectedItems: [],
      isSelectAll: false,
      isIndeterminate: false,
      operationCategory: "DOCUMENT",
      count: 0,
      indexNames: [],
      selectIndex: '',
      documentId: '',
      indices: [],
      sortOrder: '',
      sortField: '',
      searchFields: [],
      updateFields: [],
      searchCount: 0,
      timeSearch: { times: [], field: '' },
      resultFilter: '',
      expandedItems: [],
      activeCollapse: ['basic'],
      queryHistory: [],
      hasQueried: false,
      loading: false,
      indexColorMap: {},
      viewMode: 'table',
      tableSelected: [],
      leftCollapsed: false,
      detailDialogVisible: false,
      detailItem: null
    };
  },
  computed: {
    filteredJsonData() {
      if (!this.resultFilter) return this.jsonData;
      const kw = this.resultFilter.toLowerCase();
      return this.jsonData.filter(item => {
        const text = JSON.stringify(item).toLowerCase();
        return text.includes(kw);
      });
    },
    emptyText() {
      return this.hasQueried ? '暂无数据，尝试调整查询条件或创建文档' : '选择索引并点击查询开始探索文档';
    }
  },
  watch: {
    selectedItems(val) {
      const total = this.jsonData.length;
      const checked = val.length;
      this.isSelectAll = checked > 0 && checked === total;
      this.isIndeterminate = checked > 0 && checked < total;
    }
  },
  created() {
    this.loadHistory();
  },
  methods: {
    refreshList() {
      this.getDocumentsPage();
      this.getDocumentCount();
      this.currentPage = 1;
      this.saveToHistory();
    },
    getParams(operationType) {
      return { ...this.connectParam, operationCategory: this.operationCategory, operationType };
    },
    async getDocumentsPage() {
      this.loading = true;
      try {
        let names = [];
        if (this.indices.length === 0 || !this.indices) {
          await this.getindexNames();
          names = this.indexNames;
        } else {
          names = this.indices;
        }
        const params = this.getParams("PAGE");
        if (this.timeSearch.times && this.timeSearch.times.length !== 0 && this.timeSearch.field) {
          params.timeSearch = {
            beginTime: this.timeSearch.times[0],
            endTime: this.timeSearch.times[1],
            field: this.timeSearch.field
          };
        } else {
          params.timeSearch = {};
        }
        params.pageSize = this.pageSize;
        params.pageNum = this.currentPage;
        params.documentId = this.documentId;
        params.sortField = this.sortField;
        params.sortOrder = this.sortOrder;
        params.indices = names;
        params.searchFields = this.searchFields;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.jsonData = response.data.data.rows || [];
        this.searchCount = response.data.data.count || 0;
        this.hasQueried = true;
        this.assignIndexColors();
        this.selectedItems = [];
        this.tableSelected = [];
        this.isSelectAll = false;
        this.isIndeterminate = false;
      } catch (error) {
        console.log(error);
      } finally {
        this.loading = false;
      }
    },
    showCreateDialog() {
      this.createDialogVisible = true;
      this.getindexNames();
    },
    resetCreateForm() {
      this.newJson = '';
      this.selectIndex = '';
    },
    async createJson() {
      try {
        const parsedJson = JSON.parse(this.newJson);
        if (Array.isArray(parsedJson)) {
          parsedJson.forEach(item => {
            this.jsonData.push({ ...item, id: this.jsonData.length + 1 });
          });
        } else {
          const params = this.getParams("INSERT");
          params.document = JSON.stringify(parsedJson);
          params.indexName = this.selectIndex;
          const response = await this.axios.post('/api/elasticsearch/operation', params);
          this.refreshList();
          this.$message({ message: response.data.message, type: 'success' });
        }
        this.createDialogVisible = false;
        this.resetCreateForm();
      } catch (error) {
        this.$message.error(error.message);
      }
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
      this.getDocumentsPage();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.getDocumentsPage();
    },
    async getindexNames() {
      const params = this.getParams("INDEX_LIST");
      params.operationCategory = "INDEX";
      const response = await this.axios.post('/api/elasticsearch/operation', params);
      const values = response.data.data || [];
      this.indexNames = values.map(item => item.index);
    },
    async batchDelete() {
      this.$confirm(`此操作将永久删除选中的 ${this.selectedItems.length} 个文档, 是否继续?`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        const params = this.getParams("DELETE");
        params.documentIds = this.selectedItems;
        params.indices = this.indexNames;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.selectedItems = [];
        this.tableSelected = [];
        this.refreshList();
        this.$message({ message: response.data.message, type: 'success' });
      }).catch(() => {});
    },
    async deleteSingle(item) {
      this.$confirm('确定删除该文档?', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        const params = this.getParams("DELETE");
        params.documentIds = [item.id];
        params.indices = [item.index];
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.refreshList();
        this.$message({ message: response.data.message, type: 'success' });
      }).catch(() => {});
    },
    async batchUpdate() {
      this.$confirm('此操作将更新勾选的索引文档, 是否继续?', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        const params = this.getParams("UPDATE");
        params.documentIds = this.selectedItems;
        params.indices = this.indexNames;
        params.updateFields = this.updateFields;
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.selectedItems = [];
        this.tableSelected = [];
        this.refreshList();
        this.$message({ message: response.data.message, type: 'success' });
      }).catch(() => {});
    },
    async getDocumentCount() {
      const params = this.getParams("COUNT");
      const response = await this.axios.post('/api/elasticsearch/operation', params);
      this.count = response.data.data || 0;
    },
    copyJson(item) {
      const el = document.createElement('textarea');
      el.value = JSON.stringify(item, null, 2);
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      this.$message.success('JSON 已复制到剪贴板');
    },
    removeDomain(item) {
      const index = this.searchFields.indexOf(item);
      if (index !== -1) this.searchFields.splice(index, 1);
    },
    removeUpdateDomain(item) {
      const index = this.updateFields.indexOf(item);
      if (index !== -1) this.updateFields.splice(index, 1);
    },
    addDomain() {
      this.searchFields.push({ key: '', value: '' });
    },
    addUpdateDomain() {
      this.updateFields.push({ key: null, value: null });
    },
    toggleExpand(id) {
      const idx = this.expandedItems.indexOf(id);
      if (idx > -1) this.expandedItems.splice(idx, 1);
      else this.expandedItems.push(id);
    },
    handleSelectAllChange(val) {
      if (val) {
        this.selectedItems = this.jsonData.map(item => item.id);
      } else {
        this.selectedItems = [];
      }
    },
    handleTableSelectionChange(val) {
      this.tableSelected = val;
      this.selectedItems = val.map(item => item.id);
    },
    clearAllConditions() {
      this.documentId = '';
      this.indices = [];
      this.sortOrder = '';
      this.sortField = '';
      this.timeSearch = { times: [], field: '' };
      this.searchFields = [];
      this.updateFields = [];
      this.resultFilter = '';
      this.activeCollapse = ['basic'];
      this.$message.success('查询条件已清空');
    },
    openAiModal() {
      this.$refs.aiFillModal.open();
    },
    showDetail(item) {
      this.detailItem = item;
      this.detailDialogVisible = true;
    },
    getIndexColor(indexName) {
      return this.indexColorMap[indexName] || '#909399';
    },
    assignIndexColors() {
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00C9A7', '#845EC2', '#FF6F91'];
      const uniqueIndices = [...new Set(this.jsonData.map(item => item.index))];
      uniqueIndices.forEach((idx, i) => {
        if (!this.indexColorMap[idx]) {
          this.indexColorMap[idx] = colors[i % colors.length];
        }
      });
    },
    getFieldSummary(source) {
      if (!source || typeof source !== 'object') return '-';
      const keys = Object.keys(source).slice(0, 3);
      return keys.map(k => `${k}: ${this.truncate(String(source[k]), 20)}`).join(' | ');
    },
    truncate(str, len) {
      if (!str) return '';
      return str.length > len ? str.slice(0, len) + '...' : str;
    },
    saveToHistory() {
      const label = this.buildHistoryLabel();
      const snapshot = {
        documentId: this.documentId, indices: [...this.indices], sortOrder: this.sortOrder,
        sortField: this.sortField, timeSearch: { ...this.timeSearch },
        searchFields: JSON.parse(JSON.stringify(this.searchFields)),
        updateFields: JSON.parse(JSON.stringify(this.updateFields)), label
      };
      const existing = this.queryHistory.find(h => h.label === label);
      if (existing) return;
      this.queryHistory.unshift(snapshot);
      if (this.queryHistory.length > HISTORY_MAX) this.queryHistory = this.queryHistory.slice(0, HISTORY_MAX);
      localStorage.setItem(HISTORY_KEY, JSON.stringify(this.queryHistory));
    },
    loadHistory() {
      try {
        const raw = localStorage.getItem(HISTORY_KEY);
        if (raw) this.queryHistory = JSON.parse(raw);
      } catch (e) { this.queryHistory = []; }
    },
    handleHistoryClick(cmd) {
      if (cmd === 'clear') { this.queryHistory = []; localStorage.removeItem(HISTORY_KEY); return; }
      this.documentId = cmd.documentId || '';
      this.indices = cmd.indices ? [...cmd.indices] : [];
      this.sortOrder = cmd.sortOrder || '';
      this.sortField = cmd.sortField || '';
      this.timeSearch = cmd.timeSearch || { times: [], field: '' };
      this.searchFields = cmd.searchFields ? JSON.parse(JSON.stringify(cmd.searchFields)) : [];
      this.updateFields = cmd.updateFields ? JSON.parse(JSON.stringify(cmd.updateFields)) : [];
      this.$message.success('已恢复历史查询条件');
    },
    buildHistoryLabel() {
      const parts = [];
      if (this.documentId) parts.push(`ID:${this.documentId}`);
      if (this.indices.length) parts.push(`索引:${this.indices.length}个`);
      if (this.searchFields.length) parts.push(`字段:${this.searchFields.length}个`);
      if (!parts.length) parts.push('无条件查询');
      return parts.join(' | ');
    }
  }
};
</script>

<style lang="scss" scoped>
.doc-layout {
  margin-bottom: 0;
}

.query-panel {
  max-height: calc(100vh - 64px);
  overflow-y: auto;
  margin-bottom: 0;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 2px; }

  .panel-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 12px;
    i { margin-right: 6px; color: var(--primary); }
  }

  .query-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--card-border);
  }

  .query-form {
    ::v-deep .el-collapse-item__header { font-weight: 500; font-size: 13px; color: var(--text-primary); }
    ::v-deep .el-collapse-item__content { padding-bottom: 4px; }
    .el-form-item { margin-bottom: 10px; }
    .index-helper { margin-top: 4px; display: flex; align-items: center; gap: 6px; }
    .dynamic-fields {
      .dynamic-field-row {
        display: flex; align-items: center; gap: 6px; margin-bottom: 6px;
        .el-input { flex: 1; }
      }
    }
  }
}

.result-panel {
  max-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  padding-bottom: 12px;

  .result-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .panel-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);
      i { margin-right: 6px; color: var(--primary); }
    }
  }

  .result-stats {
    margin-bottom: 12px;
    flex-shrink: 0;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 10px;
      border-radius: var(--radius);
      padding: 10px 14px;

      .stat-icon {
        width: 36px; height: 36px; border-radius: 8px;
        display: flex; align-items: center; justify-content: center;
        font-size: 16px;
      }

      .stat-label { font-size: 11px; color: var(--text-secondary); }
      .stat-value { font-size: 20px; font-weight: 700; line-height: 1.2; }
    }

    .stat-total {
      background: linear-gradient(135deg, #e6f2ff 0%, #f0f7ff 100%);
      .stat-icon { background: #d0e8ff; color: #409EFF; }
      .stat-value { color: #409EFF; }
    }

    .stat-hit {
      background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
      .stat-icon { background: #c8e6c9; color: #67C23A; }
      .stat-value { color: #67C23A; }
    }
  }

  .result-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
    padding: 8px 10px;
    background: #f8f9fa;
    border-radius: var(--radius-sm);
    flex-wrap: wrap;
    gap: 6px;
    flex-shrink: 0;

    .result-toolbar-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .result-toolbar-right { display: flex; align-items: center; gap: 6px; }
  }

  .result-content {
    flex: 1;
    overflow-y: auto;
    min-height: 0;

    &::-webkit-scrollbar { width: 5px; }
    &::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 3px; }

    ::v-deep .el-empty { padding: 30px 0; }

    .expand-json {
      padding: 12px;
      background: #f8f9fa;
      border-radius: var(--radius-sm);
    }

    .field-summary {
      font-size: 12px;
      color: var(--text-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .json-list {
    .json-result-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-left-width: 3px;
      border-radius: var(--radius);
      padding: 10px 12px;
      margin-bottom: 10px;
      transition: box-shadow 0.2s;

      &:hover { box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06); }
      &.is-selected { border-color: var(--primary); background: #f5f9ff; }

      .json-result-header {
        display: flex; justify-content: space-between; align-items: center;
        margin-bottom: 8px; padding-bottom: 8px; border-bottom: 1px solid var(--card-border);

        .json-meta { display: flex; align-items: center; gap: 6px; }
        .json-actions {
          display: flex; align-items: center; gap: 2px;
          opacity: 0.5; transition: opacity 0.2s;
        }
      }
      &:hover .json-actions { opacity: 1; }
    }
  }

  .result-pagination {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid var(--card-border);
    text-align: center;
    flex-shrink: 0;
  }
}
</style>
