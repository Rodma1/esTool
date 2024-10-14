<template>
    <div>
        <el-button @click="refreshList">查询</el-button>
        <el-table ref="multipleTable" :data="tableData" tooltip-effect="dark" style="width: 100%"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55">
            </el-table-column>
            <!-- <el-table-column label="日期" width="120">
                <template slot-scope="scope">{{ scope.row.date }}</template>
            </el-table-column> -->
            <el-table-column prop="alias" label="别名" >
            </el-table-column>
            <el-table-column prop="index" label="文档名">
            </el-table-column>
            <el-table-column prop="isWriteIndex" label="是否是写入索引" show-overflow-tooltip>
            </el-table-column>
            <el-table-column fixed="right" label="操作">
                <template slot-scope="scope">
                    <el-button @click="deleteAlias(scope.row)" type="text" size="medium">删除</el-button>
                    <el-button @click="updateAliasWriteIndex(scope.row)" type="text" size="medium">设为写入索引</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>
  
<script>
export default {
    props: {
        connectParam: Object,
    },
    data() {
        return {
            tableData: [],
            multipleSelection: [],
            operationCategory: "ALIAS",
            dialogVisible: false,
        }
    },

    methods: {
        handleSelectionChange(val) {
            this.multipleSelection = [];
            val.forEach(element => {
                this.multipleSelection.push(element.index)
            });
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
                const params = this.getParams("LIST")
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.tableData = response.data.data
                console.log(this.tableData)
            } catch (error) {
                console.log(error)
            }


        },
        async updateAliasWriteIndex(row) {

            try {
                const params = this.getParams("WRITE_INDEX")
                params.indexName = row.index
                params.alias = row.alias
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                this.refreshList()
            } catch (error) {
                console.log(error)
            }

            // 表单验证和提交逻辑
            this.dialogVisible = false;
        },
        deleteAlias(row) {
            console.log(row)
            this.$confirm('此操作将取消别名'+ row.alias +'和索引' + row.index +'的关联, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                const params = this.getParams("DISASSOCIATION")
                params.alias = row.alias
                params.indices = [row.index]
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                this.refreshList()

            }).catch((error) => {
                console.log(error)
            });
        },
    },
    // mounted() {
    //     this.refreshList()
    // }
}
</script>