package mwg.wb.webapi.controller;

import mwg.wb.business.CommonHelper;
import mwg.wb.business.CookHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.client.elasticsearch.dataquery.DishQuery;
import mwg.wb.client.elasticsearch.dataquery.NewSearch;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.common.DidxHelper;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.comment.RatingStaticBO;
import mwg.wb.model.cook.CookCategory;
import mwg.wb.model.cook.CookDish;
import mwg.wb.model.cook.CookGallery;
import mwg.wb.model.cook.CookIngredient;
import mwg.wb.model.cook.CookRecipe;
import mwg.wb.model.cook.CookStep;
import mwg.wb.model.searchresult.DishBOSR;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apicook")
public class CookController {
	@Autowired
	private HttpServletRequest request;
	private static CommonHelper _commonHelper = null;
	private static CookHelper _cookHelper = null;

	private static ORThreadLocal factoryRead = null;
	private static ClientConfig _config = null;

	private static synchronized CookHelper GetCookClientBySiteID() {

		if (_config == null) {
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			_config = config;

		}
		if (factoryRead == null) {
			try {
				factoryRead = new ORThreadLocal();
			} catch (Throwable e) {

				e.printStackTrace();
			}
			factoryRead.initReadAPI(_config, 0);
		}
		if (_cookHelper == null) {

			_cookHelper = new CookHelper(factoryRead, _config);
		}
		return _cookHelper;

	}

	public CookController() {

	}

	@RequestMapping(value = "/gettotalrating", method = RequestMethod.GET)
	public ResponseEntity<RatingStaticBO> GetTotalRating(int objectID, int objectType, int siteID) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		codetimer.reset();
		RatingStaticBO rating = new RatingStaticBO();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			rating = cookHelper.GetRatingStatic(objectID, objectType, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(rating, header, status);

	}

	@RequestMapping(value = "/getallcategory", method = RequestMethod.GET)
	public ResponseEntity<List<CookCategory>> GetAllCategory(int siteID) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		codetimer.reset();
		List<CookCategory> listCategory = new ArrayList<>();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			listCategory = cookHelper.GetAllCategory();

