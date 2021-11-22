package mwg.wb.pkg.preproduct;

import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.pkg.productse.ProductSE;

public class testprenext {
	public static LineNotify lineNotify = null;
	public static String backendGroupToken = "5c1VIENb6dZjNsQ7fwc7KlyIhMVXvw93adBrY8wTUxS";

	public static void main(String args[]) throws Exception {
		productprenext();

	}

	public static void productprenext() {
		System.out.println("Hello World!");

		ObjectTransfer objTransfer = new ObjectTransfer();
		ORThreadLocal factoryRead = null;
		try {
			factoryRead = new ORThreadLocal();
			factoryRead.IsWorker = true;
		} catch (Exception e) {

			e.printStackTrace();
		}
		ClientConfig clientConfig = new ClientConfig();
		/// init config
		clientConfig.SERVER_ORIENTDB_READ_USER = "admin";
		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
		clientConfig.SERVER_ORIENTDB_WRITE_USER = "admin";
		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
		clientConfig.DATACENTER = 3;
		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";

		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";

		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
		clientConfig.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";

		clientConfig.ERP_SERVICES_URL = "http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx";
		clientConfig.ERP_SERVICES_AUTHEN = "ksdfswfrew3ttc!@4#123";
		/// initRead
		factoryRead.initRead(clientConfig, 0, 1);

		objTransfer.clientConfig = clientConfig;
		objTransfer.factoryRead = factoryRead;

		ProductSE newsSE = new ProductSE();
		newsSE.InitObject(objTransfer);

		MessageQueue message = new MessageQueue();
		message.SiteID = 1;
		message.Lang = "vi-VN";
		message.DataCenter = 3;
		message.Action = DataAction.Add;
		// message.Note = "DIDX_NOTIFY_GAMEAPP_VIEWCOUNT";

		int[] ids = new int[] { 58077, 58181, 58267, 59532, 59932, 59952, 60167, 60276, 60340, 60369, 60383, 60541,
				60546, 60633, 60671, 60686, 60809, 60815, 60831, 60857, 60860, 60909, 61086, 61193, 61196, 61307, 61309,
				61335, 61383, 61386, 66591, 66593, 66599, 67520, 67581, 67652, 67655, 67761, 67762, 67774, 67928, 67985,
				68008, 68096, 68097, 68098, 68116, 68308, 68453, 68454, 68476, 68574, 68635, 68867, 68919, 68920, 68957,
				68958, 68997, 69021, 69052, 69066, 69129, 69220, 69300, 69318, 69335, 69348, 69351, 69401, 69405, 69413,
				69480, 69494, 69639, 69650, 69698, 69704, 69714, 69721, 69723, 69724, 69776, 69783, 69815, 69824, 69828,
				69829, 69830, 69878, 69895, 69898, 69901, 70012, 70029, 70042, 70049, 70050, 70111, 70115, 70126, 70133,
				70163, 70238, 70329, 70357, 70359, 70360, 70701, 70955, 71061, 71298, 71306, 71770, 71788, 72373, 73021,
				73090, 73403, 73703, 73704, 73705, 74016, 74017, 74110, 74113, 74495, 74699, 74997, 75091, 75180, 75372,
				75373, 75416, 75484, 75773, 75785, 75787, 76014, 76481, 78124, 78151, 78268, 78479, 80213, 84667, 84798,
				86689, 87837, 87838, 87839, 87840, 87842, 87846, 88540, 88573, 88695, 88851, 88973, 88976, 89033, 89161,
				90709, 91131, 91751, 91800, 92419, 92541, 92962, 93705, 93708, 93709, 93713, 103244, 103404, 106211,
				106979, 108558, 108559, 108561, 108562, 111107, 113263, 114110, 114111, 114112, 114113, 114114, 114115,
				131915, 139392, 139401, 142463, 145723, 147939, 149456, 153856, 154897, 155261, 160730, 161554, 162326,
				167150, 179530, 179673, 182153, 188705, 190321, 190322, 190323, 190324, 190325, 190326, 191276, 191482,
				191483, 192001, 192003, 194327, 194917, 195012, 195577, 197228, 197512, 198150, 198413, 198792, 198986,
				199801, 200294, 200330, 200485, 200533, 201228, 202268, 202703, 202862, 202865, 202919, 203053, 203619,
				204089, 204403, 204404, 205773, 206176, 207641, 207649, 209535, 209564, 209792, 209796, 209798, 209800,
				210089, 210246, 210441, 210476, 210644, 210648, 210652, 210653, 210654, 210655, 211161, 211163, 211570,
				211644, 212116, 212212, 212247, 212357, 212374, 213022, 213023, 213031, 213033, 213588, 213590, 213591,
				213957, 214418, 214644, 214645, 214648, 214815, 214816, 214908, 214925, 215773, 216172, 216173, 216174,
				217287, 217308, 217856, 217859, 217935, 217936, 217937, 218355, 218361, 218363, 218594, 218621, 218662,
				219314, 219477, 219895, 219900, 219913, 220092, 220170, 220649, 220654, 220851, 220903, 220977, 221414,
				221817, 221820, 221926, 222512, 222629, 222631, 225734 };

		for (int i = 0; i < ids.length; i++) {
			try {
				message.Identify = Integer.toString(ids[i]);// "219695";//"219694";//"219696";
				newsSE.Refresh(message);
				//newsSE.RefreshPreProduct(ids[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("xong");
		var a = 1;
	}
}
