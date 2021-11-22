package mwg.wb.model.sim;

import java.util.Date;

public class SimPackageBO {
	/**
	 * Mã gói sim
	 */
	public int PACKAGEID;
	public String PACKAGENAME;
	/**
	 * Id nhà mạng
	 */
	public int SIMNETWORKID;
	/**
	 * Thứ tự hiển thị
	 */
	public int DISPLAYORDER;
	/**
	 * Nhóm loại sim
	 */
	public int GROUPID;
	/**
	 * Mã gói ERP
	 */
	public int ERPID;
	/**
	 * Giá min của gói sim
	 */
	public double MINPRICE;
	/**
	 * Chi tiết gói sim
	 */
	public String POPUPDETAIL;
	/**
	 * Thông tin Khuyến mãi gói sim ( hiển thị ở trang chọn sim số)
	 */
	public String PROMOTIONDETAIL;
	/**
	 * Đặc điểm nổi bật của gói sim
	 */
	public String KEYSELLPOINT;
	/**
	 * Dung lượng data
	 */
	public String DATA;
	/**
	 * Gọi nội mạng
	 */
	public String INTERNALNETWORK;
	/**
	 * Gọi ngoại mạng
	 */
	public String EXTERNALNETWORD;

	/**
	 * Số lượng tin nhắn
	 */
	public String SMS;

	/**
	 * Hình gói cước, Desktop
	 */
	public String PACKAGEIMAGE;

	/**
	 * Hình gói cước, Mobile
	 */
	public String MOBILEPACKAGEIMAGE;

	/**
	 * meta title
	 */
	public String METATITLE;
	/**
	 * meta desc
	 */
	public String METADESCRIPTION;

	/**
	 * CreatedDate
	 * 
	 */
	public Date CREATEDDATE;

	/**
	 * CreatedUser
	 * 
	 */
	public String CREATEDUSER;

	/**
	 * Kích hoạt gói sim
	 */
	public boolean ISACTIVED;
	/**
	 * Ngày kích hoạt
	 */
	public Date ACTIVEDDATE;
	/**
	 * User kích hoạt gói
	 */
	public String ACTIVEDUSER;
	/**
	 * UpdatedDate
	 * 
	 */
	public Date UPDATEDDATE;

	/**
	 * UpdatedUser
	 * 
	 */
	public String UPDATEDUSER;
	/**
	 * IsDeleted
	 * 
	 */
	public boolean ISDELETED;

	/**
	 * DeletedDate
	 * 
	 */
	public Date DELETEDDATE;

	/**
	 * DeletedUser
	 * 
	 */
	public String DELETEDUSER;
	/**
	 * Mã sản phẩm ERP
	 */
	public String PRODUCTCODE;
	/**
	 * Mã loại gói cước đối tác
	 */
	public String PARTNERPACKAGEID;

	/**
	 * Áp dụng bán kèm: => Tạm bỏ, 0 điện thoại cơ bản, 1 điện thoại thông minh, 2
	 * cả hai
	 */
	public int ISAPPLYINCLUDED;
	/**
	 * Danh sách áp dụng bán kèm
	 */
	public String ApplyIncluded;
}
