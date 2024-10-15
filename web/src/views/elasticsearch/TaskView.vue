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
            <el-table-column prop="task_id" label="任务Id" >
            </el-table-column>
            <el-table-column prop="type" label="类型">
            </el-table-column>
            <el-table-column prop="start_time" label="启动时间">
            </el-table-column>
            <el-table-column prop="running_time" label="运行时间">
            </el-table-column>
            <el-table-column prop="ip" label="地址">
            </el-table-column>
            <el-table-column fixed="right" label="操作">
                <template slot-scope="scope">
                    <el-button @click="stopTask(scope.row)" type="text" size="medium">停止任务</el-button>
                </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作">
                <template slot-scope="scope">
                    <el-button @click="taskInfo(scope.row)" type="text" size="medium">查看详情</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 右侧滑出的抽屉 -->
        <el-drawer
            title="任务详情"
            :visible.sync="drawerVisible"
            direction="rtl"
            size="30%">
            <pre>{{ formattedInfo }}</pre>
        </el-drawer>
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
            operationCategory: "TASK",
            drawerVisible: false,
            info: {}
        }
    },
    computed: {
        // 格式化 JSON 数据以便在弹窗中展示
        formattedInfo() {
            return JSON.stringify(this.info, null, 2);
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

        stopTask(row) {
            console.log(row)
            this.$confirm('此操作将取消任务 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                const params = this.getParams("PUT")
                params.taskId = row.task_id
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
        async taskInfo(row) {
            // 假设使用axios发起请求获取数据

            try {
                const params = this.getParams("INFO")
                params.taskId = row.task_id
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.info = response.data.data
                this.drawerVisible = true;  // 打开弹窗
            } catch (error) {
                console.log(error)
            }
        },
        handleDialogClose() {
            this.info = {};  // 清空 info 数据
        }
    },
    // mounted() {
    //     this.refreshList()
    // }
}
</script>
