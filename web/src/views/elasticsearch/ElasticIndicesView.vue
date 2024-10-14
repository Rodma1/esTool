<template>
    <div>
        <el-button @click="refreshList">查询</el-button>
        <el-button @click="dialogVisible = true">创建索引</el-button>
        <el-button @click="deleteIndex">删除索引</el-button>
        <el-button @click="aliasDialogVisible = true">关联别名</el-button>

        <el-table ref="multipleTable" :data="tableData" tooltip-effect="dark" style="width: 100%"
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
            <el-table-column fixed="right" label="操作" width="100">
                <template slot-scope="scope">
                    <el-button @click="handleClick(scope.row)" type="text" size="small">查看</el-button>
                    <el-button type="text" size="small">编辑</el-button>
                </template>
            </el-table-column>
        </el-table>


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
    </div>
</template>

<script>
export default {
    props: {
        connectParam: Object,
    },
    data() {
        return {
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
            multipleSelection: [],
            operationCategory: "INDEX",
            dialogVisible: false,
            aliasDialogVisible: false,
            createIndexFrom: {
                indexName: ''
            },
            associationAliasFrom: {
                alias: ''
            },
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
    },
    // mounted() {
    //     this.fetchData()
    // }
}
</script>
