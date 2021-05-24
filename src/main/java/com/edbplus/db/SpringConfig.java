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

import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.*;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @ClassName ActiverecordConfig
 * @Description: activerecord基础配置文件
 * @Author 杨志佳
 * @Date 2021/5/20
 * @Version V1.0
 **/
public class SpringConfig extends Config {



    // 适配方法-jfinal版本必须大于等于 4.9.11 ,否则无法适配改造
    public SpringConfig(String name, DataSource dataSource, int transactionLevel) {
        super(name,dataSource,transactionLevel);
    }



    /**
     * 获取连接
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {

//        System.out.println("Spring适配改造获取连接对象 getConnection()");
        Connection conn = null;
        ConnectionHolder conHolder = conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.getDataSource());

        // 不是spring的事务管理，则走原来的逻辑
        if(conHolder == null ){
            conn = (Connection)this.getThreadLocalConnection();
            if (conn != null) {
                return conn;
            } else {
                return this.isShowSql() ? (new SqlReporter(this.getDataSource().getConnection())).getConnection() : this.getDataSource().getConnection();
            }
        }else{
            // 获取当前线程的连接
            conn = (Connection)this.getThreadLocalConnection();
            if (conn != null) {
                return conn;
            } else {
                // 通过spring获取连接对象
                conn = DataSourceUtils.getConnection(this.getDataSource());
            }
        }
        return  conn;
    }

    /**
     * 获取当前连接 -- 如果已经有事务的话，这里是会有对象的
     * @return
     */
    public Connection getThreadLocalConnection() {
        Connection connection =  super.getThreadLocalConnection();
        // 如果当前线程有连接对象，则获取当前线程的连接对象
        if(connection != null){
            return connection;
        }
        // 如果是spring事务，则返回spring的事务对象，否则直接取 jfinal 当前线程的事务对象
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.getDataSource());
        if(conHolder == null){
            return super.getThreadLocalConnection();
        }else{
            // 获取spring的事务对象
            return DataSourceUtils.getConnection(this.getDataSource());
        }
    }

    /**
     * 是否在事务中(适配spring事务判断)
     * @return
     */
    public boolean isInTransaction() {
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.getDataSource());
        // 不是spring的事务管理，则走原来的逻辑
        if(conHolder == null){
            return this.getThreadLocalConnection() != null;
        }
        return conHolder != null && (conHolder.getConnectionHandle()!=null || conHolder.isSynchronizedWithTransaction());
    }

    public void close(ResultSet rs, Statement st, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException var7) {
                LogKit.error(var7.getMessage(), var7);
            }
        }

        if (st != null) {
            try {
                st.close();
            } catch (SQLException var6) {
                LogKit.error(var6.getMessage(), var6);
            }
        }

        // 统一关闭连接
        close(conn);
    }

    public void close(Statement st, Connection conn) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException var5) {
                LogKit.error(var5.getMessage(), var5);
            }
        }
        // 统一关闭连接
        close(conn);
    }

    /**
     * 改造关闭对象(适配spring事务关闭的方法)
     * @param conn
     */
    public void close(Connection conn) {
        //
//        System.out.println("关闭事务");
        // 是否在事务中
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.getDataSource());

        // spring事务则走这个方法 && !conHolder.isSynchronizedWithTransaction()
        if (conHolder != null ) {
            // 直接交给spring来关闭 -- 是否释放，在spring里会有判断
            DataSourceUtils.releaseConnection(conn, this.getDataSource());
        }else{
            // 原 jfinal 事务走法
            if (this.getThreadLocalConnection() == null){
                // 连接
                if (conn != null)
                {
                    try {
                        //conn.close(); -- 替换成jdbc事务管理关闭
                        DataSourceUtils.releaseConnection(conn, this.getDataSource());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }




}
