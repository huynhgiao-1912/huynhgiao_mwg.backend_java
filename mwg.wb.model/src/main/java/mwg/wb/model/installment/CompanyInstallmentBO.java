package mwg.wb.model.installment;

public class CompanyInstallmentBO {
	private int companyid, percent, month;

	public CompanyInstallmentBO(int companyid, int percent, int month) {
		this.companyid = companyid;
		this.percent = percent;
		this.month = month;
	}

	public int getCompanyid() {
		return companyid;
	}

	public int getPercent() {
		return percent;
	}

	public int getMonth() {
		return month;
	}
}
