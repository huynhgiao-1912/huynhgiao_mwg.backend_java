package mwg.wb.model.promotion;

public enum ProductRelevantType {
	Promotion(0), BillPromotion(1), Combo(2);

	private int value;

	ProductRelevantType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
