package mwg.wb.client.resource;

public class XMLResourceBody {
	
	//CRM Service
	public static String CRM_GetCurrentInStocksBHXOnlByStore = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetCurrentInStocksBHXOnlByStore xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <productID>{productID}</productID>\r\n" + 
			"      <storeID>{storeID}</storeID>\r\n" + 
			"      <objCRMResultMessageBO>\r\n" + 
			"        <IsError>true</IsError>\r\n" + 
			"        <Message>string</Message>\r\n" + 
			"        <MessageDetail>string</MessageDetail>\r\n" + 
			"      </objCRMResultMessageBO>\r\n" + 
			"    </GetCurrentInStocksBHXOnlByStore>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	public static String CRM_GetCurrentInStocksBHXOnl = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetCurrentInStocksBHXOnl xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <productID>{productID}</productID>\r\n" + 
			"      <objCRMResultMessageBO>\r\n" + 
			"        <IsError>true</IsError>\r\n" + 
			"        <Message>string</Message>\r\n" + 
			"        <MessageDetail>string</MessageDetail>\r\n" + 
			"      </objCRMResultMessageBO>\r\n" + 
			"    </GetCurrentInStocksBHXOnl>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	public static String CRM_GetListProvinceHasProductInStock = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetListProvinceHasProductInStock xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <strProductCode>{strProductCode}</strProductCode>\r\n" + 
			"      <intToProvinceID>{intToProvinceID}</intToProvinceID>\r\n" + 
			"    </GetListProvinceHasProductInStock>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	//BHX service
	public static String BHX_GetPromotionBHXByProductID = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"  <soap:Header>\r\n" + 
			"    <AuthenSoapHeader xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <GUID>string</GUID>\r\n" + 
			"      <AuthenData>{AuthenData}</AuthenData>\r\n" + 
			"      <DelegatingUser>string</DelegatingUser>\r\n" + 
			"      <LanguageID>1</LanguageID>\r\n" + 
			"      <ModuleID>1</ModuleID>\r\n" + 
			"    </AuthenSoapHeader>\r\n" + 
			"  </soap:Header>\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetPromotionBHXByProductID xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <strProductID>{strProductID}</strProductID>\r\n" + 
			"      <intQuantity>{intQuantity}</intQuantity>\r\n" + 
			"      <intStoreID>{intStoreID}</intStoreID>\r\n" + 
			"      <bolIsGetStock>{bolIsGetStock}</bolIsGetStock>\r\n" + 
			"      <intOldDays>{intOldDays}</intOldDays>\r\n" + 
			"    </GetPromotionBHXByProductID>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	public static String BHX_GetPromotionBHXByPromotionId = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"  <soap:Header>\r\n" + 
			"    <AuthenSoapHeader xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <GUID>string</GUID>\r\n" + 
			"      <AuthenData>{AuthenData}</AuthenData>\r\n" + 
			"      <DelegatingUser>\"null\"</DelegatingUser>\r\n" + 
			"      <LanguageID>1</LanguageID>\r\n" + 
			"      <ModuleID>1</ModuleID>\r\n" + 
			"    </AuthenSoapHeader>\r\n" + 
			"  </soap:Header>\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetPromotionById xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <intPromotionId>{intPromoId}</intPromotionId>\r\n" + 
			"      <intStroreId>{intStoreId}</intStroreId>\r\n" + 
			"    </GetPromotionById>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	public static String BHX_GetPromotionByProductClearStock = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"    <soap:Header>\r\n" + 
			"    <AuthenSoapHeader xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <GUID>\"null\"</GUID>\r\n" + 
			"      <AuthenData>{AuthenData}</AuthenData>\r\n" + 
			"      <DelegatingUser>\"null\"</DelegatingUser>\r\n" + 
			"      <LanguageID>1</LanguageID>\r\n" + 
			"      <ModuleID>1</ModuleID>\r\n" + 
			"    </AuthenSoapHeader>\r\n" + 
			"  </soap:Header>\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetPromotionByProductClearStock xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <strProductID>{strProductID}</strProductID>\r\n" + 
			"      <intStoreID>{intStoreID}</intStoreID>\r\n" + 
			"      <strQuantity>{strQuantity}</strQuantity>\r\n" + 
			"      <intOldDays>{intOldDays}</intOldDays>\r\n" + 
			"      <dtmDateApply>{dtmDateApply}</dtmDateApply>\r\n" + 
			"    </GetPromotionByProductClearStock>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	public static String BHX_GetPriceBHXOnline = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
			"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
			"  <soap:Header>\r\n" + 
			"    <AuthenSoapHeader xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <GUID>string</GUID>\r\n" + 
			"      <AuthenData>{AuthenData}</AuthenData>\r\n" + 
			"      <DelegatingUser>string</DelegatingUser>\r\n" + 
			"      <LanguageID>0</LanguageID>\r\n" + 
			"      <ModuleID>0</ModuleID>\r\n" + 
			"    </AuthenSoapHeader>\r\n" + 
			"  </soap:Header>\r\n" + 
			"  <soap:Body>\r\n" + 
			"    <GetPriceBHXOnline xmlns=\"http://tempuri.org/\">\r\n" + 
			"      <productId>{productId}</productId>\r\n" + 
			"      <intStoreId>{intStoreId}</intStoreId>\r\n" + 
			"    </GetPriceBHXOnline>\r\n" + 
			"  </soap:Body>\r\n" + 
			"</soap:Envelope>";
	
	public static String PROMOTION_WEBGETPRD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
			+ "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
			+ "  <soap:Body>\r\n"
			+ "    <GetPromotionByProduct xmlns=\"http://tempuri.org/\">\r\n"
			+ "      <strAuthen>{Authen}</strAuthen>\r\n"
			+ "    <strProductID>{ProductID}</strProductID>\r\n"
			+ "      <intProvinceID>{ProvinceID}</intProvinceID>\r\n"
			+ "      <intOutputTypeID>{OutputTypeID}</intOutputTypeID>\r\n"
			+ "      <decSalePrice>{SalePrice}</decSalePrice>\r\n"
			+ "      <intInventoryStatusID>{InventoryStatusID}</intInventoryStatusID>\r\n"
			+ "      <intSiteID>{SiteID}</intSiteID>\r\n"
			+ "    </GetPromotionByProduct>\r\n"
			+ "  </soap:Body>\r\n"
			+ "</soap:Envelope>";
	
	public static String PROMOTION_WEBGETSUBBRAND = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
			+ "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
			+ "  <soap:Body>\r\n"
			+ "    <GetPromotionBySubBrand xmlns=\"http://tempuri.org/\">\r\n"
			+ "      <strAuthen>{Authen}</strAuthen>\r\n"
			+ "      <strSubGroupID>{SubGroupID}</strSubGroupID>\r\n"
			+ "      <intBrandIDPRD>{ManuID}</intBrandIDPRD>\r\n"
			+ "      <intProvinceID>{ProvinceID}</intProvinceID>\r\n"
			+ "      <intOutputTypeID>{OutputTypeID}</intOutputTypeID>\r\n"
			+ "      <decSalePrice>{SalePrice}</decSalePrice>\r\n"
			+ "      <intInventoryStatusID>{InventoryStatusID}</intInventoryStatusID>\r\n"
			+ "      <intSiteID>{SiteID}</intSiteID>\r\n"
			+ "    </GetPromotionBySubBrand>\r\n"
			+ "  </soap:Body>\r\n"
			+ "</soap:Envelope>";
}
