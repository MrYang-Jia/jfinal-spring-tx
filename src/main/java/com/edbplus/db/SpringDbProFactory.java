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

import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.IDbProFactory;

/**
 * @ClassName SpringDbProFactory
 * @Description: 自定义 dbPro 工厂
 * @Author 杨志佳
 * @Date 2021/5/23
 * @Version V1.0
 **/
public class SpringDbProFactory implements IDbProFactory {
    @Override
    public DbPro getDbPro(String configName) {
        SpringDbPro springDbPro = new SpringDbPro(configName);
        return springDbPro;
    }
}
