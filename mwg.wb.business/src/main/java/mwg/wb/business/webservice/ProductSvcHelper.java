package mwg.wb.business.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import mwg.wb.client.OracleClient;
import mwg.wb.model.api.ClientConfig;
import oracle.jdbc.OracleTypes;

public class ProductSvcHelper {

	private ClientConfig config = null;

	public ProductSvcHelper(ClientConfig config) {
		this.config = config;
	}

	public OracleClient getOracleClient222() throws ClassNotFoundException, SQLException {
		return new OracleClient(config.DB_URL, config.DB_USER, config.DB_PASS);
	}
	public OracleClient getOracleClient() throws ClassNotFoundException, SQLException {
		// dbClient.closeConnection();
		return new OracleClient(config.DB_CONNECTIONSTRING,config.tns_admin,config.wallet_location,1);
	}


	public int getSoldProductQuantity(int siteID, int productID, long fromDate, long toDate)
			throws Throwable {
		OracleClient oclient = null;
		Connection connection = null;
		CallableStatement cs = null;
		int count = -1;
		try {
			oclient = getOracleClient();
			connection = oclient.getConnection();
			cs = connection.prepareCall("{call TGDD_NEWS.PRODUCT_TRACKING_STA_GETCOUNT(?,?,?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMBER);
			cs.setInt(2, productID);
			cs.setDate(3, new Date(fromDate));
			cs.setDate(4, new Date(toDate));
			cs.setInt(5, siteID);
			cs.execute();
			count = cs.getInt(1);
		} finally {
			//closeConnection(oclient,connection, cs);
			OracleClient.CloseConnection(connection);
			OracleClient.CloseCallableStatement(cs);
		}
		return count;
	}

	public int updateProductStatus(int productID, int statusID, int siteID, String languageID, double price)
			throws Throwable {
		OracleClient oclient = null;
		Connection connection = null;
		CallableStatement cs = null;
		int count = -1;
		try {
			oclient = getOracleClient();
			connection = oclient.getConnection();
			cs = connection.prepareCall("{call PRODUCT_PRODUCTSTATUS_UPD(?,?,?,?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMBER);
			cs.setInt(2, productID);
			cs.setInt(3, statusID);
			cs.setInt(4, siteID);
			cs.setString(5, languageID);
			cs.setDouble(6, price);
			cs.execute();
			count = cs.getInt(1);
		} finally {

			OracleClient.CloseConnection(connection);
			OracleClient.CloseCallableStatement(cs);

		}
		return count;

	}

}
