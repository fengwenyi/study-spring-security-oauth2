# study-spring-security-oauth2

学习Spring Security OAuth2

## 概述

OAuth 是协议，Security是安全框架


OAuth2 官方提供的初始化SQL

https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql

## 介绍

#### 授权服务器

通过授权码发放令牌

![基于 JDBC 实现](./study-spring-security-oauth2-auth-mysql)

#### 资源服务

通过令牌访问资源，如果没有令牌，无法访问资源，如下图：

![保护资源，无令牌无法访问](./images/06.png)

## 效果流程

#### 第一步，获取授权码

打开浏览器，输入地址：

```
http://localhost:8080/oauth/authorize?client_id=客户端ID&response_type=code
```

第一次访问会跳转到登录页面

![登录](./images/01.png)

登录成功，选择授权后会跳转我的个人网站，浏览器地址上还会包含一个授权码（`code=3FKELV`），浏览器地址栏会显示如下地址：

![授权码](./images/02.png)

有了这个授权码就可以获取访问令牌了

#### 第二步，通过授权码向服务器申请令牌

**地址：**

```
http://客户端ID:客户端密码@localhost:8080/oauth/token
```

**headers:**

`Content-Type` : `application/x-www-form-urlencoded`

**参数：**

`grant_type` : `authorization_code`

`code` : `3FKELV`

截图如下：

![申请令牌](./images/03.png)

#### 第三步，通过令牌访问资源

```
http://localhost:8081/contents/?access_token=0b2d44da-cd2f-4608-9f8c-063f5e50393f
```

效果

![通过令牌访问资源](./images/04.png)

如果令牌不正确，就会有如下错误：

![无效令牌](./images/05.png)

