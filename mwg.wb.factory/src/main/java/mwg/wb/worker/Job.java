package mwg.wb.worker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.ProductOldHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.business.WorkerPackage;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.business.helper.BhxPriceHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.queue.QueueMesssageEventArgs;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.client.service.BhxServiceHelper;
import mwg.wb.client.service.CrmServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.client.service.ErpPromotionHelper;
import mwg.wb.common.CurrentProcessInfo;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.notify.LineNotify;

import mwg.wb.model.api.ClientConfig;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

//import xeus.jcl.JarClassLoader;
//import xeus.jcl.JclObjectFactory;

public class Job {
	static String g_Path_Log = Utils.getCurrentDir() + "log/";
	public static Map<String, Long> Hasrocessed = Collections.synchronizedMap(new HashMap<String, Long>());
	public static Map<String, Ididx> g_ActiveAssemblyList = new HashMap<String, Ididx>();
	// public static JarClassLoader jcl = new JarClassLoader();

//	public static void LoadAllPackage() throws IOException {
//		jcl.add("D:\\TheGioiDiDong\\_GITMicroservice\\mwg.backend-java\\build\\test\\mwg.wb.price.jar"); // Load jar file
//		factory = JclObjectFactory.getInstance();
//	}

	public static boolean RequireStop = false;
	public static boolean lock = false;
	public static org.xeustechnologies.jcl.JclObjectFactory factory = null;

	public static LineNotify lineNotify = null;
	// public static String backendGroupToken =
	// "sizZI5Yq5AiyBXtz1tEw9hrTqjKKKh6yPszn7qi1BaU";

	public static LineNotify lineNotifyLog = null;
	public static String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";
	public static String backendGroupTokenLogError = "sizZI5Yq5AiyBXtz1tEw9hrTqjKKKh6yPszn7qi1BaU";

	// static Gson gSon = new GsonBuilder().setDateFormat("yyyy-MM-dd
	// HH:mm:ss").create();
// 
	// da xu ly:ms.productse.ProductSE16515611vi-VN: 2019-09-16 00:00:00:2019-09-16
	// 09:
	public static int ExecuteProcessQueue(WorkerPackage pkg, ORThreadLocal afactoryWrite, ORThreadLocal afactoryRead) {

		ObjectMapper mapper = null;
		ErpHelper eprhelper = null;
		ErpPromotionHelper erpPromoHelper = null;
		CrmServiceHelper crmhelper = null;

		QueueRabbitMQ queueConnect = null;
		// OrientDBFactory factory = null;
		PriceHelper priceHelper = null;
		CategoryHelper cateHelper = null;
		ProductHelper productHelper = null;
		CategoryHelper categoryHelper = null;
		ProductOldHelper productOldHelper = null;
		ClientConfig clientConfig = null;
		BhxServiceHelper bhxServiceHelper = null;
		BhxPriceHelper bhxPriceHelper = null;
		BHXStoreHelper bHXStoreHelper = null;
		// RedisCluster redisCluster = null;
		Ididx iPackage = null;
		String pkName = "";

		long tid = Thread.currentThread().getId();
		// FileHelper.AppendAllText(pkg.PackageId + "_queue.txt", tid +
		// ":ExecuteProcessQueue\r\n");
		boolean g_checkDouplicate = false;

		clientConfig = WorkerHelper.GetWorkerClientConfig();
		String OracledbURL = clientConfig.DB_URL;
		String udb = clientConfig.DB_USER;
		String passdb = clientConfig.DB_PASS;

		try {

			lineNotify = new LineNotify("LINE", backendGroupTokenLogError);
			lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);
			lineNotifyLog.DataCenter = pkg.DataCenter;
			lineNotify.DataCenter = pkg.DataCenter;
			OracleClient dbclient = null;// new OracleClient(OracledbURL, udb, passdb);

//			DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
//			mapper = new ObjectMapper();
//			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
//			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ObjectMapper mapperES = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);

			mapper = new ObjectMapper();
			DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
			mapper.setDateFormat(df);
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT);
			// mapper.setSerializationInclusion(Include.NON_DEFAULT);
			// mapper = DidxHelper.generateNonNullJsonMapper(GConfig.DateFormatString);

