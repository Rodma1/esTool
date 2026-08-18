// utils/messageUtil.js
import Vue from 'vue'; // 确保 Vue 已经安装

/**
 * 处理接口返回消息
 * @param {Promise} apiPromise axios请求的 Promise
 * @param {string} defaultSuccessMessage 默认成功消息
 * @returns {Promise<boolean>} 是否成功
 */
export async function handleApiMessage(apiPromise, defaultSuccessMessage = '操作成功') {
  try {
    const response = await apiPromise;

    if (response && response.data && response.data.code === 500) {
      // 失败消息
      Vue.prototype.$message.error(response.data.message || '操作失败');
      return false;
    } else {
      // 成功消息
      const msg = response?.data?.message || defaultSuccessMessage;
      Vue.prototype.$message.success(msg);
      return true;
    }
  } catch (err) {
    // 网络或请求异常
    Vue.prototype.$message.error(err?.message || '网络异常');
    return false;
  }
}