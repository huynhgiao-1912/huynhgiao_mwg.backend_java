
package mwg.wb.model.installment;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 6/5/2015 
/// Thông tin trả góp
/// </summary>

import java.util.Date;

public class PaymentCategoryBO {

	public PaymentCategoryBO() {
	}

	/// <summary>
	/// PaymentID
	///
	/// </summary>
	public int PaymentID;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// RangeFrom
	///
	/// </summary>
	public int RangeFrom;

	/// <summary>
	/// RANGetO
	///
	/// </summary>
	public int RANGetO;

	/// <summary>
	/// PaymentPercentFrom
	///
	/// </summary>
	public int PaymentPercentFrom;

	/// <summary>
	/// PaymentPercentTo
	///
	/// </summary>
	public int PaymentPercentTo;

	/// <summary>
	/// PaymentMonth
	///
	/// </summary>
	public int PaymentMonth;

	/// <summary>
	/// PaymentValue
	/// Lãi suất nhập theo %
	/// </summary>
	public double PaymentValue;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// PaymentType
	/// 1: HomeCredit, 2: ACS, 3: FE-Credit
	/// </summary>
	public int PaymentType;

	/// <summary>
	/// LoanFrom
	///
	/// </summary>
	public int LoanFrom;

	/// <summary>
	/// LoanTo
	///
	/// </summary>
	public int LoanTo;

	/// <summary>
	/// IsEligible
	///
	/// </summary>
	public int IsEligible;

	/// <summary>
	/// PaymentName
	///
	/// </summary>
	public String PaymentName;

	/// <summary>
	/// ProductID
	/// Mã Sản phẩm áp dụng
	/// </summary>
	public int ProductID;

	/// <summary>
	/// FromDate
	///
	/// </summary>
	public Date FromDate;

	/// <summary>
	/// ToDate
	///
	/// </summary>
	public Date ToDate;

	/// <summary>
	/// BriefID
	/// Mã giấy tờ cần có: 1 CMND+ HK,2: CMND+BLX,3: Tất cả giấy tờ trên
	/// </summary>
	public int BriefID;

	/// <summary>
	/// ListDealID
	/// Danh sách Mã giấy tờ cần có: 1 sinh viên(công chức nhà nước), 2 có chứng
	/// minh thu nhập, 3 có thẻ thành viên ACS, 4 Có hóa đơn điện
	/// </summary>
	public String ListDealID;

	/// <summary>
	/// Note
	///
	/// </summary>
	public String Note;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String ProductName;

	public double PrepaymentAmount;

	public double PercentInstallment;
	public double InsuranceFees;
	public int InstallmentType;
	public int IsDefault;
	public int SiteID;
	public int IsApplyOnProvince;
	public String ListProvinceID;
	public int ERPInstallmentID;
	public double ServiceFee;
	public String SiteIDList;
	public String InventoryStatusIdList; // danh sách trạng thái sản phẩm

	public int InventoryStatusId; // Trạng thái sản phẩm :: 1 => mới, 2 => cũ
	public boolean IsAttachedSim;
	public int FromSimPrice;
	public int ToSimPrice;
	public String InstallmentProductName; // tên sản phẩm map với HC => duyệt hồ sơ online 1 phút

	public String LanguageId;

	public class PaymentCategoryExportBO {

		public PaymentCategoryExportBO() {
		}

		public int PaymentID;
		public int CategoryID;
		public int RangeFrom;
		public int RANGetO;
		public int PaymentPercentFrom;
		public int PaymentPercentTo;
		public int PaymentMonth;
		public double PaymentValue;
		public double InsuranceFees;
		public double PrepaymentAmount;
		public int PaymentType;
		public int LoanFrom;
		public int LoanTo;
		public int IsEligible;
		public String PaymentName;
		public int ProductID;
		public String FromDate;
		public String ToDate;
		public int BriefID;
		public String ListDealID;
		public int InstallmentType;
		public int IsDefault;
		public double PercentInstallment;
		public int ERPInstallmentID;
		public double ServiceFee;
		public String SiteIDList;
		public String InventoryStatusIdList;
		public String ProductName;
		public int IsAttachedSim;
		public int FromSimPrice;
		public int ToSimPrice;
		public String InstallmentProductName; // tên sản phẩm map với HC => duyệt hồ sơ online 1 phút
		public String LanguageId;
	}

	public class PaymentCategoryException {

		public PaymentCategoryException() {
		}

		public int ExceptionID;
		public String ExceptionName;
		public int ExceptionType;
		public String Note;
		public String CreateUser;
		public String UpdateUser;
	}

	public class PaymentCategoryExceptionProduct {

		public PaymentCategoryExceptionProduct() {
		}

		public int ExceptionID;
		public int ProductID;
		public Date FromDate;
		public Date ToDate;
		public int ERPProgramID;
		public String ProductName;
	}
}
