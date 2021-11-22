package mwg.wb.common;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GraphDBConstant {

	public static String TAG = "GraphDBConstantInfo.txt";

	public static String EDGE_INFO_FILENAME = "graphdb_nodes_relationship.json";
	/**
	 * Dung trong injection, day data len queue
	 * 
	 * @key String: updated table
	 * @value TableEdgesInfo
	 */
	public static Map<String, TableEdgesInfo> TABLE_EDGES_INFO = new HashMap<String, TableEdgesInfo>();

	/**
	 * Dung trong upsert, day data tu queue len graph
	 * 
	 * @key String: updated table
	 * @value TableEdgesInfo
	 */
	public static Map<String, EdgeInfo> EDGES_LIST = new HashMap<String, EdgeInfo>();

	public static void initEdgesList() throws Exception {

		String edgesJsonString = new String(Files.readAllBytes(Paths.get(Utils.getCurrentDir() + EDGE_INFO_FILENAME)));

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		TableEdgesInfo[] edgeInfoArray = mapper.readValue(edgesJsonString, TableEdgesInfo[].class);

		for (TableEdgesInfo table : edgeInfoArray) {

			List<EdgeInfo> edges = table.edges;
			if (edges != null) {
				for (EdgeInfo edge : edges) {

					String edgeString = edge.edge;

					if (EDGES_LIST.get(edgeString) == null) {

						EDGES_LIST.put(edgeString, edge);
					} else {
						System.out.println("Warning!!!!! Trung key edge: " + edgeString);
						Logs.LogFile(TAG, "Warning!!!!! Trung key edge: " + edgeString);
					}

				}
			}
		}
		System.out.println("Khoi tao thanh cong: " + EDGES_LIST.size() + " edge");
		Logs.LogFile(TAG, "Khoi tao thanh cong: " + EDGES_LIST.size() + " edge");
	}

	public static void initTableEdgesInfo() throws Exception {

		String edgesJsonString = new String(Files.readAllBytes(Paths.get(EDGE_INFO_FILENAME)));

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		TableEdgesInfo[] edgeInfoArray = mapper.readValue(edgesJsonString, TableEdgesInfo[].class);

		for (TableEdgesInfo e : edgeInfoArray) {

			// Khong de dbtable rong
			if (Utils.StringIsEmpty(e.dbtable)) {
				e.dbtable = e.table;
			}

			TABLE_EDGES_INFO.put(e.table, e);
		}

		Logs.LogFile(TAG, "Khoi tao thanh cong: " + TABLE_EDGES_INFO.size() + " table");
		System.out.println("Khoi tao thanh cong: " + TABLE_EDGES_INFO.size() + " table");
	}

}