			odbtimer.end();

		} catch (Throwable e) {

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(listCategory, header, status);

	}

	@RequestMapping(value = "/getcateinfobyurl", method = RequestMethod.GET)
	public ResponseEntity<CookCategory> GetCateInfoByUrl(String url) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		codetimer.reset();
		var category = new CookCategory();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			category = cookHelper.GetCateInfoByUrl(url);

			odbtimer.end();

		} catch (Throwable e) {

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(category, header, status);

	}

	@RequestMapping(value = "/getdishdetail", method = RequestMethod.GET)
	public ResponseEntity<CookDish> GetDetailDish(int dishID, int siteID) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		codetimer.reset();
		CookDish dishBO = new CookDish();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			dishBO = cookHelper.GetDishByID(dishID);
			if (dishBO == null)
				return null;
			if (dishBO != null && dishBO.IsDeleted == 1)
				return null;

			var rating = cookHelper.GetRatingStatic(dishBO.DishID, 85, 2);
			dishBO.RatingStatic = rating;
			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(dishBO, header, status);

	}

	@RequestMapping(value = "/getrecipesinfobydishid", method = RequestMethod.GET)
	public ResponseEntity<List<CookRecipe>> GetListRecipeByDishId(int dishID, int siteID) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		codetimer.reset();
		List<CookRecipe> listRecipe = new ArrayList<CookRecipe>();
		List<CookStep> listStep = new ArrayList<CookStep>();
		List<CookIngredient> listIngredient = new ArrayList<CookIngredient>();

		List<Integer> listStepID = new ArrayList<Integer>();
		List<Integer> listRecipeID = new ArrayList<Integer>();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			listRecipe = cookHelper.GetListRecipeByDishId(dishID);

			if (listRecipe != null && listRecipe.size() > 0)
			// get list steps
			{
				listRecipeID = listRecipe.stream().map(x -> x.Recipeid).collect(Collectors.toList());
				listStep = cookHelper.GetListStepByDishId(dishID);
				// get list ingredients
				listIngredient = cookHelper.GetListIngredientByDishId(dishID);
				odbtimer.end();

				listStepID = listStep.stream().map(x -> x.StepId).collect(Collectors.toList());

				List<CookGallery> listGallery = cookHelper.GetGalleryBySteps(listRecipeID, listStepID);

				for (var item : listStep) {
					item.ListGallery = listGallery.stream().filter(x -> x.stepId == item.StepId)
							.collect(Collectors.toList());

				}

				// map gallery to step

				for (var item : listRecipe) {
					var relateStep = listStep.stream().filter(x -> x.RecipeId == item.Recipeid)
							.collect(Collectors.toList());
					var relateIngredient = listIngredient.stream().filter(x -> x.RecipeId == item.Recipeid)
							.collect(Collectors.toList());

					item.ListStep = relateStep;
					item.ListIngredient = relateIngredient;
				}
			} else {
				odbtimer.end();
			}

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(listRecipe, header, status);

	}

	@RequestMapping(value = "/searchdishes", method = RequestMethod.POST)
	public ResponseEntity<DishBOSR> SearchDishes(@RequestBody DishQuery dishQuery) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer = new CodeTimer("timer-odb");
		var querytimer = new CodeTimer("timer-es-query");
		codetimer.reset();

		var cookHelper = GetCookClientBySiteID();

		DishBOSR data = new DishBOSR();
		try {

			estimer.reset();
			var solist = cookHelper.SearchDishes(dishQuery, querytimer);
			estimer.end();

			data.message = solist.message;
			data.total = solist.total;
			data.facetList = solist.faceList;

			if (solist.dishesSO != null && solist.dishesSO.size() > 0) {
				@SuppressWarnings("unchecked")
				List<Integer> lids = new ArrayList(solist.dishesSO.keySet());

				odbtimer.reset();
				data.result = cookHelper.GetListDishes(lids);
				if (data.result != null && data.result.size() > 0) {
					for (var dish : data.result) {
						var item = solist.dishesSO.values().stream().filter(x -> x.DishID == dish.DishID).findFirst()
								.orElse(null);
						if (item != null)
							dish.ListRecipe = item.ListRecipe;
						dish.RatingStatic = item.RatingStatic;
					}

				}
				odbtimer.end();

			}
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			data.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer, querytimer);

		return new ResponseEntity<DishBOSR>(data, header, status);

	}

	@RequestMapping(value = "/getrelateddish", method = RequestMethod.GET)
	public ResponseEntity<List<CookDish>> GetRelatedDish(String cateIDList, String tag, int size) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");

		var odbtimer = new CodeTimer("timer-odb");
		var esquerytimer = new CodeTimer("timer-es-query");
		codetimer.reset();

		var cookHelper = GetCookClientBySiteID();

		List<CookDish> result = new ArrayList<CookDish>();
		try {

			var solist = cookHelper.GetRelatedDish(cateIDList, tag, size, esquerytimer);

			if (solist.dishesSO != null && solist.dishesSO.size() > 0) {
				@SuppressWarnings("unchecked")
				List<Integer> lids = new ArrayList(solist.dishesSO.keySet());

				odbtimer.reset();
				result = cookHelper.GetListDishes(lids);
				if (result != null && result.size() > 0) {
					for (var dish : result) {
						var item = solist.dishesSO.values().stream().filter(x -> x.DishID == dish.DishID).findFirst()
								.orElse(null);
						if (item != null)
							dish.ListRecipe = item.ListRecipe;

					}

				}
				odbtimer.end();

			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, odbtimer, esquerytimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/getrelateddishnew", method = RequestMethod.GET)
	public ResponseEntity<List<CookDish>> GetRelatedDishNew(String cateIDs, int dishID, int size) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");

		var odbtimer = new CodeTimer("timer-odb");
		var esquerytimer = new CodeTimer("timer-es-query");
		codetimer.reset();

		var cookHelper = GetCookClientBySiteID();

		if (size > 20)
			size = 20;
		List<CookDish> result = new ArrayList<CookDish>();
		try {

			var solist = cookHelper.GetRelatedDishNew(cateIDs, size, dishID, esquerytimer);

			if (solist.dishesSO != null && solist.dishesSO.size() > 0) {

				List<Integer> lids = new ArrayList<Integer>(solist.dishesSO.keySet());

				odbtimer.reset();
				result = cookHelper.GetListDishes(lids);
				if (result != null && result.size() > 0) {
					for (var dish : result) {
						var item = solist.dishesSO.values().stream().filter(x -> x.DishID == dish.DishID).findFirst()
								.orElse(null);
						if (item != null)
							dish.ListRecipe = item.ListRecipe;

					}

				}
				odbtimer.end();

			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, odbtimer, esquerytimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/getdishbynewsid", method = RequestMethod.GET)
	public ResponseEntity<CookDish> GetDishByNewsId(int newsID, int siteID) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		codetimer.reset();
		CookDish dishBO = new CookDish();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			dishBO = cookHelper.GetDishByNewsId(newsID);
			if (dishBO == null)
				return null;
			if (dishBO != null && dishBO.IsDeleted == 1)
				return null;

			var rating = cookHelper.GetRatingStatic(dishBO.DishID, 85, 2);
			dishBO.RatingStatic = rating;
			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(dishBO, header, status);

	}
	@RequestMapping(value = "/getrecipeinfobyrecipeid", method = RequestMethod.GET)
	public ResponseEntity<CookRecipe> GetRecipeInfoByRecipeId(int recipeID) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		codetimer.reset();
		CookRecipe recipe = new CookRecipe();

		var cookHelper = GetCookClientBySiteID();

		try {
			odbtimer.reset();
			recipe = cookHelper.GetRecipeInfoByRecipeId(recipeID);
			

		
			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(recipe, header, status);

	}
	@GetMapping(value = "/getlistbycookdish")
	public ResponseEntity<CookDish[]> getListByCookdish(int pageSize){
		var codetimer = new CodeTimers();
		CookDish[] cookDishes = null;
		var status = HttpStatus.OK;

		try {
			codetimer.start("timer-all");
			cookDishes = GetCookClientBySiteID().getListByCookdish(pageSize);
			codetimer.pause("timer-all");
			if(DidxHelper.isVu())
				System.out.println("Tong conng :" + cookDishes.length);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}


		var header = HeaderBuilder.buildHeaders(codetimer);
		return new ResponseEntity<CookDish[]>(cookDishes, header, status);
	}

}