			if (Utils.StringIsEmpty(clientConfig.ERP_SERVICES_URL)) {

				Logs.WriteLine("ERP_SERVICES_URL==null");
				return -1;
			}
			eprhelper = new ErpHelper(clientConfig.ERP_SERVICES_URL, clientConfig.ERP_SERVICES_AUTHEN);
			erpPromoHelper = new ErpPromotionHelper(clientConfig.ERP_SERVICES_PROMOTION_URL,
					clientConfig.ERP_SERVICES_AUTHEN);
			queueConnect = new QueueRabbitMQ();

			queueConnect.Init(clientConfig.SERVER_RABBITMQ_URL);
			if (!queueConnect.Connect()) {
				FileHelper.AppendAllText(g_Path_Log + pkg.PackageId + "_queue.txt",
						tid + ":queueConnect.Connect()\r\n");
			}
			;
			if (!queueConnect.StartDequeue(pkg.Queue)) {

				FileHelper.AppendAllText(g_Path_Log + pkg.PackageId + "_queue.txt", tid + ":StartDequeue\r\n");

			}

			FileHelper.AppendAllText(g_Path_Log + pkg.PackageId + "_" + pkg.Queue + "_queue.txt",
					"threadid " + tid + ":" + Utils.GetCurrentDate() + ":processing...\r\n");

			// factory = new OrientDBFactory();
			// factory.InitConnect();
			priceHelper = new PriceHelper(afactoryRead, clientConfig);
			productHelper = new ProductHelper(afactoryRead, clientConfig);
			categoryHelper = new CategoryHelper(afactoryRead, clientConfig);
			productOldHelper = new ProductOldHelper(afactoryRead, clientConfig);
			cateHelper = new CategoryHelper(afactoryRead, clientConfig);
			bhxServiceHelper = new BhxServiceHelper(clientConfig);
			// bhxPriceHelper = new BhxPriceHelper();
			crmhelper = new CrmServiceHelper(clientConfig);
			bHXStoreHelper = new BHXStoreHelper(mapperES, clientConfig);
			// redisCluster = new RedisCluster(clientConfig);
			boolean logit = false;
			String logfile = "log";

			if (pkg.ProcessClassName.equals("mwg.wb.pkg.product.Product")) {

				iPackage = new mwg.wb.pkg.product.Product();
				pkName = "ProductInfo (gallery, url)";
			}
			// NEW
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.upsert.Upsert")) {
				iPackage = new mwg.wb.pkg.upsert.Upsert();
				pkName = "Upsert (init Graph)";
				g_checkDouplicate = false;
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.Status")) {
				iPackage = new mwg.wb.pkg.price.Status();
				g_checkDouplicate = true;
				pkName = "Product Status";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.WebStatusUpdate")) {
				iPackage = new mwg.wb.pkg.price.WebStatusUpdate();
				g_checkDouplicate = true;
				pkName = "WebStatusUpdate";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.StockStore")) {
				iPackage = new mwg.wb.pkg.price.StockStore();
				g_checkDouplicate = true;
				pkName = "Stock Store";
			}

			if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.Price")) {
				iPackage = new mwg.wb.pkg.price.Price();
				g_checkDouplicate = true;
				logfile = "price";
				logit = true;
				pkName = "Price";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.promotion.Promotion")) {
				iPackage = new mwg.wb.pkg.promotion.Promotion();
				g_checkDouplicate = true;
				logfile = "promotion";
				logit = true;
				pkName = "Promotion";
			}
//			if (pkg.ProcessClassName.equals("mwg.wb.pkg.promotion.PromotionV2")) {
//				iPackage = new mwg.wb.pkg.promotion.PromotionV2();
//				checkDouplicate = true;
//				logfile = "promotion";
//				logit = true;
//			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.promotion.PromotionBanKem")) {
				iPackage = new mwg.wb.pkg.promotion.PromotionBanKem();
				g_checkDouplicate = true;
				logfile = "promotionbankem";
				logit = true;
			}

