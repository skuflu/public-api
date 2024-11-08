package se.centevo.config;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class AuthorizedDataSource implements DataSource {
	private final DataSource dataSource;

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return dataSource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return dataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return dataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = dataSource.getConnection();
		
		addSystemUserCodeInSessionContext(connection);
		
      	return connection;
	}

	private void addSystemUserCodeInSessionContext(Connection connection) throws SQLException {
		String tenantSystemUser = TenantContext.getTenantSystemUser();

		if(tenantSystemUser != null) {
			var rs = connection.createStatement().executeQuery("SELECT SystemUserId FROM SystemSecurity.SystemUser WHERE Code = '" + tenantSystemUser + "'");
			if(rs.next()) {
				int systemUserId = rs.getInt("SystemUserId");
				CallableStatement cs = connection.prepareCall("{Call SystemSecurity.spUtilSetUserNoReturn (?)}");
				cs.setInt(1, systemUserId);
				cs.executeUpdate();
				cs.close();
			} else {
				throw new RuntimeException(tenantSystemUser + " not found in database for tenant " + TenantContext.getTenantId());
			}
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return dataSource.getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return dataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		dataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		dataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return dataSource.getLoginTimeout();
	}
}
