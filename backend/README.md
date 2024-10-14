前言： 写一个es工具


## 一. 目的

1. 有一个自己的个人网站是一件装逼的事情
2. 在完善自己的个人网站中不断学习

## 二. 项目标题和描述

我这个网站叫做   **神唱网站**，**神唱 **  是   **神的孩子都在歌唱**的简称，**神的孩子都在歌唱 **  的名字来源于   **五月天**  的   **神的孩子都在唱歌**

该项目是前后端分离项目: 

[后端](https://github.com/Rodma1/navigation)

[前端](https://github.com/Rodma1/navigation_ui)

## 三. 使用技术

后端： springboot + mybatis-plus  + postgresql 

前端： vue + element

> element: https://element.eleme.cn/#/zh-CN/component/installation
>
> css:  https://www.runoob.com/css/css-examples.html

## 四. 安装部署

### 4.1 环境



| 环境            | 版本           |
| --------------- | -------------- |
| 一台linux服务器 | centos7        |
| postgres        | 14.6           |
| redis           | 6.2.6          |
| java            | 1.8            |
| vue             | @vue/cli 5.0.8 |
| node            | v16.14.0       |
| npm             | 8.3.1          |



### 4.2 代码打包

#### 4.2.1 后端代码打包

拉取最新代码，然后使用idea打开项目，在配置文件里面修改你的数据库密码

![image-20231013112625345](image/image-20231013112625345.png)



打成jar，可以使用命令   **java -jar 文件名.jar**  测试运行

#### 4.2.2 前端代码打包

进入到下载好的前端目录下，命令行运行

安装依赖
`npm install`

打包  `npm run build`

运行： `npm run serve`

然后就能打包出一个dist目录，这就是前端的可执行文件了

![image-20240924101537917](image/image-20240924101537917.png)

### 4.4 项目部署linux服务器

以下是我部署的方法，大家可以当做参考

#### 4.4.1 后端

将jar包上传到目录**/study/project/navigate/cyz_navigate**，然后在创建一个config目录存放配置文件**application.yml**，这样子我们的启动的时候可以指定配置文件允行，方便修改配置。



然后在 `/etc/systemd/system`目录下建立一个 `cyz_navigate.service` 在里面配置如下信息,修改以下你的jar包路径就可以

```shell
[Unit]
Description=navication
After=network.target

[Service]
User=root
ExecStart=/usr/bin/java  -Dfile.encoding=UTF-8 -Xms256m -Xmx512m -jar   -Dspring.config.location=/study/project/navigate/cyz_navigate/config/application.yml /study/project/navigate/cyz_navigate/cyz_navigate.jar
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

配置好了后就可以通过以下方式启动和关闭服务了

```shell
systemctl start cyz_navigate.service
systemctl restart cyz_navigate.service
systemctl status cyz_navigate.service
```

你可以专门去写个shell脚本去执行

![image-20240924102414936](image/image-20240924102414936.png)



以下是我的目录

![image-20240924102433585](image/image-20240924102433585.png)

#### 4.4.2 前端

将dist`包放入到服务器的/study/project/navigate/navigate_web/`目录（可以通过压缩成zip包的方式上传，在服务器上解压）

![image-20240924102952141](image/image-20240924102952141.png)

然后需要下载nginx， 可以看我这篇文章（[一文了解和使用nginx（附带图文）](https://blog.csdn.net/weixin_46654114/article/details/134046040?ops_request_misc=%257B%2522request%255Fid%2522%253A%25222E6D96CC-899C-4334-BBE3-AEF3D7F6DC95%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fblog.%2522%257D&request_id=2E6D96CC-899C-4334-BBE3-AEF3D7F6DC95&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~blog~first_rank_ecpm_v1~rank_v31_ecpm-1-134046040-null-null.nonecase&utm_term=nginx&spm=1018.2226.3001.4450)），然后编辑nginx的配置信息，`/usr/local/nginx/conf/nginx.conf`，参考我的如下配置

``` shell
server {
        listen 80;
        server_name localhost;

        location / {
            root /study/project/navigate/dist;
            index index.html index.htm;
        }

        location /api/ {
            proxy_pass http://127.0.0.1:8089/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }

```

配置好后重启一下

``` shell
/usr/local/nginx/sbin/nginx -s reload
```



访问： http://localhost/#/menu



## 五. 页面效果

![image-20240926110037371](image/image-20240926110037371.png)

> 作者：神的孩子都在歌唱
>
> 本人博客：https://blog.csdn.net/weixin_46654114
>
> 转载说明：务必注明来源，附带本人博客连接。
