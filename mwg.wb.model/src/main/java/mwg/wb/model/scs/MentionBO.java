package mwg.wb.model.scs;

public class MentionBO {

	public long Id;
	public String Mention;
	public String SearchText;
	public String Description;
	public int Type;// 1-user, 2-department, 3-group....
	public int ObjectId;// Id of object that references to type field(eg: Id group, Id user...)
	public String Object;
	public String Hashtags;
	public long Version;
	public int IsDeleted;
	
}
