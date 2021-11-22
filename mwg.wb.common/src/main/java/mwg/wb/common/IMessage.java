package mwg.wb.common;

import java.util.Date;

public interface IMessage {
	public enum DataAction {
		Add, Update, Delete, IntervalAction 
	}

	/// <summary>
	///
	/// </summary>
	long ID = 0;

	/// <summary>
	///
	/// </summary>

	/// <summary>
	///
	/// </summary>
	DataAction Action = DataAction.Add;

	/// <summary>
	///
	/// </summary>
	String Identify = "";
	/// <summary>
	///
	/// </summary>
	int SiteID = 0;
	/// <summary>
	///
	/// </summary>
	String Lang = "vi-VN";
	String Term = "";

	/// <summary>
	///
	/// </summary>
	Date CreatedDate = null;

	/// <summary>
	///
	/// </summary>
	long Version = 0;

	String ClassName = "";

}