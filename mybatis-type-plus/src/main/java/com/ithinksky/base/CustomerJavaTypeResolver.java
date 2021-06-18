package com.ithinksky.base;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.sql.Types;

/**
 * 类功能描述：
 *
 * @author: tengpeng.gao
 * @since: 2021/6/17 18:17
 */
public class CustomerJavaTypeResolver extends JavaTypeResolverDefaultImpl {

    public CustomerJavaTypeResolver() {
        super();
        //把数据库的 TINYINT 映射成 Integer
        super.typeMap.put(Types.TINYINT,
                new JavaTypeResolverDefaultImpl.JdbcTypeInformation("TINYINT", new FullyQualifiedJavaType(Integer.class.getName())));
    }


}
