<template>
    <div>
        <el-button @click="refreshList">查询</el-button>
        <el-button @click="dialogVisible = true">创建索引</el-button>
        <el-button @click="deleteIndex">删除索引</el-button>
        
        <el-button @click="aliasDialogVisible = true">关联别名</el-button>
           <!-- 创建映射按钮 -->
        <el-button @click="createMappingDialogVisible = true">创建映射</el-button>
                <!-- 检索框 -->
        <el-input 
            v-model="searchQuery" 
            placeholder="请输入索引名进行检索" 
            clearable 
            style="margin-bottom: 20px; width: 300px;">
        </el-input>


        <el-table ref="multipleTable" :data="filteredTableData" tooltip-effect="dark" style="width: 100%"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55">
            </el-table-column>
            <!-- <el-table-column label="日期" width="120">
                <template slot-scope="scope">{{ scope.row.date }}</template>
            </el-table-column> -->
            <el-table-column prop="index" label="索引名" width="120">
            </el-table-column>
            <el-table-column prop="docsCount" label="文档数" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="storeSize" label="存储大小" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="priStoreSize" label="主分片存储大小" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="health" label="健康状态" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="pri" label="主分片" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="rep" label="副分片" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="docsDeleted" label="删除文档数" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="status" label="状态" show-overflow-tooltip>
            </el-table-column>
