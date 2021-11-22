package mwg.wb.model.commonpackage;

public enum SuggestSearchTypes {
	Category(1), SuggestSearch(2), CategoryManuFacture(3);


    private int value;

    SuggestSearchTypes(int v) {
        this.value = v;
    }

    public int getValue() {
        return this.value;
    }

    public static SuggestSearchTypes fromString(int t) {
        for (SuggestSearchTypes b : SuggestSearchTypes.values()) {
            if (b.value == t) {
                return b;
            }
        }
        return null;
    }
}