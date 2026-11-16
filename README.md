# simple-test-langchain4j

一个基于 **Spring Boot + LangChain4j** 的多会话流式聊天示例。后端通过 OpenAI 兼容接口接入智谱 GLM 或 DeepSeek，前端使用静态 HTML、htmx、Alpine.js 和 daisyUI，无需单独执行前端构建。

## 功能特性

- 多会话标签页，可新建、切换和关闭会话
- 基于 SSE 的流式响应，实时展示模型输出
- 按 `memoryId` 隔离聊天记忆，每个会话最多保留 20 条消息
- 支持 Markdown 渲染、代码高亮和 HTML 安全过滤
- 提供 GLM 4.5、GLM 4.7 和 DeepSeek 三组 Maven 配置
- API Key 全部通过环境变量注入，不写入源码

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 运行环境 | Java 21、Maven |
| 后端 | Spring Boot 4.1.1、LangChain4j 1.23.0 |
| 模型接入 | LangChain4j OpenAI 兼容客户端、OkHttp |
| 前端 | HTML、htmx、Alpine.js、daisyUI、Tailwind CSS |
| 内容展示 | Marked、DOMPurify、Highlight.js、Mustache |

## 项目结构

```text
.
├── pom.xml                         # Maven 父工程、依赖版本和模型 Profile
├── langchain4j/
│   ├── pom.xml                     # Web 应用模块
│   └── src/main/
│       ├── java/com/hirong/agentic/
│       │   ├── AgenticApplication.java
│       │   ├── service/Assistant.java
│       │   ├── sse/StreamController.java
│       │   └── web/SpaFallbackController.java
│       └── resources/
│           ├── application.properties
│           ├── application-glm4.5.properties
│           ├── application-glm4.7.properties
│           ├── application-deepseek.properties
│           └── static/             # 聊天页面及前端资源
└── README.md
```

## 环境准备

- JDK 21
- Maven 3.6.3 或更高版本
- 所选模型服务的 API Key

检查环境：

```bash
java -version
mvn -version
```

## 配置 API Key

默认使用 `glm4.5` Profile，对应环境变量为 `GLM_API_KEY`。

macOS / Linux：

```bash
export GLM_API_KEY="你的 API Key"
```

Windows PowerShell：

```powershell
$env:GLM_API_KEY="你的 API Key"
```

不同 Profile 所需的环境变量如下：

| Maven Profile | 模型 | Base URL | 环境变量 |
| --- | --- | --- | --- |
| `glm4.5`（默认） | `GLM-4.5-Flash` | `https://open.bigmodel.cn/api/paas/v4` | `GLM_API_KEY` |
| `glm4.7` | `GLM-4.7-Flash` | `https://open.bigmodel.cn/api/paas/v4` | `GLM_API_KEY` |
| `deepseek` | `deepseek-flash` | `https://api.deepseek.com/` | `DEEPSEEK_API_KEY` |

> 请确认账号已开通对应模型，并以模型服务提供商的实际可用模型名称为准。

## 启动应用

在项目根目录执行：

```bash
mvn -pl langchain4j spring-boot:run
```

切换到其他模型：

```bash
# GLM 4.7
mvn -pl langchain4j -Pglm4.7 spring-boot:run

# DeepSeek（需先设置 DEEPSEEK_API_KEY）
mvn -pl langchain4j -Pdeepseek spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080
```

前端依赖通过 CDN 加载，打开页面时需要能够访问相应 CDN。

## 构建

```bash
mvn -pl langchain4j -am clean package
```

## API 示例

### 普通对话

```http
GET /api/chat?message=你好
```

```bash
curl --get --data-urlencode "message=你好" http://localhost:8080/api/chat
```

### 流式对话

```http
POST /api/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

请求体：

```json
{
  "memoryId": "demo-session",
  "message": "你好，请介绍一下自己"
}
```

`memoryId` 用于区分不同会话的记忆。SSE 按模型输出持续返回文本片段，结束时发送 `[DONE]`。

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"memoryId":"demo-session","message":"你好，请介绍一下自己"}'
```

## 注意事项

- 聊天记忆保存在应用内存中，服务重启后会丢失。
- 当前示例未接入用户认证和持久化存储，不建议直接作为生产服务暴露到公网。
- 不要将真实 API Key 写入配置文件、命令历史或提交到仓库。
