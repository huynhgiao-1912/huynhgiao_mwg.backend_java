package mwg.wb.common;

import java.util.List;

public class TableEdgesInfo {
	
	/**
	 * schema in root db
	 * if dbschema == null, use default schema;
	 * else use this dbschema
	 */
	public String dbschema;
	
	/**
	 * table in root db
	 * if dbtable != null use this dbtable
	 * else use fromtable in {@EdgeInfo}
	 * 
	 */
	public String dbtable;
	
	/**
	 * Updated table
	 */
	public String table;
	
	/**
	 * Edge list of updated table
	 */
	public List<EdgeInfo> edges;
}
