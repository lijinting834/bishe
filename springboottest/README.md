# 校园智慧充电桩管理系统（简化版：管理员 + 学生/教师）

> 你原来的“汽车管理系统”后端已改造成“校园智慧充电桩管理系统”。前端为了最快落地，使用 **Vue2 + Element-UI CDN** 的单页，直接放在 Spring Boot 的 `static/index.html`，启动后用浏览器打开即可。

## 1. 技术栈
- 后端：Spring Boot 2.2.2 + Spring Data JPA + MySQL
- 前端：Vue2 + Element-UI（CDN）+ Axios（CDN）
- 可选：MQTT 遥测接入（Paho MQTT Client）

## 2. 启动步骤
1) 创建数据库（按你本地用户名密码修改 `application.yml`）
```sql
CREATE DATABASE IF NOT EXISTS campus_charge DEFAULT CHARACTER SET utf8mb4;
```
2) 启动后端
- 用 IDEA 打开 `springboottest` 项目
- 运行 `SpringboottestApplication`
- 默认端口：`8181`

3) 访问前端
- 浏览器打开：`http://localhost:8181/`
- 首次会弹出登录：输入任意用户名，选择角色（ADMIN / STUDENT / TEACHER）即可

## 3. 核心功能（对照毕设）
- **站点管理**：新增/编辑/删除站点（如 A区宿舍楼下）
- **充电桩管理**：新增/编辑/删除桩；模拟心跳（可选接口）
- **充电订单**：开始充电 / 结束充电（自动生成电量与费用：可用于演示）
- **告警管理**：查看未处理告警、处理告警
- **看板统计**：站点数、桩数、充电中数量、未处理告警数、累计金额等

## 4. API 一览
- 登录：`POST /api/auth/login`
- 站点：`GET/POST/PUT/DELETE /api/site/...`
- 充电桩：`GET/POST/PUT/DELETE /api/pile/...`
- 订单：`POST /api/order/start`、`POST /api/order/stop`、`GET /api/order/findAll`、`GET /api/order/findByUserId/{userId}`
- 告警：`GET /api/alarm/findAll?handled=false`、`PUT /api/alarm/handle/{id}`
- 看板：`GET /api/dashboard/summary`

## 5. MQTT 遥测（可选）
1) `application.yml` 中把 `mqtt.enabled` 改为 `true`
2) 启动一个 MQTT Broker（例如 Mosquitto）
3) 发布模拟消息（示例 topic：`campus/charge/telemetry/A01`）：
```json
{
  "pileNo": "A01",
  "status": "CHARGING",
  "temperature": 38.5,
  "powerKw": 1.2
}
```
- 温度 >= 60 会触发 `OVER_TEMP` 告警
- status=FAULT/OFFLINE 会触发对应告警

## 6. 你下一步建议（毕设最省事路线）
- 把“学生/教师”页面做成：只能看到自己的订单（已提供 `findByUserId`）
- 管理员页面：站点/桩/告警/统计
- MQTT 数据模拟：你前面说准备用 MQTT 造数据，这块已经预留好了