//			if (pkg.ProcessClassName.equals("mwg.wb.pkg.promotion.PromotionGroup")) {
//				iPackage = new mwg.wb.pkg.promotion.PromotionSubBrand();
//				checkDouplicate = true;
//				logfile = "promotionsubbrand";
//				logit = true;
//			}
			// mwg.wb.pkg.validate.Validate

			if (pkg.ProcessClassName.equals("mwg.wb.pkg.validate.Validate")) {
				iPackage = new mwg.wb.pkg.validate.Validate();
				g_checkDouplicate = true;

			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.validate.ClearCache")) {
				iPackage = new mwg.wb.pkg.validate.ClearCache();
				g_checkDouplicate = true;

			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.productse.ProductSE")) {
				iPackage = new mwg.wb.pkg.productse.ProductSE();
				g_checkDouplicate = true;
				pkName = "ProductSE (Init Elastic)";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.cook.Cook")) {
				iPackage = new mwg.wb.pkg.cook.Cook();
				g_checkDouplicate = true;
				pkName = "Cook";
			}

			if (pkg.ProcessClassName.equals("mwg.wb.pkg.news.News")) {
				iPackage = new mwg.wb.pkg.news.News();
				g_checkDouplicate = true;
				pkName = "News";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.news.Faq")) {
				iPackage = new mwg.wb.pkg.news.Faq();
				g_checkDouplicate = true;
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.gameapp.GameAppSE")) {
				iPackage = new mwg.wb.pkg.gameapp.GameAppSE();
				g_checkDouplicate = true;
				pkName = "GameAppSE";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.productold.ProductOld")) {
				iPackage = new mwg.wb.pkg.productold.ProductOld();
				g_checkDouplicate = true;
				pkName = "ProductOld";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.productold.ProductOldPromotion")) {
				iPackage = new mwg.wb.pkg.productold.ProductOldPromotion();
				g_checkDouplicate = true;
				pkName = "ProductOldPromotion";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.sim.Sim")) {
				iPackage = new mwg.wb.pkg.sim.Sim();
				g_checkDouplicate = true;
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.CodeStock")) {
				iPackage = new mwg.wb.pkg.price.CodeStock();
				g_checkDouplicate = true;
				pkName = "Product Stock";
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.common.CommonPackage")) {
				iPackage = new mwg.wb.pkg.common.CommonPackage();
				g_checkDouplicate = true;
			}
			if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.WebStatusUpdate")) {
				iPackage = new mwg.wb.pkg.price.WebStatusUpdate();
				g_checkDouplicate = true;
			}
			if (iPackage == null) {

				Logs.WriteLine("ProcessClassName==null");
				return -1;
			}
			CurrentProcessInfo.PackageID = pkg.PackageId;
			CurrentProcessInfo.Queue = pkg.Queue;

			CurrentProcessInfo.ActiveDate = Utils.GetCurrentDate();
			ObjectTransfer objTrans = new ObjectTransfer();
			objTrans.factoryWrite = afactoryWrite;
			objTrans.factoryRead = afactoryRead;
			objTrans.priceHelper = priceHelper;
			objTrans.mapper = mapper;
			objTrans.productHelper = productHelper;
			objTrans.categoryHelper = categoryHelper;
			objTrans.productOldHelper = productOldHelper;
			objTrans.erpHelper = eprhelper;
			objTrans.erpPromotionHelper = erpPromoHelper;
			objTrans.clientConfig = clientConfig;
			objTrans.clientDB = dbclient;
			objTrans.notifyHelper = lineNotify;
			objTrans.notifyHelperLog = lineNotifyLog;
			objTrans.bhxServiceHelper = bhxServiceHelper;
			// objTrans.redisCluster = redisCluster;
			// objTrans.bhxPriceHelper = bhxPriceHelper;
			objTrans.bHXStoreHelper = bHXStoreHelper;
			objTrans.crmHelper = crmhelper;
