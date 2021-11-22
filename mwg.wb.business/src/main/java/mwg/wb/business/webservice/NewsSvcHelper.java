package mwg.wb.business.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mwg.wb.client.OracleClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.common.ViewTrackingBO;
import mwg.wb.model.scs.ResultBO;
import mwg.wb.model.webservice.NewsView;
import mwg.wb.model.webservice.ViewsKeywordSuggest;
import oracle.jdbc.OracleTypes;

public class NewsSvcHelper {

	private OracleClient dbClient = null;

	private static ClientConfig config = null;

	public NewsSvcHelper(ClientConfig aconfig) {
		config = aconfig;
//		if (dbClient == null) {
//			try {
//				dbClient = new OracleClient(config.DB_URL, config.DB_USER, config.DB_PASS);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

	}

	public void getConnect222() throws ClassNotFoundException, SQLException {
		// dbClient.closeConnection();
		dbClient = new OracleClient(config.DB_URL, config.DB_USER, config.DB_PASS);
	}
	public void getConnect() throws ClassNotFoundException, SQLException {
		// dbClient.closeConnection();
		dbClient = new OracleClient(config.DB_CONNECTIONSTRING,config.tns_admin,config.wallet_location,1);
	}

	public List<NewsView> GetTopView7Days() throws SQLException {
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.GAMEAPP_NEWS_GETMOSTVIEW(?)}");
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
			// connectDB.close();
			ex.printStackTrace();
			return null;
		} finally {


//			if (connectDB != null) {
//				connectDB.close();
//			}
//			if (cs != null) {
//				cs.close();
//			}
			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);

		}
	}
	public List<NewsView> GetTopView7Days2() throws SQLException {
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.GAMEAPP_NEWS_GETMOSTVIEW(?)}");
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
			// connectDB.close();
			ex.printStackTrace();
			return null;
		} finally {


//			if (connectDB != null) {
//				connectDB.close();
//			}
//			if (cs != null) {
//				cs.close();
//			}
			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);

		}
	}

	public List<NewsView> GetTopView15Days(int cateID) throws SQLException {
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.NEWSDMX_GETTOPNEWS(?,?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.CURSOR);
			cs.setInt(2, cateID);
			cs.setInt(3, 2);// 2 là 15 ngày 1 là 60 ngày
			cs.setInt(4, 15);
			cs.execute();
			var result = (ResultSet) cs.getObject(1);
			List<NewsView> news = new ArrayList<NewsView>();
			int dem = 0;
			if (result != null) {
				while (result.next()) {
//					if (dem >= 15)
//						break;

					NewsView newsview = new NewsView();
					newsview.NewsID = result.getInt("NEWSID");
					newsview.Viewcounter = result.getInt("Viewcounter");
					newsview.FirstActivedDate = result.getDate("FirstActivedDate");
					news.add(newsview);
				}
			}
			OracleClient.CloseResultSet(result);

			return news;
		} catch (Exception ex) {
			// connectDB.close();
			ex.printStackTrace();
			return null;
		} finally {
			// connectDB.close();
//			dbClient.closeConnection();
//			if (connectDB != null) {
//				connectDB.close();
//			}
//			if (cs != null)
//				cs.close();

			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);
		}
	}
	public ResultBO<Integer> TrackingViewDish2(ViewTrackingBO viewTracking) throws Throwable {
		return null;
	}
	public ResultBO<Integer> TrackingViewDish(ViewTrackingBO viewTracking) throws Throwable {

		var result = new ResultBO<Integer>();
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.COOK_DISH_VIEWTRACKING_ADD(?,?,?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMBER);
			cs.setInt(2, viewTracking.Id);

			Timestamp ts = new Timestamp(viewTracking.Date.getTime());

			cs.setTimestamp(3, ts);
			cs.setInt(4, 0);
			cs.setString(5, "VanHanh");
			cs.execute();

			result.StatusCode = 200;
			result.Message = "success";
			result.Result = 1;

		}

		catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = "failed";
			result.Result = 0;
			e.printStackTrace();
		} finally {
//			if (dbClient != null) {
//				dbClient.closeConnection();
//			}
//			if (connectDB != null) {
//				connectDB.close();
//			}
//			if (cs != null) {
//				cs.close();
//			}

			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);

		}
		return result;

	}

	public ResultBO<Integer> TrackingViewNews(ViewTrackingBO viewTracking) throws Throwable {

		var result = new ResultBO<Integer>();
		CallableStatement cs = null;
		Connection connectDB = null;

		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.NEWS_VIEWTRACKING_ADD(?,?,?,?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMBER);
			cs.setInt(2, viewTracking.Id);

			Timestamp ts = new Timestamp(viewTracking.Date.getTime());
			cs.setInt(3, 0);
			cs.setTimestamp(4, ts);
			cs.setInt(5, 0);
			cs.setString(6, "Thanh Phi");
			cs.execute();

			result.StatusCode = 200;
			result.Message = "success";
			result.Result = 1;
		} catch (Throwable e) {
			e.printStackTrace();
			result.StatusCode = 500;
			result.Message = "failed";
			result.Result = 0;

		} finally {
//			if (dbClient != null) {
//				dbClient.closeConnection();
//			}
//			if (connectDB != null) {
//				connectDB.close();
//			}
//			if (cs != null) {
//				cs.close();
//			}

			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);
		}
		return result;
	}

	public NewsView GetMostviewbyNewsID(int newID) throws Throwable {
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();

			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.GAMEAPP_NEWS_GETMOSTVIEWBYID(?,?)}");
			cs.registerOutParameter(1, OracleTypes.CURSOR);
			// oracle.jdbc.OracleTypes.CURSOR
			cs.setInt(2, newID);
			cs.execute();
				var result = (ResultSet) cs.getObject(1);
			NewsView news = null;
			int dem = 0;
			if (result != null) {
				while (result.next()) {
					news = new NewsView();
					news.NewsID = result.getInt("Newsid");
					news.Viewcounter = result.getInt("ViewCounter");
					break;
				}
			}
			OracleClient.CloseResultSet(result);

			return news;
		} catch (Exception ex) {
			// connectDB.close();
			ex.printStackTrace();
			return null;
		} finally {
//			if (dbClient != null) {
//				dbClient.closeConnection();
//			}
//			if (connectDB != null) {
//				connectDB.close();
//			}
//			if (cs != null) {
//				cs.close();
//			}

			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);
		}
	}

	/**
	 * Add new keyword-suggest for news
	 * 
	 * @param keyWordSuggest
	 * @return
	 * @throws Throwable
	 */
	public ResultBO<Integer> addKeywordSuggestForNews(String keyWordSuggest) {

		var result = new ResultBO<Integer>();
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();
			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.KEYWORD_SUGGEST_NEWS_ADD(?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMBER);
			cs.setString(2, keyWordSuggest);
			cs.setString(3, "Admin");
			cs.execute();

			result.Result = cs.getInt(1);
			result.StatusCode = 200;
			result.Message = "success";

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = "failed";
			result.Result = 0;
			e.printStackTrace();
		} finally {
			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);
		}
		return result;
	}
	public ResultBO<Integer> addKeywordSuggestForNews2(String keyWordSuggest) {

		var result = new ResultBO<Integer>();
		CallableStatement cs = null;
		Connection connectDB = null;
		try {
			getConnect();
			connectDB = dbClient.getConnection();

			cs = connectDB.prepareCall("{call TGDD_NEWS.KEYWORD_SUGGEST_NEWS_ADD(?,?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMBER);
			cs.setString(2, keyWordSuggest);
			cs.setString(3, "Admin");
			cs.execute();

			result.Result = cs.getInt(1);
			result.StatusCode = 200;
			result.Message = "success";

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = "failed";
			result.Result = 0;
			e.printStackTrace();
		} finally {
			OracleClient.CloseConnection(connectDB);
			OracleClient.CloseCallableStatement(cs);
		}
		return result;
	}

	public void closeConnection(OracleClient oclient,Connection connection, CallableStatement cs) throws Throwable {
		try{
			if (oclient != null) {
				oclient.closeConnection();
			}
		} catch (Throwable e){

		}
		try{
			if (connection != null) {
				connection.close();
			}
		} catch (Throwable e){

		}

		try{
			if (cs != null) {
				cs.close();
			}
		} catch (Throwable e){

		}
	}
}
