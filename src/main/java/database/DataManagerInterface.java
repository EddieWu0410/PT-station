package database;

import entity.BegInfo;
import entity.BegSeedDetail;
import entity.Notice;
import entity.Post;
import entity.Profile;
import entity.Seed;
import entity.SeedPromotion;
import entity.SeedWithPromotionDTO;
import entity.User;
import entity.UserPT;
import entity.UserStar;
import entity.PostReply;
import entity.SeedWithVotes;
import java.io.File;
import java.util.Date;
public interface DataManagerInterface{
//DB1

    public int RegisterUser(User userinfo);// 返回状态：0 success，1 邮箱重复，2其他原因
    public String LoginUser(User userinfo);
    public int UpdateInformation(User userinfo);// 返回状态：0 success，1 不存在，2其他原因
    public User GetInformation(String userid);// 返回用户的全部基本信息

    public UserPT GetInformationPT(String userid);//返回用户的全部pt站信息
    public int UpdateInformationPT(UserPT userinfo);//返回状态：0 success，1 邮箱重复，2其他原因
    public int RegisterUserPT(UserPT userinfo);//返回状态：0 success，1 邮箱重复，2其他原因

    public Seed GetSeedInformation(String seedid);//返回种子的全部信息;
    public int RegisterSeed(Seed seedinfo);//添加一个新的种子，0成功，其他失败信息待定;
    public int UpdateSeed(Seed seedinfo);//接收新的种子然后更新其全部属性;
    public int DeleteSeed(String seedid);//删除一个种子，返回状态：0 success，1 不存在,2其他原因

    public Seed[] SearchSeed(String userQ);//传入搜索的关键词或句子，返回搜索到的种子信息（按照公共字符数量排序）
    public SeedWithPromotionDTO[] GetSeedListByTag(String tag);//获取某个标签下的种子列表
    public Seed[] GetSeedListByUser(String userid);//获取某个用户的种子列表

    public int AddNotice(Notice notice);//返回状态：0 success，1 重复，2其他原因
    public boolean UpdateNotice(Notice notice);//返回状态：0 success，1 重复，2其他原因
    public boolean DeleteNotice(String noticeid);//删除公告，返回状态：0 success，1 重复，2其他原因

    public int GetUserAvailableInviteTimes(String userid);//获取用户的剩余邀请次数
    public int InviteUser(String inviterid,String inviteemail);//邀请用户，返回状态：0 success，1 重复，2其他原因

    public boolean AddCollect(String userid,String seedid);//添加一个收藏，返回状态：0 success，1 不存在,2其他原因
    public boolean DeleteCollect(String userid,String seedid);//删除一个收藏，返回状态：0 success，1 不存在,2其他原因
    public UserStar[] GetUserStarList(String userid);//获取用户的收藏列表

    public int CheckAdmin(String userid);//检查用户是否为管理员，返回状态：0 success，1 不存在,2其他原因


// ----------------------------------------------------------------------------------------------------

//DB2
    public int AddBegSeed(BegInfo info);//添加一个新的求种信息，返回状态：0 success，1 重复，2其他原因
    public int UpdateBegSeed(BegInfo info);//更新一个求种信息，返回状态：0 success，1 重复，2其他原因
    public int DeleteBegSeed(String begid);//删除一个求种信息，返回状态：0 success，1 重复，2其他原因
    public int VoteSeed(String begId, String seedId, String userId);//求种结果投票，返回状态：0 success，1 重复，2其他原因
    public int SubmitSeed(String begid,Seed seed);//提交种子，返回状态：0 success，1 重复，2其他原因
    public void SettleBeg();//结算所有求种信息，求种信息中需要增加Beg截止日期，默认14天，期间投票>的则Beg成功，否则Beg失败，并发放对应奖励
    public BegSeedDetail[] GetBegList();//获取所有求种信息（包含BegSeed表内容和BegInfo的Info字段）
    public BegInfo GetBegDetail(String begid);//获取一个求种信息的详细信息
    public BegSeedDetail GetBegSeedDetail(String begid);//获取一个求种信息的详细信息（包含BegSeed表数据和BegInfo表的Info字段）
    public SeedWithVotes[] GetBegSeedListWithVotes(String begid);//获取一个求种信息下的种子列表（包含投票信息）
    public int SubmitBegSeed(String begid, String seedid, String userid);//提交一个种子到求种信息中，返回状态：0 success，1 重复，2其他原因

    public Post[] SearchPost(String userQ);//传入搜索的关键词或句子，返回搜索到的帖子信息（按照公共字符数量排序）
    public Post[] GetPostList();//获取用户的帖子列表
    public Post GetPost(String postid);//获取一个帖子的详细信息
    public PostReply[] GetPostReplyList(String postid);//获取一个帖子的回复列表
    public int AddPost(Post post);//添加一个新的帖子，返回状态：0 success，1 重复，2其他原因
    public int UpdatePost(Post post);//更新一个帖子，返回状态：0 success，1 不存在,2其他原因
    public int DeletePost(String postid);//删除一个帖子，返回状态：0 success，1 不存在,2其他原因

    public int AddComment(String postid, String userid, String comment);//添加一个评论，返回状态：0 success，1 不存在，2其他原因
    public int DeleteComment(String postid,String commentid);//删除一个评论，返回状态：0 success，1 不存在,2其他原因

    

    public boolean ExchangeMagicToUpload(String userid,int magic);//将魔力值兑换为上传量，返回状态：0 success，1 不存在,2其他原因
    public boolean ExchangeMagicToDownload(String userid,int magic);//将魔力值兑换为下载量，返回状态：0 success，1 不存在,2其他原因
    public boolean ExchangeMagicToVip(String userid,int magic);//将魔力值兑换为VIP次数，返回状态：0 success，1 不存在,2其他原因

    public boolean UploadTransmitProfile(Profile profile);
    public Profile GetTransmitProfile(String profileid);//获取迁移信息
    public boolean ExamTransmitProfile(String profileid, boolean result, Integer grantedUpload);//审核迁移信息,0成功，1失败
    public Profile[] GetTransmitProfileList();//获取所有迁移信息
    public int InviteNewUser(String inviterid, String invitedemail);//邀请新用户，返回状态：0 success，1 重复，2其他原因
    public int UploadMigration(String userid, File file, String uploadtogive);
    public int createBagSeed(BegInfo begInfo, String userid, String info);
    public Seed[] getAllSeeds(); // 获取所有种子信息
    public SeedPromotion[] getAllSeedPromotions(); // 获取所有种子推广信息
    public int createSeedPromotion(String seedid, Date startTime, Date endTime, Integer discount); // 添加种子推广信息
}   