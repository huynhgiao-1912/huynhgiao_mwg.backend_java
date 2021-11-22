package mwg.wb.webapi.controller;

import com.fasterxml.jackson.core.JsonParseException;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.elasticsearch.dataquery.SearchFlag;
import mwg.wb.client.elasticsearch.dataquery.StoreQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.commonpackage.KeyWordRedirectSO;
import mwg.wb.model.commonpackage.TimerSeoBOR;
import mwg.wb.model.general.DistrictBO;
import mwg.wb.model.general.ProvinceBO;
import mwg.wb.model.general.ProvinceInfoBO;
import mwg.wb.model.general.WardBO;
import mwg.wb.model.html.InfoBO;
import mwg.wb.model.other.BooleanWrapper;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.products.GroupCategoryBO;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductManuBO;
import mwg.wb.model.products.ProductPriceRangeBO;
import mwg.wb.model.products.ProductPropBO;
import mwg.wb.model.products.ProductWarrantyCenterBO;
import mwg.wb.model.products.ProductWarrantyManuBO;
import mwg.wb.model.products.URLGoogleBO;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.FacePropSR;
import mwg.wb.model.searchresult.ProductBOSR;
import mwg.wb.model.searchresult.ProductSOSR;
import mwg.wb.model.searchresult.StoreSR;
import mwg.wb.model.system.KeyWordBO;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.HppcMaps;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apicategory")
public class CategoryController {
	private static ORThreadLocal oclient = null;
	private static CategoryHelper _cateHelper = null;
	private static ProductHelper _productHelper = null;
	private static ClientConfig config = null;

	// lockers
	private static ReentrantLock phelperLocker = new ReentrantLock(), configLocker = new ReentrantLock(),
			orientLocker = new ReentrantLock(), cateLocker = new ReentrantLock();

	private static ORThreadLocal getOrientClient() throws JsonParseException, IOException {
		try {
			orientLocker.lock();
			if (oclient == null) {
				oclient = new ORThreadLocal();
				oclient.initReadAPI(getConfig(), 0);
			}
			return oclient;
		} finally {
			orientLocker.unlock();
		}
	}

	private static ProductHelper getProductHelper() throws JsonParseException, IOException {
		try {
			phelperLocker.lock();
			if (_productHelper == null) {
				_productHelper = new ProductHelper(getOrientClient(), getConfig());
			}
			return _productHelper;
		} finally {
			phelperLocker.unlock();
		}
	}

	private static ClientConfig getConfig() {
		try {
			configLocker.lock();
			if (config == null) {
				config = ConfigUtils.GetOnlineClientConfig();
			}
			return config;
		} finally {
			configLocker.unlock();
		}
	}

	public CategoryController() {
	}

	private static CategoryHelper getCategoryHelper() throws JsonParseException, IOException {
		try {
			cateLocker.lock();
			if (_cateHelper == null) {
				_cateHelper = new CategoryHelper(getOrientClient(), getConfig());
			}
			return _cateHelper;
		} finally {
			cateLocker.unlock();
		}
	}

//	private static synchronized ORThreadLocal getOrientClient() {
//		if (oclient == null) {
//			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
//			try {
//				oclient = new ORThreadLocal();
//			} catch (JsonParseException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			oclient.initReadAPI(config, 0);
//		}
//		return oclient;
//	}

//	private static synchronized ProductHelper getProductHelper() {
//		if (_productHelper == null) {
//			_productHelper = new ProductHelper(getOrientClient(), config);
//		}
//
////		if (_productHelper == null)
////			_productHelper = new ProductHelper(oclient, config);
//		return _productHelper;
//	}

