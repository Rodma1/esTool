<template>
    <div style="display: flex; justify-content: space-between;">
        <!-- 查询条件 -->
        <div style="width: 500px; overflow: hidden;">
            <div style="display: flex; align-items: center; margin-bottom: 10px;">
                <el-button @click="refreshList">查询</el-button>
                <el-button type="danger" @click="batchDelete">批量删除</el-button>
                <el-button type="primary" @click="showCreateDialog">创建 JSON 数据</el-button>
            </div>
            <div style="display: flex; align-items: center; margin-bottom: 10px;">

            <el-input style="flex: 1; margin-right: 10px;" v-model="documentId" placeholder="根据文章Id查询"></el-input>
                <el-select v-model="indices" multiple placeholder="查询的索引" style="flex: 1;">
                    <el-option v-for="item in indexNames" :key="item" :label="item" :value="item"></el-option>
                </el-select>
            </div>
            <div style="display: flex; align-items: center; margin-bottom: 10px;">
                <el-select v-model="sortOrder" placeholder="排序类型" style="margin-right: 10px; flex: 0 0 100px;">
                    <el-option label="Asc" value="Asc"></el-option>
                    <el-option label="Desc" value="Desc"></el-option>
                </el-select>
                <el-input style="flex: 1; margin-right: 10px;" v-model="sortField" placeholder="要排序的字段"></el-input>

            </div>
            <el-form :model="timeSearch">
                <div class="block">
                    <span class="demonstration">输入时间</span>
                    <el-date-picker
                        v-model="timeSearch.times"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        :default-time="['00:00:00', '23:59:59']"
                        clearable
                    >

                    </el-date-picker>
                    <el-form-item>
                        <el-input style="max-width: 150px;" v-model="timeSearch.field" placeholder="时间排序字段"></el-input>
                    </el-form-item>
                </div>
            </el-form>
            <el-form :model="searchFields">
                <el-form-item
                    v-for="(domain, index) in searchFields"
                    :label="'字段' + index"
                    :key="'字段' + index"
                    :prop="'domains.' + index + '.value'"
                    :rules="{ required: true, message: '字段不能为空', trigger: 'blur' }"
                    style="display: flex; align-items: center; flex-wrap: nowrap;"
                >
                    <el-input style=" max-width: 150px;" v-model="domain.key"></el-input>
                    <el-input style="margin-right: 5px; max-width: 150px;" v-model="domain.value"></el-input>
                    <el-button @click.prevent="removeDomain(domain)" style="white-space: nowrap;">删除</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="addDomain">新增查询字段</el-button>
                </el-form-item>
            </el-form>

        </div>

        <!-- JSON 数据区域 -->
        <div class="json-viewer-container" >
            <b style="margin-left: 5%;">总数： {{ count }}</b>
            <b style="margin-left: 5%;">查询数: {{searchCount}}</b>

            <el-dialog title="创建 JSON 数据" :visible.sync="createDialogVisible" width="30%" @close="resetCreateForm">
                <el-input type="textarea" v-model="newJson" placeholder="请输入 JSON 数据" :rows="10"></el-input>
                <el-select v-model="selectIndex" clearable placeholder="请选择">
                    <el-option v-for="item in indexNames" :key="item" :label="item" :value="item"></el-option>
                </el-select>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="createDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="createJson">创建</el-button>
                </span>
            </el-dialog>
            <div v-for="item in jsonData" :key="item.id" class="json-item">
                <el-checkbox v-model="selectedItems" :label="item.id" class="custom-checkbox">
                    <div class="checkbox-content">
                        <span class="index-name">索引名: {{ item.index }}</span>
                        <span class="index-id">索引id: {{ item.id }}</span>
                    </div>
                </el-checkbox>
                <el-button style="margin-left: 5%;" type="text" icon="el-icon-document-copy" @click="copyJson(item)">复制</el-button>
                <json-viewer :value="item.source" style="font-size: 18px;" :expanded="true"></json-viewer>
            </div>
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage"
                           :page-size="pageSize" :total="count" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
        </div>
    </div>
</template>
<script>

