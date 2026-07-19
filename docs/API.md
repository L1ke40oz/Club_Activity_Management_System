# 校园社团活动管理系统 - 后端 API 文档

> Base URL: `http://localhost:8080`
> 认证方式: Session（Cookie: `JSESSIONID`）
> 数据格式: JSON
> 编码: UTF-8

前端通过 Vite 代理访问后端，实际请求前缀为 `/api`（会被代理去掉后转发到 `http://localhost:8080`）。本文档以后端真实路径为准。

## 统一响应格式

除 SSE 流式接口外，所有接口均返回统一结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

- `code`: 业务状态码，`200` 表示成功；常见错误码 `400`（参数/业务错误）、`401`（未登录）、`403`（无权限）、`404`（资源不存在）、`500`（服务器异常）。
- `message`: 提示信息。
- `data`: 业务数据，错误时通常为 `null`。

> 说明：以下接口中标注「需登录」表示需携带有效 `JSESSIONID`；标注「需管理员」表示当前用户 `role` 必须为 `1`。未登录访问受保护接口时返回 `code: 401`。

---

## 一、用户模块 `/user`

### 1.1 注册
- **POST** `/user/register`
- 权限：无需登录
- 请求体：
```json
{
  "studentId": "2024001",
  "name": "张三",
  "password": "123456",
  "className": "计科2401",
  "contactInfo": "13800000001"
}
```
- 响应：`data: "注册成功"`

