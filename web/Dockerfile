# 使用 Nginx 官方镜像作为基础镜像
FROM nginx:alpine

# 将生成的 dist 目录中的静态文件复制到 Nginx 的默认静态文件目录
COPY ./dist /usr/share/nginx/html

# 复制自定义的 Nginx 配置文件（可选）
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露 Nginx 端口
EXPOSE 80

# 启动 Nginx 服务器
CMD ["nginx", "-g", "daemon off;"]
