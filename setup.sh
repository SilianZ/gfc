#!/bin/bash

# 更新包列表
sudo apt update

# 进入gfc目录
cd ./gfc

# 安装前端依赖并启动
cd ./frontend
sudo apt install -y yarn
yarn install
yarn run serve &

# 安装后端依赖并启动
cd ../backend
sudo apt install -y redis-server
sudo apt install -y openjdk-8-jdk-headless
echo 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
sudo service redis-server start
mvn clean install
mvn spring-boot:run

