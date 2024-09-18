# GFC 商赛平台

欢迎来到 GFC 商赛平台！这是一个模拟现实商业比赛的创新平台，通过运营公司，考察和锻炼选手的商业眼光、沟通能力、数学能力和市场理解力。比赛的唯一评分标准是在两天（也就是比赛进行11个财年后）公司的规模与资产。

## 商赛介绍

GFC 商赛旨在通过不同的形式，模拟世界公司和金融机构的管理层，面对不同地区的业务、不同时期的危机和挑战，从而体验金融世界无穷的魅力。

在比赛期间，参赛者需从自身情况出发，以将自身事业大展宏图为目的，做出决定公司存亡的决策。选手将身临其境地体验市场调研、买卖股票、合作交易、开发产业等金融手段，以及与来自不同地区选手之间的互惠互助、并驱争先，从而体会现实世界中金融行业的严酷与其中机遇的奥妙。

## 比赛背景综述

ABC 三国因政治原因爆发了为期 10 年的战争，战争过后，24 世纪，ABC 三座帝国在废墟上打造了新家园，重新开始建设他们的国家。本次商赛将取缔传统商赛中固定行业的概念，选手可以选择自己喜欢的行业进行投资发展（原材料 & 产品 & 服务），也可跨行业发展。所有队伍的启动资金为 100 亿，比赛的唯一评分标准是公司最终的资产总值。ABC 三个国家间有着复杂的地理环境，需要铁路连接。

## 安装与使用

1. **克隆仓库**：
```sh
git clone https://github.com/Amazingkenneth/gfc.git
```
2. **安装前端依赖并启动**：
```sh
cd ./gfc/frontend
sudo apt install -y npm
sudo npm install --global yarn
yarn install
yarn run serve
```
3. **安装后端依赖并启动**：
```sh
cd ../backend
sudo apt install -y redis-server
sudo apt install -y openjdk-8-jdk-headless
echo 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
sudo service redis-server start
mvn clean install
mvn spring-boot:run
```

## 贡献

欢迎对本项目进行贡献！请 Fork 此仓库并提交 Pull Request，或提交 Issue 以报告问题或提出新功能建议。

## 许可证

本项目采用 [Apache 2.0 许可证](./LICENSE.txt)。

**特别感谢**：

感谢所有参赛选手和支持者的参与与贡献！