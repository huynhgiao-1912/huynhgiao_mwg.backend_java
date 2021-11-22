








package mwg.wb.model.promotion;
    /// <summary>
    /// Created by 		: Bạch Xuân Cường 
    /// Created date 	: 10/5/2012 
    /// Giờ vàng
    /// </summary>	

import java.util.Date;

public class CustomerGoldHourBO
    {

        public CustomerGoldHourBO()
        {
        }
        /// <summary>
        /// Ðôi tuong message
        /// </summary>
        public int GoldHourID ;
        public int DiscountType ;
        public String PhoneNumber ;

        public long ProductID ;

        public String FullName ;


        /// <summary>
        /// CreatedDate
        /// NGAY TAO
        /// </summary>
        public Date CreatedDate ;

        /// <summary>
        /// ProgramID
        /// M? CHUONG TRINH
        /// </summary>
        public int ProgramID ;



        /// <summary>
        /// IsDeleted
        /// 
        /// </summary>
        public boolean IsDeleted ;
        public boolean IsActived ;
    
        /// <summary>
        /// Gender
        /// 0:NAM 1:N?
        /// </summary>
        public int Gender ;


        /// <summary>
        /// IsWin
        /// 
        /// </summary>
        public int StatusID ;



        /// <summary>
        /// ProvinceID
        /// 
        /// </summary>
        public int ProvinceID ;
        public int SiteID ;

        /// <summary>
        /// DistrictID
        /// 
        /// </summary>
        public int DistrictID ;

        /// <summary>
        /// Address
        /// 
        /// </summary>
        public String Address ;

        public String ProductName ;

        /// <summary>
        /// Có tồn tại không?
        /// </summary>
        public boolean IsExist ;

        /// <summary>
        /// Có đang chọn không?
        /// </summary>
        public boolean IsSelected ;

        /// <summary>
        /// Có chỉnh sữa không?
        /// </summary>
        public boolean IsEdited ;



    }
