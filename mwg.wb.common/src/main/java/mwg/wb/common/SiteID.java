package mwg.wb.common;

public enum SiteID {
    THEGIOIDIDONG(1, "vi-VN"),
    DIENMAYXANH(2, "vi-VN"),
    BLUESTRONICS(6, "km-KH"),
    BACHHOAXANH(11, "vi-VN");

    private final int value;
    private final String langID;

    SiteID(final int newValue, String langID) {
        value = newValue;
        this.langID = langID;
    }

    public int getValue() {
        return value;
    }

    public String getLangID() {
        return langID;
    }
}
