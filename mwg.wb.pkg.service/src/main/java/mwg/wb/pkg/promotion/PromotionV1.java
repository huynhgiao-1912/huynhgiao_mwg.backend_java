package mwg.wb.pkg.promotion;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
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
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.search.PromotionSO;

public class PromotionV1 implements Ididx {
	// private OrientDBFactory factoryDB = null;
//	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
//	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private ClientConfig clientConfig = null;
	String indexDB = "";

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
//		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
//		mapper = (ObjectMapper) objectTransfer.mapper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		indexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
	}

	public void UpsertODatabaseJson(boolean isLog, String note, String VertexClasses, String recordName,
			String recordValue, Object obj) throws IllegalArgumentException, IllegalAccessException {

		Field[] fields = obj.getClass().getFields();

		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();
			sql = sql + cl + "=:" + cl + ",";

		}
		sql = StringUtils.strip(sql, ",");
		Map<String, Object> params = new HashMap<String, Object>();

		for (final Field _item : fields) {
			Object cc = _item.get(obj);
			String o = _item.getName().toLowerCase();
			if (cc != null) {
				String value = String.valueOf(cc);
				// Logs.WriteLine(o+"="+value);
				Type f = _item.getType();
				if (f == Date.class) {
					params.put(o, Utils.FormatDateForGraph((Date) cc));
				} else if (f == boolean.class) {

					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);

				} else if (f == byte.class) {
					params.put(o, Byte.valueOf(value));
				} else if (f == char.class) {
					params.put(o, value.charAt(0));
				} else if (f == short.class) {
					params.put(o, Short.valueOf(value));
				} else if (f == int.class) {
					params.put(o, Integer.valueOf(value));
				} else if (f == long.class) {
					params.put(o, Long.valueOf(value));
				} else if (f == float.class) {
					params.put(o, Float.valueOf(value));
				} else if (f == double.class) {
					params.put(o, Double.valueOf(value));

				} else if (f == String.class) {
					params.put(o, String.valueOf(value));
				} else {
					params.put(o, String.valueOf(value));

				}

			} else {

				params.put(o, null);
			}

		}
		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
		Logs.Log(isLog, note, sql);
		PushSysData(isLog, note, sql, params);

	}

	public int PushSysData(boolean isLog, String Note, String sql, Map<String, Object> params) {

		return 1;
	}

	public int PushSysData(String sql) {

		return 1;
	}

	public int PushSysData(boolean isLog, String Note, String sql) {

		return 1;
	}

	public boolean IndexDataES(mwg.wb.model.promotion.Promotion[] lstPromotion, boolean isLog, String strNOTE,
			long ProductID, int SiteID, String LangID, int DataCenter) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (lstPromotion == null) {

			lstPromotion = new mwg.wb.model.promotion.Promotion[] {};
		}
		var solist = Stream.of(lstPromotion).map(x -> x.getSOObject()).toArray(PromotionSO[]::new);
		// if (lstPromotion != null && lstPromotion.length > 0) {
		for (int i = 0; i < 5000000; i++) {

			try {

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
						.UpdateFieldOBject(indexDB, esKeyTerm, "PromotionSoList", solist);

			} catch (Exception e) {
				Logs.WriteLine("Exception Index promotion status to ES FAILED: " + ProductID);
				Logs.WriteLine(e);
				Utils.Sleep(100);
			}
		}

		// }else {}

		return false;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;

		try {
			String[] messIdentify = message.Identify.split("\\|");
			if (messIdentify.length < 2) {
				Logs.WriteLine(messIdentify + ":" + messIdentify.length);
				return rsmsg;
			}
			String strNOTE = message.Note + "";

			boolean isLog = false;
			if (strNOTE.contains("LOG")) {

				isLog = true;
			}

			String productCode = messIdentify[0].trim();
			// int productID = Integer.valueOf(messIdentify[1]);

			int SalePrice = -1;
			int OutputTypeID = 0;
			int ProvinceID = -1;
			int InventoryStatusID = 1;
			int siteid = message.SiteID;
			String Lang = message.Lang;
			if (Utils.StringIsEmpty(Lang)) {
				Lang = "vi-VN";

			}
			int BrandID = DidxHelper.getBrandBySite(message.SiteID, Lang);
			// String arecordid = siteid + "_" + SalePrice + "_" + OutputTypeID + "_" +
			// InventoryStatusID;
			String arecordid = siteid + "_" + BrandID + "_" + "_" + SalePrice + "_" + OutputTypeID + "_"
					+ InventoryStatusID;

			String recordidcommon = BrandID + "_" + SalePrice + "_" + OutputTypeID + "_" + InventoryStatusID + "_"
					+ productCode;

			Logs.Log(isLog, strNOTE, "GetPromotionByPrdID " + arecordid);

			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();
			messageRepush.SiteID = message.SiteID;
			SqlInfo sqlinfoDel = new SqlInfo();

			sqlinfoDel.Sql = "update product_promotion set isdeleted=1 where recordidcommon='" + recordidcommon + "'";

			messageRepush.SqlList.add(sqlinfoDel);
			mwg.wb.model.promotion.Promotion[] allList = new mwg.wb.model.promotion.Promotion[] {};
			mwg.wb.model.promotion.Promotion[] lstpromotion = null;
			try {
				lstpromotion = erpHelper.GetPromotionByPrdID(productCode, ProvinceID, OutputTypeID, SalePrice, BrandID);
			} catch (Exception e) {
				rsmsg.Message = "ERP returned null";
				rsmsg.StackTrace = Utils.stackTraceToString(e);
				Logs.LogException(e);
				if (message.DataCenter == 3) {
					rsmsg.Code = ResultCode.Success;
				} else {
					rsmsg.Code = ResultCode.Retry;
				}
				return rsmsg;
			}
			var testdata = Arrays.asList(lstpromotion).stream().filter(x -> x.IsSpecialOutputTye == 1)
					.collect(Collectors.toList());

			int ProductIDX = (int) productHelper.GetProductIDByProductCodeFromCache(productCode);
			if (ProductIDX <= 0) {
				Logs.LogException("promotion_productnofoundfromcode.txt", message.Identify + "," + productCode);
				rsmsg.Code = ResultCode.Success;
				return rsmsg;

			}
			ProductBO productBO = null;
			if (GConfig.ProductTaoLao.containsKey((long) ProductIDX)) {
			} else {
				productBO = productHelper.GetProductBOByProductIDSEFromCache(ProductIDX, siteid, Lang);
			}
			if (lstpromotion != null && lstpromotion.length > 0) {
				List<mwg.wb.model.promotion.Promotion> listTempPromotion = new ArrayList<mwg.wb.model.promotion.Promotion>();
				for (mwg.wb.model.promotion.Promotion item : lstpromotion) {
					if (item != null) {

						String recordid = arecordid + "_" + item.ProvinceId + "_" + productCode + "_"
								+ item.PromotionID;
						// System.out.println("process promotion" + recordid + "->" +
						// item.PromotionListGroupName);
						if (siteid == 2) {
							recordid += "_" + item.ProductId;// th km vua giam tien, vua tang qua (productcodes)
						}

						item.OutputTypeID = OutputTypeID;
						item.SalePrice = SalePrice;
						item.SiteID = siteid;
						item.ProductCode = productCode;
						item.recordid = recordid;
						item.BrandID = BrandID;
						item.didxupdateddate = Utils.GetCurrentDate();
						item.IsDeleted = 0;
						item.LangID = Lang;
						item.recordidcommon = recordidcommon;

						if (siteid == 1 || siteid == 2) {
							long t1 = System.currentTimeMillis();
							productHelper.getHelperBySite(siteid).processPromotion2(item, productBO, siteid, Lang,
									InventoryStatusID, erpHelper, listTempPromotion);
							long deta = System.currentTimeMillis() - t1;
							if (deta > 200) {

								String msg = "\r\nsiteid=" + siteid + ",ProductIDX=" + ProductIDX + ",productCode="
										+ productCode;
								Logs.LogFactorySlowMessage(msg, deta);

							}
						}
						Logs.Log(isLog, strNOTE, "UpsertODatabaseJson : " + item.recordid);

						SqlInfo sqlinfo = new SqlInfo();

						RefSql ref = new RefSql();
						Utils.BuildSql(isLog, strNOTE, "product_promotion", "recordid", item.recordid, item, ref);
						sqlinfo.Sql = ref.Sql;
						sqlinfo.Params = ref.params;
						messageRepush.SqlList.add(sqlinfo);
						if (ProductIDX == 9999 || ProductIDX == 99999 || ProductIDX == 9999999
								|| ProductIDX == 99999999) {

						} else {
							var code = item.ProductCode.trim();
							String cmd2 = "create edge e_code_promotion  from (select from pm_product where productid='"
									+ code + "')   to(select from  product_promotion where recordid ='" + item.recordid
									+ "' and in('e_code_promotion')[productid='" + code + "'].size() = 0)";
							SqlInfo sqlinfoEdge = new SqlInfo();
							sqlinfoEdge.Sql = cmd2;
							messageRepush.SqlList.add(sqlinfoEdge);
						}

						listTempPromotion.add(item);

					}

				}

			} else {
				Logs.WriteLine("n stpromotion.length<=0");

			}
			mwg.wb.model.promotion.Promotion[] lstpromotion11 = null;
			if (siteid == 6) {
				BrandID = 11;
				arecordid = siteid + "_" + BrandID + "_" + "_" + SalePrice + "_" + OutputTypeID + "_"
						+ InventoryStatusID;

				// arecordid = siteid + "_" + SalePrice + "_" + OutputTypeID + "_" +
				// InventoryStatusID;
				recordidcommon = BrandID + "_" + SalePrice + "_" + OutputTypeID + "_" + InventoryStatusID + "_"
						+ productCode;
				SqlInfo sqlinfoDel11 = new SqlInfo();
				sqlinfoDel11.Sql = "update product_promotion set isdeleted=1 where recordidcommon='" + recordidcommon
						+ "'";
				messageRepush.SqlList.add(sqlinfoDel11);

				Logs.Log(isLog, strNOTE, "GetPromotionByPrdID11 " + arecordid);

				try {
					lstpromotion11 = erpHelper.GetPromotionByPrdID(productCode, ProvinceID, OutputTypeID, SalePrice,
							BrandID);
				} catch (Exception e) {
					rsmsg.Message = "ERP returned null";
					rsmsg.StackTrace = Utils.stackTraceToString(e);
					Logs.LogException(e);
					if (message.DataCenter == 3) {
						rsmsg.Code = ResultCode.Success;
					} else {
						rsmsg.Code = ResultCode.Retry;
					}
					return rsmsg;
				}
				if (lstpromotion11 != null && lstpromotion11.length > 0) {

					for (mwg.wb.model.promotion.Promotion item : lstpromotion11) {
						if (item != null) {
							String recordid = arecordid + "_" + item.ProvinceId + "_" + productCode + "_"
									+ item.PromotionID;
							item.OutputTypeID = OutputTypeID;
							item.SalePrice = SalePrice;
							item.SiteID = siteid;
							item.ProductCode = productCode;
							item.recordid = recordid;
							item.BrandID = BrandID;
							item.didxupdateddate = Utils.GetCurrentDate();
							item.IsDeleted = 0;
							item.LangID = Lang;
							item.recordidcommon = recordidcommon;
							Logs.Log(isLog, strNOTE, "UpsertODatabaseJson : " + item.recordid);

							SqlInfo sqlinfo = new SqlInfo();

							RefSql ref = new RefSql();
							Utils.BuildSql(isLog, strNOTE, "product_promotion", "recordid", item.recordid, item, ref);
							sqlinfo.Sql = ref.Sql;
							sqlinfo.Params = ref.params;
							messageRepush.SqlList.add(sqlinfo);

							var code = item.ProductCode.trim();
							String cmd2 = "create edge e_code_promotion  from (select from pm_product where productid='"
									+ code + "')   to(select from  product_promotion where recordid ='" + item.recordid
									+ "' and in('e_code_promotion')[productid='" + code + "'].size() = 0)";
							SqlInfo sqlinfoEdge = new SqlInfo();
							sqlinfoEdge.Sql = cmd2;
							messageRepush.SqlList.add(sqlinfoEdge);

						}

					}

				} else {
					Logs.WriteLine("KM 11 stpromotion.length<=0");

				}

			}
			allList = (mwg.wb.model.promotion.Promotion[]) ArrayUtils.addAll(lstpromotion, lstpromotion11);

			var listPro = Arrays.asList(allList).stream().filter(x -> x.ProvinceId == 3 && x.IsOnlineOutputType == 1)
					.collect(Collectors.toList());

			long ProductID = productHelper.GetProductIDByProductCodeFromCache(productCode);
			if (ProductID > 0) {
				String dfcode = productHelper.getDefaultCodeFromCache(ProductID, message.SiteID,
						message.SiteID == 6 ? 163 : 3);
				if (dfcode != null && dfcode.equals(productCode)) {
					boolean rs = IndexDataES(allList, isLog, strNOTE, ProductID, message.SiteID, message.Lang,
							message.DataCenter);

					if (!rs) {

						rsmsg.Code = ResultCode.Retry;
						Logs.WriteLine("IndexDataES false  ");
						Logs.Log(isLog, strNOTE, "IndexDataES false ");
						return rsmsg;

					}
				}
				// int ie = Utils.GetQueueNum(ProductID);
				String qu = "gr.dc4.sql.promotion";
				String qu2 = "gr.dc4.sql.promotion";
				String qubk = "gr.dc2.sql.promotion";
				String qudev = "gr.beta.sql.promotion";
				messageRepush.Source = "PROMOTION";
				messageRepush.Action = DataAction.Update;
				messageRepush.ClassName = "ms.upsert.Upsert";
				messageRepush.CreatedDate = Utils.GetCurrentDate();
				messageRepush.Lang = message.Lang;
				messageRepush.SiteID = message.SiteID;
				messageRepush.Identify = String.valueOf(ProductID);
				messageRepush.Note = strNOTE;
				messageRepush.DataCenter = message.DataCenter;
				try {
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
							isLog,strNOTE, message.DataCenter);
				} catch (Exception e) {
					Logs.LogException(e);
					rsmsg.Code = ResultCode.Retry;
					rsmsg.StackTrace = Utils.stackTraceToString(e);
					Logs.Log(isLog, strNOTE, "push promotion  " + e.getMessage());
					return rsmsg;
				}

			}
			rsmsg.Code = ResultCode.Success;
		} catch (Throwable e) {
			rsmsg.StackTrace = Utils.stackTraceToString(e);
			rsmsg.Code = ResultCode.Retry;
			Logs.LogException(e);
		}
		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
