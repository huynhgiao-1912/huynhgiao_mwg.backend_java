package mwg.wb.model.common;

import java.util.*;

public class ViewTrackingBO {
	public int Id;
	public Date Date;

	public OBJECTTYPE Type;
	public String User;

	public enum OBJECTTYPE {
		COOKDISH, COOKCATE, NEWS, PRODUCT
	}
}
