package mwg.wb.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import mwg.wb.business.SpecialsaleProgram.Pm_ProductBO;
import mwg.wb.business.SpecialsaleProgram.SpecialsaleProgramHelper;
import mwg.wb.business.helper.APIPriceHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.other.CamPreorder;
import mwg.wb.model.other.ORIntWrapper;
import mwg.wb.model.pricedefault.PriceDefaultGRBO;
import mwg.wb.model.pricestrings.PriceStringBO;
import mwg.wb.model.products.PriceStockObject;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.ProductItemExchange;
import mwg.wb.model.products.SpecialSaleProgramBO;
import mwg.wb.model.search.StockSO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.Reader;
//import java.sql.Clob;
//import java.sql.Connection;
//import java.sql.Driver;
//import java.sql.DriverManager;
//import java.sql.NClob;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;

public class PriceHelper {

//	OrientDBClient2 database;
	ORThreadLocal factoryRead = null;
	Gson gson = null;
	ObjectMapper mapper, mapperes;
	BHXStoreHelper bhxStoreHelper = null;
	ClientConfig config = null;
	private static CategoryHelper categoryHelper = null;
	private static String KEY_CACHED = "PriceHelper";
	public PriceHelper(ORThreadLocal afactoryRead, ClientConfig clientConfig) {
		factoryRead = afactoryRead;
		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
				.create();
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
		mapperes = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
		bhxStoreHelper = new BHXStoreHelper(mapperes, clientConfig);
		config = clientConfig;
		categoryHelper = new CategoryHelper(factoryRead, config);
	}

//	public PriceHelper() {
//
//		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
//				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
//				.create();
////database = new OrientDBClient(mwg.wb.common.OrientDBType.READ);
//		//database = new OrientDBClient2("172.16.3.71", 2424);
//		// connectInfo.HostName, connectInfo.Port,
//		// connectInfo.DatabaseName,
//		// Orient.Client.ODatabaseType.Graph,
//		// connectInfo.UserName, connectInfo.Password,
//		// "productheler" + pool);
//		//database.Connect();
//	}

//	public PriceHelper(OrientDBFactory factory) {
//
//		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
//				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
//				.create();
//
//		database = factory.GetConnect("read");
//	}

	public void Close() {
//		try {
//			if (database.isOpen())
//				database.Close();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}

	}

	public StockSO[] GetStockSOByProductCode(String productCode, int brandID) throws Throwable {
		return factoryRead.QueryFunction("product_GetStockSOByProductCode", StockSO[].class, false, productCode,
				brandID);
	}

