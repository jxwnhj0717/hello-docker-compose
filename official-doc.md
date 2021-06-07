# Docker Compose

https://docs.docker.com/compose/



## Docker Compose概述

### 特性:

1. Multiple isolated environments on a single host
2. Preserve volume data when containers are created
3. Only recreate containers that have changed
4. Variables and moving a composition between environments

### 常见场景:

1. Development environments
2. Automated testing environments
3. Single host deployments



## Get started

演示在Docker环境下开发。

关键：将代码挂载到Docker Container目录中，代码变化后不需要重新build image。



## 使用环境变量

1. 使用环境变量文件.env
2. 执行docker compose run时传入环境变量



## 使用profiles

通过profiles控制启动docker compose中的部分服务。比如开发环境和测试环境需要不同的服务。



## Extend Services

定义多个compose files，控制不同环境的配置。



## 网络

docker compose默认会创建一个网络${app}_default，同一个docker compose中的服务可以通过name通信。

通过docker swarm可以配置多机互联网络。可以为服务指定不同的网络。可以使用一个已经存在的网络。



## 启动顺序

通过depends_on可以控制服务的启动和停止的顺序。

如果需要更细粒的控制，比如等待服务A可访问之后再启动服务B，需要使用脚本控制。

