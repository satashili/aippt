# AI PPT生成工具 - 后端

本项目是AI驱动的PPT生成工具的后端部分，使用Spring Boot开发。

## 环境变量配置

为了保护敏感信息安全，本项目使用`.env`文件存储环境变量，如数据库密码、API密钥等。

### 配置步骤

1. 在项目根目录创建`.env`文件（已被添加到`.gitignore`，不会被提交到仓库）
2. 在`.env`文件中添加以下环境变量（根据实际情况修改值）：

```
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/aippt?serverTimezone=GMT%2B8&characterEncoding=utf-8&useSSL=false
DB_USERNAME=root
DB_PASSWORD=your_password

# Google OAuth2配置
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# 邮件服务配置
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Stripe配置
STRIPE_API_KEY=your_stripe_api_key
STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret
```

### .env文件位置

系统会在以下位置按顺序查找.env文件：

1. 当前运行目录（`.`）
2. 项目根目录（`..`）
3. backend目录（`backend`或`../backend`）
4. 配置目录（`config`或`../config`）
5. 资源目录（`src/main/resources`）

为了确保系统能找到.env文件，建议将其放在以下位置之一：
- 项目根目录: `/aippt/.env`
- 后端目录: `/aippt/backend/.env`

### 使用开发环境配置文件

如果您不想使用.env文件，也可以使用Spring的profile功能。项目提供了一个`application-dev.yml`文件，包含了开发环境需要的所有配置。

要使用这个配置文件，可以在启动项目时添加`spring.profiles.active=dev`参数：

```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

或者在IDE中添加环境变量：`spring.profiles.active=dev`

### 环境变量加载机制

项目使用`dotenv-java`库在启动时自动加载`.env`文件中的环境变量。如果`.env`文件不存在或某个环境变量未设置，将使用`application.yml`中配置的默认值或特定profile的配置值。

## 项目启动

确保已配置好环境变量后，可以通过以下命令启动项目：

```bash
./mvnw spring-boot:run
```

## 安全注意事项

- 永远不要提交`.env`文件到Git仓库
- 不要在公共场合或非加密通道分享环境变量的值
- 定期更新敏感信息，特别是生产环境中的密码和API密钥 