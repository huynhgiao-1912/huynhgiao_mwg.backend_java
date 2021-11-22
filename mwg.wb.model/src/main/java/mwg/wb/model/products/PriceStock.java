package mwg.wb.model.products;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dùng để lưu trữ tồn kho trong table productprice (graph)
 */
public class PriceStock {
	public String recordID;
	public String dataEX;

	private static ObjectMapper mapper;

	private static ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
		}
		return mapper;
	}

	public static PriceStock fromListPrices(String recordID, List<ProductErpPriceBO> prices)
			throws JsonProcessingException {
		var o = new PriceStockObject();
		o.quantities = prices.stream().collect(Collectors.toMap(x -> x.ProvinceId, x -> x.Quantity));
		o.centerQuantities = prices.stream().collect(Collectors.toMap(x -> x.ProvinceId, x -> x.CenterQuantity));
		o.sampleQuantities = prices.stream().collect(Collectors.toMap(x -> x.ProvinceId, x -> x.SampleQuantity));
		o.relateQuantities = prices.stream().collect(Collectors.toMap(x -> x.ProvinceId, x ->
				x.TotalQuantityRelateProvince));
		o.hubQuantities = prices.stream().collect(Collectors.toMap(x -> x.ProvinceId, x -> x.quantityOLOLHub));
		var priceStock = new PriceStock();
		priceStock.recordID = recordID;
		priceStock.dataEX = getObjectMapper().writeValueAsString(o);
		return priceStock;
	}
	
	public static PriceStock fromListPricesBhx(String recordID, List<ProductErpPriceBO> prices)
			throws JsonProcessingException {
		var o = new PriceStockObject();
		o.quantities = prices.stream().collect(Collectors.toMap(x -> x.StoreID, x -> x.Quantity));
		o.centerQuantities = prices.stream().collect(Collectors.toMap(x -> x.StoreID, x -> x.CenterQuantity));
		o.sampleQuantities = prices.stream().collect(Collectors.toMap(x -> x.StoreID, x -> x.SampleQuantity));
		var priceStock = new PriceStock();
		priceStock.recordID = recordID;
		priceStock.dataEX = getObjectMapper().writeValueAsString(o);
		return priceStock;
	}

}
