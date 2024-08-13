package se.centevo.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantAwareRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
    	String tenantId = TenantContext.getTenantId();
    	if(tenantId != null)
    		return tenantId;
    	else
    		return "default";
    }
    
    
}