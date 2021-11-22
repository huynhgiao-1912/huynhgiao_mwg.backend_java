package mwg.wb.model.seo;

import mwg.wb.model.common.Upsertable;

public class ProductUrlUpsert implements Upsertable {
	public boolean isdeleted;
	public String json;
	public String url, recordid, langid;
	public int siteid;

	@Override
	public String indexValue() {
		return recordid;
	}
}
