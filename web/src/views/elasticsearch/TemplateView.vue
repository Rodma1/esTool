<template>
    <div>
        <el-button @click="refreshList">查询</el-button>
        <el-button @click="dialogVisible = true">创建模板</el-button>

        <el-table ref="multipleTable" :data="tableData" tooltip-effect="dark" style="width: 100%"
            @selection-change="handleSelectionChange">

            <el-table-column type="expand">
                <template slot-scope="scope">
                    <el-form label-position="left" inline class="demo-table-expand">
                        <el-form-item label="关联模板">
                            <span>{{ scope.row.composedOf }}</span>
                        </el-form-item>

                    </el-form>
                </template>
            </el-table-column>

            <el-table-column type="selection" width="55">
            </el-table-column>
            <!-- <el-table-column label="日期" width="120">
                <template slot-scope="scope">{{ scope.row.date }}</template>
            </el-table-column> -->
            <el-table-column prop="name" label="模板名">
            </el-table-column>
            <el-table-column prop="indexPatterns" label="匹配规则">
            </el-table-column>
            <el-table-column fixed="right" label="操作">
                <template slot-scope="scope">
                    <el-button @click="deleteTemplate(scope.row)" type="text" size="medium">删除</el-button>
                    <el-button @click="getTemplateInfo(scope.row)" type="primary" style="margin-left: 16px;">
                        查看详情
        
                    </el-button>
                </template>
            </el-table-column>



        </el-table>
        <el-drawer title="我是标题" :visible.sync="drawer" :with-header="false">
            <el-button style="margin-left: 5%;" type="text" icon="el-icon-document-copy"
                            @click="copyJson(templateInfo)">复制</el-button>

            <json-viewer :value="templateInfo" style="font-size: 18px;" :expanded="true"></json-viewer>

        </el-drawer>

        <el-dialog title="创建模板" :visible.sync="dialogVisible" width="30%">
            <el-form ref="form" :model="createTemplateFrom" label-width="80px">
                <el-form-item label="模板名称">
                    <el-input v-model="createTemplateFrom.indexTemplate"></el-input>
                </el-form-item>

                <el-form-item label="模板内容">
                    <el-input  type="textarea" v-model="createTemplateFrom.indexTemplateContent"></el-input>
                </el-form-item>
                <!-- 其他表单项 -->
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="createTemplate" :loading="false">确 定</el-button>
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
        async createTemplate() {

            try {
                const params = this.getParams("PUT")
                params.indexTemplate = this.createTemplateFrom.indexTemplate
                params.indexTemplateContent = this.createTemplateFrom.indexTemplateContent
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                this.dialogVisible = false
            } catch (error) {
                console.log(error)
            }
        },
        deleteTemplate(row) {
            this.$confirm('此操作将删除' + row.name + '是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                const params = this.getParams("DELETE")
                params.indexTemplate = row.name
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
                this.refreshList()

            }).catch((error) => {
                console.log(error)
                this.$message.error(error)
            });
        },

        async getTemplateInfo(row) {

            try {

                const params = this.getParams("INFO")
                params.indexTemplate = row.name
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.templateInfo = response.data.data
                this.drawer = true
            } catch (error) {
                console.log(error)
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
    },
    // mounted() {
    //     this.refreshList()
    // }
}
</script>

<style>
.demo-table-expand {
    font-size: 0;
}

.demo-table-expand label {
    width: 90px;
    color: #99a9bf;
}

.demo-table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 50%;
}
</style>