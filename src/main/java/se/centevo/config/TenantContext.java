package se.centevo.config;

class TenantContext {
	private TenantContext() {}

    private static InheritableThreadLocal<String> currentTenant = new InheritableThreadLocal<>();
    private static InheritableThreadLocal<String> currentTenantSystemUser = new InheritableThreadLocal<>();

    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getTenantId() {
        return currentTenant.get();
    }

    public static void setTenantSystemUser(String tenantSystemUser) {
        currentTenantSystemUser.set(tenantSystemUser);
    }

    public static String getTenantSystemUser() {
        return currentTenantSystemUser.get();
    }

    public static void clear(){
        currentTenant.remove();
        currentTenantSystemUser.remove();
    }
}
