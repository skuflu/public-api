package se.centevo.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LiquibaseProperties.class)
class TenantDataSourceConfig {
    private final TenantDataSourceProperties dataSourceProperties;
    private final LiquibaseProperties liquibaseProperties;

    @Bean
    DataSource getDataSource() {
        TenantAwareRoutingDataSource tenantAwareRoutingDataSource = new TenantAwareRoutingDataSource();
        tenantAwareRoutingDataSource.setTargetDataSources(dataSourceProperties.getDatasources());
        tenantAwareRoutingDataSource.afterPropertiesSet();
        return tenantAwareRoutingDataSource;
    }
    
    @Bean
    DynamicDataSourceBasedMultiTenantSpringLiquibase tenantLiquibase() {
        return new DynamicDataSourceBasedMultiTenantSpringLiquibase(dataSourceProperties, liquibaseProperties);
    }
}