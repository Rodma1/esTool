<template>
  <div class="page-wrapper">
    <div class="app-card about-card">
      <div class="about-header">
        <div class="profile-section">
          <img src="@/assets/logo.png" alt="Profile" class="profile-img">
          <div class="profile-info">
            <h1>你好，我是 神唱</h1>
            <p class="subtitle">一名程序猿，专注于后端开发，并热衷于网络知识。</p>
          </div>
        </div>
      </div>

      <el-divider></el-divider>

      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="12">
          <div class="info-block">
            <div class="block-title"><i class="el-icon-s-tools"></i> 技能</div>
            <ul class="info-list">
              <li><span class="label">编程语言:</span> JavaScript, Python, Java</li>
              <li><span class="label">框架:</span> Vue.js, Spring Boot, Django, Flask</li>
              <li><span class="label">工具:</span> Git, Docker</li>
            </ul>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="12">
          <div class="info-block">
            <div class="block-title"><i class="el-icon-s-cooperation"></i> 项目</div>
            <ul class="info-list">
              <li><a href="https://github.com/yourproject" target="_blank">es Tool</a> - es连接管理查看工具</li>
              <li><a href="https://github.com/yourproject" target="_blank">导航网站</a> - 导航网站 Java Vue</li>
            </ul>
          </div>
        </el-col>
      </el-row>

      <el-divider></el-divider>

      <div class="info-block contact-block">
        <div class="block-title"><i class="el-icon-message"></i> 联系我</div>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="8">
            <div class="contact-item">
              <i class="el-icon-message"></i>
              <span>1156028692@qq.com</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="contact-item">
              <i class="el-icon-link"></i>
              <a href="https://github.com/Rodma1?tab=repositories" target="_blank">我的 GitHub</a>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="contact-item">
              <i class="el-icon-document"></i>
              <a href="https://blog.csdn.net/weixin_46654114?type=blog" target="_blank">我的 CSDN</a>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AboutView'
}
</script>

<style lang="scss" scoped>
.about-card {
  max-width: 1000px;
  margin: 0 auto;
}

.about-header {
  text-align: center;
  padding: 20px 0;

  .profile-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .profile-img {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      object-fit: cover;
      border: 3px solid var(--primary-light);
      padding: 3px;
    }

    .profile-info {
      h1 {
        margin: 0 0 8px 0;
        font-size: 24px;
        color: var(--text-primary);
      }

      .subtitle {
        margin: 0;
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
  }
}

.info-block {
  .block-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 16px;

    i {
      margin-right: 6px;
      color: var(--primary);
    }
  }

  .info-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      padding: 8px 0;
      border-bottom: 1px solid var(--card-border);
      font-size: 14px;
      color: var(--text-secondary);

      &:last-child {
        border-bottom: none;
      }

      .label {
        font-weight: 600;
        color: var(--text-primary);
        margin-right: 8px;
      }

      a {
        color: var(--primary);
        text-decoration: none;
        font-weight: 500;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}

.contact-block {
  margin-top: 8px;

  .contact-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 0;
    font-size: 14px;
    color: var(--text-secondary);

    i {
      color: var(--primary);
      font-size: 16px;
    }

    a {
      color: var(--primary);
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
