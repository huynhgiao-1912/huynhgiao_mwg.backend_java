package mwg.wb.business.SpecialsaleProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mwg.wb.business.LogHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.SpecialSaleProgramBO;

public class SpecialsaleProgramHelper {
	private static ORThreadLocal oclient = null;

	public static Map<Integer, Pm_specialsaleprogramBO> list_pm_specialsaleprogram = new HashMap<Integer, Pm_specialsaleprogramBO>();
	public static Map<Integer, Pm_specialsaleprogram_brandBO> list_pm_specialsaleprogram_brand = new HashMap<Integer, Pm_specialsaleprogram_brandBO>();
	public static Map<Integer, Pm_specialsaleprogram_invsttBO> list_pm_specialsaleprogram_invstt = new HashMap<Integer, Pm_specialsaleprogram_invsttBO>();
	public static Map<Integer, Pm_specialsaleprogram_productBO> list_pm_specialsaleprogram_product = new HashMap<Integer, Pm_specialsaleprogram_productBO>();
	public static Map<Integer, Pm_specialsaleprogram_subgroupBO> list_pm_specialsaleprogram_subgroup = new HashMap<Integer, Pm_specialsaleprogram_subgroupBO>();

	private static SpecialsaleProgramHelper instance;
	private static long Expire = System.currentTimeMillis() + 15 * 60 * 1000;// 15 phut

	public static SpecialsaleProgramHelper getInstance(ORThreadLocal afactoryRead, ClientConfig aconfig) {

		if (instance == null) {
			synchronized (SpecialsaleProgramHelper.class) {
				instance = new SpecialsaleProgramHelper(afactoryRead, aconfig);
			}

		}

		return instance;
	}

	public SpecialsaleProgramHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		oclient = afactoryRead;
		try {
			LoadAll("");
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
	}

	public SpecialSaleProgramBO GetSpecialsaleProgram(String productcode, int brandid, int inventorystatusid,
			int subgroupid) {
		long cDate = System.currentTimeMillis();

		if (Expire < cDate) {
			Expire = System.currentTimeMillis() + 15 * 60 * 1000;
			try {

				LoadAll("");

			} catch (Throwable e) {
				LogHelper.WriteLog(e);
			}
		}

		List<Integer> progralidList = new ArrayList<Integer>();

		List<Pm_specialsaleprogramBO> rsList = new ArrayList<Pm_specialsaleprogramBO>();

		for (Entry<Integer, Pm_specialsaleprogram_brandBO> entry : list_pm_specialsaleprogram_brand.entrySet()) {
			var item = entry.getValue();
			if (item.brandid == brandid && item.specialsaleprogramtype == 5 && item.begindate.getTime() <= cDate
					&& item.enddate.getTime() >= cDate) {
				progralidList.add(item.specialsaleprogramid);

			}
		}

		for (Entry<Integer, Pm_specialsaleprogram_subgroupBO> entry : list_pm_specialsaleprogram_subgroup.entrySet()) {
			var item = entry.getValue();
			if (item.specialsaleprogramtype == 5

					&& item.begindate.getTime() <= cDate && item.enddate.getTime() >= cDate) {
				if (item.subgroupid == subgroupid || item.isallbrand > 0) {
					progralidList.add(item.specialsaleprogramid);
				}

			}
		}

		for (Entry<Integer, Pm_specialsaleprogram_productBO> entry : list_pm_specialsaleprogram_product.entrySet()) {
			var item = entry.getValue();
			if (item.productid.equals(productcode) && item.specialsaleprogramtype == 5
					&& item.begindate.getTime() <= cDate && item.enddate.getTime() >= cDate) {
				progralidList.add(item.specialsaleprogramid);

			}
		}
		List<Integer> reENd = new ArrayList<Integer>();

		for (Entry<Integer, Pm_specialsaleprogram_invsttBO> entry : list_pm_specialsaleprogram_invstt.entrySet()) {
			var item = entry.getValue();
			if (item.inventorystatusid == inventorystatusid && item.specialsaleprogramtype == 5
					&& item.begindate.getTime() <= cDate && item.enddate.getTime() >= cDate) {
				if (progralidList.contains(item.specialsaleprogramid)) {
					reENd.add(item.specialsaleprogramid);
				}

			}
		}

		if (reENd.size() > 0) {
			for (Integer id : reENd) {
				var item = list_pm_specialsaleprogram.get(id);

				if (item != null) {
					rsList.add(item);

				}
			}

		}

		if (rsList != null && rsList.size() > 0) {
			var it = rsList.get(0);
			SpecialSaleProgramBO rs = new SpecialSaleProgramBO();
			rs.SpecialSaleProgramName = it.specialsaleprogramname;
			rs.BeginDate = it.begindate;
			rs.EndDate = it.enddate;
			rs.SpecialSaleProgramType = it.specialsaleprogramtype;
			rs.IsActived = it.isactived > 0 ? true : false;
			rs.IsDeleted = it.isdeleted > 0 ? true : false;
			rs.CreatedUser = it.createduser;
			rs.CreatedDate = it.createddate;
			rs.UpdatedUser = it.updateduser;
			rs.UpdatedDate = it.updateddate;
			rs.ActivedUser = it.activeduser;
			rs.ActivedDate = it.activeddate;
			rs.DeletedUser = it.deleteduser;
			rs.DeletedDate = it.deleteddate;
			rs.DeletedReason = it.deletedreason;
			rs.DeliveryFromDate = it.deliveryfromdate;
			rs.DeliveryToDate = it.deliverytodate;
			rs.LockOrderType = it.lockordertype;
			rs.IsHavingSubProduct = it.ishavingsubproduct > 0 ? true : false;
			rs.NotifyTypeList = it.notifytypelist;
			rs.ProcessUserList = it.processuserlist;
			rs.GetPromotionType = it.getpromotiontype;
			return rs;

		}

		return null;
	}

