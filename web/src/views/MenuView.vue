<template>
  <div id="navMenu" class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
        <div class="sidebar-brand">
          <i class="el-icon-s-data brand-icon"></i>
          <span v-show="!isCollapse" class="brand-text">ES Tool</span>
        </div>
        <el-menu
          :default-active="activeIndex"
          :router="true"
          class="sidebar-menu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#9899ac"
          active-text-color="#ffffff"
          @select="handleSelect"
        >
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="/elasticsearch">
            <i class="el-icon-s-data"></i>
            <span slot="title">ES 操作</span>
          </el-menu-item>
          <el-menu-item index="/ai-assistant">
            <i class="el-icon-magic-stick"></i>
            <span slot="title">AI 助手</span>
          </el-menu-item>
          <el-menu-item index="/about">
            <i class="el-icon-info"></i>
            <span slot="title">关于我</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部栏 -->
        <el-header class="app-header" height="48px">
          <div class="header-left">
            <i
              :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
              class="toggle-btn"
              @click="toggleSidebar"
            ></i>
          </div>
          <div class="header-right">
            <div v-if="activeConnection" class="connection-badge">
              <span class="status-dot green"></span>
              <span class="connection-text">
                {{ activeConnection.hostName }}:{{ activeConnection.port }}
              </span>
              <el-tag v-if="activeConnection.versionNumber" size="mini" type="success" effect="plain" class="version-tag">
                v{{ activeConnection.versionNumber }}
              </el-tag>
            </div>
            <div v-else class="connection-badge">
              <span class="status-dot gray"></span>
              <span class="connection-text text-muted">未连接</span>
            </div>
            <el-dropdown trigger="click">
              <span class="user-profile">
                <i class="el-icon-user-solid"></i>
                <span>Admin</span>
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main class="app-main">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "navMenu",
  data() {
    return {
      activeIndex: '/home'
    };
  },
  computed: {
    isCollapse() {
      return this.$store.state.sidebarCollapsed;
    },
    activeConnection() {
      return this.$store.state.activeConnection;
    }
  },
  watch: {
    $route() {
      this.setCurrentRoute();
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
      this.activeIndex = this.$route.path;
    },
    toggleSidebar() {
      this.$store.dispatch('toggleSidebar');
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  ::v-deep .el-container {
    height: 100%;
  }
}

.sidebar {
  background: var(--sidebar-bg);
  transition: width 0.3s;
  display: flex;
  flex-direction: column;

  .sidebar-brand {
    height: 48px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    white-space: nowrap;
    overflow: hidden;

    .brand-icon {
      font-size: 22px;
      margin-right: 12px;
      color: var(--sidebar-active);
    }

    .brand-text {
      transition: opacity 0.3s;
    }
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;
    padding-top: 8px;

    ::v-deep .el-menu-item {
      height: 44px;
      line-height: 44px;
      margin: 4px 12px;
      border-radius: 6px;
      transition: all 0.2s;

      &:hover {
        background: var(--sidebar-hover) !important;
      }

      &.is-active {
        background: var(--sidebar-active) !important;
        color: #fff !important;

        i {
          color: #fff !important;
        }
      }

      i {
        color: var(--sidebar-text);
        margin-right: 8px;
      }
    }
  }
}

.app-header {
  background: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .toggle-btn {
      font-size: 18px;
      color: var(--text-secondary);
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: var(--primary);
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .connection-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 2px 10px;
      background: #f3f6f9;
      border-radius: 20px;
      font-size: 12px;

      .connection-text {
        color: var(--text-primary);
        font-weight: 500;
      }

      .version-tag {
        margin-left: 4px;
        font-size: 11px;
        line-height: 1;
        padding: 0 4px;
      }
    }

    .user-profile {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      color: var(--text-primary);
      font-size: 14px;
      font-weight: 500;

      &:hover {
        color: var(--primary);
      }
    }
  }
}

.app-main {
  background: var(--content-bg);
  padding: 0;
  overflow-y: auto;
}
</style>
