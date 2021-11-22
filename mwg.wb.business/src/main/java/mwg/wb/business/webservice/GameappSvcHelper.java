package mwg.wb.business.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mwg.wb.client.OracleClient;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.webservice.NewsView;
import oracle.jdbc.OracleTypes;

public class GameappSvcHelper {
	private OracleClient dbClient = null;
	private Connection connectDB = null;
	private static ClientConfig config = null;

	public GameappSvcHelper(ClientConfig aconfig) {
		config = aconfig;

	}

	public void getConnect222() throws ClassNotFoundException, SQLException {
		// dbClient.closeConnection();
		
		
		//dbClient = new OracleClient(config.DB_URL, config.DB_USER, config.DB_PASS);
	}
	public void getConnect() throws ClassNotFoundException, SQLException {
		// dbClient.closeConnection();
		dbClient = new OracleClient(config.DB_CONNECTIONSTRING,config.tns_admin,config.wallet_location,1);
	}

	public List<NewsView> GetTopView7Days() throws SQLException {
		CallableStatement cs=null;
		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs= connectDB.prepareCall("{call TGDD_NEWS.GAMEAPP_NEWS_GETMOSTVIEW(?)}");
			cs.registerOutParameter(1, OracleTypes.CURSOR);
			// oracle.jdbc.OracleTypes.CURSOR
			// cs.setInt(2, 1);// 1 là view 2 là comment
			cs.execute();
			var result = (ResultSet) cs.getObject(1);
			List<NewsView> news = new ArrayList<NewsView>();
			int dem = 0;
			if (result != null) {
				while (result.next()) {
					if (dem >= 15)
						break;

					NewsView newsview = new NewsView();
					newsview.NewsID = result.getInt("NEWSID");
					newsview.Viewcounter = result.getInt("Viewcounter");

					news.add(newsview);
				}
			}
			OracleClient.CloseResultSet(result);

			return news;
		} catch (Exception ex) {

			ex.printStackTrace();
			return null;
		} finally {
			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);
		}
	}

}
