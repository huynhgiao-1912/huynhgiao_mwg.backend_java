package mwg.wb.model.social;

import java.util.Date;

public class SocialNotifyBO {
	public long NotifyID;
    public Date Createddate;

    public SocialUserBO FromUserA;
    public SocialUserBO ToUserB;
    public SocialNewsBO NewsObj;
    public SocialCookBO CookObj;
    
    public String Content;
    public String Note;

    public Boolean IsRead;
    public long CommentID;

    public String ExtendInfo1;
    public String ExtendInfo2;

    public int Type;//1. userA reply comment userB, 2: user A comment news's userB, 3: userA follow topic, 4: has new news in topic,5: user folow uer

    public SoialTopicBO TopicObj;
    public int SiteID;
    
}