	public synchronized void LoadAll(String table) throws Throwable {
		if (Utils.StringIsEmpty(table) || table.equals("pm_specialsaleprogram")) {
			list_pm_specialsaleprogram.clear();
			Pm_specialsaleprogramBO[] AllProgam = specialsaleprogram_getAllPro();
			for (Pm_specialsaleprogramBO pm_specialsaleprogramBO : AllProgam) {
				list_pm_specialsaleprogram.put(pm_specialsaleprogramBO.specialsaleprogramid, pm_specialsaleprogramBO);

			}
		}

		if (Utils.StringIsEmpty(table) || table.equals("pm_specialsaleprogram_brand")) {
			list_pm_specialsaleprogram_brand.clear();
			Pm_specialsaleprogram_brandBO[] specialsaleprogram_getAllBrand = specialsaleprogram_getAllBrand();
			for (Pm_specialsaleprogram_brandBO pm_specialsaleprogram_brandBO : specialsaleprogram_getAllBrand) {
				list_pm_specialsaleprogram_brand.put(pm_specialsaleprogram_brandBO.recordid,
						pm_specialsaleprogram_brandBO);
			}
		}
		if (Utils.StringIsEmpty(table) || table.equals("pm_specialsaleprogram_subgroup")) {
			list_pm_specialsaleprogram_subgroup.clear();
			Pm_specialsaleprogram_subgroupBO[] AllSubgroup = specialsaleprogram_getAllSubgroup();
			for (Pm_specialsaleprogram_subgroupBO pm_specialsaleprogram_subgroupBO : AllSubgroup) {
				list_pm_specialsaleprogram_subgroup.put(pm_specialsaleprogram_subgroupBO.recordid,
						pm_specialsaleprogram_subgroupBO);

			}
		}
		if (Utils.StringIsEmpty(table) || table.equals("pm_specialsaleprogram_product")) {
			list_pm_specialsaleprogram_product.clear();

			Pm_specialsaleprogram_productBO[] AllProduct = specialsaleprogram_getAllProduct();
			for (Pm_specialsaleprogram_productBO pm_specialsaleprogram_productBO : AllProduct) {
				list_pm_specialsaleprogram_product.put(pm_specialsaleprogram_productBO.recordid,
						pm_specialsaleprogram_productBO);
			}
		}
		if (Utils.StringIsEmpty(table) || table.equals("pm_specialsaleprogram_invstt")) {
			list_pm_specialsaleprogram_invstt.clear();
			Pm_specialsaleprogram_invsttBO[] AllInvstt = specialsaleprogram_getAllInvstt();
			for (Pm_specialsaleprogram_invsttBO pm_specialsaleprogram_invsttBO : AllInvstt) {
				list_pm_specialsaleprogram_invstt.put(pm_specialsaleprogram_invsttBO.recordid,
						pm_specialsaleprogram_invsttBO);
			}
		}
	}

//	specialsaleprogram_getAllBrand
//	specialsaleprogram_getAllSubgroup
//	specialsaleprogram_getAllProduct
//	specialsaleprogram_getAllPro
//	specialsaleprogram_getAllInvstt
	public Pm_specialsaleprogram_brandBO[] specialsaleprogram_getAllBrand() throws Throwable {

		return oclient.QueryFunction("specialsaleprogram_getAllBrand", Pm_specialsaleprogram_brandBO[].class, false);

	}

	public Pm_ProductBO GetPm_ProductBO(String prductCode) throws Throwable {

		Pm_ProductBO[] rs = oclient.QueryFunction(
				"select 	subgroupid, brandid, issetupproduct from  	pm_product   where productid =" + prductCode,
				Pm_ProductBO[].class, false);
		if (rs != null && rs.length > 0) {
			return rs[0];
		}
		return null;
	}

	public Pm_specialsaleprogram_subgroupBO[] specialsaleprogram_getAllSubgroup() throws Throwable {

		return oclient.QueryFunction("specialsaleprogram_getAllSubgroup", Pm_specialsaleprogram_subgroupBO[].class,
				false);

	}

	public Pm_specialsaleprogram_productBO[] specialsaleprogram_getAllProduct() throws Throwable {

		return oclient.QueryFunction("specialsaleprogram_getAllProduct", Pm_specialsaleprogram_productBO[].class,
				false);

	}

	public Pm_specialsaleprogramBO[] specialsaleprogram_getAllPro() throws Throwable {

		return oclient.QueryFunction("specialsaleprogram_getAllPro", Pm_specialsaleprogramBO[].class, false);

	}

	public Pm_specialsaleprogram_invsttBO[] specialsaleprogram_getAllInvstt() throws Throwable {

		return oclient.QueryFunction("specialsaleprogram_getAllInvstt", Pm_specialsaleprogram_invsttBO[].class, false);

	}

}
