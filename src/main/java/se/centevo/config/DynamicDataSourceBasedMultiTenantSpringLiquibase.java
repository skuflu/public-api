package se.centevo.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//https://callistaenterprise.se/blogg/teknik/2020/10/03/multi-tenancy-with-spring-boot-part3/
@Slf4j
@RequiredArgsConstructor
class DynamicDataSourceBasedMultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {
	private final TenantDataSourceProperties dataSourceProperties;
	private final LiquibaseProperties liquibaseProperties;
	private ResourceLoader resourceLoader;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("DynamicDataSources based multitenancy enabled");
        runOnAllTenants(dataSourceProperties.getDatasources());
    }
    
	@Override
	public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

    protected void runOnAllTenants(Map<Object, Object> dataSources) throws LiquibaseException {
        for(Object key : dataSources.keySet()) {
            if(key.equals("default"))
                continue;
        	DataSource dataSource = (DataSource)dataSources.get(key);
            log.info("Initializing Liquibase for tenant " + key);

            try (Connection conn = dataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                log.info("Going to create DB schema PublicApi if not exists.");
                statement.execute("""
                    IF (SCHEMA_ID('PublicApi') IS NULL) 
                        BEGIN
                            EXEC ('CREATE SCHEMA [PublicApi]');
                        END
                """);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create schema 'PublicApi'", e);
            }

            try {
                SpringLiquibase liquibase = this.getSpringLiquibase(dataSource);
                liquibase.afterPropertiesSet();
            } catch (LiquibaseException e) {
                log.error("Failed to run Liquibase for tenant " + key, e);
            }
            log.info("Liquibase finished for tenant " + key);
        }
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setLiquibaseSchema("PublicApi");
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        return liquibase;
    } 
}
