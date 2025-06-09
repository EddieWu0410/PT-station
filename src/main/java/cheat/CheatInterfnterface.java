package cheat;
import org.apache.commons.lang3.tuple.Pair;
import entity.Appeal;
import entity.User;
import java.io.File;

public interface CheatInterfnterface{
    public Pair<String,String>[] GetFakeSeed();//返回做假种的列表，<seedid,userid>
    public void DetectFakeSeed();//检测所有种子是否为假种，并将检测结果写入数据表
    public void DetectTrans();//检测所有种子是存在伪造上传量下载量

    public boolean DetectFakeSeed(String seedid);//检测单个用户是否存在假种
    public void PunishUser();//扫描数据库中的可疑表，标记可疑用户
    public String[] GetPunishedUserList();//获取所有可疑用户的列表
    
    public boolean AddAppeal(Appeal appeal);//数据库中写入一个申诉请求
    public Appeal GetAppeal(String appealid);//获取某个申诉
    public Appeal[] GetAppealList();//获取所有申诉列表
    public boolean HandleAppeal(String appealid, Integer status);//处理申诉
    // status = 0表示未处理， = 1表示通过， = 2表示拒绝

    public User[] GetCheatUsers();//获取作弊用户列表，返回User对象数组
    public User[] GetSuspiciousUsers();//获取可疑用户列表，返回User对象数组
    public int UnbanUser(String userid);//解封用户，返回0表示成功，1表示用户不存在，2表示用户未被封禁
    public int BanUser(String userid);//封禁用户，返回0表示成功，1表示用户不存在，2表示用户已被封禁

    public int SubmitAppeal(String userid, String content, File file); // 添加新的申诉，返回0表示成功，1表示用户不存在，2表示内容不合法

}
