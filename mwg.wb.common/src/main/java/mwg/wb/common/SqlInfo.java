package mwg.wb.common;

import java.util.Map;

public class SqlInfo {

	public SqlInfo() {

	}

	public SqlInfo(String sql) {
		Sql = sql;
	}

	public int Type;
	public int Order;
	public String Hash;
	public String Sql;
	 public String msg;
	public String tablename;
	public String tablekey;
	public Map<String, Object> Params;
}