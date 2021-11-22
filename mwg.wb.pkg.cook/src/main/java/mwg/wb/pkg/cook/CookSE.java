package mwg.wb.pkg.cook;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import mwg.wb.business.CookHelper;
import mwg.wb.business.NewsHelper;
import mwg.wb.business.helper.cook.TGDDCookHelper;
import mwg.wb.business.helper.news.TGDDNewsHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.cook.CookDish;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCategoryBO;
import mwg.wb.model.search.DishSO;
import mwg.wb.model.search.NewsSO;

public class CookSE implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_cook";

	private ClientConfig config = null;

	CookHelper cookHelper = null;
	private final Lock queueLock = new ReentrantLock();

	@Override
	public void InitObject(ObjectTransfer inobjectTransfer) {
		factoryRead = (ORThreadLocal) inobjectTransfer.factoryRead;
		config = (ClientConfig) inobjectTransfer.clientConfig;

		cookHelper = new CookHelper(factoryRead, config);

	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;

		long DishID = Utils.toLong(message.Identify);//
		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
			if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COOK_VIEWCOUNT) {
				if (config.IS_NOT_UPDATE_ES == 1) {
					r.Code = ResultCode.Success;
					return r;
				}
				long ViewCounter = Utils.toLong(message.Data);
				if (ViewCounter > 0) {

					var update = new UpdateRequest(currentIndexDB, String.valueOf(DishID))
							.doc("{\"ViewCount\":" + ViewCounter + "  }", XContentType.JSON).docAsUpsert(true)
							.detectNoop(false);
					var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
					UpdateResponse response = null;
					try {
						queueLock.lock();
						response = client.update(update, RequestOptions.DEFAULT);
					} catch (Throwable e) {

						r.Message = Logs.GetStacktrace(e);
						r.Code = ResultCode.Retry;
						return r;
					} finally {
						queueLock.unlock();
					}
					if (response != null && response.getResult() == Result.UPDATED) {
						r.Code = ResultCode.Success;
					} else {

						r.Message = "-Index dish viewcount to ES FAILED: " + DishID + ", " + response
								+ "######################";
						r.Code = ResultCode.Retry;
					}
				} else {

					return r;
				}

			} else {

				CookDish dishBO = null;
				try {
					dishBO = cookHelper.GetDishByID(DishID);
				} catch (Throwable e) {

					r.Code = ResultCode.Retry;
					r.Message = e.getMessage();
					return r;
				}

				if (dishBO == null) {
					r.Code = ResultCode.Success;
					r.Message = "Dish #" + DishID + " does not exist";
					return r;
				}

				try {
					IndexDishSE(dishBO, r);
				} catch (Throwable e) {
					r.Code = ResultCode.Retry;
					r.Message = e.getMessage();
					return r;
				}

			}
		}

		return r;
	}

	private void IndexDishSE(CookDish dishBO, ResultMessage r) throws Throwable {
		var dishSO = new DishSO();

		dishSO.DishID = dishBO.DishID;

		String keyword = dishBO.Title + " " + dishBO.DishName;
		if (!Utils.StringIsEmpty(dishBO.Keyword))
			keyword += " " + dishBO.Keyword;

		if (!Utils.StringIsEmpty(dishBO.RecipeNameList))
			keyword += " " + dishBO.RecipeNameList;

		dishSO.Keyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(keyword));

		dishSO.ActivedDate = dishBO.ActivedDate;

		dishSO.CreatedDate = dishBO.CreatedDate;
		dishSO.DeletedDate = dishBO.DeletedDate;

		dishSO.DishName = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(dishBO.DishName));

		dishSO.IsActived = dishBO.IsActived;
		dishSO.IsDeleted = dishBO.IsDeleted;
		dishSO.IsDraft = dishBO.IsDraft;
		dishSO.IsFeatured = dishBO.IsFeatured;
		dishSO.LastDidxUpdated = new Date();

		dishSO.ShortDescription = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(dishBO.ShortDescription));

		dishSO.Tag = DidxHelper.ConvertToTagsTerm(dishBO.Tag);

		dishSO.Tipsnote = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(dishBO.Tipsnote));
		dishSO.Title = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(dishBO.Title));

		dishSO.UpdatedDate = dishBO.UpdatedDate;
		dishSO.ViewCount = dishBO.ViewCount;

		if (!Utils.StringIsEmpty(dishBO.CategoryIdList)) {
			dishSO.CategoryIdList = dishBO.CategoryIdList.replace(",", " ");
			var lstCate = dishBO.CategoryIdList.split(",");
			if (lstCate.length == 1 && Integer.valueOf(lstCate[0]) == 3) {
				dishSO.IsMeoVatOnly = 1;
			} else {
				dishSO.IsMeoVatOnly = 0;
			}

		} else {
			dishSO.CategoryIdList = "";
			dishSO.IsMeoVatOnly = 0;
		}

		if (!Utils.StringIsEmpty(dishBO.RecipeIdList)) {
			dishSO.RecipeIdList = dishBO.RecipeIdList.replace(",", " ");
		} else {
			dishSO.RecipeIdList = "";
		}
		if (!Utils.StringIsEmpty(dishBO.CategoryNameList)) {
			dishSO.CategoryNameList = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(dishBO.CategoryNameList));
		} else {
			dishSO.CategoryNameList = "";
		}

		if (!Utils.StringIsEmpty(dishBO.RecipeNameList)) {
			dishSO.RecipeNameList = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(dishBO.RecipeNameList));
		} else {
			dishSO.RecipeNameList = "";
		}

		// list reciepe

		var lstRecipe = cookHelper.GetListRecipeByDishId(dishSO.DishID);

		dishSO.ListRecipe = lstRecipe;

		var rating = cookHelper.GetRatingStatic(dishBO.DishID, 85, 2);
		dishSO.RatingStatic = rating;

		if (!Utils.StringIsEmpty(dishBO.VideoUrl)) {
			dishSO.IsHasVideo = 1;
		} else {
			dishSO.IsHasVideo = 0;
		}

		var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(currentIndexDB,
				dishSO, dishSO.DishID + "");
		if (!rs) {
			r.Message = "-Index dish to ES FAILED: " + dishSO.DishID + " ######################";
			r.Code = ResultCode.Retry;
		}

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
