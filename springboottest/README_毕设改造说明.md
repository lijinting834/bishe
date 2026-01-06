# 校园智慧充电桩管理系统（基于原汽车管理系统改造）

## 1. 运行环境
- MySQL 8.x
- JDK 8+（项目是 Spring Boot 2.2.2）

## 2. 数据库
1）新建数据库：`campus_charge`

2）修改配置：`src/main/resources/application.yml`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

> 当前使用 JPA 的 `ddl-auto: update`，首次启动会自动建表（charge_site / charge_pile / charge_order / alarm_record / sys_user）。

## 3. 启动后端
在 `springboottest` 目录执行：
```bash
mvn spring-boot:run
```
默认端口：`8181`

## 4. 打开前端页面
浏览器访问：
- `http://localhost:8181/index.html`

> 本项目为了“最快可跑”，前端采用 CDN 版 Vue2 + ElementUI 的单页页面（不用 npm）。

## 5. MQTT（可选加分项）
配置在 `application.yml`：
```yml
mqtt:
  enabled: true
  broker: tcp://127.0.0.1:1883
  topic: campus/pile/+/telemetry
```

### 5.1 遥测消息示例
Topic：`campus/pile/A01/telemetry`
Payload：
```json
{ "pileNo":"A01", "status":"CHARGING", "temperature":45, "power":1.2, "ts": 1730000000000 }
```

### 5.2 告警规则（简化）
- 温度 > 70℃：写入告警 `OVER_TEMP`
- status == FAULT：写入告警 `FAULT`

## 6. 角色与流程（简化版）
- 管理员：站点管理、充电桩管理、告警查看、统计概览
- 学生/教师：选择充电桩 -> 开始充电 -> 输入电量结束 -> 生成订单金额（1.5元/度）
