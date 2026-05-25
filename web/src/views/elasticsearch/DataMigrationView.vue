<template>
  <div class="migration-container">

    <!-- ========== 导出区 ========== -->
    <div class="app-card export-section">
      <div class="section-header">
        <div class="section-title"><i class="el-icon-download"></i> 数据导出</div>
        <el-tag size="small" type="info">当前连接: {{ connectParam.hostName }}:{{ connectParam.port }}</el-tag>
      </div>

      <el-form label-width="100px" size="small">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="源索引">
              <el-select v-model="exportForm.indexName" filterable allow-create placeholder="选择或输入索引名" style="width: 100%">
                <el-option v-for="item in sourceIndexNames" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="导出模式">
              <el-select v-model="exportForm.mode" placeholder="选择模式" style="width: 100%">
                <el-option label="全量导出" value="all"></el-option>
                <el-option label="条件筛选" value="filter"></el-option>
                <el-option label="多 ID 导出" value="ids"></el-option>
                <el-option label="多值字段" value="terms"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每批大小">
              <el-input-number v-model="exportForm.pageSize" :min="100" :max="5000" :step="100" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 条件筛选区 -->
        <div v-if="exportForm.mode === 'filter'" class="filter-area">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="时间范围">
                <el-date-picker v-model="exportForm.timeSearch.times" type="datetimerange" range-separator="至"
                  start-placeholder="开始" end-placeholder="结束" value-format="yyyy-MM-dd HH:mm:ss"
                  :default-time="['00:00:00', '23:59:59']" clearable style="width: 100%" size="small"
                ></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="时间字段">
                <el-input v-model="exportForm.timeSearch.field" placeholder="时间字段名" size="small"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="时间格式">
                <el-select v-model="exportForm.timeSearch.formatType" placeholder="选择格式" style="width: 100%" size="small">
                  <el-option label="时间戳(毫秒)" value="timestamp"></el-option>
                  <el-option label="日期字符串" value="date_string"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <div class="dynamic-fields">
            <div v-for="(field, index) in exportForm.searchFields" :key="'sf-' + index" class="dynamic-field-row">
              <el-input v-model="field.key" placeholder="字段名" size="mini"></el-input>
              <el-input v-model="field.value" placeholder="字段值" size="mini"></el-input>
              <el-button type="text" class="text-danger" icon="el-icon-delete" size="mini" @click="exportForm.searchFields.splice(index, 1)"></el-button>
            </div>
            <el-button type="text" icon="el-icon-plus" size="mini" @click="exportForm.searchFields.push({ key: '', value: '' })">添加筛选字段</el-button>
          </div>
        </div>

        <!-- 多 ID 输入区 -->
        <div v-if="exportForm.mode === 'ids'" class="ids-area">
          <el-form-item label="文档 ID 列表" label-width="100px">
            <el-input type="textarea" v-model="exportForm.idsText" placeholder="输入多个文档 ID，逗号或换行分隔" :rows="4"></el-input>
          </el-form-item>
        </div>

        <!-- 多值字段区 -->
        <div v-if="exportForm.mode === 'terms'" class="terms-area">
          <div v-for="(tf, index) in exportForm.termsFields" :key="'tf-' + index" class="terms-field-item">
            <el-row :gutter="8">
              <el-col :span="4">
                <el-input v-model="tf.key" placeholder="字段名" size="small"></el-input>
              </el-col>
              <el-col :span="18">
                <el-input v-model="tf.valuesText" placeholder="多个值，逗号或换行分隔（如: id1,id2,id3）" size="small"></el-input>
              </el-col>
              <el-col :span="2">
                <el-button type="text" class="text-danger" icon="el-icon-delete" size="small" @click="exportForm.termsFields.splice(index, 1)"></el-button>
              </el-col>
            </el-row>
          </div>
          <el-button type="text" icon="el-icon-plus" size="small" @click="exportForm.termsFields.push({ key: '', valuesText: '' })">添加字段</el-button>
        </div>

        <div class="export-actions">
          <el-button type="primary" icon="el-icon-download" :loading="exportLoading" @click="doExport">导出数据</el-button>
        </div>
      </el-form>

      <!-- 导出结果 -->
      <div v-if="exportResult" class="export-result">
        <div class="result-header">
          <span><i class="el-icon-document"></i> 导出结果 ({{ exportCount }} 条文档)</span>
          <div class="result-actions">
            <el-button type="text" icon="el-icon-document-copy" size="small" @click="copyExportData">复制</el-button>
            <el-button type="text" icon="el-icon-download" size="small" @click="downloadExportFile">下载文件</el-button>
          </div>
        </div>
        <el-input type="textarea" v-model="exportData" :rows="8" readonly class="export-textarea"></el-input>
      </div>
    </div>

    <!-- ========== 导入区 ========== -->
    <div class="app-card import-section">
      <div class="section-header">
        <div class="section-title"><i class="el-icon-upload2"></i> 数据导入</div>
      </div>

      <!-- 目标 ES 连接配置 -->
      <div class="target-connection">
        <div class="sub-title"><i class="el-icon-link"></i> 目标 ES 连接</div>
        <el-form label-width="100px" size="small">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="选择连接" label-width="80px">
                <el-select v-model="importTarget.selectKey" clearable placeholder="已保存连接" style="width: 100%" @change="selectImportTarget">
                  <el-option v-for="item in savedConnections" :key="item.hostName + ':' + item.port + ':' + item.version"
                    :label="item.hostName + ':' + item.port + ' (v' + item.version + ')'"
                    :value="item.hostName + ':' + item.port + ':' + item.version"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="协议" label-width="60px">
                <el-select v-model="importTarget.scheme" style="width: 100%">
                  <el-option label="http" value="http"></el-option>
                  <el-option label="https" value="https"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="IP" label-width="40px">
                <el-input v-model="importTarget.hostName" placeholder="IP 地址"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item label="端口" label-width="50px">
                <el-input v-model="importTarget.port" placeholder="端口"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item label="版本" label-width="50px">
                <el-input v-model="importTarget.version" placeholder="7/8/9"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="目标索引" label-width="80px">
                <el-select v-model="importTarget.indexName" filterable allow-create placeholder="选择或输入" style="width: 100%">
                  <el-option v-for="item in targetIndexNames" :key="item" :label="item" :value="item"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="用户名" label-width="80px">
                <el-input v-model="importTarget.userName" placeholder="用户名"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="密码" label-width="80px">
                <el-input v-model="importTarget.password" placeholder="密码" show-password></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-button type="success" icon="el-icon-connection" size="small" plain @click="testImportTarget" style="margin-top: 30px">连接测试</el-button>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 导入内容来源 -->
      <div class="import-content">
        <div class="sub-title"><i class="el-icon-document"></i> 导入内容</div>
        <el-radio-group v-model="importMode" size="small" style="margin-bottom: 10px">
          <el-radio-button label="paste">粘贴内容</el-radio-button>
          <el-radio-button label="file">上传文件</el-radio-button>
          <el-radio-button label="direct">从导出结果直接导入</el-radio-button>
        </el-radio-group>

        <!-- 粘贴模式 -->
        <div v-if="importMode === 'paste'">
          <el-input type="textarea" v-model="importContent" placeholder="粘贴 ES Bulk NDJSON 格式数据" :rows="8"></el-input>
        </div>

        <!-- 文件上传模式 -->
        <div v-if="importMode === 'file'">
          <el-upload
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            accept=".json,.ndjson,.txt"
            :limit="1"
          >
            <el-button type="primary" size="small" icon="el-icon-upload2">选择文件</el-button>
            <span slot="tip" class="el-upload__tip">支持 .json / .ndjson / .txt 格式的 Bulk NDJSON 文件</span>
          </el-upload>
        </div>

        <!-- 直接导入模式 -->
        <div v-if="importMode === 'direct'">
          <el-alert v-if="!exportData" title="请先在导出区完成数据导出" type="warning" :closable="false" show-icon></el-alert>
          <el-alert v-else :title="'将导出的 ' + exportCount + ' 条文档直接导入到目标索引'" type="success" :closable="false" show-icon></el-alert>
        </div>

        <div class="import-actions">
          <el-button type="primary" icon="el-icon-upload2" :loading="importLoading" :disabled="!canImport" @click="doImport">导入数据</el-button>
        </div>
      </div>

      <!-- 导入结果 -->
      <div v-if="importResult" class="import-result">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="总数">{{ importResult.total }}</el-descriptions-item>
          <el-descriptions-item label="成功">
            <el-tag type="success" size="mini">{{ importResult.success }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <el-tag v-if="importResult.errors > 0" type="danger" size="mini">{{ importResult.errors }}</el-tag>
            <el-tag v-else type="success" size="mini">0</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="importResult.hasErrors" type="danger" size="mini">有错误</el-tag>
            <el-tag v-else type="success" size="mini">全部成功</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- ========== 脚本使用帮助 ========== -->
    <div class="app-card help-section">
      <el-collapse>
        <el-collapse-item title="脚本 / curl 使用帮助" name="help">
          <div class="help-block">
            <h4>curl 直接导入到目标 ES（最快方式）</h4>
            <pre>curl -X POST "http://192.168.1.53:9200/_bulk" \
  -H "Content-Type: application/json" \
  --data-binary @export_data.json</pre>
            <p>如果目标 ES 有认证：</p>
            <pre>curl -X POST "http://192.168.1.53:9200/_bulk" \
  -H "Content-Type: application/json" \
  -u elastic:123456 \
  --data-binary @export_data.json</pre>
          </div>

          <div class="help-block">
            <h4>curl 通过系统 API 导出</h4>
            <pre>curl -X POST "http://localhost:8089/elasticsearch/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "hostName":"192.168.1.79","port":9200,"scheme":"http","version":"7",
    "operationCategory":"DOCUMENT","operationType":"EXPORT",
    "indexName":"my_index","pageSize":1000
  }'</pre>
            <p>条件筛选导出：</p>
            <pre>curl -X POST "http://localhost:8089/elasticsearch/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "hostName":"192.168.1.79","port":9200,"scheme":"http","version":"7",
    "operationCategory":"DOCUMENT","operationType":"EXPORT",
    "indexName":"my_index","pageSize":1000,
    "searchFields":[{"key":"status","value":"active"}],
    "timeSearch":{"beginTime":"2024-01-01 00:00:00","endTime":"2024-12-31 23:59:59","field":"timestamp"}
  }'</pre>
            <p>多 ID 导出：</p>
            <pre>curl -X POST "http://localhost:8089/elasticsearch/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "hostName":"192.168.1.79","port":9200,"scheme":"http","version":"7",
    "operationCategory":"DOCUMENT","operationType":"EXPORT",
    "indexName":"my_index",
    "documentIds":["id1","id2","id3"]
  }'</pre>
          </div>

          <div class="help-block">
            <h4>curl 通过系统 API 导入</h4>
            <pre>curl -X POST "http://localhost:8089/elasticsearch/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "hostName":"192.168.1.53","port":9200,"scheme":"http","version":"7",
    "operationCategory":"DOCUMENT","operationType":"IMPORT_BULK",
    "indexName":"target_index",
    "document":"&lt;bulk ndjson content&gt;"
  }'</pre>
          </div>

          <div class="help-block">
            <h4>Python 脚本示例</h4>
            <pre>import requests

