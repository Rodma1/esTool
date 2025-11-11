<template>
    <div>
        <!-- 输入文本框和选择分词器 -->
        <el-form :model="formData" label-width="80px">
            <el-form-item label="索引名">
                <el-input v-model="formData.indexName" placeholder="请输入索引名"></el-input>
            </el-form-item>

            <el-form-item label="分词器">
                <el-select v-model="formData.analyzer" placeholder="请选择分词器">
                    <el-option v-for="(item, index) in analyzersMap" :key="index" :label="item" :value="item"></el-option>
                </el-select>
                <el-button type="text" size="small" @click="fetchAnalyzerOperations" style="margin-top: 10px;">
                    刷新分词器
                </el-button>
                <el-button type="text" size="small" @click="toggleAnalyzerList" style="margin-top: 10px; margin-left: 10px;">
                    {{ isAnalyzerListVisible ? '隐藏分词器列表' : '显示分词器列表' }}
                </el-button>
            </el-form-item>

            <!-- 分词器列表展示 -->
            <el-table v-if="isAnalyzerListVisible && analyzers.length > 0" :data="analyzers" style="width: 100%; margin-top: 20px;">
                <el-table-column label="组件" prop="component"></el-table-column>
                <el-table-column label="名称" prop="name"></el-table-column>
                <el-table-column label="版本" prop="version"></el-table-column>
            </el-table>

            <el-form-item label="文本">
                <el-input type="textarea" v-model="formData.document" placeholder="请输入文本" rows="4"></el-input>
            </el-form-item>

            <el-button type="primary" @click="analyzeText">分词</el-button>
        </el-form>

        <!-- 分词结果展示 -->
        <el-table :data="analysisResult" style="width: 100%" v-if="analysisResult.length > 0">
            <el-table-column label="分词结果">
                <template slot-scope="scope">
                    {{ scope.row }}
                </template>
            </el-table-column>
        </el-table>

        <!-- 错误信息展示 -->
        <el-alert v-if="errorMessage" type="error" :title="errorMessage"></el-alert>
    </div>
</template>

<script>
export default {
    props: {
        connectParam: Object,
    },
    data() {
        return {
            formData: {
                indexName: '',  // 索引名
                analyzer: '',  // 分词器，初始化为空，后续从接口获取
                document: ''  // 输入文本
            },
            analyzers: [],  // 分词器列表
            analysisResult: [],  // 存放分词结果
            operationCategory: "ANALYZE",
            errorMessage: '',  // 错误信息
            isAnalyzerListVisible: false,  // 控制分词器列表显示与隐藏
            analyzersMap: {}
        };
    },
    methods: {
        // 获取请求参数
        getParams(operationType) {
            const params = this.connectParam;
            params.operationCategory = this.operationCategory;
            params.operationType = operationType;
            return params;
        },

        // 获取分词器列表
        async fetchAnalyzers() {
            this.errorMessage = '';  // 清除之前的错误信息
            try {
                // 获取请求参数
                const params = this.getParams("PLUGINS");

                // 请求后端接口获取分词器列表
                const response = await this.axios.post('/api/elasticsearch/operation', params);

                // 假设返回的分词器列表在 response.data.data 中
                this.analyzers = response.data.data;

            } catch (error) {
                this.errorMessage = '无法获取分词器列表。';
                console.error(error);
            }
        },
        // 获取分词器操作列表
        async fetchAnalyzerOperations() {
            this.errorMessage = '';  // 清除之前的错误信息
            try {
                // 获取请求参数
                const params = this.getParams("ANALYZERS");

                // 请求后端接口获取分词器列表
                const response = await this.axios.post('/api/elasticsearch/operation', params);

                // 从 Map 数据中提取出分词器名称
                this.analyzersMap = Object.keys(response.data.data);  // 提取 Map 的键作为分词器名称
                // 默认选择第一个分词器
                if (this.analyzersMap.length > 0) {
                    this.formData.analyzer = this.analyzersMap[0];
                }
            } catch (error) {
                this.errorMessage = '无法获取分词器列表。';
                console.error(error);
            }

        },

        // 切换分词器列表的显示和隐藏
        toggleAnalyzerList() {
            this.isAnalyzerListVisible = !this.isAnalyzerListVisible;
        },

        // 分词操作
        async analyzeText() {
            this.errorMessage = '';  // 清除之前的错误信息
            try {
                // 获取请求参数
                const params = this.getParams("ANALYZE");

                // 请求数据
                params.indexName = this.formData.indexName
                params.document = this.formData.document
                params.analyzer = this.formData.analyzer

                // 发送请求到后端
                const response = await this.axios.post('/api/elasticsearch/operation', params);
                this.analysisResult = response.data.data;  // 返回的分词结果
            } catch (error) {
                this.errorMessage = '分词失败，请检查输入的索引和文本。';
                console.error(error);
            }
        }
    },

    // 页面加载时获取分词器列表
    mounted() {
        this.fetchAnalyzers();  // 获取分词器列表
        this.fetchAnalyzerOperations();
    }
};
</script>

<style scoped>
.el-table {
    margin-top: 20px;
}
</style>
