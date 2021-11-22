package mwg.wb.pkg.productold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;

import mwg.wb.business.ProductHelper;
import mwg.wb.business.ProductOldHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.model.api.ClientConfig;

public class App {

	public static void main(String args[]) throws JsonParseException, IOException {
//		pushProduct();
//		pushPromotion();
//		testNghiaProductOld();
		if (DidxHelper.hostNameIs("142990-NTNghia")) {
//			pushPromotionNghia();
			pushProduct();
//			pushPromotion();
		}
	}

	public static void testNghiaProductOld() throws IOException {
		var config = new ClientConfig();
		config.SERVER_ORIENTDB_READ_USER = "admin";
		config.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
		config.SERVER_ORIENTDB_WRITE_USER = "admin";
		config.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
		config.DATACENTER = 3;
		config.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";

		config.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";

		config.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
		config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
		config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
		config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";

		config.ERP_SERVICES_URL = "http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx";
		config.ERP_SERVICES_AUTHEN = "ksdfswfrew3ttc!@4#123";

		var oread = new ORThreadLocal();
		var oldHelper = new ProductOldHelper(oread, config);
		var productHelper = new ProductHelper(oread, config);
		oread.initRead(config, 0, 1);

		var erp = new ErpHelper(config.ERP_SERVICES_URL, config.ERP_SERVICES_AUTHEN);
		String[] listInit = { "", "", "", "", "", "", "", "" };
		var m = new MessageQueue() {
			{
				// oldid
				Identify = "25609966";
				Note = "";
				SiteID = 1;
				Lang = "vi-VN";
				DataCenter = 3;
				Action = DataAction.Add;
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productOldHelper = oldHelper;
		tools.productHelper = productHelper;
		var old = new ProductOldTest();
		old.InitObject(tools);
		old.Refresh(m);
		System.out.print("xong luon !");
	}

	public static void pushPromotionNghia() throws JsonParseException, IOException {
		var config = new ClientConfig();
		config.SERVER_ORIENTDB_READ_USER = "admin";
		config.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
		config.SERVER_ORIENTDB_WRITE_USER = "admin";
		config.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
		config.DATACENTER = 3;
		config.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";

		config.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";

		config.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
		config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
		config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
		config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
		config.ELASTICSEARCH_PRODUCT_OLD_INDEX = "ms_productold";

		config.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
		config.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
		var oread = new ORThreadLocal();
		var oldHelper = new ProductOldHelper(oread, config);
		oread.initRead(config, 0, 1);
		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
		var m = new MessageQueue() {
			{
				// productcode|imei|productID|storeid
//				Identify = "3051098001006|AAANZ7E090000K720447|198105|1760";
				Identify = "NEW3051098001038|778929V1090489|200017|908";

				Note = "";
				SiteID = 2;
				Lang = "vi-VN";
				DataCenter = 3;
				Action = DataAction.Add;
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productOldHelper = oldHelper;
		var promo = new ProductOldPromotion();
		promo.InitObject(tools);
		promo.RefreshNew(m);
		System.out.print("hello");
	}

	public static void pushProduct() throws JsonParseException, IOException {
		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		var oldHelper = new ProductOldHelper(oread, config);
		var productHelper = new ProductHelper(oread, config);

		oread.initRead(config, 0, 1);
//		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
//				"werwerewrw32423!@4#123");
		var erp = new ErpHelper("http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"ksdfswfrew3ttc!@4#123");

		int[] listInitsiteID2 = { 31180339, 31180322, 31180206, 31179756, 31179701, 31179347, 31179244, 31179011,
				31178977, 31178704, 31178348, 31178188, 31177967, 31177933, 31177915, 31177730, 31177603, 31177535,
				31177505, 31177249, 31176681, 31175682, 31175423, 31175230, 31175189, 31174738, 31174465, 31174098,
				31174042, 31173734, 31173735, 31173736, 31173746, 31173726, 31173714, 31173707, 31173567, 31173502,
				31173500, 31173499, 31173497, 31173463, 31173460, 31173443, 31173442, 31173440, 31173441, 31173406,
				31173183, 31173184, 31173138, 31173053, 31173028, 31173006, 31173007, 31172929, 31172912, 31172913,
				31172905, 31172860, 31172355, 31172048, 31171842, 31171731, 31171537, 31170935, 31170774, 31170557,
				31170074, 31170058, 31170018, 31169716, 31169673, 31169584, 31169318, 31168671, 31168389, 31167914,
				31167872, 31167356, 31167228, 31167040, 31166444, 31166280, 31166195, 31166172, 31165473, 31165471,
				31165468, 31164841, 31164545, 31164148, 31163990, 31161587, 31161266, 31161113, 31160837, 31160585,
				31160565, 31160288, 31160184, 31159828, 31159718, 31159714, 31159582, 31159526, 31158988, 31158965,
				31158777, 31158581, 31158393, 31158298, 31158263, 31158254, 31158099, 31158086, 31158017, 31157862,
				31157854, 31157769, 31157746, 31157575, 31157420, 31157340, 31157326, 31157325, 31157220, 31157205,
				31157160, 31157122, 31157060, 31156906, 31156815, 31156709, 31156560, 31156381, 31156134, 31155924,
				31155778, 31155663, 31155491, 31155490, 31155308, 31155269, 31154858, 31154755, 31154650, 31154649,
				31154636, 31154142, 31154134, 31154084, 31154060, 31154000, 31153973, 31153615, 31153355, 31153352,
				31153322, 31153150, 31153082, 31153035, 31152955, 31152790, 31152695, 31152510, 31152470, 31152306,
				31152292, 31152220, 31152171, 31152159, 31152147, 31152133, 31152069, 31152049, 31151932, 31151926,
				31151925, 31151883, 31151858, 31151857, 31151847, 31151846, 31151841, 31151840, 31151839, 31151838,
				31151831, 31151829, 31151767, 31151766, 31151764, 31151762, 31151760, 31151758, 31151757, 31151756,
				31151755, 31151754, 31151689, 31151583, 31151277, 31150976, 31150960, 31150957, 31150921, 31150890,
				31150672, 31150663, 31150416, 31150374, 31150309, 31150301, 31150149, 31150008, 31149861, 31149852,
				31149709, 31149696, 31149507, 31149007, 31148941, 31148909, 29349282, 31148908, 31148252, 31148218,
				31148156, 31148018, 31148012, 31147836, 31147835, 31147802, 31147795, 31147756, 31147623, 31147583,
				31147489, 31147480, 31147471, 31147235, 31147178, 31147158, 31147139, 31146913, 31146856, 31146747,
				31146725, 31146675, 31146654, 31146623, 31146605, 31146519, 31146499, 31146480, 31146430, 31146045,
				31146037, 31145987, 31145914, 31145908, 31145753, 31145700, 31145693, 31145663, 31145613, 31145598,
				31145493, 31145466, 31145442, 31145430, 31145419, 31145407, 31145406, 31145404, 31145397, 31145396,
				31145229, 31145181, 31145175, 31145139, 31145109, 31145013, 31144914, 31144864, 31144863, 31144803,
				31144786, 31144714, 31144579, 31144480, 31144479, 31144411, 31144405, 31144356, 31144215, 31144181,
				31144122, 31144108, 31144055, 31143966, 31143830, 31143753, 31143715, 31143713, 31143647, 31143477,
				31143463, 31143399, 31143344, 31143314, 31143205, 31143191, 31143162, 31143142, 31143141, 31143121, };

		var m = new MessageQueue();
		int[] listSamsungNote20 = { 31150624, 31147964, 31130503, 31118306, 31115402, 31115401, 31112762, 31105397,
				31105069, 31104481, 31104327, 31101816, 31093882, 31089867, 31080132, 31080083, 31079846, 31079814,
				31069522, 31067787, 31057306, 31046732, 31036698, 31031873, 31031876, 31031815, 31031795, 31031783,
				31031780, 31017188, 31017189, 31013372, 31013297, 31012860, 30998736, 30992447, 30992197, 30992192,
				30992094, 30992077, 30992019, 30991915, 30991920, 30991874, 30991877, 30991836, 30990147, 30986839,
				30972852, };
		// productid 210653
		int[] listIphon11Promax = { 30729776, 30711926, 30711847, 30698999, 30693202, 30692899, 30690116, 30684648,
				30682237, 30654030, 30634943, 30629243, 30622706, 30622705, 30620331, 30614741, 30613475, 30613070,
				30613072, 30611688, 30610719, 30610541, 30610179, 30609356, 30598393, 30594112, 30594051, 30593940,
				30593905, 30578937, 30575236, 30489382, 30470086, 30470089, 30470077, 30456671, 30417195, 30402366,
				30389986, 30389971, 30351802, 30351796, 30351798, 30343846, 30321004, 30321002, 30305068, 30305063,
				30236775, 30236734 };

		int[] listRedmi9s = { 31174466, 31174392, 31172726, 31171833, 31171729, 31169919, 31165878, 31165871, 31165874,
				31165870, 31165844, 31165683, 31160657, 31158942, 31158253, 31157205, 31150841, 31150843, 31150834,
				31150845, 31150840, 31147480, 31146314, 31145043, 31143747, 31139193, 31138115, 31137595, 31130749,
				31130753, 31130746, 31130730, 31130744, 31130745, 31130718, 31130728, 31130717, 31130683, 31130603,
				31130600, 31130573, 31130574, 31130241, 31128023, 31126688, 31125748, 31123557, 31119932, 31117759,
				31112050 };
		

		var tools = new ObjectTransfer();

		tools.productOldHelper = oldHelper;
		tools.erpHelper = erp;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productOldHelper = oldHelper;
		tools.productHelper = productHelper;
		var old = new ProductOld();
		old.InitObject(tools);
//		for (int i : listRedmi9s) {
			m = new MessageQueue() {
				{
					// oldid
					Identify = 32471526 + "";
					Note = "";
					SiteID = 2;
					Lang = null;
					DataCenter = 4;
					Action = DataAction.Add;
				}
			};
			old.Refresh(m);
//		}

	}

	public static void pushPromotion() throws JsonParseException, IOException {
		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		var oldHelper = new ProductOldHelper(oread, config);
		oread.initRead(config, 0, 1);
		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
		var m = new MessageQueue() {
			{
				// productcode|imei|productid|Inventorystatusid|storeid
				Identify = "0131491001460|354756102644324|198986|2|356";
				Note = "";
				SiteID = 1;
				Lang = "vi-VN";
				DataCenter = 3;
				Action = DataAction.Add;
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productOldHelper = oldHelper;
		var promo = new ProductOldPromotion();
		promo.InitObject(tools);
		promo.Refresh(m);
		System.out.print("hello");
	}
}