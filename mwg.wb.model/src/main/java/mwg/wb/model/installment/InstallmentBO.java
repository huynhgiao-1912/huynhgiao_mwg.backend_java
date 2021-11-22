
package mwg.wb.model.installment;

import java.util.Date;
import java.util.List;

public class InstallmentBO {
	public InstallmentBO() {
	}

	public int PaymentID;

	public int ID;

	public int CompanyID;

	public int CategoryID;

	public double RentMoneyFrom;

	public double RentMoneyTo;

	public int PaymentPercentFrom;

	public int PaymentPercentTo;

	public int PaymentMonth;

	public double Co_efficient;

	public double LoanFrom;

	public double LoanTo;

	public boolean IsExist;

	public int SpecialCondition;

	public String PaymentName;

	// Bổ sung trả góp mới 2015

	public int ProductId;

	public Date FromDate;

	public Date ToDate;

	public int BriefId; // Mã giấy tờ cần có: 1 CMND+ HK,2: CMND+BLX,3: Tất cả giấy tờ trên

	public String ListDealId;// Danh sách giấy tờ cần có: 1 sinh viên(công chức nhà nước), 2 có chứng minh
								// thu nhập, 3 có thẻ thành viên ACS, 4 Có hóa đơn điện

	public String Note;

	public String Approvedtime;

	public double MoneyPayPerMonth;
	public double PrepaymentAmount;
	public double SpecialInterest;
	public double InsuranceCoefficient;
	public double InsuranceFee;
	public String ProductName;
	public double PercentInstallment; // lãi suất theo phần trăm hiển thị
	public int InstallmentType; // các gói trả góp đặc biệt
	public int IsDefaultPackage; // gói mặc định trong gói trả góp đặc biệt
	public double PriceFrom;
	public double PriceTo;
	public double TotalPay; // Tổng góp
	public int IsApplyOnProvince;
	public List<String> ListProvinceID;
	public String listProvinceIDStr;
	public double ServiceFee;
	public int ErpInstallProgramId;

	public String SiteIDList;
	public int[] siteIDListArr;
	public String InventoryStatusIdList; // danh sách trạng thái sản phẩm

	public boolean isDeleted;

	public int paymentType;

	public int fromSimPrice;

	public int isApplyForCate;

	public String languageID;

	public int isDefault;

	public Date didx_updateddate;

/// <summary>
/// Số tháng trả góp theo giá sản phẩm, có kèm lãi suất min max
/// </summary>

	public class InstallmentFeatureMonth {
		public int InstallMonth;
		public double MinInterestRate;
		public double MaxInterestRate;
		// public int MonthId ;
		// public int CateGroupId ;
		// public double FromPrice ;
		// public double ToPrice ;
		// public int InstallPartnerId ;
		// public int IsDefaultMonth ;
		// public Date CreatedDate ;
	}
/// <summary>
/// Gói trả góp mặc định, feature, dành cho version 2018
/// </summary>

	public class InstallmentFeature {
		public int MonthDetailId;
		public int InstallMonth;
		public double FromPrice;
		public double ToPrice;
		public int InstallPartnerId;
		public double FromPercent;
		public int CateGroupId;
		public int BriefId;
		// public double ToPercent ;
		// public double InterestRate ;
		// public Date FromDate ;
		// public Date ToDate ;
		// public double InsuranceFee ;
		// public double ServiceFee ;
		// public int ErpInstallmentId ;
		// public Date CreatedDate ;
		// public String SiteIdList ;
		// public double FromLoan ;
		// public double ToLoan ;
	}

/// <summary>
/// Lưu trữ thông tin khai báo trả góp Ngân lượng
/// </summary>
}
