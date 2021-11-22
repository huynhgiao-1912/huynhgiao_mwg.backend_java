package mwg.wb.pkg.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpDataCache;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
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
import mwg.wb.model.search.SimSO;
import mwg.wb.model.sim.SimBO;
import mwg.wb.model.sim.SimPackageErpBO;

public class Sim implements Ididx {

	static final String CurrentIndexDB = "ms_sim";
	static final String CurrentTypeDB = "sim";
//	private OrientDBFactory factoryDB = null;
//	private static ProductHelper productHelper = null;

//	private ObjectMapper mapper = null;
//	private ORThreadLocal factoryWrite = null;
	private ErpHelper erpHelper = null;
	private volatile List<SimPackageErpBO> cached = null;
	private long lastUpdated = 0;
	private ErpDataCache erp;
	private int DataCenter = 0;
	private ClientConfig clientConfig = null;

	public Sim() {
		erp = new ErpDataCache();
	}

	private List<SimPackageErpBO> getPackages(boolean log, String note, int datacenter) throws Exception {
		if (cached == null || System.currentTimeMillis() - lastUpdated > 1800000) {
			var array = erp.GetSimPackages();
			if (array != null) {
				cached = Collections.synchronizedList(Arrays.asList(array));
				lastUpdated = System.currentTimeMillis();

				String queue = "gr.dc4.sql.sim";
				String queue2 = "gr.dc4.sql.sim";
				String queueBK = "gr.dc2.sql.sim";
				String queueDev = "gr.beta.sql.sim";
				QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).pushUpsertODBObjects("sim_package_erp",
						"packagestypeid", queue, queue2, queueBK, queueDev, cached, log, note, datacenter);

			} else if (cached == null)
				cached = new ArrayList<SimPackageErpBO>();
		}
		return cached;
	}

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
	}

	public ResultMessage doDelete(MessageQueue message, String simID, String note, boolean isLog) {
		ResultMessage resultMessage = new ResultMessage();
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();
		SqlInfo sqlinfo = new SqlInfo();
		RefSql ref = new RefSql();

		sqlinfo.Sql = "update sim set isdeleted=1 where simno='" + simID + "'";
		sqlinfo.Params = ref.params;
		messageRepush.SqlList.add(sqlinfo);

		// Logs.WriteLine("push " + simID);

		String queue = "gr.dc4.sql.sim";
		String queue2 = "gr.dc2.sql.sim";
		String queueBK = "gr.dc4.sql.sim";
		String queueDev = "gr.beta.sql.sim";
		messageRepush.Identify = message.Identify;
		messageRepush.Action = DataAction.Update;
		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
		messageRepush.CreatedDate = Utils.GetCurrentDate();
		messageRepush.Type = 0;
		messageRepush.Note = message.Note;
		try {

			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue2, queueBK, queueDev,
					messageRepush, isLog, note, DataCenter);

		} catch (Exception e) {
			Logs.LogException(e);
			resultMessage.StackTrace = Logs.GetStacktrace(e);
			resultMessage.Message = e.getMessage();
			resultMessage.Code = ResultCode.Retry;
			return resultMessage;
		}

		try {

			var update = new UpdateRequest(CurrentIndexDB, simID).doc("{\"IsDeleted\":1}", XContentType.JSON)
					.docAsUpsert(true).detectNoop(false);
			var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
			var response = client.update(update, RequestOptions.DEFAULT);
			if (response != null && response.getResult() == Result.UPDATED) {

			} else {

				Logs.WriteLine("-Index sim status to ES FAILED: " + simID + ", " + response.getResult().name()
						+ "######################");
				Utils.Sleep(100);
				resultMessage.Code = ResultCode.Retry;
				return resultMessage;
			}
		} catch (Exception e) {
			Logs.WriteLine("Exception Index price status to ES FAILED: " + simID);
			Logs.WriteLine(e);
			Utils.Sleep(100);
			resultMessage.StackTrace = Logs.GetStacktrace(e);
			resultMessage.Message = e.getMessage();
			resultMessage.Code = ResultCode.Retry;
			return resultMessage;
		}
		resultMessage.Code = ResultCode.Success;
		return resultMessage;
	}

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		ResultMessage resultMessage = new ResultMessage();
		String simID = message.Identify;
		String note = message.Note;
		boolean isLog = Utils.IsMessageSaveLog(message.Note);

		if (message.Action == DataAction.Delete) {
			return doDelete(message, simID, note, isLog);
		}
		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
			try {
				SimBO sim = erpHelper.GetInfoSIMByIMEI(simID);
				if (sim != null) {
//			if (sim != null)
//				factoryWrite.UpsertODatabaseJson(log, note, "sim", "simno", sim.SimNo, sim);
					SimSO simso = new SimSO() {
						{
							SimNo = sim.SimNo.trim();
							SimLength = sim.SimNo.length();
							Price = sim.Price;
							MainGoupID = sim.MainGoupID;
							ProductNO = sim.ProductNo.trim();
							NetworkId = sim.SimNetworkID;
							SubgroupId = sim.SubGroupID;
							GroupId = (sim.SimGroupIDList != null && !sim.SimGroupIDList.equals(""))
									? "G" + sim.SimGroupIDList.replace(",", "G G") + "G"
									: "";

							UpdatedDate = (sim.UpdatedDate != null) ? sim.UpdatedDate : Utils.GetDefaultDate();
							StoreID = (!Utils.StringIsEmpty(sim.StoreIDList))
									? "T" + sim.StoreIDList.replace(",", "T T") + "T"
									: "";
							Is3G = sim.SubGroupID == 2091 ? 1 : 2;
							Keyword = DidxHelper.FormatKeywordField(((sim.SimGroupName == null) ? "" : sim.SimGroupName)
									+ " " + (sim.SimNetworkName == null ? "" : sim.SimNetworkName) + " "
									+ sim.SimNo.trim() + " " + sim.SimNoDisplay);
							Keyword_us = DidxHelper.FilterVietkey(Keyword);
							IsDeleted = 0;
							BrandID = sim.BrandID;
							LanguageID = DidxHelper.GenTerm3(sim.LanguageID);
							PriceByPackage = new HashMap<String, Double>();
							didx_updateddate = Utils.GetCurrentDate();
							didx_source = "se";
							ProvinceId  = sim.ProvinceId;
							DistrictId  = sim.DistrictId;
						}
					};
					var packages = getPackages(isLog, note, DataCenter);
					synchronized (packages) {
						packages.stream().filter(p -> p.brandid == sim.BrandID)
								.forEach(p -> simso.PriceByPackage.put("backage_" + p.packagestypeid,
										p.packagessaleprice > sim.Price ? p.packagessaleprice : sim.Price));
					}
					ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
							.IndexObject2(CurrentIndexDB, CurrentTypeDB, simso, simso.SimNo);

					MessageQueue messageRepush = new MessageQueue();
					messageRepush.SqlList = new ArrayList<SqlInfo>();
					SqlInfo sqlinfo = new SqlInfo();
					RefSql ref = new RefSql();
					Utils.BuildSql(isLog, note, "sim", "simno", sim.SimNo.trim(), sim, ref);
					sqlinfo.Sql = ref.Sql;
					sqlinfo.Params = ref.params;
					messageRepush.SqlList.add(sqlinfo);

					Logs.getInstance().Log(isLog, note, "BuildSql:ref:", ref);

					String qu = "gr.dc4.sql.sim";
					String qu2 = "gr.dc2.sql.sim";
					String qubk = "gr.dc4.sql.sim";
					String qudev = "gr.beta.sql.sim";
					messageRepush.Identify = message.Identify;
					messageRepush.Action = DataAction.Update;
					messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
					messageRepush.CreatedDate = Utils.GetCurrentDate();
					messageRepush.Type = 0;
					messageRepush.DataCenter = message.DataCenter;
					messageRepush.Note = message.Note;
					try {

						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev,
								messageRepush, isLog, message.Note, DataCenter);

					} catch (Exception e) {
						Logs.LogException(e);
						resultMessage.StackTrace = Logs.GetStacktrace(e);
						resultMessage.Message = e.getMessage();
						resultMessage.Code = ResultCode.Retry;
						return resultMessage;
					}

				}
				resultMessage.Code = ResultCode.Success;

			} catch (Exception e) {
				Logs.LogException(e);
				resultMessage.StackTrace = Logs.GetStacktrace(e);
				resultMessage.Message = e.getMessage();
				resultMessage.Code = ResultCode.Retry;
				return resultMessage;
			}
		} else {
			return doDelete(message, simID, note, isLog);
		}
		return resultMessage;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
