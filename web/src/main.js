import Vue from 'vue'
import App from './App.vue'
// import NavMenuView from './views/NavMenuView'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from '@/utils/axios';
import JsonViewer from 'vue-json-viewer'
import JsonEditor  from 'vue-jsoneditor';

Vue.prototype.axios = axios
Vue.use(JsonViewer)
Vue.use(JsonEditor)
Vue.config.productionTip = false
Vue.use(ElementUI)
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
