## 功能
1. 提供web服务，负责user对象的增加、修改和查看。
2. 数据存储在mongodb中。

## docker-compose常用命令
1. docker compose build构建镜像。
2. docker compose up启动服务。
3. -f 指定docker-compose描述文件，默认为docker-compose.yml。

## docker-compose.yml
1. 示例展示了通过docker compose快速启动多个服务。
2. 示例包含三个服务，nginx负责web服务反向代理，web是主服务，db是mongodb服务。
3. nginx服务使用官方的nginx:stable镜像，通过volume挂载配置nginx.conf，nginx.conf通过服务名web配置代理服务的地址。
4. web服务通过Dockerfile构建镜像，在spring boot profile docker指定mongodb的连接地址为服务名db。
5. db服务使用官方的mongo镜像，端口映射是为了方便本地访问mongodb的数据。

## docker-compose-dev.yml
1. 示例展示了在docker环境中进行代码热更和配置热更。
2. 示例包含两个服务，web是主服务，db是mongodb服务。
3. web服务挂载了两个volume，一个是配置，一个是代码。
4. 在IDE中编辑代码和配置，执行编译，容器通过volume获得新的文件。
5. com.example.reload.ReloadResourcesMonitor负责配置更新逻辑，com.example.reload.ReloadClassesMonitor负责代码更新逻辑。
6. 代码更新依赖Java Instrumentation技术，具体逻辑在agent子项目中。

## 小结
docker compose适合在开发环境和自动化测试环境中使用，可以方便地启动多个服务，不需要关注服务的安装过程。

## 官方文档
[官方文档小结](official-doc.md)