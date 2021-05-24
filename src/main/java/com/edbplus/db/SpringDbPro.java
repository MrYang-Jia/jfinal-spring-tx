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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @ClassName SpringDbPro
 * @Description: 定义dbpro对象，主要是适配 tx 与spring事务协同一致
 * @Author 杨志佳
 * @Date 2021/5/23
 * @Version V1.0
 **/
public class SpringDbPro extends DbPro {

    // 这个是必须扩展的，这样子才能使用 IDbProFactory 扩展实现
    public SpringDbPro(String configName) {
        super(configName);
    }

    /**
     * Execute transaction.
     * @param config the Config object
     * @param transactionLevel the transaction level
     * @param atom the atom operation
     * @return true if transaction executing succeed otherwise false
     */
    public boolean tx(Config config, int transactionLevel, IAtom atom) {
        //System.out.println("打开改造的spring事务");
        Connection conn = config.getThreadLocalConnection();
        if (conn != null) {	// Nested transaction support
            try {
                if (conn.getTransactionIsolation() < transactionLevel)
                    conn.setTransactionIsolation(transactionLevel);
                boolean result = atom.run();
                if (result)
                    return true;
                throw new NestedTransactionHelpException("Notice the outer transaction that the nested transaction return false");	// important:can not return false
            }
            catch (SQLException e) {
                throw new ActiveRecordException(e);
            }
        }

        Boolean autoCommit = null;
        ConnectionHolder conHolder = null;
        try {
            //System.out.println("当前线程1-2:"+Thread.currentThread().getId());
            conn = config.getConnection();
            autoCommit = conn.getAutoCommit();
            // 设置当前线程的连接对象
            config.setThreadLocalConnection(conn);

            // ======== 定义spring的事务管理对象，在spring多线程组里才能获取到同一个事务对象，否则无法正确执行 =========
            // 建立spring事务关联
            conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.config.getDataSource());
            conHolder = new ConnectionHolder(conn);
            conHolder.requested();
            conHolder.setSynchronizedWithTransaction(true);
            // 绑定事务对象
            TransactionSynchronizationManager.bindResource(this.config.getDataSource(), conHolder);
            // 保留jfinal事务体系
            conn.setTransactionIsolation(transactionLevel);
            // 设置为不自动提交
            conn.setAutoCommit(false);
            // 执行内部方法
            boolean result = atom.run();
            if (result)
                conn.commit();
            else{
                conn.rollback();
            }

            return result;
        } catch (NestedTransactionHelpException e) {
            if (conn != null) try {conn.rollback();} catch (Exception e1) {
                LogKit.error(e1.getMessage(), e1);}
            LogKit.logNothing(e);
            return false;
        } catch (Throwable t) {
            if (conn != null) try {conn.rollback();} catch (Exception e1) {LogKit.error(e1.getMessage(), e1);}
            throw t instanceof RuntimeException ? (RuntimeException)t : new ActiveRecordException(t);
        } finally {
            try {
                if (conn != null) {
                    // 由 jfinal 事务控制决定当前节点是否提交，如果出现交错性事务，以 jfinal 的事务节点为主，尽量避免交错性事务，导致事务混乱
                    if (autoCommit != null)
                        conn.setAutoCommit(autoCommit);
//					conn.close();
                    // 设置成false
                    conHolder.setSynchronizedWithTransaction(false);
                    // 重置对象本身 -- 不然后续的释放无法正确执行
                    conHolder.released();
                    // 释放事务对象
                    TransactionSynchronizationManager.unbindResource(this.config.getDataSource());
                    // 释放连接对象
                    DataSourceUtils.releaseConnection(conn, this.config.getDataSource());

                }
            } catch (Throwable t) {
                LogKit.error(t.getMessage(), t);	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
            } finally {
                // 保留jfinal事务体系 -- 移除当前线程连接对象
                config.removeThreadLocalConnection();	// prevent memory leak
            }
        }
    }
}
