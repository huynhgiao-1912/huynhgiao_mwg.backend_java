package mwg.wb.pkg.promotion;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.ProductHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.search.PromotionSO;

public class SysDB implements Ididx {

	private ObjectMapper mapper = null;

	private ClientConfig clientConfig = null;
	private OracleClient dbclient = null;
	String indexDB = "";

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		mapper = (ObjectMapper) objectTransfer.mapper;
		dbclient = (OracleClient) objectTransfer.clientDB;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;

	}

	public static List<SqlInfo> BuildSqlList(String Identify, String table, String recordName, String recordValue,
			String otherCol, Connection cnndb, String strNote, boolean isLog) throws SQLException {
		ResultMessage resultMessage = new ResultMessage();

		List<SqlInfo> SqlList = new ArrayList<SqlInfo>();

		resultMessage.Code = ResultCode.Success;
		if (recordValue.isEmpty())
			return SqlList;

		String tbl = table;

		// Upsert
		// product_language|recordid|659311|simage,mimage,bimage,updateddate,updateduser,
		boolean isadd = true;
		String sqlDb = "SELECT *   FROM  " + tbl + " where  " + recordName + "='" + recordValue + "'";
		if (!otherCol.isEmpty()) {
			if (!otherCol.contains(recordName)) {
				otherCol = otherCol + "," + recordName;
			}
			sqlDb = "SELECT " + otherCol + "   FROM  " + tbl + " where  " + recordName + "='" + recordValue + "'";
			isadd = false;
		}

		String collectionname = table;

		String productCode = "";
		Statement cs = cnndb.createStatement();
		ResultSet reader = cs.executeQuery(sqlDb);

		ResultSetMetaData rsmd = reader.getMetaData();
		int columnsCount = rsmd.getColumnCount();
		while (reader.next()) {

			long st = System.currentTimeMillis();

			String sql = "update " + collectionname + " SET ";
			for (int i = 1; i < columnsCount + 1; i++) {

				String cl = rsmd.getColumnName(i).toLowerCase();

				sql = sql + "`" + cl + "`=:" + cl + ",";

			}
			sql = StringUtils.strip(sql, ",");

			Map<String, Object> params = new HashMap<String, Object>();
			for (int i = 1; i < columnsCount + 1; i++) {

				String cl = rsmd.getColumnName(i).toLowerCase();
				int sqlType = rsmd.getColumnType(i);

				if (reader.getObject(i) != null) {
					switch (sqlType) {
					case Types.BIGINT:
					case Types.INTEGER:
					case Types.TINYINT:
					case Types.SMALLINT:

						params.put(cl, reader.getInt(i));

						break;
					case Types.DATE:

						params.put(cl, Utils.FormatDateForGraph(reader.getDate(i)));

						break;
					case Types.TIMESTAMP:
						params.put(cl, Utils.FormatDateForGraph(new Date(reader.getTimestamp(i).getTime())));

						break;
					case Types.DOUBLE:
						params.put(cl, reader.getDouble(i));

						break;
					case Types.FLOAT:
						params.put(cl, reader.getFloat(i));

						break;
					case Types.NVARCHAR:
						params.put(cl, reader.getString(i).trim());
						break;
					case Types.VARCHAR:
						params.put(cl, reader.getString(i).trim());

						break;
					case Types.BLOB:
						params.put(cl, Utils.BlobToString(reader.getBlob(i)).trim());

						break;
					case Types.CLOB:
						params.put(cl, Utils.ClobToString(reader.getClob(i)).trim());

						break;
					case Types.NCLOB:
						params.put(cl, Utils.NClobToString(reader.getNClob(i)).trim());
						break;
					default:
						params.put(cl, reader.getString(i).trim());

						break;
					}
				} else {

					params.put(cl, null);
				}

			}
			int rsOr = 0;

			if (table.equals("pm_product")) {
				sql = sql + " Upsert where recordid=" + Utils.toInt(reader.getString("recordid"));
			} else {
				sql = sql + " Upsert where " + recordName + "=" + recordValue;
			}

			Logs.Log(isLog, strNote, sql);

			SqlInfo sqlinfoUpdate = new SqlInfo();
			sqlinfoUpdate.Sql = sql;
			sqlinfoUpdate.Params = params;
			SqlList.add(sqlinfoUpdate);

		}
		return SqlList;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;

		String identify = message.Identify;
		String tbl = message.Source;
		String cot = message.Data;

		String strNOTE = message.Note + "";

		boolean isLog = false;
		if (strNOTE.contains("LOG")) {

			isLog = true;
		}
		Connection cnndb = dbclient.getConnection();
		try {
			List<SqlInfo> ls = BuildSqlList(identify, tbl, "newsid", identify, "viewcount", cnndb, strNOTE, isLog);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