	public List<ProductErpPriceBO> GetListPriceByProductID(long ProductID) {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		String functionname = "select product_GetPriceByID(:p) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction( functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {
					// String productlangRID = ls.next().getProperty("rs").toString();
					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
//		Logs.WriteLine(js2);

					return Arrays.asList(gson.fromJson(js2, ProductErpPriceBO[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	public List<ProductErpPriceBO> GetListPriceByByArea(long ProductID, int PriceArea) {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		params.put("m", PriceArea);
		String functionname = "select product_GetPriceByArea(:p,:m) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {

					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					return Arrays.asList(gson.fromJson(js2, ProductErpPriceBO[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	public List<ProductErpPriceBO> TestCallFunctionInvalid(long ProductID, int PriceArea, int SiteID) {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		params.put("m", PriceArea);
		params.put("s", SiteID);
		String functionname = "select TestCallFunctionInvalid(:p,:m,:s) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {

					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					return Arrays.asList(gson.fromJson(js2, ProductErpPriceBO[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	public List<ProductErpPriceBO> GetListPriceByBySiteID(long ProductID, int PriceArea, int SiteID) {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		params.put("m", PriceArea);
		params.put("s", SiteID);
		String functionname = "select product_GetPriceBySite(:p,:m,:s) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {

					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					return Arrays.asList(gson.fromJson(js2, ProductErpPriceBO[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	public List<ProductErpPriceBO> getListPriceStrings(long productID, int priceArea, int siteID) throws Throwable {
		var prices = factoryRead.queryFunction("product_getPriceStrings", PriceStringBO[].class, productID, siteID,
				priceArea);
		return Stream.of(prices).flatMap(x -> {
			try {
				var list = mapper.readValue(x.Data, ProductErpPriceBO[].class);
				for (var item : list) {
					item.RecordID = x.RecordID;
				}
				return Stream.of(list);
			} catch (IOException e) {
				e.printStackTrace();
				return Stream.empty();
			}
		}).collect(Collectors.toList());

	}
	public List<ProductErpPriceBO[]> getListPriceStringsByListID(int[] productID, int siteID) throws Throwable {
		var prices = factoryRead.queryFunction("product_getPriceStringsByIDList", PriceStringBO[].class, productID, siteID);
		var map = new ArrayList<ProductErpPriceBO[]>();
		if(prices != null && prices.length > 0) {
			 Stream.of(prices).forEach(x -> {
				try {
					var list = mapper.readValue(x.Data, ProductErpPriceBO[].class);
					for (var item : list) {
						item.RecordID = x.RecordID;
					}
					map.add(list);
				} catch (IOException e) {
					Logs.LogException(e);
				}
			});
		}
		return map;
	}

	public List<ProductErpPriceBO> getListPriceStringsWithQuantities(long productID, int priceArea, int siteID,
			int provinceID, String languageID) throws Throwable {
		var prices = factoryRead.queryFunction("product_getPriceStrings", PriceStringBO[].class, productID, siteID,
				priceArea);
		return processQuantities(siteID, prices, provinceID);
	}
	
	public List<ProductErpPriceBO> getCachedListPriceStrings() {
		return null;
	}

	public List<ProductErpPriceBO> getListPriceStringsWithQuantities(String productCode, int priceArea, int siteID,
			int provinceID) throws Throwable {
		var prices = factoryRead.queryFunction("product_getPriceStringsByProductCode", PriceStringBO[].class,
				productCode, siteID, priceArea);
		return processQuantities(siteID, prices, provinceID);
	}

	public int getCategoryID(int productID) throws Throwable {
		var rs = factoryRead.queryFunction("product_getCategoryByProductID", ProductBO[].class,
				productID);
		return rs == null || rs.length == 0 ? -1 : rs[0].CategoryID;
	}

	public List<ProductErpPriceBO> processQuantities(int siteID, PriceStringBO[] prices, int provinceID) {
		var quantities = Stream.of(prices).collect(Collectors.toMap(x -> x.RecordID, x -> {
			try {
				return x.DataEx != null ? mapper.readValue(x.DataEx, PriceStockObject.class) : new PriceStockObject();
			} catch (IOException e1) {
				e1.printStackTrace();
				return new PriceStockObject();
			}
		}));
		ORIntWrapper iw = new ORIntWrapper();
		iw.intresult = 0;
		return Stream.of(prices).flatMap(x -> {
			try {
				var list = mapper.readValue(x.Data, ProductErpPriceBO[].class);
				for (var item : list) {
					item.RecordID = x.RecordID;
				}
				return Stream.of(list).filter(y -> y != null && (y.ProvinceId == provinceID || provinceID <= 0))
						.map(y -> {
							if (quantities != null) {
								for (var u : quantities.entrySet()) {
									
									PriceStockObject priceStockObject = u.getValue();
									var keyU = u.getKey();

									// Tồn sp mới
									if (priceStockObject.quantities != null)
										for (var q : priceStockObject.quantities.entrySet()) {
											y.TotalQuantity += q.getValue();
											if (q.getKey() == y.ProvinceId) {
												y.Quantity += q.getValue();
												if (keyU.equals(y.RecordID)) {
													y.ProductCodeQuantity += q.getValue();
												}
											}
											if (keyU.equals(y.RecordID)) {
												y.ProductCodeTotalQuantity += q.getValue();
											}
										}

									// tồn trưng bày
									if (priceStockObject.sampleQuantities != null) {
										for (var q : priceStockObject.sampleQuantities.entrySet()) {
											y.TotalSampleQuantity += q.getValue();
											if (q.getKey() == y.ProvinceId) {
												y.SampleQuantity += q.getValue();
											}
										}
									}

									// tồn chuyển hàng
									if (priceStockObject.relateQuantities != null) {
										for (var q : priceStockObject.relateQuantities.entrySet()) {
											if (q.getKey() == y.ProvinceId && keyU.equals(y.RecordID)) {
												y.TotalQuantityRelateProvince += q.getValue();
											}
										}
									}

									// tồn trung tâm
									if (keyU.equals(y.RecordID) && priceStockObject.centerQuantities != null
											&& priceStockObject.centerQuantities.containsKey(y.ProvinceId)) {
										y.CenterQuantity = priceStockObject.centerQuantities.get(y.ProvinceId);
									}

									// tồn hub
									if (keyU.equals(y.RecordID) && priceStockObject.hubQuantities != null
											&& priceStockObject.hubQuantities.containsKey(y.ProvinceId)) {
										y.quantityOLOLHub = priceStockObject.hubQuantities.get(y.ProvinceId);
									}
								}
							}
							try {
								y.specialSale = getSpecialSaleProgramFromCache(y.ProductCode, 1);
							} catch(Throwable ignored) {
							}
							if (iw.intresult == 0) {
								try {
									iw.intresult =categoryHelper.getCategoryIDByProductIDFromCache((int) y.ProductId);
								 
								} catch (Throwable e) {
									iw.intresult = -1;
								}
							}
							y.CategoryId = iw.intresult;
							APIPriceHelper.getHelperBySite(siteID).ProcessProductStatus(y);

							// chuyển trạng thái 11 thành 4 (TGDĐ)
							// bỏ sau khi up live web mới
							// 20210311
//							if (siteID == 1 && y.WebStatusId == 11
//									&& !DidxHelper.isBeta() && !DidxHelper.isStaging() && !DidxHelper.isLocal()) {
//								y.WebStatusId = 4;
//							}

							return y;
						});
				
			} catch (IOException e) {
				e.printStackTrace();
				return Stream.empty();
			}
		}).collect(Collectors.toList());
	}

//	public void Dispose() {
//		database.Close();
//	}

	// lấy productcode by productid
	public List<String> GetListCodeBySiteID(long ProductID, int PriceArea, int SiteID) {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		params.put("m", PriceArea);
		params.put("s", SiteID);
		String functionname = "select product_GetListCodeBySite(:p,:m,:s) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {

					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					return Arrays.asList(gson.fromJson(js2, String[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	public List<InStockBO> GetStockByProductCode(String ProductCode, int BrandID) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductCode);
		params.put("m", BrandID);
		String functionname = "select product_GetStockByProductCode(:p,:m) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {
					// String productlangRID = ls.next().getProperty("rs").toString();
					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					// Logs.WriteLine(js2);
					return Arrays.asList(gson.fromJson(js2, InStockBO[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	public List<InStockBO> getStockByProductCodeNew(String productCode, int siteID) throws Throwable {
		var list = factoryRead.queryFunction("stock_getProvinceByCode", InStockBO[].class, productCode, siteID);
		return list == null ? null : Arrays.asList(list);
	}

	// get tồn của 1 code cơ sở
	protected List<InStockBO> GetStockByBaseProductCodeBHX(String ProductCode, int[] StoreIds) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productcode", ProductCode);
		params.put("storeidlist", StoreIds);
		params.put("inventorystatusid", 1);
		params.put("saleorderonlineid", 0);
		params.put("gencompanyid", 3);
		params.put("erpwaitlockquantity", 0);
		String functionname = "select bhx_stock_GetByProductcode(:productcode,:storeidlist,:inventorystatusid,:saleorderonlineid,:gencompanyid,:erpwaitlockquantity) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {
					// String productlangRID = ls.next().getProperty("rs").toString();
					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					// Logs.WriteLine(js2);
					var result = Arrays.asList(gson.fromJson(js2, InStockBO[].class));
					int provice = 0;
					List<Integer> storeids = new ArrayList<Integer>();
					for (int integer : StoreIds) {
						provice = bhxStoreHelper.getProvinceByStore(integer);
						storeids.add(integer);
					}
					for (InStockBO inStockBO : result) {
						inStockBO.productcode = ProductCode;
						inStockBO.baseProductCode = ProductCode;
						// var province = StoreProvinceConfig.GetProvinceByStore(inStockBO.s)
						inStockBO.provinceid = provice;
						inStockBO.storeidlist = storeids;
					}
					return result;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}

		return null;
	}

	// lấy danh sách quy đổi
	public List<ProductItemExchange> GetItemExchangeByCodeBHX(String ProductCode) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productcode", ProductCode);
		String functionname = "select bhx_stock_GetItemExchangeByCode(:productcode) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {
					// String productlangRID = ls.next().getProperty("rs").toString();
					String js = ls.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					// Logs.WriteLine(js2);
					return Arrays.asList(gson.fromJson(js2, ProductItemExchange[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return null;
	}

	public List<InStockBO> GetStockByProductCodeBHX(String productCode, int[] StoreIds) throws Throwable {

		// lấy danh sách quy đổi của code
		var lstexchange = GetItemExchangeByCodeBHX(productCode);
		if (lstexchange == null || lstexchange.isEmpty()) {
			Logs.WriteLine("Ko có quy đổi code");
			return null;
		}

		List<InStockBO> result = new ArrayList<InStockBO>();
		InStockBO tmp = new InStockBO();
		tmp.quantity = 0;
		tmp.productcode = productCode;

		// kiểm tra code có phải là cơ sở?
		for (ProductItemExchange productItemExchange : lstexchange) {
			if (productItemExchange != null) {
				if (productItemExchange.productcode != null
						&& productItemExchange.productcode.trim().equals(productCode)) {
					// là code cơ sở
					if (productItemExchange.unitproductcode != null
							&& productItemExchange.unitproductcode.trim().equals(productCode)
							&& productItemExchange.ischeckstockquantity == 1) {
						Logs.WriteLine("Code: " + productCode + " là code cơ sở");
						return GetStockByBaseProductCodeBHX(productCode, StoreIds);
					}
					// trường hợp ko phải code cơ sở
					var base = GetStockByBaseProductCodeBHX(productItemExchange.unitproductcode, StoreIds);
					if (base == null || base.size() == 0) {
						// code cơ sở ko có thì thoát luôn
						Logs.WriteLine("Ko có code cơ sở hoặc code co so = 0" + productItemExchange.unitproductcode
								+ " của " + productCode);
						break;
					}
					var baseproduct = base.get(0);
					var quantity_stock = baseproduct.quantity / productItemExchange.exchangequantity;
					tmp.quantity = (int) quantity_stock;
					tmp.baseProductCode = productItemExchange.unitproductcode;
					// tmp.productcode = baseproduct.productcode;
					tmp.provinceid = baseproduct.provinceid;
					tmp.storeidlist = baseproduct.storeidlist;
					result.add(tmp);

					return result;
				}
			}
		}

		return null;
	}

	public List<InStockBO> GetNewStockByProductCodeBHX(String productCode, int[] StoreIds) throws Throwable {

		var lstexchange = GetItemExchangeByCodeBHX(productCode);
		if (lstexchange == null || lstexchange.isEmpty()) {
			Logs.WriteLine("Ko có quy đổi code");
			return null;
		}

		ProductItemExchange itemExchange = lstexchange.stream()
				.filter(stock -> stock.productcode.trim().equals(productCode)).limit(1).findFirst().get();
		if (itemExchange.unitproductcode.trim().equals(productCode) && itemExchange.ischeckstockquantity == 1) {
			return GetStockByBaseProductCodeBHX(productCode, StoreIds);
		}
		String baseProductCode = itemExchange.unitproductcode;
		var lstStock = GetStockByBaseProductCodeBHX(baseProductCode, StoreIds);
		if (lstStock == null || lstStock.size() == 0) {
			Logs.WriteLine("Ko có code cơ sở hoặc code co so = 0" + baseProductCode + " của " + productCode);
			return List.of();
		}

		List<InStockBO> result = new ArrayList<InStockBO>();
		InStockBO stock = new InStockBO();
		stock.quantity = 0;
		stock.quantitynew = 0f;
		stock.productcode = productCode;
		var baseproduct = lstStock.get(0);
		var quantity_stock = baseproduct.quantity / itemExchange.exchangequantity;
		stock.quantity = (int) quantity_stock;
		stock.quantitynew = quantity_stock;
		stock.baseProductCode = baseProductCode;
		// tmp.productcode = baseproduct.productcode;
		stock.provinceid = baseproduct.provinceid;
		stock.storeidlist = baseproduct.storeidlist;
		result.add(stock);
		return result;
	}
//
//     public List<InStockBO> GetStockByProductCodeRest(string ProductCode, int BrandID)
//     {
//         return database.QueryFunction<List<InStockBO>>("product_GetStockByProductCode", ProductCode, BrandID.ToString());
//     }

	public void CloseOResultSet(OResultSet vao) {

		try {
			if (vao != null)
				vao.close();
		} catch (Throwable e) {
			Logs.LogException(e);
		}

	}

	public ProductErpPriceBO GetPriceByProductCode(String ProductCode, int Province) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productcode", ProductCode);
		params.put("province", Province);
		String functionname = "select product_getPriceByCodeAndProvince(:productcode,:province) as rs";
		OResultSet ls = null;
		try {
			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {
					// String productlangRID = ls.next().getProperty("rs").toString();
//					String js = ls.next().toJSON().replace("{\"rs\": ", "");
//					String js2 = js.substring(0, js.length() - 1);
					// Logs.WriteLine(js2);
					// return Arrays.asList(gson.fromJson(js2, InStockBO[].class));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);

		}

		// return database.QueryFunction(functionname, params,
		// ProductErpPriceBO[].class, true)[0];
		return null;
	}

	public static Map<Long, Date> g_listCreatedDate = new HashMap<Long, Date>();

	public Date GetCreatedDateFromCache(long productID) {

		if (g_listCreatedDate.containsKey(productID)) {
			return g_listCreatedDate.get(productID);

		}
		Date rs = GetCreatedDate(productID);
		synchronized (PriceHelper.class) {
			g_listCreatedDate.put(productID, rs);
		}

		return rs;
	}

	public Date GetCreatedDate(long productID) {
		if (GConfig.ProductTaoLao.containsKey(productID)) {
			return Utils.GetDefaultDate();
		}
		return factoryRead.QueryScalarDate("select createddate from product where productid = " + productID,
				"createddate");

	}

	public CamPreorder getCamPreorder(long productID) throws Throwable {
		var list = factoryRead.QueryFunction("product_campreorder", CamPreorder[].class, false, productID);
		return list.length > 0 ? list[0] : null;
	}

	public ProductErpPriceBO getDefaultPriceStrings(int productID, int siteID, int provinceID, int priceAreaID,
			String langID) throws Throwable {
		var wrap = factoryRead.queryFunction("product_getDefaultPriceStrings", PriceDefaultGRBO[].class, productID,
				siteID, priceAreaID, langID);
		if (wrap == null || wrap.length == 0) {
			return null;
		}
		String data = wrap[0].data;
		if (Strings.isNullOrEmpty(data)) {
			return null;
		}
		var prideDefaults = mapper.readValue(data, ProductErpPriceBO[].class);
		if (prideDefaults != null && prideDefaults.length > 0) {
			return Stream.of(prideDefaults).filter(x -> x.ProvinceId == provinceID).findFirst().orElse(null);
		}
		return null;
	}
	
	public Pm_ProductBO GetPm_ProductBO(String prductCode) throws Throwable {
		Pm_ProductBO[] rs = factoryRead.QueryFunction("pm_product_getInfo", Pm_ProductBO[].class, false, prductCode);
		if (rs != null && rs.length > 0) {
			return rs[0];
		}
		return null;
	}
	
	public Pm_ProductBO GetPm_ProductFromCache(String prductCode) throws Throwable {
		String key =  "pm_product_" + prductCode;
		var rs = (Pm_ProductBO) CacheStaticHelper.GetFromCache(prductCode, 100);
		if (rs == null) {
			rs = GetPm_ProductBO(prductCode);
			CacheStaticHelper.AddToCache(key, rs);

		}

		return rs;
	}
	public SpecialSaleProgramBO getSpecialSaleProgramFromCache(String productCode, int inventorystatusid)  throws Throwable  {
		String key = KEY_CACHED+ "getSpecialSaleProgramFromCache" + productCode+"_"+inventorystatusid;
		var rs = (SpecialSaleProgramBO) CacheStaticHelper.GetFromCache(key, 10 );
		if (rs == null) {
			rs = getSpecialSaleProgram(  productCode,   inventorystatusid);
			CacheStaticHelper.AddToCache(key, rs); 
		}

		return rs;
	}
	public SpecialSaleProgramBO getSpecialSaleProgram(String productCode, int inventorystatusid) throws Throwable {
		Pm_ProductBO codeInfo = GetPm_ProductFromCache(productCode);
		if (codeInfo != null) {
			SpecialSaleProgramBO sale = SpecialsaleProgramHelper.getInstance(factoryRead, config)
					.GetSpecialsaleProgram(productCode, codeInfo.brandid, inventorystatusid, codeInfo.subgroupid);
			if (sale != null) {

				sale.IsSetupProduct = codeInfo.issetupproduct > 0 ? true : false;
				return sale;
			}

		}
		return null;
	}
}
