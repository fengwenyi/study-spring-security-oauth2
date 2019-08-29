# 基于 JDBC 存储令牌

## 第一部分，授权与认证

#### 构建数据库

OAuth2 官方提供的初始化SQL

```
https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql
```

#### 数据库依赖

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

#### 配置

- 方式一

**数据库连接配置**

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://localhost:3306/study-spring-security-oauth2?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
```

这里只能用 `jdbc-url` 不能直接用 `url`


**获取数据源**

```
@Bean
@Primary
@ConfigurationProperties(prefix = "spring.datasource")
public DataSource dataSource() {
    return DataSourceBuilder.create().build();
}
```

- 方式二

**数据库连接配置**

```yaml
spring:
  datasource:
    hikari:
      jdbc-url: jdbc:mysql://localhost:3306/study-spring-security-oauth2?useUnicode=true&characterEncoding=utf-8&useSSL=false
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: root
      password: 123456
```

**获取数据源**

```
@Bean
@Primary
@ConfigurationProperties(prefix = "spring.datasource.hikari")
public DataSource dataSource() {
    return DataSourceBuilder.create().build();
}
```

- 方式三

我们可以写：

```
@Autowired
private DataSource dataSource;
```

完整配置如下：

```yaml
spring:
  application:
    name: study-spring-security-oauth2-auth-mysql

  datasource:
    name: study-spring-security-oauth2-auth-mysql
    type: com.zaxxer.hikari.HikariDataSource
#    driver-class-name: com.mysql.cj.jdbc.Driver
#    url 会报错， 用 jdbc-url
#    url: jdbc:mysql://localhost:3306/study-spring-security-oauth2?useUnicode=true&characterEncoding=utf-8&useSSL=false
#    jdbc-url: jdbc:mysql://localhost:3306/study-spring-security-oauth2?useUnicode=true&characterEncoding=utf-8&useSSL=false
#    username: root
#    password: 123456

    hikari:
      minimum-idle: 5
      idle-timeout: 600000
      maximum-pool-size: 10
      auto-commit: true
      pool-name: MyHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000
      connection-test-query: SELECT 1
      jdbc-url: jdbc:mysql://localhost:3306/study-spring-security-oauth2?useUnicode=true&characterEncoding=utf-8&useSSL=false
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: root
      password: 123456

  jpa:
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    hibernate:
      ddl-auto: update
    show-sql: true
```

#### 认证配置

```java
package com.fengwenmyi.study_spring_security_oauth2_auth_mysql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * @author Erwin Feng
 * @since 2019-08-21 17:43
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    
    @Autowired
    private DataSource dataSource;

    /*@Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        // 配置数据源（注意，我使用的是 HikariCP 连接池），以上注解是指定数据源，否则会有冲突
        return DataSourceBuilder.create().build();
    }*/


    @Bean
    public TokenStore tokenStore() {
        // 基于 JDBC 实现，令牌保存到数据
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public ClientDetailsService jdbcClientDetails() {
        // 基于 JDBC 实现，需要事先在数据库配置客户端信息
        return new JdbcClientDetailsService(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 设置令牌
        endpoints.tokenStore(tokenStore());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 读取客户端配置
        clients.withClientDetails(jdbcClientDetails());
    }

}
```



#### Bean配置

```java
package com.fengwenmyi.study_spring_security_oauth2_auth_mysql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Erwin Feng
 * @since 2019-08-22 12:01
 */
@Configuration
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
```

#### 权限配置

```java
package com.fengwenmyi.study_spring_security_oauth2_auth_mysql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Erwin Feng
 * @since 2019-08-21 17:25
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用自定义认证与授权
        auth.userDetailsService(userDetailsService);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/oauth/check_token");
    }
}
```

## 第二部分，基于数据实现用户认证与授权