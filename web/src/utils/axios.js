// src/utils/axios.js
import axios from 'axios';

const instance = axios.create({
    // baseURL: process.env.VUE_APP_BASE_API, // 设置基础 URL
    timeout: 10000, // 设置请求超时
});
export default instance;