	@RequestMapping(value = "/getactivemanubycategoryid", method = RequestMethod.GET)
	public ResponseEntity<ProductManuBO[]> GetActiveManuByCategoryID(int categoryID, int siteID, String langID,
																	 String keyword) {
		var status = HttpStatus.OK;
		var timer = new CodeTimers();
		timer.start("all");
		ProductManuBO[] manu = null;
		try {
			var phelper = getProductHelper();
			timer.start("elastic");
			var qry = new ProductQuery() {
				{
					CategoryId = categoryID;
					LanguageID = langID;
					PageIndex = 0;
					PageSize = 1;
					ProvinceId = DidxHelper.getDefaultProvinceIDBySiteID(siteID);
					SiteId = siteID;
					Keyword = keyword;
					WebStatusIDList = new int[] { 2, 4, 8, 11};
				}
			};
			ProductSOSR search = null;
			search = phelper.Ela_SearchProduct(qry, true, false, false, null);
			timer.pause("elastic");
			timer.start("odb");
			var idlist = Arrays.asList(search.faceListManu).stream().map(FaceManuSR::getID)
					.collect(Collectors.toList());
			manu = phelper.getOrientClient().QueryFunction("manu_GetByIDList", ProductManuBO[].class, false, idlist,
					siteID, langID);
			timer.pause("odb");
			// manu nganh hang cha
			timer.start("grouping");
			List<ProductManuBO> manulist = new ArrayList<>(Arrays.asList(manu));
			var grouped = manulist.stream().filter(x -> x.ManufacturerName != null)
					.collect(Collectors.groupingBy(x -> x.ManufacturerName));
			for (var list : grouped.values()) {
				ProductManuBO max = null;
				for (var m : list) {
					manulist.remove(m);
					if (max == null
							|| (m.BigLogo != null && (max.BigLogo == null || max.ManufactureID < m.ManufactureID))
							|| (max.BigLogo == null && m.BigLogo == null && max.ManufactureID < m.ManufactureID)) {
						max = m;
					}
				}
				manulist.add(max);
			}
			manu = manulist.stream().sorted(Comparator.comparingDouble(ProductManuBO::getDisplayOrder))
					.toArray(ProductManuBO[]::new);
			timer.pause("grouping");
			timer.pause("all");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<ProductManuBO[]>(manu, HeaderBuilder.buildHeaders(timer),
				status);
	}

	@RequestMapping(value = "/getproductpropbycategory", method = RequestMethod.GET)
	public ResponseEntity<ProductPropBO[]> GetProductPropByCategory(int categoryID, int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductBOSR resultEs = new ProductBOSR();

		ProductQuery qry = new ProductQuery();
		qry.CategoryId = categoryID;
		qry.SiteId = siteID;
		qry.LanguageID = langID;
		qry.PageSize = 1;
		qry.WebStatus = -2;
		qry.SearchFlags = Set.of(SearchFlag.ISNOTKIDWATCH);
		qry.ProvinceId = siteID == 6 ? 163 : 3;
		ProductPropBO[] result = null;
		ProductSOSR solist = null;
		try {
			solist = getProductHelper().Ela_SearchProduct(qry, false, false, true, null);

			List<FacePropSR> esprop = Arrays.asList(solist.faceListProp);
			List<String> check = esprop.stream().map(v -> v.propID + "_" + v.propValueID).collect(Collectors.toList());

			int[] aids = esprop.stream().mapToInt(o -> o.propID).distinct().toArray();

			resultEs.faceListProp = solist.faceListProp;

			if (resultEs.faceListProp != null) {
				result = getProductHelper().GetProductPropByCategoryByListID(aids, siteID, langID);
			}
			List<ProductPropBO> oprop = Arrays.asList(result);
			for (var v1 : result) {
				var i = v1.ProductPropValueBOLst.iterator();
				while(i.hasNext()) {
					var v2 = i.next();
					if (!check.contains(v1.PropertyID + "_" + v2.ValueID)) {
						i.remove();
					}
				}
			}
			timer.end();
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<ProductPropBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getlistyesnopropertybycategory", method = RequestMethod.GET)
	public ResponseEntity<ProductPropBO[]> GetListYesNoPropertyByCategory(int categoryID, int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductPropBO[] result = null;
		try {
			result = getCategoryHelper().GetListYesNoPropertyByCategory(categoryID, siteID, langID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<ProductPropBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductpropbycategory2", method = RequestMethod.GET)
	public ResponseEntity<ProductPropBO[]> GetProductPropByCategory2(int categoryID, int siteID, String langID,
											 Boolean getAddition) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductPropBO[] result = null;
		boolean addition = getAddition == null ? false : getAddition;
		try {
			result = getCategoryHelper().GetProductPropByCategory(categoryID, siteID, langID, addition);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<ProductPropBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getlistproductpricerangebycategory", method = RequestMethod.GET)
	public ResponseEntity<ProductPriceRangeBO[]> GetListProductPriceRangeByCategory(int categoryID, int siteID,
			String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductPriceRangeBO[] result = null;
		try {
			result = getCategoryHelper().GetListProductPriceRangeByCategory(categoryID, siteID, langID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<ProductPriceRangeBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getallcategories", method = RequestMethod.GET)
	public ResponseEntity<ProductCategoryBO[]> GetAllCategories(int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductCategoryBO[] result = null;
		try {
			result = getCategoryHelper().GetAllCategories(siteID, langID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		Arrays.sort(result, Comparator.comparingInt(ProductCategoryBO::getDisplayOrder));
		timer.end();
		return new ResponseEntity<ProductCategoryBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getcategorybyid", method = RequestMethod.GET)
	public ResponseEntity<ProductCategoryBO> getCategoryByID(int categoryID, int siteID, String languageID) {
		ProductCategoryBO result = null;
		var status = HttpStatus.OK;
		try {
			result = getCategoryHelper().getCategoryByIDFromCached(categoryID, siteID, languageID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getstorebyid", method = RequestMethod.GET)
	public ResponseEntity<StoreBO> getStoreByID(int storeID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		StoreBO result = null;
		try {
			//result = getCategoryHelper().getStoreByID(storeID);
				result = getCategoryHelper().getStoreByIDFromCached(storeID);
			
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<StoreBO>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getkeywordbycategory", method = RequestMethod.GET)
	public ResponseEntity<KeyWordBO[]> getKeywordByCategory(int categoryID, int siteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		KeyWordBO[] result = null;
		try {
			result = getProductHelper().GetListKeyWordByCate(categoryID, siteID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<KeyWordBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getallprovince", method = RequestMethod.GET)
	public ResponseEntity<ProvinceBO[]> getAllProvince() {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProvinceBO[] result = null;
		try {
			result = getCategoryHelper().getAllProvince();
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<ProvinceBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getallprovincebycountry", method = RequestMethod.GET)
	public ResponseEntity<ProvinceBO[]> getAllProvincebyCountry(int countryID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProvinceBO[] result = null;
		try {
			result = getCategoryHelper().getAllProvinceByCountry(countryID);
//			if (result != null || result.length > 0) {
//				for (ProvinceBO provinceBO : result) {
//					
//					var districts = Arrays.asList(getCategoryHelper().getDistrictByProvince(provinceBO.ProvinceID));
////					var stores = getCategoryHelper().getStoreByProvince(provinceBO.ProvinceID, 0, 1);
////					var storesMap = Arrays.stream(stores)
////							.collect(Collectors.groupingBy(x -> x.DistrictID, Collectors.toList()));
////					for (var d : districts) {
////						d.StoreBOList = storesMap.get(d.DistrictID);
////						if (d.StoreBOList == null) {
////							d.StoreBOList = new ArrayList<>();
////						}
////					}
//					provinceBO.DistrictBOList = districts;
//				}
//				
//			}
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<ProvinceBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getpropvbycategory", method = RequestMethod.GET)
	public ResponseEntity<ProductPropBO[]> getPropVByCategoryID(int categoryID, int siteID, String languageID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductPropBO[] result = null;
		try {
			result = getCategoryHelper().getPropVByCategoryID(categoryID, siteID, languageID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<ProductPropBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/gettotalstorebysite", method = RequestMethod.GET)
	public ResponseEntity<Integer> getTotalStoreBySite(int siteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		int result = 0;
		try {
			result = getCategoryHelper().getTotalStoreBySite(siteID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<Integer>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getstorebyprovince", method = RequestMethod.GET)
	public ResponseEntity<StoreBO[]> getStoreByProvince(int provinceID, int districtID, int brandID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		StoreBO[] result = null;
		try {
			result = getCategoryHelper().getStoreByProvince(provinceID, districtID, brandID);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<StoreBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getdistrictbyprovince", method = RequestMethod.GET)
	public ResponseEntity<DistrictBO[]> getDistrictByProvince(int provinceID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		DistrictBO[] result = null;
		try {
			result = getCategoryHelper().getDistrictByProvince(provinceID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<DistrictBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getwardbydistrict", method = RequestMethod.GET)
	public ResponseEntity<WardBO[]> getWardByDistrict(int districtID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		WardBO[] result = null;
		try {
			result = getCategoryHelper().getWardByDistrict(districtID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<WardBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

//	@RequestMapping(value = "/get", method = RequestMethod.GET)
//	public ResponseEntity<> get() {
//		var timer = new CodeTimer("timer-all");
//		
//		timer.end();
//		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
//	}

	// SearchKeywordRedirect
	@RequestMapping(value = "/searchkeywordredirect", method = RequestMethod.GET)
	public ResponseEntity<KeyWordRedirectSO[]> SearchKeywordRedirect(String Keyword, Integer SiteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		if(SiteID == null) {
			SiteID = 1;
		}
		if(Keyword == null || Utils.StringIsEmpty(Keyword)) {
			return new ResponseEntity<KeyWordRedirectSO[]>(null, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
		}
		KeyWordRedirectSO[] result = null;
		try {
			result = getCategoryHelper().SearchKeywordRedirect(Keyword, SiteID);
			if (result != null && result.length > 0) {
				result[0].UrlDMX = result[0].UrlDMX.replace(",", "").replace("---", "-").replace("--", "-");
			}
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<KeyWordRedirectSO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getprovinceinfo", method = RequestMethod.GET)
	public ResponseEntity<ProvinceInfoBO> getProvinceInfo(int provinceID, int districtID, int wardID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProvinceInfoBO prov = null;
		try {
			prov = getCategoryHelper().getProvinceInfo(provinceID, districtID, wardID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.getStackTrace();
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<>(prov, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getdataurlredirect", method = RequestMethod.GET)
	public ResponseEntity<URLGoogleBO[]> GetDataUrlRedirect() {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		URLGoogleBO[] googleBO = null;
		try {
			googleBO = getCategoryHelper().GetDataUrlRedirect();
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.getStackTrace();
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<>(googleBO, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getdataurlredirectbysiteandproject", method = RequestMethod.GET)
	public ResponseEntity<URLGoogleBO[]> GetDataUrlRedirectBySiteAndProject(String ProjectName, int SiteID,
			int Pageindex, int PageSize) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		URLGoogleBO[] googleBO = null;
		try {
			googleBO = getCategoryHelper().GetDataUrlRedirectBySiteAndProject(ProjectName, SiteID, Pageindex, PageSize);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.getStackTrace();
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<>(googleBO, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/searchtimerseo", method = RequestMethod.GET)
	public ResponseEntity<TimerSeoBOR> SearchTimerSeo(int ProductID, int CategoryID, int SiteID, int pageIndex,
			int PageSize) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		TimerSeoBOR timerseo = null;
		try {
			timerseo = getCategoryHelper().SearchTimerSeo(ProductID, CategoryID, SiteID, pageIndex, PageSize);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.getStackTrace();
			Logs.LogException(e);
		}
		timer.end();
		return new ResponseEntity<>(timerseo, HeaderBuilder.buildHeaders(timer), status);
	}

	private static List<Integer> tgdddmx = List.of(1, 2);
	@RequestMapping(value = "/searchstorelatlonbysite2", method = RequestMethod.GET)
	public ResponseEntity<StoreSR> searchstoreLatLonBySite2(int Distance,int DistrictId,int IsSalesStore,
	boolean IsSearchLike, int IsShowweb, String Keyword, double Lat, double Lon, int MaxRecords, int PageIndex, int PageSize,
	int ProvinceId,int SearchType,String productCode, String siteIDList, int[] storeIDs, String termUrl) {
		var status = HttpStatus.OK;
		StoreSR storesr = null;
		boolean stockCheck = false;
		var timer = new CodeTimers();
		timer.start("all");
		StoreQuery qry = new StoreQuery();
		qry.Distance = Distance;
		qry.DistrictId = DistrictId;
		//qry.ExtensionObject = ExtensionObject;
		qry.IsSalesStore = IsSalesStore;
		qry.IsSearchLike = IsSearchLike;
		qry.Keyword = Keyword;
		qry.Lat = Lat;
		qry.Lon = Lon;
		qry.MaxRecords = MaxRecords;
		qry.PageIndex = PageIndex;
		qry.PageSize = PageSize;
		qry.ProvinceId = ProvinceId;
		qry.SearchType = SearchType;
		qry.productCode = productCode;
		qry.siteIDList = siteIDList;
		qry.storeIDs = storeIDs;
		qry.termUrl = termUrl;
		try {
			if (!Strings.isNullOrEmpty(qry.productCode) && qry.siteIDList != null) {
				timer.start("stockcheck");
				List<Integer> siteIDs = new ArrayList<>();
				for (var x : qry.siteIDList.split(",")) {
					try {
						var y = Integer.parseInt(x);
						if (y > 0) {
							siteIDs.add(y);
						}
					} catch (NumberFormatException ignored) {
					}
				}
				int brandid = siteIDs.size() == 2 && siteIDs.containsAll(tgdddmx) ? 4 : siteIDs.size() == 1 ?
						DidxHelper.getBrandBySite(siteIDs.get(0), DidxHelper.getLangBySiteID(siteIDs.get(0))) : -1;
				if (brandid > 0) {
					qry.storeIDs = getProductHelper().SearchStoreByProductCode(qry.productCode, brandid, qry.ProvinceId,
							qry.DistrictId, new BooleanWrapper());
					stockCheck = true;
				}
				timer.pause("stockcheck");
			}
			timer.start("search");
			if (stockCheck && qry.storeIDs.length == 0) {
				storesr = new StoreSR();
				storesr.stores = new StoreBO[0];
			} else {
				storesr = getCategoryHelper().searchStoreLatLonBySiteCached(qry);
			}
			timer.pause("search");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.getStackTrace();
			Logs.LogException(e);
		}
		//test
		timer.pause("all");
		return new ResponseEntity<>(storesr, HeaderBuilder.buildHeaders(timer), status);
	}
	@RequestMapping(value = "/searchstorelatlonbysite", method = RequestMethod.POST)
	public ResponseEntity<StoreSR> searchstoreLatLonBySite(@RequestBody StoreQuery qry) {
		var status = HttpStatus.OK;
		StoreSR storesr = null;
		boolean stockCheck = false;
		var timer = new CodeTimers();
		timer.start("all");

		try {
			if (!Strings.isNullOrEmpty(qry.productCode) && qry.siteIDList != null) {
				timer.start("stockcheck");
				List<Integer> siteIDs = new ArrayList<>();
				for (var x : qry.siteIDList.split(",")) {
					try {
						var y = Integer.parseInt(x);
						if (y > 0) {
							siteIDs.add(y);
						}
					} catch (NumberFormatException ignored) {
					}
				}

				int brandid = siteIDs.size() == 2 && siteIDs.containsAll(tgdddmx) ? 4 : siteIDs.size() == 1 ?
						DidxHelper.getBrandBySite(siteIDs.get(0), DidxHelper.getLangBySiteID(siteIDs.get(0))) : -1;

				if (brandid > 0) {
					qry.storeIDs = getProductHelper().SearchStoreByProductCode(qry.productCode, brandid, qry.ProvinceId,
							qry.DistrictId, new BooleanWrapper());
					stockCheck = true;
				}
				timer.pause("stockcheck");
			}
			timer.start("search");
			if (stockCheck && qry.storeIDs.length == 0) {
				storesr = new StoreSR();
				storesr.stores = new StoreBO[0];
			} else {
				storesr = getCategoryHelper().searchStoreLatLonBySiteCached(qry);
			}
			timer.pause("search");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.getStackTrace();
			Logs.LogException(e);
		}
		timer.pause("all");
		return new ResponseEntity<>(storesr, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getstoresbyprovinceid", method = RequestMethod.GET)
	public ResponseEntity<ProvinceBO> getStoresByProvinceID(int provinceID, int brandID) {
		ProvinceBO result = null;
		var status = HttpStatus.OK;
		try {
			var cateH = getCategoryHelper();
			var provs = cateH.getProvinceByListID(new int[] {provinceID});
			if (provs != null && provs.length > 0) {
				result = provs[0];
				var districts = Arrays.asList(cateH.getDistrictByProvince(provinceID));
				var stores = cateH.getStoreByProvince(provinceID, 0, brandID);
				var storesMap = Arrays.stream(stores)
						.collect(Collectors.groupingBy(x -> x.DistrictID, Collectors.toList()));

				for (var d : districts) {
					d.StoreBOList = storesMap.get(d.DistrictID);
					if (d.StoreBOList == null) {
						d.StoreBOList = new ArrayList<>();
					}
				}
				result.DistrictBOList = districts;
			}
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getproductmanubylistcategoryid", method = RequestMethod.GET)
	public ResponseEntity<ProductManuBO[]> GetProductManuByListCategoryID(String listCategoryID, int siteID, String LangID) {
		
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		var odbtimer = new CodeTimer("timer-odb");
		ProductManuBO[] manu = null;
		try {
			var chelper = getCategoryHelper();
			codetimer.reset();
			manu = chelper.GetProductManuByListCategoryID(listCategoryID, siteID, LangID);
			codetimer.end();
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		return new ResponseEntity<ProductManuBO[]>(manu, HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer),
				status);
	}

	@RequestMapping(value = "/getactiveaccessorymanu", method = RequestMethod.GET)
	public ResponseEntity<ProductManuBO[]> getActiveAcessoryManu(int provinceID, int siteID, String LangID) {

		var status = HttpStatus.OK;
		var timer = new CodeTimers();
		ProductManuBO[] manu = null;
		try {
			var chelper = getCategoryHelper();
			timer.start("all");
			manu = chelper.GetProductManuByListCategoryID("482", siteID, LangID);
//			var names = Arrays.stream(manu).map(x -> x.ManufacturerName).filter(x -> x != null)
//					.toArray(String[]::new);
			var qry = new ProductQuery() {{
				ProvinceId = provinceID;
				SiteId = siteID;
				LanguageID = LangID;
				SearchFlags = Set.of(SearchFlag.ACCESSORY_GENUINE);
//				ListManufacturename = names;
				WebStatusIDList = new int[] {2, 4, 8, 9, 11};
				PageSize = 1;
			}};
			var facet = getProductHelper().getHelperBySite(siteID).SearchProduct(qry, true,
					false, false, timer);
			if (facet.faceListManu != null && facet.faceListManu.length > 0) {
				getProductHelper().getCateManuName(facet, siteID, LangID);
				var validManus = Arrays.stream(facet.faceListManu).map(x -> x.manufacturerUrl)
						.filter(x -> x != null).distinct().collect(Collectors.toList());
				manu = Arrays.stream(manu).filter(x -> validManus.contains(x.URL))
						.toArray(ProductManuBO[]::new);
			} else {
				manu = null;
			}
			timer.pause("all");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		return new ResponseEntity<ProductManuBO[]>(manu, HeaderBuilder.buildHeaders(timer),
				status);
	}

	@RequestMapping(value = "/productwarrantymanuselect", method = RequestMethod.GET)
	public ResponseEntity<ProductWarrantyManuBO[]> productWarrantyManuSelect(int warrantymanuID, int categoryID,
			int siteID) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		ProductWarrantyManuBO[] result = null;
		try {
			result = getCategoryHelper().productWarrantyManuSelect(warrantymanuID, categoryID, siteID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		codetimer.end();
		return new ResponseEntity<ProductWarrantyManuBO[]>(result, HeaderBuilder.buildHeaders(codetimer), status);
	}

	@RequestMapping(value = "/getproductwarrantycenterbymanufacturerandprovince", method = RequestMethod.GET)
	public ResponseEntity<ProductWarrantyCenterBO[]> getProductWarrantyCenterByManufacturerAndProvince(int manuID,
			int provinceID, int categoryID) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		ProductWarrantyCenterBO[] result = null;
		try {
			result = getCategoryHelper().getProductWarrantyCenterByManufacturerAndProvince(manuID, provinceID,
					categoryID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			Logs.LogException(e);
		}
		codetimer.end();
		return new ResponseEntity<ProductWarrantyCenterBO[]>(result, HeaderBuilder.buildHeaders(codetimer), status);
	}

	
	@RequestMapping(value = "/getallgroupcate", method = RequestMethod.GET)
	public ResponseEntity<GroupCategoryBO[]> getAllGroupCate(int siteID, String langID,Integer clearcache) {
		int clear = clearcache == null ? 0 : clearcache;
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		GroupCategoryBO[] cached = null;
		try {
			var chelper = getCategoryHelper();
			cached = chelper.getAllGroupCateFromCate(siteID,langID,clear);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(cached, HeaderBuilder.buildHeaders(timer), status);
	}
	@RequestMapping(value = "/getgroupcatebyparentid", method = RequestMethod.GET)
	public ResponseEntity<GroupCategoryBO[]> getGroupCateByParentID(int parentID, int siteID, String langID) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		GroupCategoryBO[] cached = null;
		try {
			var chelper = getCategoryHelper();
			
			cached = chelper.getGroupCateByParentID(parentID,siteID,langID);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(cached, HeaderBuilder.buildHeaders(timer), status);
	}
	@RequestMapping(value = "/getgroupcatebygroupid", method = RequestMethod.GET)
	public ResponseEntity<GroupCategoryBO[]> getGroupCateByGroupID(int groupID, int siteID, String langID) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		GroupCategoryBO[] cached = null;
		try {
			var chelper = getCategoryHelper();
			
			cached = chelper.getGroupCateByGroupID(groupID,siteID,langID);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(cached, HeaderBuilder.buildHeaders(timer), status);
	}

	@GetMapping(value = "gettotalstorebysiteid")
	public ResponseEntity<Integer> getTotalStoreBySiteID(int siteID){

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		int result = 0;

		try{
			var chelper = getCategoryHelper();
			result = chelper.getTotalStoreBySiteID(siteID);
		}catch (Throwable e){
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer),status);
	}
	@GetMapping(value = "getcategoryidinactivebyurl")
	public ResponseEntity<Integer[]> getCategoryIDInactiveByUrl(String url, int siteID, String lang){
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		Integer[] result = null;

		try{
			var chelper = getCategoryHelper();
			result = chelper.getCateIDInactivebyUrl(url,siteID,lang);
		}catch (Throwable e){
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer),status);
	}
	@RequestMapping(value = "/getstorebyhtmlid", method = RequestMethod.GET)
	public ResponseEntity<StoreBO[]> getStoreByHtmlID(int htmlID , Double Distance, Double Lat, Double Lon, Integer PageIndex, Integer PageSize) {


		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		StoreSR result = null;
		try {
			InfoBO infoBO = getCategoryHelper().getStoreByHtmlID(htmlID);
			if (infoBO == null)
				return  new ResponseEntity<>(null,HttpStatus.OK);
			String  strStoreID= infoBO.InfoLangBO != null ? infoBO.InfoLangBO.Content : "";
		//	qry.siteIDList = strStoreID;
			if(!Utils.StringIsEmpty(strStoreID)){
				var lstStoreID = Stream.of(strStoreID.split("\\,")).mapToInt(x ->{
					return Utils.toInt(x);
				}).toArray();
				//result = getCategoryHelper().getStoreByHtmlIDCached(lstStoreID);
				if (lstStoreID == null && lstStoreID.length == 0) { //
					result = new StoreSR();
					result.stores = new StoreBO[0];
				 return new ResponseEntity<StoreBO[]>(result.stores, HeaderBuilder.buildHeaders(timer), status);
				} else {
					if( Distance == null  || Lat == null || Lon == null){ // chỉ lấy theo store chưa sắp xếp
					    var storeBO = getCategoryHelper().getStoreByHtmlIDCached(lstStoreID);
						return new ResponseEntity<StoreBO[]>(storeBO, HeaderBuilder.buildHeaders(timer), status);
					}
					var qry = new StoreQuery();
					qry.Distance = Distance;
//					qry.DistrictId = DistrictId;
					qry.Lat = Lat;
					qry.Lon = Lon;
					qry.PageIndex = PageIndex;
					qry.PageSize = PageSize;
//					qry.ProvinceId = ProvinceId;
					qry.SearchType = 1;
					qry.storeIDs = lstStoreID;
					result = getCategoryHelper().searchStoreLatLonBySiteCached(qry);
				}
			}

		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<StoreBO[]>(result.stores, HeaderBuilder.buildHeaders(timer), status);
	}
	@RequestMapping(value = "/getstorebyhtmlid2", method = RequestMethod.GET)
	public ResponseEntity<StoreBO[]> getStoreByHtmlID(int htmlID) {
		var qry = new StoreQuery();

		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		StoreBO[] result = null;
		try {
			InfoBO infoBO = getCategoryHelper().getStoreByHtmlID(htmlID);
			if (infoBO == null)
				return  new ResponseEntity<>(null,HttpStatus.OK);
			String  strStoreID= infoBO.InfoLangBO != null ? infoBO.InfoLangBO.Content : "";
			if(!Utils.StringIsEmpty(strStoreID)){
				var lstStoreID = Stream.of(strStoreID.split("\\,")).mapToInt(x ->{
					return Utils.toInt(x);
				}).toArray();
				result = getCategoryHelper().getStoreByHtmlIDCached(lstStoreID);
			}

		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<StoreBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}



}
