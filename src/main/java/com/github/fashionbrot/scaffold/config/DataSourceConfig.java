package com.github.fashionbrot.scaffold.config;

import com.github.fashionbrot.scaffold.exception.ScaffoldException;
import com.github.fashionbrot.scaffold.mapper.BaseMapper;
import com.github.fashionbrot.scaffold.mapper.MysqlMapper;
import com.github.fashionbrot.scaffold.mapper.OracleMapper;
import com.github.fashionbrot.scaffold.mapper.SqlServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class DataSourceConfig {

    @Autowired
    private MysqlMapper mysqlMapper;
    @Autowired
    private OracleMapper oracleMapper;
    @Autowired
    private SqlServerMapper sqlServerMapper;

    @Value("${datasource.type}")
    private String datasource;

    @Bean
    @Primary
    public BaseMapper getGeneratorDao(){
        if("mysql".equalsIgnoreCase(datasource)){
            return mysqlMapper;
        }else if("oracle".equalsIgnoreCase(datasource)){
            return oracleMapper;
        }else if("sqlserver".equalsIgnoreCase(datasource)){
            return sqlServerMapper;
        }else {
            throw new ScaffoldException("不支持当前数据库：" + datasource);
        }
    }

}