export default {
    props: {
        connectParam: Object,
    },

    data() {
        return {
            jsonData: [
            ],
            createDialogVisible: false,
            newJson: '',
            currentPage: 1,
            pageSize: 10,
            selectedItems: [],
            operationCategory: "DOCUMENT",
            count: 0,
            indexNames: [],
            selectIndex: '',
            documentId: '',
            indices:[],
            sortOrder: '',
            sortField: '',
            searchFields: [],
            searchCount: 0,
            timeSearch: {
                times: [],
                field: '' // 其他字段
            }

        };
    },

    methods: {
        // 刷新
        refreshList() {
            // 执行刷新列表的操作
            this.getDocumentsPage()
            this.getDocumentCount()
            this.currentPage = 1
            this.pageSize = 10
        },
        //  获取链接请求参数
        getParams(operationType) {
            const params = this.connectParam
            params.operationCategory = this.operationCategory
            params.operationType = operationType
            return params
        },
        //  获取文档列表数据

        async getDocumentsPage() {
            // 假设使用axios发起请求获取数据

            try {
                let names = [];
                if (this.indices.length === 0 || !this.indices) {
                    await this.getindexNames()
                    names =  this.indexNames;
                } else {
                    names = this.indices;
                }


                const params = this.getParams("PAGE")
                if ( this.timeSearch.times && this.timeSearch.times.length !== 0 && this.timeSearch.field) {
                    const timesSearch = {}
                    // const timesArray = this.times.split(',');
                    timesSearch.beginTime = this.timeSearch.times[0]
                    timesSearch.endTime = this.timeSearch.times[1]
                    timesSearch.field = this.timeSearch.field
                    params.timeSearch = timesSearch
                } else {
                    params.timeSearch = {}
                }
                params.pageSize = this.pageSize
                params.pageNum = this.currentPage
                params.documentId = this.documentId
                params.sortField = this.sortField;
                params.sortOrder = this.sortOrder;
                params.indices = names
                params.searchFields = this.searchFields;
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.jsonData = response.data.data.rows;
                this.searchCount = response.data.data.count
            } catch (error) {
                console.log(error)
            }


        },
        showCreateDialog() {
            this.createDialogVisible = true;
            this.getindexNames()

        },
        resetCreateForm() {
            this.newJson = '';
        },
        async createJson() {
            try {
                const parsedJson = JSON.parse(this.newJson);
                if (Array.isArray(parsedJson)) {
                    parsedJson.forEach(item => {
                        this.jsonData.push({ ...item, id: this.jsonData.length + 1 });
                    });
                } else {
                    // this.jsonData.push({ ...parsedJson, id: this.jsonData.length + 1 });
                    const params = this.getParams("INSERT")
                    params.document = JSON.stringify(parsedJson);
                    params.indexName = this.selectIndex
                    const response = await this.axios.post('/api/elasticsearch/operation', params);
                    this.refreshList()
                    this.$message({
                        message: response.data.message,
                        type: 'success'
                    });
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
            this.getDocumentsPage()

        },
        handleCurrentChange(page) {
            this.currentPage = page;
            this.getDocumentsPage()
        },

        async getindexNames() {
            const params = this.getParams("INDEX_LIST")
            params.operationCategory = "INDEX"
            const response = await this.axios.post('/api/elasticsearch/operation', params);
            const values = response.data.data
            const indexNames = []
            values.forEach(item => {
                indexNames.push(item.index)
            })
            this.indexNames = indexNames;
        },

        async batchDelete() {

            // console.log(this.selectedItems)
            // this.jsonData = this.jsonData.filter(item => !this.selectedItems.includes(item.id));

            this.$confirm('此操作将永久删除选中的索引文档, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {

                const params = this.getParams("DELETE")
                params.documentIds = this.selectedItems
                params.indices = this.indexNames
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.selectedItems = [];
                this.refreshList()
                this.$message({
                    message: response.data.message,
                    type: 'success'
                });
            }).catch((error) => {
                console.log(error)
            });



        },
        // 获取总数
        async getDocumentCount() {
            const params = this.getParams("COUNT")
            const response = await this.axios.post('/api/elasticsearch/operation', params);
            this.count = response.data.data
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
        // resetForm(formName) {
        //     this.$refs[formName].resetFields();
        // },
        removeDomain(item) {
            const index = this.searchFields.indexOf(item);
            if (index !== -1) {
                this.searchFields.splice(index, 1)
            }
        },
        addDomain() {
            this.searchFields.push({key: '', value: ''});
        }
    },

    // mounted() {
    //     this.getindexNames()
    // }
};
</script>

<style>
.json-viewer-container {
    max-width: 60%;
    margin: 10px auto;
    padding: 20px;
    background-color: #f9f9f9;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.json-item {
    margin-bottom: 20px;
    padding: 10px;
    border-bottom: 1px solid #eee;
    position: relative;
}

.json-viewer-container .json-viewer {
    font-family: 'Courier New', Courier, monospace;
    font-size: 14px;
    line-height: 1.6;
}

.el-checkbox {
    margin-right: 10px;
}

.custom-checkbox {
    font-size: 14px;
    display: flex;
    align-items: center;
}

.checkbox-content {
    display: flex;
    align-items: center;
    padding: 5px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background-color: #f9f9f9;
    margin-left: 5px;
}

.index-name {
    margin-right: 10px;
    font-weight: bold;
    color: #333;
}

.index-id {
    color: #555;
}
</style>
