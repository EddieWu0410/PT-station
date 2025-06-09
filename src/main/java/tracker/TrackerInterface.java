package tracker;
import java.io.File;

import entity.TransRecord;
public interface TrackerInterface{
    public boolean AddUpLoad(String userid,int newTotalUploadForSeed,String infoHash);//给用户新增上传量，newTotalUploadForSeed为该种子的新总上传量，返回false成功，返回true失败;
    public boolean ReduceUpLoad(String userid,int upload);//给用户减上传量，返回false成功，返回true失败;
    public boolean AddDownload(String userid,int newTotalDownloadForSeed,String infoHash);//给用户增加下载量，newTotalDownloadForSeed为该种子的新总下载量，返回false成功，返回true失败;
    public boolean ReduceDownload(String userid,int newTotalDownload);//给用户减少下载量，返回false成功，返回true失败;
    public boolean AddMagic(String userid,int magic);//给用户增加魔力值，返回false成功，返回true失败;
    public boolean ReduceMagic(String userid,int magic);//给用户减少魔力值，返回false成功，返回true失败;

    public int SaveTorrent(String seedid,File TTorent);//保存seedid对应的ttorent信息
    public File GetTTorent(String seedid,String userid);//根据种子id获得ttorent信息然后构建Ttorent文件并返回,同时记录用户的下载行为
    
    public int AddRecord(TransRecord rd);//新增一个seedid对应的种子的传输任务记录
}