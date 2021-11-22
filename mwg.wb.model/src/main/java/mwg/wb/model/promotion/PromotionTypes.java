package mwg.wb.model.promotion;

public enum PromotionTypes {
	Discount(0), Gift(1), Undefined(2), AndDiscountGift(3), OrDiscountGift(4), Combo(5), Text(6);

	private int value;

	PromotionTypes(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
