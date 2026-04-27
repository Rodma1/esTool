import Vue from 'vue';
import VueRouter from 'vue-router';
import HomeView from '../views/HomeView.vue';
import NavMenuView from '../views/MenuView.vue';
import ElasticsearchView from '../views/ElasticsearchView.vue';
import AboutView from "@/views/AboutView.vue";
Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    component: NavMenuView,
    meta: { title: '整体页面布局' },
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'home',
        component: HomeView,
        meta: { title: '首页', icon: 'el-icon-s-home' }
      },
      {
        path: '/about',
        name: 'about',
        component: AboutView,
        meta: { title: '关于我', icon: 'el-icon-info' }
      },
      {
        path: '/elasticsearch',
        name: 'elasticsearch',
        component: ElasticsearchView,
        meta: { title: 'ES 操作', icon: 'el-icon-s-data' }
      }
    ]
  }
];

const router = new VueRouter({
  routes
});

export default router;
