package vip;
import java.io.File;

import org.apache.commons.lang3.tuple.Pair;

import entity.Seed;
public interface VipInterface{
    
    public Pair<File,Integer>  GetTTorent(String seedid,String userid,String ip);//获取专线下载的文件，并记录用户的下载行为
    //如果用户权限足够，文件存在，integer为0，如果用户权限足够，文件不存在，integer为1，file为null 如果用户权限不足，file为null，integer为2；

    public int AddFarmerNumber(int number,String seedid);//种子增加了新的保种人数,返回值：0，写入成功，1写入失败,其他待定
    public int ReduceFarmerNumber(int number,String seedid);//种子降低的保种人数，返回值:0,写入成功，1:写入失败,其他待定
    public int KeepSeed(String seedid);//将种子加入保种列表
    public int RemoveSeed(String seedid);//将种子移除保种列表

    public void CheckSeed();//由外部触发，调用类内函数更新保种列表
    public Seed[] GetSeedToPlant();//获取当前需要保种的所有种子信息
}