//ORdb start 24/jun/2020
			iPackage.InitObject(objTrans);
			for (int i = 0; i < 100000000; i++) {

				
				boolean checkDouplicate=g_checkDouplicate;
//				Date dateC = new Date();
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTime(dateC);
//				int d = calendar.get(Calendar.HOUR_OF_DAY);
//				if (pkg.Queue.contains("didx.promotion")) {
//					if (pkg.PackageId.contains("servicepromotionsingle")) {
//
//					}else {
//
//						if (7 < d && d < 22) {
//							Utils.Sleep(10000);
//							continue;
//						}
//						
//					}
//				}

				if (RequireStop == true) {
					Logs.WriteLine("ExecuteProcessQueueTest end");
					break;
				}

				MessageQueue messageQueue = null;
				// Logs.WriteLine("Dequeue " + pkg.Queue + " ...");
				QueueMesssageEventArgs msg = null;
				try {
					msg = queueConnect.Dequeue(pkg.Queue);
				} catch (Throwable exRabit) {

					Logs.LogException(exRabit);
					continue;
				}

				if (msg != null) {

					// messageQueue = gSon.fromJson(msg.Message, MessageQueue.class);
					try {
						messageQueue = mapper.readValue(msg.Message, MessageQueue.class);
					} catch (Throwable exC) {
						Logs.LogErrorRequireFix("messagequeue_fail", msg.Message + "\r\n========\r\n");
						queueConnect.Ack(msg.Tag);
						continue;
					}

					if (messageQueue != null) {

						if (messageQueue.Type == 14 && messageQueue.ClassName.equals("mwg.wb.pkg.upsert")
								&& (messageQueue.Note + "").contains("COOK_DISH_TRGG(inserting)")) {
							checkDouplicate = true;

						}
//						if(messageQueue.ClassName != null
//								&& messageQueue.ClassName.equals("mwg.wb.pkg.productse.ProductSE")
//								&& messageQueue.Note.contains("20210415")) {
//							queueConnect.Ack(msg.Tag);
//							continue;
//						}//tam skip
//			
//						var dd = Utils.StringToDate("2021-06-02 17:42:56");
//						if (messageQueue.Note != null && messageQueue.Note.contains("COOK")
//								&& (messageQueue.CreatedDate.getTime() < dd.getTime())) {
//							queueConnect.Ack(msg.Tag);
//							continue;
//						} // tam skip

						if (pkg.Queue.equals("gr.dc4.didx.promotion") && messageQueue.Note.equals("DIDX_RENOTI")) {

							String queue = "gr.dc4.didx.promotionbk";
							String queuebk = "gr.dc2.didx.promotionbk";
							String queueBeta = "gr.beta.didx.promotionbk";
							messageQueue.Note = "DIDX_RENOTI";
							QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue, queuebk,
									queueBeta, messageQueue, false, "", 4);

							queueConnect.Ack(msg.Tag);
							continue;

						}
						if (pkg.Queue.endsWith(".sysdata0")) {

							String InjectProgramName = messageQueue.Source + "";

							if (InjectProgramName.equals("pm_saleorder_locking")
									|| InjectProgramName.equals("crm_productlocking")
									|| InjectProgramName.equals("news") || InjectProgramName.equals("pm_store")
									|| InjectProgramName.equals("accessory_product_tmp")
									|| InjectProgramName.equals("gen_store_ward_distance")
									|| InjectProgramName.equals("product_detail")
							) {

								String queue = "gr.dc4.sql.sysdata." + InjectProgramName;
								String queuebk = "gr.dc2.sql.sysdata." + InjectProgramName;
								String queueBeta = "gr.beta.sql.sysdata." + InjectProgramName;
								// messageQueue.Note = "DIDX_RENOTI";
								QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue,
										queuebk, queueBeta, messageQueue, false, "", messageQueue.DataCenter);

								queueConnect.Ack(msg.Tag);
								continue;
							}
						}

						if (pkg.Queue.contains("didx.status") && messageQueue.Note.equals("DIDX_RENOTI")) {

							queueConnect.Ack(msg.Tag);
							continue;

						}

//						if (messageQueue.Note.equals("20210519")) {
////ProductUserGallery
//
//							long hash = 0;
//							if (messageQueue.Hash > 0) {
//								hash = messageQueue.Hash;
//							} else {
//								hash = Utils.toLong(messageQueue.Identify);
//							}
//							if (hash > 0) {
//
//								int h = Utils.GetQueueNum(hash);
//								if (h >= 0) {
//									String queue = "gr.dc4.didx.product" + h;
//									String queuebk = "gr.dc2.didx.product" + h;
//									String queueBeta = "gr.beta.didx.product";
//									messageQueue.Note = "repush20210519";
//									QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue,
//											queuebk, queueBeta, messageQueue, false, "", messageQueue.DataCenter);
//
//									queueConnect.Ack(msg.Tag);
//									continue;
//
//								} else {
//
//								}
//							}
//
//						}

						if (pkg.ProcessClassName.equals("mwg.wb.pkg.productse.ProductSE")) {

							long hash = 0;
							if (messageQueue.Hash > 0) {
								hash = messageQueue.Hash;
							} else {
								hash = Utils.toLong(messageQueue.Identify);
							}
							if (hash > 0) {

								int h = Utils.GetQueueNum(hash);
								if (h >= 0) {
									String queue = "gr.dc4.didx.product" + h;
									String queuebk = "gr.dc2.didx.product" + h;
									String queueBeta = "gr.beta.didx.product";
									// messageQueue.Note="repush20210519";
									QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue,
											queuebk, queueBeta, messageQueue, false, "", messageQueue.DataCenter);

									queueConnect.Ack(msg.Tag);
									continue;

								} else {

								}
							}

						}

						if (pkg.IsActiveTK == 1) {

							long hash = 0;
							if (messageQueue.Hash > 0) {
								hash = messageQueue.Hash;
							} else {
								hash = Utils.toLong(messageQueue.Identify);
							}
							if (hash > 0) {

								int h = Utils.GetQueueNum(hash);
								if (h > 0) {
									String qu = "gr.dc4.sql.sysdata" + h;
									String qu2 = "gr.dc4.sql.sysdata" + h;
									String qubk = "gr.dc2.sql.sysdata" + h;
									String qudev = "gr.beta.sql.sysdata";
									QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev,
											messageQueue, false, "", pkg.DataCenter);
									queueConnect.Ack(msg.Tag);
									continue;
								} else {

								}
							}

						}

						String note = messageQueue.Note + "";
						boolean isLog = false;
						if (note.contains("DIDX_LOG")) {
							if (clientConfig.IS_NOT_PROCESS_LOG == 1) {
								// Logs.WriteLine("ClassName null");
								queueConnect.Ack(msg.Tag);
								continue;
							}
							isLog = true;

						}
						if (messageQueue.SiteID == 8) {

							queueConnect.Ack(msg.Tag);
							continue;

						}
