<template>
    <div class="about">
      <div class="container">
        <div class="header">
          <h1>关于我</h1>
        </div>
        <div class="content">
          <div class="profile-card">
            <img src="@/assets/logo.png" alt="Profile Picture" class="profile-img">
            <div class="profile-info">
              <h2>你好，我是 神唱</h2>
              <p>一名 程序猿。我专注于 后端开发，并热衷于 网络知识。</p>
            </div>
          </div>
          <div class="skills-card">
            <h2>技能</h2>
            <ul>
              <li>编程语言：JavaScript, Python, Java</li>
              <li>框架：Vue.js,  Spring Boot, Django, Flask</li>
              <li>工具：Git, Docker </li>
            </ul>
          </div>
          <div class="projects-card">
            <h2>项目</h2>
            <ul>
              <li><a href="https://github.com/yourproject">日审</a> - 收集日志，管理日志</li>
              <li><a href="https://github.com/yourproject">硬waf</a> - waf防火墙 java</li>
              <li><a href="https://github.com/yourproject">堡垒机</a> - 堡垒机 python</li>
              <li><a href="https://github.com/yourproject">超融合</a> - 超融合 java</li>
              <li><a href="https://github.com/yourproject">导航网站</a> - 导航网站 java vue</li>

            </ul>
          </div>
          <div class="contact-card">
            <h2>联系我</h2>
            <p>电子邮件：1156028692@qq.com</p>
            <p>GitHub：<a href="https://github.com/Rodma1?tab=repositories">我的github</a></p>
            <p>CSDN: <a href="https://blog.csdn.net/weixin_46654114?type=blog">我的CSDN</a></p>

          </div>
          <div>
     
      </div>
        </div>
        
      </div>
   
    </div>
  </template>
  
  <script>
  export default {
    name: 'AboutView',
    // props: {
    //   msg: String
    // }
  }
  </script>
  
  <style scoped>

  .about {
    /* position: fixed; */
    width: 100%;
    /* height: 80vh; */
    font-family: 'Arial', sans-serif;
    color: #333;
    background: linear-gradient(to right, #ECE9E6, #d3d2d2);
    padding: 20px 0;
    background-size: 100%;
  }
  .container {
    width: 80%;
    margin: 0 auto;
    background: #fff;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }
  .header {
    text-align: center;
    margin-bottom: 20px;
  }
  .content {
    display: grid;
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .profile-card, .skills-card, .projects-card, .contact-card {
    background: #f9f9f9;
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }

  .profile-card {
    display: flex;
    align-items: center;
  }
  .profile-img {
    border-radius: 50%;
    width: 100px;
    height: 100px;
    object-fit: cover;
    margin-right: 20px;
  }
  .profile-info h2 {
    margin: 0 0 10px 0;
  }
  .skills-card ul, .projects-card ul {
    list-style: none;
    padding: 0;
  }
  .skills-card li, .projects-card li {
    margin: 10px 0;
  }
  .contact-card a {
    color: #3498db;
    text-decoration: none;
  }
  .contact-card a:hover {
    text-decoration: underline;
  }
  @media (min-width: 768px) {
    .content {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  @media (min-width: 1024px) {
    .content {
      grid-template-columns: repeat(3, 1fr);
    }
    .profile-card {
      grid-column: span 2;
    }
  }
  </style>
  