### 1.2 登录
- **POST** `/user/login`
- 权限：无需登录
- 请求体：
```json
{
  "studentId": "admin",
  "password": "admin123"
}
```
- 响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "studentId": "admin",
    "name": "系统管理员",
    "role": 1
  }
}
```
- 说明：登录成功后服务端写入 Session，后续请求需携带 Cookie。

### 1.3 登出
- **POST** `/user/logout`
- 权限：需登录
- 响应：`data: "退出成功"`

### 1.4 获取当前用户信息
- **GET** `/user/info`
- 权限：需登录
- 响应：
```json
{
  "data": {
    "studentId": "admin",
    "name": "系统管理员",
    "role": 1,
    "className": null,
    "contactInfo": null,
    "noShowCount": 0,
    "createTime": "2026-07-02T02:21:45"
  }
}
```

### 1.5 用户列表
- **GET** `/user/list`
- 权限：需登录
- 响应：`data: [SysUser, ...]`（`password` 字段置空）

### 1.6 更新用户
- **PUT** `/user/update`
- 权限：需登录
- 请求体：
```json
{
  "studentId": "2024001",
  "name": "张三丰",
  "className": "计科2402",
  "contactInfo": "13900000001",
  "password": "newpass"
}
```
- 说明：`password` 为空或不传则不修改密码。

---

## 二、社团模块 `/club`

### 2.1 社团列表（公开已通过）
- **GET** `/club/list`
- 权限：需登录
- 查询参数：`name`（可选，按名称模糊搜索）
- 说明：仅返回 `is_deleted=0` 且 `status=1`（审批通过）的社团。
- 响应：
```json
{
  "data": [
    {
      "clubId": 1,
      "name": "编程爱好者协会",
      "category": "学术科技",
      "establishedDate": "2023-09-01",
      "advisor": "刘老师",
      "creatorId": "2024001",
      "description": "...",
      "maxMembers": 200,
      "status": 1,
      "isDeleted": 0,
      "createTime": "2026-07-02T02:21:45"
    }
  ]
}
```

### 2.2 待审批社团列表
- **GET** `/club/pending`
- 权限：需管理员
- 说明：返回 `status=0`（待审批）的社团。
- 响应：`data: [ClubInfo, ...]`

### 2.3 我申请的社团
- **GET** `/club/my-applied`
- 权限：需登录
- 说明：返回当前用户创建、且处于待审批（`status=0`）或已驳回（`status=2`）的社团。
- 响应：`data: [ClubInfo, ...]`

### 2.4 社团详情
- **GET** `/club/{id}`
- 权限：需登录
- 响应：`data: ClubInfo`；社团不存在或已解散返回 `code: 404`。

### 2.5 社团详情（公开）
- **GET** `/club/detail/{id}`
- 权限：无需登录
- 说明：供主页社团详情页使用，逻辑同 2.4。
- 响应：`data: ClubInfo`

### 2.6 申请创建社团
- **POST** `/club/apply`
- 权限：需登录
- 请求体：
```json
{
  "name": "音乐社",
  "category": "文艺",
  "establishedDate": "2024-03-01",
  "advisor": "王老师",
  "description": "热爱音乐的同学聚集地",
  "maxMembers": 100
}
```
- 说明：`creatorId` 由后端从 Session 获取；提交后 `status=0`（待审批）；`maxMembers` 不传默认 200。
- 响应：`data: "社团申请已提交，等待管理员审批"`

### 2.7 管理员直接创建社团
- **POST** `/club/add`
- 权限：需登录（一般由管理员使用）
- 请求体：同 2.6
- 说明：创建后 `status=1`（直接通过）。
- 响应：`data: "社团创建成功"`

### 2.8 审批通过社团
- **PUT** `/club/approve/{id}`
- 权限：需管理员
- 说明：将社团置为 `status=1`，自动把创建者设为「社长」成员，并向创建者发送站内信。
- 响应：`data: "社团审批通过"`

### 2.9 驳回社团申请
- **PUT** `/club/reject/{id}`
- 权限：需管理员
- 说明：将社团置为 `status=2`，并向创建者发送站内信。
- 响应：`data: "已驳回"`

### 2.10 更新社团
- **PUT** `/club/update`
- 权限：需登录
- 请求体（需包含 `clubId`）：
```json
{
  "clubId": 1,
  "name": "编程协会",
  "advisor": "张老师"
}
```
- 响应：`data: "社团信息更新成功"`

### 2.11 删除（解散）社团
- **DELETE** `/club/{id}`
- 权限：需管理员
- 说明：逻辑删除。
- 响应：`data: "社团已解散"`

---

## 三、社团成员模块 `/club-member`

### 3.1 社团成员列表
- **GET** `/club-member/list/{clubId}`
- 权限：需登录
- 查询参数：`status`（可选，0-待审批 / 1-已通过 / 2-已退出或已拒绝）
- 说明：返回结果附带成员姓名 `name` 与班级 `className`。
- 响应：
```json
{
  "data": [
    {
      "id": 1,
      "clubId": 1,
      "studentId": "2024001",
      "position": "社长",
      "joinTime": "2026-07-02T02:21:45",
      "status": 1,
      "name": "张三",
      "className": "计科2401"
    }
  ]
}
```

### 3.2 全部待审批成员申请
- **GET** `/club-member/pending-all`
- 权限：需管理员
- 说明：返回所有社团中 `status=0` 的加入申请，附带姓名与班级。
- 响应：`data: [ClubMember, ...]`

### 3.3 申请加入社团
- **POST** `/club-member/apply`
- 权限：需登录
- 请求体：
```json
{
  "clubId": 1
}
```
- 说明：`studentId` 由后端从 Session 获取。已申请或已加入返回 `code: 400`；社团成员已满返回 `code: 400`；曾退出过的会复用历史记录重新置为待审批。
- 响应：`data: "申请已提交"`

### 3.4 审批通过
- **PUT** `/club-member/approve/{id}`
- 权限：需登录（一般为社长/管理员）
- 说明：审批前再次校验人数上限，通过后置 `status=1` 并向申请人发送站内信。
- 响应：`data: "审批通过"`

### 3.5 审批拒绝
- **PUT** `/club-member/reject/{id}`
- 权限：需登录（一般为社长/管理员）
- 说明：置 `status=2` 并向申请人发送站内信。
- 响应：`data: "已拒绝"`

### 3.6 退出社团
- **PUT** `/club-member/quit/{clubId}`
- 权限：需登录
- 查询参数：`transferTo`（可选，接任社长的成员学号）
- 说明：
  - 普通成员：直接退出。
  - 社长退出且社团无其他成员：社团自动解散。
  - 社长退出且有其他成员：必须通过 `transferTo` 指定接任者，否则返回 `code: 400` 提示需转让或解散；转让成功后向新社长发送站内信。
- 响应示例：`data: "退出成功"` / `"已转让社长并退出社团"` / `"社团已无其他成员，社团已解散"`

### 3.7 更新职务
- **PUT** `/club-member/position`
- 权限：需登录
- 请求体：
```json
{
  "id": 1,
  "position": "副社长"
}
```
- 说明：职务可选「社长 / 副社长 / 部长 / 成员」。
- 响应：`data: "职务更新成功"`

---

## 四、活动模块 `/activity`

### 4.1 活动列表
- **GET** `/activity/list`
- 权限：需登录
- 查询参数：
  - `clubId`（可选）
  - `status`（可选，0-待审批 / 1-已发布 / 2-已结束 / 3-已驳回）
  - `title`（可选，模糊搜索）
- 说明：按 `event_time` 降序返回。
- 响应：`data: [ActivityInfo, ...]`

### 4.2 活动详情
- **GET** `/activity/{id}`
- 权限：需登录
- 响应：
```json
{
  "data": {
    "activityId": 1,
    "clubId": 1,
    "applicantId": "2024001",
    "title": "Spring Boot 技术分享会",
    "details": "...",
    "eventTime": "2026-07-09T02:21:45",
    "location": "图书馆报告厅",
    "maxParticipants": 50,
    "status": 1,
    "isDeleted": 0
  }
}
```

### 4.3 提交活动申请
- **POST** `/activity/add`
- 权限：需登录
- 请求体：
```json
{
  "clubId": 1,
  "title": "算法竞赛培训",
  "details": "为ACM校赛选拔做准备",
  "eventTime": "2026-08-01T14:00:00",
  "location": "教学楼C301",
  "maxParticipants": 40
}
```
- 说明：`applicantId` 由后端从 Session 获取，提交后 `status=0`（待审批）。

### 4.4 审批活动（通过）
- **PUT** `/activity/approve/{id}`
- 权限：需管理员
- 响应：`data: "活动已审批通过"`

### 4.5 驳回活动
- **PUT** `/activity/reject/{id}`
- 权限：需管理员
- 响应：`data: "活动已驳回"`

### 4.6 结束活动
- **PUT** `/activity/finish/{id}`
- 权限：需登录
- 响应：`data: "活动已结束"`

### 4.7 更新活动
- **PUT** `/activity/update`
- 权限：需登录
- 请求体：同创建，需包含 `activityId`。

### 4.8 删除活动
- **DELETE** `/activity/{id}`
- 权限：需登录
- 说明：逻辑删除。

---

## 五、报名签到模块 `/registration`

### 5.1 报名活动
- **POST** `/registration/signup/{activityId}`
- 权限：需登录
- 无请求体
- 说明：活动 `status` 必须为 `1`（已发布）才可报名。

### 5.2 单人签到
- **PUT** `/registration/signin/{id}`
- 权限：需登录
- 无请求体
- 说明：`id` 为报名记录主键 `id`。

### 5.3 批量签到
- **PUT** `/registration/signin/batch/{activityId}`
- 权限：需登录
- 请求体（学号数组）：
```json
["2024001", "2024002", "2024003"]
```

### 5.4 活动报名列表
- **GET** `/registration/list/{activityId}`
- 权限：需登录
- 响应：
```json
{
  "data": [
    {
      "id": 1,
      "activityId": 1,
      "studentId": "2024002",
      "regTime": "2026-07-02T02:21:45",
      "signInStatus": 0,
      "signInTime": null
    }
  ]
}
```

### 5.5 我的报名记录
- **GET** `/registration/my`
- 权限：需登录
- 响应：`data: [ActivityRegistration, ...]`

---

## 六、社团评价模块 `/club-comment`

### 6.1 社团评价列表
- **GET** `/club-comment/list/{clubId}`
- 权限：需登录
- 说明：按 `create_time` 降序返回，附带评价人姓名 `studentName`。
- 响应：
```json
{
  "data": [
    {
      "id": 1,
      "clubId": 1,
      "studentId": "2024002",
      "content": "活动组织得很好！",
      "rating": 5,
      "createTime": "2026-07-02T10:00:00",
      "studentName": "李四"
    }
  ]
}
```

### 6.2 发表评价
- **POST** `/club-comment/add`
- 权限：需登录
- 请求体：
```json
{
  "clubId": 1,
  "content": "活动组织得很好！",
  "rating": 5
}
```
- 说明：`studentId` 由后端从 Session 获取；`rating` 取值 1–5，非法值会被重置为 5。
- 响应：`data: "评价成功"`

### 6.3 删除评价
- **DELETE** `/club-comment/{id}`
- 权限：需登录
- 说明：仅评价本人或管理员可删除，否则返回 `code: 403`。
- 响应：`data: "删除成功"`

---

## 七、站内信模块 `/message`

### 7.1 我的消息
- **GET** `/message/my`
- 权限：需登录
- 说明：返回当前用户的消息，按 `create_time` 降序。
- 响应：
```json
{
  "data": [
    {
      "id": 1,
      "receiverId": "2024001",
      "title": "社团加入通知",
      "content": "恭喜！您申请加入「编程爱好者协会」已通过审批。",
      "isRead": 0,
      "createTime": "2026-07-02T10:05:00"
    }
  ]
}
```

### 7.2 未读消息数
- **GET** `/message/unread-count`
- 权限：需登录
- 响应：`data: 3`（数字）

### 7.3 标记单条已读
- **PUT** `/message/read/{id}`
- 权限：需登录
- 响应：`data: "已读"`

### 7.4 全部标记已读
- **PUT** `/message/read-all`
- 权限：需登录
- 响应：`data: "全部已读"`

---

## 八、数据统计模块 `/statistics`

### 8.1 全局概览
- **GET** `/statistics/overview`
- 权限：需登录
- 响应：
```json
{
  "data": {
    "clubCount": 20,
    "memberCount": 120,
    "activityCount": 35,
    "publishedActivityCount": 18,
    "finishedActivityCount": 12,
    "registrationCount": 300,
    "signedInCount": 240,
    "attendanceRate": 80,
    "userCount": 16
  }
}
```
- 说明：`attendanceRate` 为签到率百分比（整数）。

### 8.2 社团维度统计
- **GET** `/statistics/clubs`
- 权限：需登录
- 查询参数：`name`（可选，按名称模糊）、`category`（可选，按类别精确）
- 响应：每个社团含 `clubId`、`name`、`category`、`advisor`、`establishedDate`、`memberCount`、`activityCount`、`finishedActivityCount`、`totalRegistrations`、`totalSignIns`、`attendanceRate`。
```json
{
  "data": [
    {
      "clubId": 1,
      "name": "编程爱好者协会",
      "category": "学术科技",
      "advisor": "刘老师",
      "establishedDate": "2023-09-01",
      "memberCount": 15,
      "activityCount": 5,
      "finishedActivityCount": 3,
      "totalRegistrations": 60,
      "totalSignIns": 50,
      "attendanceRate": 83
    }
  ]
}
```

### 8.3 活动维度统计
- **GET** `/statistics/activities`
- 权限：需登录
- 查询参数：`clubId`（可选）、`status`（可选）、`startDate`（可选）、`endDate`（可选，按 `event_time` 过滤）
- 响应：每条活动含 `activityId`、`title`、`clubId`、`clubName`、`eventTime`、`location`、`status`、`registrationCount`、`signInCount`、`attendanceRate`。

### 8.4 成员活跃度统计
- **GET** `/statistics/member-activity`
- 权限：需登录
- 查询参数：`clubId`（可选，限定某社团成员）
- 响应：每个成员含 `studentId`、`name`、`className`、`clubCount`（加入社团数）、`registrationCount`、`signInCount`、`attendanceRate`、`noShowCount`（爽约次数）。

---

## 九、智能分析模块 `/ai-report`

### 9.1 生成报告
- **POST** `/ai-report/generate`
- 权限：需管理员
- 请求体：
```json
{
  "clubId": 1,
  "timeRange": "2026年春季学期"
}
```
- 说明：`clubId` 为空时生成全校报告；`timeRange` 必填，否则返回 `code: 400`。
- 响应：`data: AiReport`

### 9.2 报告列表
- **GET** `/ai-report/list`
- 权限：需登录
- 查询参数：`clubId`（可选）
- 说明：按 `create_time` 降序。
- 响应：`data: [AiReport, ...]`

### 9.3 报告详情
- **GET** `/ai-report/{id}`
- 权限：需登录
- 响应：
```json
{
  "data": {
    "reportId": 1,
    "clubId": 1,
    "timeRange": "2026年春季学期",
    "evaluationAndSuggestion": "...",
    "createTime": "2026-07-02T02:30:00"
  }
}
```

### 9.4 删除报告
- **DELETE** `/ai-report/{id}`
- 权限：需登录
- 响应：`data: "报告已删除"`

### 9.5 流式生成报告（SSE）
- **GET** `/ai-report/generate/stream`
- 权限：需登录
- 查询参数：`clubId`（可选，为空生成全校报告）、`timeRange`（必填）
- 说明：返回 `text/event-stream`（SSE）。服务端以流的形式逐段推送大模型生成的内容，前端通过 `EventSource` 消费。超时时间 120 秒。

### 9.6 流式 AI 对话（SSE）
- **POST** `/ai-report/chat/stream`
- 权限：需登录
- 请求体：
```json
{
  "messages": [
    { "role": "user", "content": "帮我分析一下编程社的运营情况" }
  ],
  "clubId": 1
}
```
- 说明：返回 `text/event-stream`（SSE），逐段推送对话回复。`messages` 为对话历史（`role` 为 `user`/`assistant`），`clubId` 可选用于携带社团上下文。超时时间 120 秒。

> SSE 接口不遵循统一响应格式，直接以事件流返回文本片段。

---

## 十、权限速查

| 接口 | 权限要求 |
|------|----------|
| `POST /user/login` | 无 |
| `POST /user/register` | 无 |
| `GET /club/detail/{id}` | 无 |
| `GET /club/pending` | 管理员 |
| `PUT /club/approve/{id}` | 管理员 |
| `PUT /club/reject/{id}` | 管理员 |
| `DELETE /club/{id}` | 管理员 |
| `GET /club-member/pending-all` | 管理员 |
| `PUT /activity/approve/{id}` | 管理员 |
| `PUT /activity/reject/{id}` | 管理员 |
| `POST /ai-report/generate` | 管理员 |
| 其他所有接口 | 登录即可 |

---

## 十一、角色与状态说明

**用户角色 (`sys_user.role`)**
- `0`：普通用户
- `1`：系统管理员

**社团状态 (`club_info.status`)**
- `0`：待审批
- `1`：正常（审批通过）
- `2`：已驳回

**社团成员状态 (`club_member.status`)**
- `0`：待审批
- `1`：已通过
- `2`：已退出 / 已拒绝

**活动状态 (`activity_info.status`)**
- `0`：待审批
- `1`：已发布（审批通过）
- `2`：已结束
- `3`：已驳回

**签到状态 (`activity_registration.sign_in_status`)**
- `0`：未签到
- `1`：已签到

**消息已读状态 (`sys_message.is_read`)**
- `0`：未读
- `1`：已读

---

## 十二、测试账号

| 学号 | 密码 | 姓名 | 角色 |
|------|------|------|------|
| `admin` | `admin123` | 系统管理员 | 管理员 |
| `2024001` ~ `2024015` | `123456` | 张三、李四…… | 普通用户 |

> 测试数据由后端 `DataInitializer` 在首次启动时自动写入。

---

## 十三、注意事项

1. 除登录、注册与少量公开接口（如 `/club/detail/{id}`）外，所有请求需携带 Cookie 中的 `JSESSIONID`。
2. 前端需配置 `withCredentials: true` 以携带 Cookie。
3. 后端已配置 CORS 允许携带凭证的跨域请求（开发环境）。
4. 日期时间格式：`yyyy-MM-ddTHH:mm:ss`（ISO 8601）；日期格式：`yyyy-MM-dd`。
5. SSE 流式接口（`/ai-report/generate/stream`、`/ai-report/chat/stream`）返回 `text/event-stream`，不适用统一响应格式。
