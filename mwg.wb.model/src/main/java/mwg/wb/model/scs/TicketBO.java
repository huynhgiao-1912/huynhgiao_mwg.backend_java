package mwg.wb.model.scs;

import java.util.Date;

public class TicketBO {
	public long Id;
	public String Content;
	public Date CreatedDate;
	public String CreatedBy;
	public HashtagBO[] HashTags;
	public MentionBO[] Mentions;
	
	public int IsDeleted;
	public long Version;
	
	
}
