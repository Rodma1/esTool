FROM centos:7
ENV JAVA_DIR=/usr/local

# ----------- JDK -----------
COPY ./jdk-11.0.29_linux-x64_bin.tar.gz $JAVA_DIR/

RUN cd $JAVA_DIR \
 && tar -xzf ./jdk-11.0.29_linux-x64_bin.tar.gz \
 && mv ./jdk-11.0.29 ./java11 \
 && rm -f ./jdk-11.0.29_linux-x64_bin.tar.gz

ENV JAVA_HOME=$JAVA_DIR/java11
ENV PATH=$PATH:$JAVA_HOME/bin

# ----------- nginx（来自宿主机）-----------
COPY ./nginx_local /usr/local/nginx
RUN chmod +x /usr/local/nginx/sbin/nginx

# ----------- 后端 -----------
WORKDIR /app
COPY ./backend/esTool.jar /app/esTool.jar
COPY ./backend/application.yml /app/application.yml
COPY ./backend/data/esConnectParam.json /app/esConnectParam.json

# ----------- 前端 -----------
COPY ./web/dist /usr/local/nginx/html

EXPOSE 8089 8000

CMD ["/bin/sh", "-c", "/usr/local/nginx/sbin/nginx && java  -Dfile.encoding=UTF-8 -Xms256m -Xmx512m -jar   -Dspring.config.location=/app/application.yml /app/esTool.jar"]

docker build -t estool .
docker build --progress=plain -t estool .
docker build -f estool  --progress=plain -t estool .
docker run -d \
  -p 8001:8000 \
  -p 9000:8089 \
  -v /chen/code/esTool/backend/application.yml:/app/application.yml \
  -v /chen/code/esTool/backend/data/esConnectParam.json:/app/esConnectParam.json \
  --name esTool \
  estool
