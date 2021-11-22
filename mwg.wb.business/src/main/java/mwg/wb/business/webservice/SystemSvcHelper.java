package mwg.wb.business.webservice;

import mwg.wb.client.OracleClient;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.system.SystemConfigBO;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SystemSvcHelper {
	private OracleClient dbClient = null;
	private Connection connectionDB = null;
	private static ClientConfig config = null;

	public SystemSvcHelper(ClientConfig aconfig) {
		config = aconfig;
	}

	public void getConnect221() throws SQLException, ClassNotFoundException {
		this.dbClient = new OracleClient(config.DB_URL, config.DB_USER, config.DB_PASS);
	}
	public void getConnect() throws ClassNotFoundException, SQLException {
		this.dbClient = new OracleClient(config.DB_CONNECTIONSTRING,config.tns_admin,config.wallet_location,1);
	}

	public SystemConfigBO[] getListSystemConfig() throws SQLException {
		SystemConfigBO[] result = null;
		CallableStatement cs = null;
		try {
			getConnect();
			connectionDB = dbClient.getConnection();
			cs = connectionDB.prepareCall("{call SYSTEM_CONFIG_SELECTALL(?)}");
			cs.registerOutParameter(1, OracleTypes.CURSOR);
			cs.execute();
			var resultSet = (ResultSet) cs.getObject(1);
			List<SystemConfigBO> listSystem = new ArrayList<SystemConfigBO>();
			if (resultSet != null) {
				while (resultSet.next()) {
					SystemConfigBO systemConfigBO = new SystemConfigBO();
					systemConfigBO.configID = resultSet.getString("CONFIGID");
					systemConfigBO.configName = resultSet.getString("CONFIGNAME");
					systemConfigBO.configValue = resultSet.getString("CONFIGVALUE");
					systemConfigBO.configType = resultSet.getInt("CONFIGTYPE");
					listSystem.add(systemConfigBO);
				}
			}
			result = listSystem.stream().toArray(SystemConfigBO[]::new);

			OracleClient.CloseResultSet(resultSet);

		} catch (Throwable throwables) {
			throwables.printStackTrace();
		} finally {
			OracleClient.CloseConnection(connectionDB);
			OracleClient.CloseCallableStatement(cs);
		}
		return result;
	}

}
