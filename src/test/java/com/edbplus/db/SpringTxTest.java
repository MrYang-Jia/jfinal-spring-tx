/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.druid.DruidPlugin;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

/**
 * @ClassName SpringTxTest
 * @Description: Spring事务案例测试
 * @Author 杨志佳
 * @Date 2021/5/20
 * @Version V1.0
 **/
public class SpringTxTest {

    String jdbcUrl = "jdbc:mysql://192.168.1.106:13306/tra_log?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useCompression=true";
    String userName = "root";
    String pwd = "dev-whbj@WHBJ";


    @Before
    public void init(){

        // druid资源池组件
        DruidPlugin mysqlDruid = new DruidPlugin(jdbcUrl, userName, pwd);
        // 启动资源池
        mysqlDruid.start();

        // 这是改造继承的对象(这里是随意改造基础的对象)
        SpringConfig activerecordConfig = new SpringConfig(
                // 默认名称 ，使用 Db.use() 时，可获取到
                DbKit.MAIN_CONFIG_NAME
                // 这里可以替换成 spring体系的datasource
                ,mysqlDruid.getDataSource()
                // 事务级别 ，如果是spring时，可使用spring的事务级别替代，这个是属于数据库事务级别定义的，都一样
                , DbKit.DEFAULT_TRANSACTION_LEVEL
        );
        // 调用已存在的config配置 --> 完美替换成功...
        ActiveRecordPlugin arp = new ActiveRecordPlugin(activerecordConfig);
        // 定义db扩展插件
        SpringDbProFactory springDbProFactory = new SpringDbProFactory();
        // 定义扩展的db插件
        arp.setDbProFactory(springDbProFactory);
        //
        arp.start();




    }

    /**
     * 测试jfinal模式下的启动情况
     */
    @Test
    public void test() {

        Db.use().tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            System.out.println(Db.use().findFirst("select 1 from dual"));
            return true;
        });

    }

}
