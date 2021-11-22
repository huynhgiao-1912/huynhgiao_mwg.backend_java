package mwg.wb.model.sim;

import mwg.wb.model.common.Upsertable;

public class SimPackageErpBO implements Upsertable {
	public int packagestypeid;
	public String packagestypename;
	public String partnerpackagestypeid;
	public int brandid;
	public double packagescostprice;
	public String description;
	public boolean isactive;
	public double simserialcostprice;
	public double packagessaleprice;

	@Override
	public String indexValue() {
		return String.valueOf(packagestypeid);
	}
}