# 1. 通过系统 API 导出
export_resp = requests.post('http://localhost:8089/elasticsearch/operation', json={
    "hostName": "192.168.1.79", "port": 9200, "scheme": "http", "version": "7",
    "operationCategory": "DOCUMENT", "operationType": "EXPORT",
    "indexName": "my_index", "pageSize": 1000
})
data = export_resp.json()['data']['data']

# 2. 保存到文件
with open('export_data.json', 'w') as f:
    f.write(data)

# 3. 直接用 curl 导入目标 ES（推荐）
# 或通过系统 API 导入：
import_resp = requests.post('http://localhost:8089/elasticsearch/operation', json={
    "hostName": "192.168.1.53", "port": 9200, "scheme": "http", "version": "7",
    "operationCategory": "DOCUMENT", "operationType": "IMPORT_BULK",
    "indexName": "target_index", "document": data
})
print(import_resp.json())</pre>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

  </div>
</template>

<script>
export default {
  props: { connectParam: Object },
  data() {
    return {
      // 导出相关
      sourceIndexNames: [],
      exportForm: {
        indexName: '',
        mode: 'all',
        pageSize: 1000,
        idsText: '',
        searchFields: [],
        termsFields: [],
        timeSearch: { times: [], field: '', formatType: 'timestamp' }
      },
      exportLoading: false,
      exportResult: false,
      exportData: '',
      exportCount: 0,

      // 导入相关
      savedConnections: [],
      targetIndexNames: [],
      importTarget: {
        selectKey: '',
        scheme: 'http',
        hostName: '',
        port: '',
        userName: '',
        password: '',
        version: '',
        indexName: ''
      },
      importMode: 'direct',
      importContent: '',
      importLoading: false,
      importResult: null
    };
  },
  computed: {
    canImport() {
      if (!this.importTarget.hostName || !this.importTarget.port || !this.importTarget.indexName) return false;
      if (this.importMode === 'paste') return !!this.importContent.trim();
      if (this.importMode === 'file') return !!this.importContent.trim();
      if (this.importMode === 'direct') return !!this.exportData;
      return false;
    }
  },
  created() {
    this.getSourceIndexNames();
    this.getSavedConnections();
  },
  methods: {
    async getSourceIndexNames() {
      const params = { ...this.connectParam, operationCategory: 'INDEX', operationType: 'INDEX_LIST' };
      const response = await this.axios.post('/api/elasticsearch/operation', params);
      const values = response.data.data || [];
      this.sourceIndexNames = values.map(item => item.index);
    },
    async getSavedConnections() {
      const response = await this.axios.get('/api/elasticsearch/connectParam');
      this.savedConnections = response.data.data || [];
    },
    selectImportTarget() {
      const found = this.savedConnections.find(
        item => item.hostName + ':' + item.port + ':' + item.version === this.importTarget.selectKey
      );
      if (found) {
        this.importTarget.scheme = found.scheme || 'http';
        this.importTarget.hostName = found.hostName;
        this.importTarget.port = found.port;
        this.importTarget.userName = found.userName || '';
        this.importTarget.password = found.password || '';
        this.importTarget.version = found.version || '';
        this.getTargetIndexNames();
      }
    },
    async getTargetIndexNames() {
      if (!this.importTarget.hostName || !this.importTarget.port) return;
      const params = {
        hostName: this.importTarget.hostName,
        port: Number(this.importTarget.port),
        scheme: this.importTarget.scheme,
        userName: this.importTarget.userName,
        password: this.importTarget.password,
        version: this.importTarget.version,
        operationCategory: 'INDEX', operationType: 'INDEX_LIST'
      };
      try {
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        const values = response.data.data || [];
        this.targetIndexNames = values.map(item => item.index);
      } catch (e) {
        this.targetIndexNames = [];
      }
    },
    async testImportTarget() {
      const params = {
        hostName: this.importTarget.hostName,
        port: Number(this.importTarget.port),
        scheme: this.importTarget.scheme,
        userName: this.importTarget.userName,
        password: this.importTarget.password,
        version: this.importTarget.version,
        operationCategory: 'INFO'
      };
      try {
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        if (response.data.code !== 200) {
          this.$message.error(response.data.message || '连接失败');
          return;
        }
        this.$message.success('目标 ES 连接成功: ' + response.data.data.number);
        this.getTargetIndexNames();
      } catch (e) {
        this.$message.error('目标 ES 连接失败');
      }
    },

    // ========== 导出操作 ==========
    async doExport() {
      if (!this.exportForm.indexName) {
        this.$message.warning('请选择源索引');
        return;
      }
      this.exportLoading = true;
      this.exportResult = false;
      try {
        const params = {
          ...this.connectParam,
          operationCategory: 'DOCUMENT',
          operationType: 'EXPORT',
          indexName: this.exportForm.indexName,
          pageSize: this.exportForm.pageSize
        };

        if (this.exportForm.mode === 'ids') {
          const ids = this.exportForm.idsText
            .split(/[,\n\r]/)
            .map(s => s.trim())
            .filter(s => s);
          if (ids.length === 0) {
            this.$message.warning('请输入文档 ID');
            this.exportLoading = false;
            return;
          }
          params.documentIds = ids;
        }

        if (this.exportForm.mode === 'filter') {
          if (this.exportForm.searchFields.length > 0) {
            params.searchFields = this.exportForm.searchFields.filter(f => f.key && f.value);
          }
          if (this.exportForm.timeSearch.times && this.exportForm.timeSearch.times.length === 2 && this.exportForm.timeSearch.field) {
            params.timeSearch = {
              beginTime: this.exportForm.timeSearch.times[0],
              endTime: this.exportForm.timeSearch.times[1],
              field: this.exportForm.timeSearch.field,
              formatType: this.exportForm.timeSearch.formatType || 'timestamp'
            };
          }
        }

        if (this.exportForm.mode === 'terms') {
          const termsFields = this.exportForm.termsFields
            .filter(f => f.key && f.valuesText)
            .map(f => ({
              key: f.key,
              values: f.valuesText.split(/[,\n\r]/).map(s => s.trim()).filter(s => s)
            }));
          if (termsFields.length === 0) {
            this.$message.warning('请添加多值字段条件');
            this.exportLoading = false;
            return;
          }
          params.termsFields = termsFields;
        }

        const response = await this.axios.post('/api/elasticsearch/operation', params);
        const data = response.data.data;
        this.exportData = data.data;
        this.exportCount = data.count;
        this.exportResult = true;
        this.$message.success('导出成功，共 ' + this.exportCount + ' 条文档');
      } catch (e) {
        this.$message.error('导出失败: ' + (e.response?.data?.message || e.message));
      } finally {
        this.exportLoading = false;
      }
    },
    copyExportData() {
      const el = document.createElement('textarea');
      el.value = this.exportData;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      this.$message.success('已复制到剪贴板');
    },
    downloadExportFile() {
      const blob = new Blob([this.exportData], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = this.exportForm.indexName + '_export.json';
      a.click();
      URL.revokeObjectURL(url);
      this.$message.success('文件下载成功');
    },

    // ========== 导入操作 ==========
    handleFileChange(file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.importContent = e.target.result;
      };
      reader.readAsText(file.raw);
    },
    async doImport() {
      if (!this.importTarget.hostName || !this.importTarget.port || !this.importTarget.indexName) {
        this.$message.warning('请配置目标 ES 连接和目标索引名');
        return;
      }
      let content = '';
      if (this.importMode === 'paste') {
        content = this.importContent;
      } else if (this.importMode === 'file') {
        content = this.importContent;
      } else if (this.importMode === 'direct') {
        content = this.exportData;
      }
      if (!content) {
        this.$message.warning('导入数据为空');
        return;
      }

      this.importLoading = true;
      this.importResult = null;
      try {
        const params = {
          hostName: this.importTarget.hostName,
          port: Number(this.importTarget.port),
          scheme: this.importTarget.scheme,
          userName: this.importTarget.userName,
          password: this.importTarget.password,
          version: this.importTarget.version,
          operationCategory: 'DOCUMENT',
          operationType: 'IMPORT_BULK',
          indexName: this.importTarget.indexName,
          document: content
        };
        const response = await this.axios.post('/api/elasticsearch/operation', params);
        this.importResult = response.data.data;
        if (this.importResult.hasErrors) {
          this.$message.warning('导入完成，但有 ' + this.importResult.errors + ' 条失败');
        } else {
          this.$message.success('导入成功，共 ' + this.importResult.success + ' 条');
        }
      } catch (e) {
        this.$message.error('导入失败: ' + (e.response?.data?.message || e.message));
      } finally {
        this.importLoading = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.migration-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    i { margin-right: 6px; color: var(--primary); }
  }
}

.export-section, .import-section, .help-section {
  padding: 16px 20px;
}

.filter-area, .ids-area {
  background: #f8f9fa;
  border-radius: var(--radius-sm);
  padding: 12px;
  margin-bottom: 12px;
}

.dynamic-fields {
  .dynamic-field-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
    .el-input { flex: 1; }
  }
}

.terms-area {
  background: #f8f9fa;
  border-radius: var(--radius-sm);
  padding: 12px;
  margin-bottom: 12px;
  .terms-field-item {
    margin-bottom: 8px;
  }
}

.export-actions, .import-actions {
  margin-top: 12px;
}

.export-result {
  margin-top: 16px;
  .result-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 500;
    .result-actions {
      display: flex;
      gap: 4px;
    }
  }
  .export-textarea ::v-deep .el-textarea__inner {
    font-family: 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.4;
  }
}

.target-connection {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 16px;
  .sub-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 12px;
    i { margin-right: 6px; color: var(--primary); }
  }
}

.import-content {
  .sub-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 8px;
    i { margin-right: 6px; color: var(--primary); }
  }
}

.import-result {
  margin-top: 16px;
}

.help-section {
  ::v-deep .el-collapse-item__header { font-weight: 500; }
  .help-block {
    margin-bottom: 16px;
    h4 {
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 8px;
      color: var(--text-primary);
    }
    p { font-size: 12px; color: var(--text-secondary); margin: 4px 0 8px; }
    pre {
      background: #1e1e1e;
      color: #d4d4d4;
      padding: 12px;
      border-radius: var(--radius-sm);
      font-size: 12px;
      line-height: 1.5;
      overflow-x: auto;
    }
  }
}
</style>