//						if (note.contains("PackageItemUnit")) {
//							queueConnect.Ack(msg.Tag);
//							continue;
//						}
//					if (note.contains("DIDX_LOG")) {
//							queueConnect.Ack(msg.Tag);
//							continue;
//						}	
//					 
//						if (note.contains("DIDX_LOG")) {
//							queueConnect.Ack(msg.Tag);
//							continue;
//			 
//			 			}
//						if (note.contains("20210519")) {
//							messageQueue.Note = "DIDX_LOG|20210519";
//
//						}

//						if (note.contains("baconchim")) {
//							queueConnect.Ack(msg.Tag);
//							continue;
//			 
//			 			}
//			if (note.contains("aaDATA-REINIT")) {
//				queueConnect.Ack(msg.Tag);
//				continue;
//			 
//			 			}
//						if(note.contains("manual")) {
//							//Logs.WriteLine("ClassName null");
//							queueConnect.Ack(msg.Tag);
//							continue;
//							
//						}
						// LogsHelper.SendLog(CurrentProcessInfo.PackageID+":active
						// "+CurrentProcessInfo.ActiveDate);
//						if (messageQueue.SiteID < 0) {
//							//Logs.WriteLine("site<0");
//							//queueConnect.Ack(msg.Tag);
//						//	continue;
//						}
//						if (Utils.StringIsEmpty( messageQueue.Lang )) {
//							Logs.WriteLine("Lang null");
//							queueConnect.Ack(msg.Tag);
//							continue;
//						}
						if (Utils.StringIsEmpty(messageQueue.ClassName)) {
							Logs.WriteLine("ClassName null");
							queueConnect.Ack(msg.Tag);
							continue;
						}

						ResultMessage rs = new ResultMessage();
						rs.Code = ResultCode.Success;
