 ![edb](docs/imgs/edb.png) 

## 说明
- 1、整合 springBoot 与 jfinalDb 的数据层操作，兼容 spring 和 jfinal 的事务
  - 1.1 基于 spring-tx 和 spring-jdbc 进行事务整合，并且保留jfinal环境本身的事务支持


# 📑 EDb使用介绍
## 开始

**maven 依赖**

```xml

<dependencies>

    <!-- jfinal-spring事务工具类 -->
    <dependency>
      <groupId>com.edbplus</groupId>
      <artifactId>jfinal-spring-tx</artifactId>
      <version>1.0</version>
    </dependency>
    
    <dependency>
        <groupId>com.jfinal</groupId>
        <artifactId>activerecord</artifactId>
        <!-- 必须大于等于4.9.11 版本，否则无效 -->
        <version>[4.9.11,)</version>
    </dependency>
    
     <!-- ================= 可选jar包组合1 和 组合2 选1个即可 ========================= -->
    <!-- ================= 可选jar包组合1 ========================= -->
            <!-- springJdbc 常用工具类 -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-jdbc</artifactId>
                <version>${spring-boot.version}</version>
            </dependency>
    <!-- ================= 可选jar包组合1 结束 ========================= -->    
        
    <!-- ================= 可选jar包组合2 ========================= -->
    <!-- 支持在 spring 之中整合使用 jfinal的事务 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-tx</artifactId>
        <version>${spring-tx.version}</version>
        <scope>provided</scope>
    </dependency>   
    <!-- 支持在 spring 之中整合使用 jfinal,通过jdbc现成的事务管理工具操作事务 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jdbc</artifactId>
        <version>${spring-tx.version}</version>
        <scope>provided</scope>
    </dependency>
     <!-- ================= 可选jar包组合结束 ========================= -->
    
</dependencies>

```

### 支持的springBoot 版本（已测的部分）
SpringBoot 1.x 事务一致性 ✔
SpringBoot 2.x 事务一致性 ✔
SpringMvc 5.x 版本 事务一致性 ✔





**基于Java的相关用例**
- [ spring启动配置参考 ](src/test/java/com/edbplus/db/config/SpringConfiguration.java)



## 帮助文档
 qq群: 192539982
 
 ![qqGroup](docs/imgs/qq_edb_group.png) 



