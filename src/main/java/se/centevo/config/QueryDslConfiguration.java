package se.centevo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.sql.SQLServer2012Templates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringExceptionTranslator;


@Configuration
public class QueryDslConfiguration {
    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration() {
        SQLTemplates templates = SQLServer2012Templates.builder().printSchema().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates); 
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        return configuration;
    }
}

