<template>
    <div id="navMenu">
        <el-container>
            <el-header style="padding: 0; height: 70px;">
                <el-menu
                    :default-active="activeIndex"
                    :router="true"
                    class="el-menu-demo"
                    mode="horizontal"
                    @select="handleSelect"
                >
                    <!-- 动态渲染菜单项 -->
                    <el-menu-item
                        v-for="(item, index) in filteredItemList"
                        :key="index"
                        :index="item.path"
                        style="height: 100%; line-height: 70px;"
                    >
                        <span>{{ item.title }}</span>
                    </el-menu-item>
                </el-menu>
            </el-header>
            <el-main>
                <router-view></router-view>
            </el-main>
        </el-container>
    </div>
</template>

<script>
export default {
    name: "navMenu",
    data() {
        return {
            activeIndex: 'home',
            itemList: [
                { path: '/home', title: '首页' },
                { path: '/about', title: '关于我' },
                { path: '/elasticsearch', title: 'es的基本操作'},
            ],
            isLoggedIn: false,  // 登录状态
        };
    },
    watch: {
        $route() {
            this.setCurrentRoute();
        }
    },
    computed: {
        filteredItemList() {
            return this.itemList;
        }
    },
    created() {
        this.setCurrentRoute();
    },
    methods: {
        handleSelect(path) {
            this.activeIndex = path;
        },
        setCurrentRoute() {
            this.activeIndex = this.$route.name; // 激活当前菜单
        },
    }
}
</script>

<style>
.el-menu-demo {
    background-color: #ADD8E6 !important;
    border-radius: 10px;
}

/* 使用 Flexbox 来调整按钮的排列 */
.auth-buttons {
    display: flex;
    align-items: center; /* 垂直居中 */
    margin-left: auto;  /* 将按钮推到右侧 */
}

.nav-button {
    margin-left: 10px;  /* 按钮之间的间距 */
}
</style>
