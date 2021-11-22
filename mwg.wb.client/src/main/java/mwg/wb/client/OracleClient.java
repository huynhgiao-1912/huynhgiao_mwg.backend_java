package mwg.wb.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import oracle.jdbc.OracleTypes;

public class OracleClient {

	private Connection conn;

	public OracleClient(String url, String user, String password) throws ClassNotFoundException, SQLException {
		if (conn == null) {
			try {
				Driver myDriver = new oracle.jdbc.driver.OracleDriver();
				DriverManager.registerDriver(myDriver);

			} catch (Throwable e) {
				System.out.println("Error occured " + e.getMessage());
			}
			DriverManager.setLoginTimeout(10);
			conn = DriverManager.getConnection(url, user, password);
			//conn = DriverManager.getConnection()
		}

	}
	public OracleClient(String url,String tns_admin, String wallet_location,int pool) throws ClassNotFoundException, SQLException {
		if (conn == null) {
			// hàm này sử dụng connectionString
			try {
				Driver myDriver = new oracle.jdbc.driver.OracleDriver();
				DriverManager.registerDriver(myDriver);

			} catch (Throwable e) {
				System.out.println("Error occured " + e.getMessage());
			}
			determineAndSetTnsHome(tns_admin,wallet_location);
			DriverManager.setLoginTimeout(10);
			conn = DriverManager.getConnection(url);
		}

	}
	private static void determineAndSetTnsHome(String tns_admin, String wallet_location) {

		String oracleHome = "E:\\MWG\\BANGOC\\DBwallet\\admin";
		String oracleWallet = "E:\\MWG\\BANGOC\\DBwallet\\wallets";

		System.setProperty("oracle.net.tns_admin", tns_admin);
		System.setProperty("oracle.net.wallet_location", wallet_location);
	}

	public Connection openConnection(String url, String user, String password) throws SQLException {
		if (conn == null) {

			conn = DriverManager.getConnection(url, user, password);
		}else if(conn.isClosed()==true) {
			conn = DriverManager.getConnection(url, user, password);
		}
		return conn;
	}

	public Connection getConnection() {

		return this.conn;
	}

	public void closeConnection() throws SQLException {
		if (this.conn != null && !this.conn.isClosed()) {
			this.conn.close();
		}
	}

	public ResultSet executeQuery(String sql) throws ClassNotFoundException, SQLException {

		return conn.createStatement().executeQuery(sql);
	}

	public boolean insertTable(String query) throws SQLException {
		Statement stmt = conn.createStatement();
		int count = stmt.executeUpdate(query);
		return (count > 0) ? true : false;
	}

//	
//	public ResultSet    callProcedure( ) {
//        Connection con = getConnection();
//        CallableStatement cs = null;
//        try {
//            cs = con.prepareCall("{call DGRAPH_SYNC_MESSAGE_SEL(2323123,10)}"); 
//            cs.registerOutParameter(1,  OracleTypes.CURSOR);
//             cs.setInt(2, 2323123); 
//             cs.setInt(3, 10);
//            cs.execute();
//            
//             return cs.getResultSet();
//               
//            
//        } catch (SQLException e) {
//            System.err.println("SQLException: " + e.getMessage());
//        }
//        finally {
//            if (cs != null) {
//                try {
//                    cs.close();
//                } catch (SQLException e) {
//                    System.err.println("SQLException: " + e.getMessage());
//                }
//            }
//            
//        }
//        return null;
//    }


	public static void CloseConnection(Connection connectDB) {
		try {
			if(connectDB != null && !connectDB.isClosed()){
				connectDB.close();
			}
		}catch (Throwable e){

		}
	}
	public static void CloseOracleClient(OracleClient dbClient){
		try {
			if(dbClient != null ){
				dbClient.closeConnection();
			}
		}catch (Throwable e){

		}
	}

	public static void CloseCallableStatement(CallableStatement cs){
		try{
			if (cs != null) {
				cs.close();
			}
		}catch (Throwable e){}
	}
	public  static void CloseResultSet(ResultSet r)  {
		try{
			if(r != null){
				r.close();
			}
		}catch (Throwable e){}
	}

}
