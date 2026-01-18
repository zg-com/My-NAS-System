//以后用来清楚删除30天之后的图片
package com.nas.cloud.task;

import com.nas.cloud.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component //不加这个Spring完全不知道它的存在，要用的话只有new一下才行，加了之后在别的地方直接用就好，spring会自动把现成的对象传进去
public class RecycleBinTask {
    @Autowired
    private FileService fileService;

    //每天凌晨三点执行一次任务
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanUp(){
        System.out.println("执行回收站清理任务...");
    }
}
