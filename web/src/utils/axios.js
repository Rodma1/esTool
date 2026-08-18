// src/utils/axios.js
import axios from 'axios';

const instance = axios.create({
    // baseURL: process.env.VUE_APP_BASE_API, // 设置基础 URL
    timeout: 10000, // 默认请求超时 10 秒
});

// AI 接口需要更长的超时时间（LLM 调用 + ES 执行可能耗时较长）
instance.interceptors.request.use(config => {
    if (config.url && config.url.startsWith('/api/ai/')) {
        config.timeout = 600000; // AI 接口 5分钟 秒超时
    }
    return config;
});

export default instance;
