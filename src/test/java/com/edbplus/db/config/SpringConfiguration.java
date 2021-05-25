package com.edbplus.db.config;


import com.edbplus.db.SpringConfig;
import com.edbplus.db.SpringDbProFactory;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.template.source.ClassPathSourceFactory;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @ClassName SpringConfig
 * @Description: db配置文件 -- 初始化配置方式 -1
 * @Author 杨志佳
 * @Date 2020/10/23
 * @Version V1.0
 **/
//@Slf4j
// 打开注释即可
//@EnableAutoConfiguration
//@Configuration
public class SpringConfiguration {

    // // 等待db1Source启动后注入
    // // 如果注入的时候报说找不到数据源，则可能是启动顺序的问题导致，可以尝试这么引入   @Qualifier("dataSource")
    @Autowired
    DataSource dataSource;

    /**
     * 生成数据源1 -- 主数据源 (ps:可以不用特意注册 dbpro，这里只是大概的一个讲解案例)
     * @return
     */
//    @Bean(name = "eDbPro")
    public DbPro getEDbPro(){
        // 调用本类的bean方法，会自动经过bean的拦截实现，获取到实例化后的bean实体

        // todo:步骤1 - 适配spring数据库连接池 -- 适配事务
        SpringConfig activerecordConfig = new SpringConfig(
                // 默认名称 ，使用 Db.use() 时，可获取到
                DbKit.MAIN_CONFIG_NAME
                // 这里可以替换成 spring体系的datasource
                ,dataSource
                // 事务级别 ，如果是spring时，可使用spring的事务级别替代，这个是属于数据库事务级别定义的，都一样
                , DbKit.DEFAULT_TRANSACTION_LEVEL
        );
        // 初始化数据源
        ActiveRecordPlugin arp = new ActiveRecordPlugin(activerecordConfig);
        // todo:步骤2 - 改造dbPro对象，实现工厂模式，适配 tx() 与 spring单线程事务一致
        SpringDbProFactory springDbProFactory = new SpringDbProFactory();
        // 设置 dbPro 工厂
        arp.setDbProFactory(springDbProFactory);
        // 其余配置请根据自己的项目自己设置
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.setDevMode(true);
        // 打印sql -- 交予底层统一打印,建议设置成false，自己定义监听器，或者交予 druid 的sql打印信息即可
        arp.setShowSql(true);
        //基础数据模板
        // arp.addSqlTemplate("/sql/all.sql");
        //添加共享模板
//        arp.getEngine().addSharedFunction("/sql/sharedfunction/common_function.sql");
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
//        // 如果不是linux环境
//        if(!SystemUtil.getOsInfo().isLinux()){
//            // 开发者模式
//            arp.setDevMode(true);
//        }else{
//            // 非开发者模式 -- 生产用该方式
//            arp.setDevMode(false);
//        }

        // 启动Record容器
        arp.start();

        return Db.use();
    }

}
