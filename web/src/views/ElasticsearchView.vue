<template>
    <div>

        <el-form ref="form" :inline="true" :model="form" label-width="80px">
            <el-form-item label="选择地址: ">
                <el-select v-model="selectHostName"  clearable  placeholder="选择连接地址"  @change="selectConnectParam()">
                    <el-option
                        v-for="item in connectForm"
                        :key="item.hostName"
                        :label="item.hostName"
                        :value="item.hostName">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="协议">
                <el-select v-model="form.scheme" clearable  placeholder="请选择协议">
                    <el-option label="http" value="http"></el-option>
                    <el-option label="https" value="https"></el-option>
                </el-select>


            </el-form-item>
            <el-form-item label="IP地址">
                <el-input style="width: 300px;" v-model="form.hostName"></el-input>
            </el-form-item>
            <el-form-item label="用户名">
                <el-input v-model="form.userName"></el-input>
            </el-form-item>
            <el-form-item label="密码">
                <el-input v-model="form.password"></el-input>
            </el-form-item>
            <el-form-item label="端口">
                <el-input v-model="form.port"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" :plain="true" @click="linkTest">链接测试</el-button>
            </el-form-item>
            <el-form-item>
                <div class="text item">

                    {{ '版本 ：' + versionInfo.number }}
                    <el-divider direction="vertical"></el-divider>
                    {{ '安装类型 ：' + versionInfo.buildType }}
                    <el-divider direction="vertical"></el-divider>
                    {{ 'lucene版本 ：' + versionInfo.luceneVersion }}
                </div>
            </el-form-item>


        </el-form>





        <el-tabs v-model="activeName" @tab-click="handleTabClick">
            <el-tab-pane label="索引操作" name="first"><ElasticIndicesView :connectParam="form"/></el-tab-pane>
            <el-tab-pane label="文档操作" name="second"><DocumentView :connectParam="form"/></el-tab-pane>
            <el-tab-pane label="别名操作" name="third"><AliasesView :connectParam="form"/></el-tab-pane>
            <el-tab-pane label="模板列表" name="fourth"><TemplateView :connectParam="form"></TemplateView></el-tab-pane>
            <el-tab-pane label="任务列表" name="fifty"><TaskView :connectParam="form"></TaskView></el-tab-pane>
            <el-tab-pane label="分析器" name="sixth"><AnalyzeView :connectParam="form"></AnalyzeView></el-tab-pane>
        </el-tabs>


    </div>
</template>

<script>
import ElasticIndicesView from "./elasticsearch/ElasticIndicesView.vue";
import DocumentView from "./elasticsearch/DocumentView.vue"
import AliasesView from "./elasticsearch/AliasesView.vue"
import TemplateView from "./elasticsearch/TemplateView.vue"
import TaskView from "@/views/elasticsearch/TaskView.vue";
import AnalyzeView from "@/views/elasticsearch/AnalyzeView.vue";

export default {
    components: {TaskView, ElasticIndicesView, DocumentView, AliasesView, TemplateView , AnalyzeView},
    data() {
        return {
            form: {
                scheme: '',
                hostName: '',
                userName: '',
                password: '',
                port: '',
                operationCategory: 'INFO',

            },
            connectForm: [],
            versionInfo: {
                "number": "-",
                "buildType": "-",
                "luceneVersion": "-"
            },
            activeName: '',
            selectHostName: '',
        }
    },
    methods: {
        async linkTest() {
            try {
                const params = this.form
                params.operationCategory = "INFO"
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                if (response.data.code !== 200) {
                    this.$message({
                        message: response.data.message,
                        type: 'error'
                    });
                    return
                }
                this.$message({
                    message: '链接成功',
                    type: 'success'
                });
                this.versionInfo = response.data.data
                this.activeName = ''
            } catch (error) {
                this.$message.error('链接失败');
            }

        },
        handleTabClick(tab) {
            this.activeName = tab.name
        },
        async getConnectForm() {
            const response = await this.axios.get('/api/elasticsearch/connectParam');
            this.connectForm = response.data.data
        },
        selectConnectParam(){
            this.form = this.connectForm.find(item1 => item1.hostName === this.selectHostName)
        },

    },
    mounted() {
        this.getConnectForm()
    }
}
</script>

<style>
.text {
    font-size: 14px;
}


.box-card {
    width: 480px;

}
</style>