<!--            <el-table-column fixed="right" label="操作" width="100">-->
<!--                <template slot-scope="scope">-->
<!--                    <el-button @click="handleClick(scope.row)" type="text" size="small">查看</el-button>-->
<!--                    <el-button type="text" size="small">编辑</el-button>-->
<!--                </template>-->
<!--            </el-table-column>-->
            <el-table-column label="操作" width="150">
                <template #default="scope">
                    <el-button type="text" @click="viewMapping(scope.row.index)">查看映射</el-button>
                </template>
            </el-table-column>
        </el-table>
        <template>
    <!-- 映射详情对话框 -->
    <el-dialog title="索引映射详情" :visible.sync="mappingDialogVisible" width="50%">
        <el-scrollbar style="max-height: 100%;">
            <!-- 使用 vue-json-viewer 显示 JSON 数据 -->
            <json-viewer 
                :key="jsonViewerKey" 
                :value="mappingData" 
                copyable 
                boxed 
                :expand-depth="expandDepth">
            </json-viewer>
        </el-scrollbar>
        <span slot="footer" class="dialog-footer">
            <el-button @click="expandAll">展开所有</el-button>
            <el-button @click="collapseAll">折叠所有</el-button>
            <el-button @click="mappingDialogVisible = false">关闭</el-button>
        </span>
    </el-dialog>
    </template>

        <el-dialog title="创建索引" :visible.sync="dialogVisible" width="30%">
            <el-form ref="form" :model="createIndexFrom" label-width="80px">
                <el-form-item label="索引名称">
                    <el-input v-model="createIndexFrom.indexName"></el-input>
                </el-form-item>
                <!-- 其他表单项 -->
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="createIndex" :loading="false">确 定</el-button>
            </span>
        </el-dialog>

        <el-dialog title="关联别名" :visible.sync="aliasDialogVisible" width="30%">
            <el-form ref="form" :model="associationAliasFrom" label-width="80px">
                <el-form-item label="别名名称">
                    <el-input v-model="associationAliasFrom.alias"></el-input>
                </el-form-item>
                <!-- 其他表单项 -->
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="aliasDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="associationAlias" :loading="false">确 定</el-button>
            </span>
        </el-dialog>
               <!-- 创建映射对话框 -->
        <el-dialog title="创建映射" :visible.sync="createMappingDialogVisible" width="50%">
            <el-form ref="createMappingForm" :model="createMappingData" label-width="120px">
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
        <!-- 创建映射对话框 -->
        <el-dialog title="创建映射" :visible.sync="createMappingDialogVisible" width="50%">
            <el-form ref="createMappingForm" :model="createMappingData" label-width="120px">
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
export default {
    components: {
        JsonViewer, // 注册 vue-jsoneditor 组件
    },
    props: {
        connectParam: Object,
    },
    data() {
        return {
            searchQuery: '', // 检索框绑定的值
            tableData: [{
                index: '',
                docsCount: '',
                storeSize: '',
                priStoreSize: '',
                health: '',
                uuid: '',
                pri: '',
                rep: '',
                docsDeleted: '',
                status: '',
            }],
            mappingDialogVisible: false, // 控制映射详情对话框的显示
            mappingData: {}, // 存储映射数据
            multipleSelection: [],
            jsonViewerKey: 0, // 用于强制刷新 json-viewer
            expandDepth: 4, // 默认展开的层级
            operationCategory: "INDEX",
            dialogVisible: false,
            aliasDialogVisible: false,
            createIndexFrom: {
                indexName: ''
            },
            associationAliasFrom: {
                alias: ''
            },
            createMappingDialogVisible: false, // 控制创建映射对话框的显示
            createMappingData: {
                indexName: '', // 索引名称
                mapping: '', // 映射内容（JSON 格式）
            },
        }
    },
    computed: {
        filteredTableData() {
            // 根据检索框的值动态过滤表格数据
            return this.tableData.filter(item =>
                item.index.toLowerCase().includes(this.searchQuery.toLowerCase())
            );
        }
    },
    methods: {
        handleSelectionChange(val) {
            this.multipleSelection = [];
            val.forEach(element => {
                this.multipleSelection.push(element.index)
            });
        },
        handleClick(row) {
            console.log(row);
        },
        refreshList() {
            // 执行刷新列表的操作
            this.fetchData();
        },

        getParams(operationType) {
            const params = this.connectParam
            params.operationCategory = this.operationCategory
            params.operationType = operationType
            return params
        },

        async fetchData() {
            // 假设使用axios发起请求获取数据

            try {
                const params = this.getParams("INDEX_LIST")
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.tableData = response.data.data
                console.log(this.tableData)
            } catch (error) {
                console.log(error)
            }


        },
        async createIndex() {

            try {
                const params = this.getParams("CREATE")
                params.indexName = this.createIndexFrom.indexName
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                console.log(response.data)
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                await this.fetchData()
                this.createIndexFrom.indexName = ''
            } catch (error) {
                console.log(error)
            }

            // 表单验证和提交逻辑
            this.dialogVisible = false;
        },

        /**
         * 关联别名
         */
        async associationAlias() {

            try {
                const params = this.getParams("INSERT")
                params.indices = this.multipleSelection
                params.operationCategory = "ALIAS"
                params.alias = this.associationAliasFrom.alias
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                this.associationAliasFrom.alias = ''
                params.indices = []
            } catch (error) {
                console.log(error)
            }

            // 表单验证和提交逻辑
            this.aliasDialogVisible = false;
        },

        deleteIndex() {

            this.$confirm('此操作将永久删除选中的索引, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                const params = this.getParams("BATCH_DELETE")
                params.indices = this.multipleSelection
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                console.log(response.data)
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                await this.fetchData()

            }).catch((error) => {
                console.log(error)
            });
        },
        async viewMapping(indexName) {
            try {
                const params = this.getParams("QUERY")

                params.operationCategory = "MAPPING"
                params.operationType = "QUERY"
                params.indexName = indexName
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.mappingData = response.data.data; // 假设返回的映射数据在 data.data 中
                this.mappingDialogVisible = true; // 打开对话框
            } catch (error) {
                console.error('获取索引映射失败:', error);
                this.$message.error('获取索引映射失败，请检查后端接口');
            }
        },
        expandAll() {
            this.expandDepth = Infinity; // 展开所有层级
            this.refreshJsonViewer(); // 强制刷新组件
        },
        collapseAll() {
            this.expandDepth = 0; // 折叠所有层级
            this.refreshJsonViewer(); // 强制刷新组件
        },
        refreshJsonViewer() {
            this.jsonViewerKey += 1; // 修改 key，强制重新渲染组件
        },
        async submitMapping() {
            try {
                // 验证映射内容是否为合法 JSON
                const parsedMapping = JSON.parse(this.createMappingData.mapping);
                const params = this.getParams("CREATE")
                params.operationCategory = "MAPPING"
                params.indexName = this.createMappingData.indexName
                params.mapping = JSON.stringify(parsedMapping) // 将映射内容转为字符串
    

                // 调用后端接口
                const response = await this.axios.post('/api/elasticsearch/operation', params);

                // 显示成功消息
                this.$message({
                    message: response.data.message || '映射创建成功',
                    type: 'success',
                });

                // 关闭对话框并清空表单
                this.createMappingDialogVisible = false;
                this.createMappingData.indexName = '';
                this.createMappingData.mapping = '';
            } catch (error) {
                if (error instanceof SyntaxError) {
                    // JSON 格式错误
                    this.$message.error('映射内容必须是合法的 JSON 格式');
                } else {
                    // 接口调用失败
                    console.error('创建映射失败:', error);
                    this.$message.error('创建映射失败，请检查后端接口');
                }
            }
        },
    },
    // mounted() {
    //     this.fetchData()
    // }
}
</script>
<style scoped>

</style>