package com.urbanmicrocad.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    /**
     * 全局注册 TypeHandler，使 UUID 和 PostgreSQL 枚举类型在所有 Mapper 中生效。
     * 解决 @TableField(typeHandler=...) 在 @TableId 主键上被忽略的问题。
     */
    @Bean
    public ConfigurationCustomizer mybatisPlusTypeHandlerCustomizer() {
        return configuration -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            registry.register(UUID.class, JdbcType.OTHER, UuidTypeHandler.class);
            registry.register(String.class, JdbcType.OTHER, PgEnumTypeHandler.class);
        };
    }
}