//						if (messageQueue.ClassName.equals("mwg.wb.pkg.upsert")) {
//
//							checkDouplicate=false;
//						}
						messageQueue.ClassName = messageQueue.ClassName.replace("_", ".");
						// banh cho này
						// messageQueue.DataCenter = pkg.DataCenter;
						String key = messageQueue.ClassName + "_" + messageQueue.Identify + "_" + messageQueue.SiteID
								+ "_" + messageQueue.BrandID + "_" + messageQueue.Lang + "_" + messageQueue.ProvinceID
								+ "_" + messageQueue.Type + "_" + messageQueue.Source + "_" + messageQueue.CategoryID
								+ "_" + messageQueue.Data + "_" + messageQueue.DistrictID + "_"
								+ messageQueue.RepushClassName + "_" + messageQueue.Storeid;

						if (pkg.ProcessClassName.equals("mwg.wb.pkg.price.Status")) {

							key = messageQueue.ClassName + "_" + messageQueue.Identify + messageQueue.SiteID + "_"
									+ messageQueue.BrandID + "_" + messageQueue.Lang + "_" + messageQueue.Type;
							// status đang tính là all tinh thanh nen ko skip theo prov va store
						}
						String msgLog = Utils.GetCurrentDate() + ":" + pkg.Queue + " LangID: " + messageQueue.Lang
								+ ", site: " + messageQueue.SiteID + ", Identify: " + messageQueue.Identify + ", NOTE:"
								+ messageQueue.Note + ",classname:" + messageQueue.ClassName + ",date:"
								+ messageQueue.CreatedDate.toString() + ",dc" + messageQueue.DataCenter;

						if (logit) {
							Logs.LogFactoryMessage(logfile, "run:" + msgLog, messageQueue.SiteID);
						}
						Logs.getInstance().Log(isLog, note, "RunRefresh", messageQueue);
						if (checkDouplicate) {
//							synchronized (Job.class) {
							if (Hasrocessed.containsKey(key)) {

								long dt = Hasrocessed.get(key);
								if (messageQueue.CreatedDate.getTime() < dt) {

									String d1 = Utils.FormatDateForGraph(messageQueue.CreatedDate);
									String d2 = Utils.FormatDateForGraph(new Date(dt));

									// Logs.WriteLine("Skip:" + key + ": " + d1 + ":" + d2);
									String strNOTE = messageQueue.Note + "";
									if (strNOTE.contains("DIDX_LOG")) {
										Logs.Log(true, strNOTE, pkg.PackageId+":"+  pkg.ProcessClassName+ ":Skip " + key + " time " + d1 + ":" + d2);

									}
									if (logit) {
										Logs.LogFactoryMessage(logfile, "skip:" + msgLog, messageQueue.SiteID);
									}
									queueConnect.Ack(msg.Tag);
									continue;
								}
							}
//							}
						}
						if (iPackage == null) {
							Logs.WriteLine("iPackage == null");
							break;
						}
						if (RequireStop == true) {
							Logs.WriteLine("ExecuteProcessQueueTest end");
							break;
						}

						boolean success = false;
						if (logit) {
							Logs.LogFactoryMessage(logfile, "exec:" + msgLog, messageQueue.SiteID);
						}
						// run forever....
						for (int k = 0; k < 100000000; k++) {
							String errorMessage = "";
							// Logs.WriteLine("Refresh:" + messageQueue.ClassName + ":" + k + ":" +
							// messageQueue.Identify);
							try {
								// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
								// key + " -b1 \r\n");
								// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
								// msg.Message + " \r\n");

								long dt1 = System.currentTimeMillis();

								ResultMessage rsCode = iPackage.Refresh(messageQueue);
//cấm chạy chỗ này, vố trong chạy
								// lineNotifyLog.PushLineNotify(messageQueue, rsCode, pkName,
								// pkg.ProcessClassName);

								// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
								// key + " -b2 \r\n");

								errorMessage = rsCode.Message + rsCode.StackTrace;
								long dt2 = System.currentTimeMillis();

								// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() +
								// ":xong\r\n");

								if (logit) {
									Logs.LogFactoryMessage(logfile, "rsCode" + rsCode.Code, messageQueue.SiteID);
								}
								long sp = dt2 - dt1;
								if (sp > 1000) {
									// Logs.WriteLine("Slow refresh ..." + sp + " ms");
								}
								if (rsCode.Code == ResultCode.ReConnect) {
									// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
									// key + " -b3 \r\n");

									return -2;
								}
								if (rsCode.Code == ResultCode.Retry) {
									success = false;
									// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
									// key + " -b4 \r\n");

								} else if (rsCode.Code == ResultCode.Success) {
									if (checkDouplicate) {
										synchronized (Job.class) {
											Hasrocessed.put(key, dt2);
										}
									}
									success = true;
									// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
									// key + " -b5 \r\n");

									break;

								} else {
									// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
									// key + " -b6 \r\n");

									success = false;
								}
							} catch (Throwable exR) {
								success = false;
								Logs.LogException(exR, pkg.Queue);
								errorMessage = Logs.GetStacktrace(exR);
								// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() + ":" +
								// key + " -b7 \r\n");

							}

							// Logs.LogTemp(pkg.Queue+"log.txt",Utils.GetCurrentDate().toString() + ":
							// sleep:300* " + k+"\r\n");
							Logs.WriteLine("sleep:300*" + k);
							if (k % 5 == 0 && k > 2) {
								String message = "Refresh error ( " + k + " times ), messageQueue: " + msgLog
										+ ", error:" + errorMessage;
								lineNotify.NotifyError(message, messageQueue.DataCenter);
							}
							Thread.sleep(3000 * k);
						} // for

						if (success) {
							// Logs.LogTemp(pkg.Queue+"log.txt", Utils.GetCurrentDate().toString() +
							// ":ack\r\n");

							queueConnect.Ack(msg.Tag);
							// break;
						} else {
							// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() +
							// ":Nack b8\r\n");
							// queueConnect.Nack2(msg.Tag); //bo qua la chet
							// break;
						}

					} else {
						// Logs.LogTemp(pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() +
						// ":Null message\r\n");

						// Logs.WriteLine("Null message");
					}
				} else {

					// Logs.LogTemp (pkg.Queue + "log.txt", Utils.GetCurrentDate().toString() +
					// ":Null message2\r\n");
					// System.out.println("Null msg 2");
					//// Logs.WriteLine("Null msg 2");
					Hasrocessed.clear();
					Thread.sleep(5000);
				}
			}
		} catch (

		Throwable e) {

			Logs.LogExceptionExit(e);
			Logs.LogException("Job IOException", e);
			Logs.LogExceptionExit("\r\n2021:" + pkg.PackageId + " exit,msg:" + e.getMessage());
		}

		finally {

			afactoryWrite.CloseNoTx();
			afactoryRead.CloseNoTx();
			afactoryWrite.CloseAll();
			if (queueConnect != null)
				try {
					queueConnect.Close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			lineNotify.NotifyError("Queue " + pkg.Queue + ",pkg " + pkg.PackageId + " exit");
			Logs.LogExceptionExit("\r\n2012:" + pkg.PackageId + " exit");
			// System.exit(1);

		}
		return 1;
	}

	public static int ExecuteSchedule(WorkerPackage pkg, ORThreadLocal afactoryWrite, ORThreadLocal afactoryRead) {
		ObjectMapper mapper = null;
		ErpHelper eprhelper = null;
		PriceHelper priceHelper = null;
		CategoryHelper cateHelper = null;
		ProductHelper productHelper = null;
		CategoryHelper categoryHelper = null;
		ProductOldHelper productOldHelper = null;
		ClientConfig clientConfig = null;
		BhxServiceHelper bhxServiceHelper = null;
		BHXStoreHelper bHXStoreHelper = null;

		// BhxPriceHelper bhxPriceHelper = null;
		Ididx iPackage = null;
		long tid = Thread.currentThread().getId();
		boolean checkDouplicate = false;

		clientConfig = WorkerHelper.GetWorkerClientConfig();

		try {

			lineNotify = new LineNotify("LINE", backendGroupTokenLogError);
//			DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
//			mapper = new ObjectMapper();
//			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
//			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//			DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
//			mapper = new ObjectMapper();
//			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
//			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			mapper = DidxHelper.generateNonNullJsonMapper(GConfig.DateFormatString);

			if (Utils.StringIsEmpty(clientConfig.ERP_SERVICES_URL)) {

				Logs.WriteLine("ERP_SERVICES_URL==null");
				return -1;
			}
			eprhelper = new ErpHelper(clientConfig.ERP_SERVICES_URL, clientConfig.ERP_SERVICES_AUTHEN);

			// factory = new OrientDBFactory();
			// factory.InitConnect();
			priceHelper = new PriceHelper(afactoryRead, clientConfig);
			productHelper = new ProductHelper(afactoryRead, clientConfig);
			categoryHelper = new CategoryHelper(afactoryRead, clientConfig);
			productOldHelper = new ProductOldHelper(afactoryRead, clientConfig);
			cateHelper = new CategoryHelper(afactoryRead, clientConfig);
			bhxServiceHelper = new BhxServiceHelper();
			bHXStoreHelper = new BHXStoreHelper(mapper, clientConfig);

			// bhxPriceHelper = new BhxPriceHelper();
			boolean logit = false;
			String logfile = "log";
			if (pkg.ScheduleClassName.equals("mwg.wb.pkg.news.News")) {
				iPackage = new mwg.wb.pkg.news.News();
			}

			if (iPackage == null) {

				// Logs.WriteLine("ProcessClassName==null");
				return -1;
			}

			ObjectTransfer objTrans = new ObjectTransfer();
			objTrans.factoryWrite = afactoryWrite;
			objTrans.factoryRead = afactoryRead;
			objTrans.priceHelper = priceHelper;
			objTrans.mapper = mapper;
			objTrans.productHelper = productHelper;
			objTrans.categoryHelper = categoryHelper;
			objTrans.productOldHelper = productOldHelper;
			objTrans.erpHelper = eprhelper;
			objTrans.clientConfig = clientConfig;

			objTrans.notifyHelper = lineNotify;
			objTrans.bhxServiceHelper = bhxServiceHelper;
			// objTrans.bhxPriceHelper = bhxPriceHelper;
			iPackage.InitObject(objTrans);
			for (int i = 0; i < 10000000; i++) {

				ResultMessage rs = new ResultMessage();
				rs.Code = ResultCode.Success;

				if (iPackage == null) {
					// Logs.WriteLine("iPackage == null");
					break;
				}

				boolean success = false;

				// run forever....
				for (int k = 0; k < 100000000; k++) {
					String errorMessage = "";

					try {
						long dt1 = System.currentTimeMillis();
						ResultMessage rsCode = iPackage.RunScheduleTask();
						errorMessage = rsCode.Message + rsCode.StackTrace;
						long dt2 = System.currentTimeMillis();

						long sp = dt2 - dt1;
						if (sp > 1000) {
							// Logs.WriteLine("Slow refresh ..." + sp + " ms");
						}
						break;
					} catch (Throwable exR) {
						success = false;
						Logs.LogException(exR);
						errorMessage = Logs.GetStacktrace(exR);

					}

					Logs.WriteLine("sleep:300*" + k);
					if (k % 5 == 0 && k > 2) {
						String message = "Schedule error ( " + k + " times ) " + ", error:" + errorMessage;
						lineNotify.NotifyError(message);
					}
					Thread.sleep(3000 * k);
				}

			}
		} catch (Throwable e) {

			Logs.LogTemp(pkg.Queue + "log.txt", tid + ":Job IOException " + e.getMessage() + "\r\n");
			Logs.LogException(e);

		}

		finally {

		}
		return 1;
	}
}
