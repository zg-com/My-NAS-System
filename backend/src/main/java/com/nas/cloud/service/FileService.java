/*service这个包里放的就是服务层,负责干脏活累活,controller只负责接待*/
package com.nas.cloud.service;

import com.nas.cloud.entity.UserFile;
import com.nas.cloud.repository.UserFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service //告诉Spring我是干活的
public class FileService {

    @Autowired //依赖注入：借用工具，借用这个UserFileRepository工具，因为FileSever自己不会SQL
    private UserFileRepository userFileRepository;
    //读取配置文件，读取application.properties中咱们设置的属性值
    @Value("${nas.upload.path}")
    private String uploadPath;

    //定义上传文件的方法，不要忘了可能发生错误，发生错误要丢给IOException处理
    public UserFile upload(MultipartFile file,Long userId) throws IOException {//MultipartFile是Spring专门用来封装上传文件的对象的,里面包含了文件的所有数据
        //业务逻辑开始
        //获取文件名,原始文件名,就是上传的那个文件的名字
        String originalFilename = file.getOriginalFilename();
        //获取文件类型
        String type = file.getContentType();
        //获取文件大小
        Long size = file.getSize();
        //生成唯一文件名 给电脑看的,当作id不要重复
        String uuid = UUID.randomUUID().toString();
        //提取后缀名,读取原文件名最后一个.后的内容
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        //拼凑新文件名
        String storageFileName = uuid + fileExtension;
        //准备父文件夹
        File storageDir = new File(uploadPath);
        if(!storageDir.exists()){//如果这个父文件夹不存在,就创建这个文件夹
            storageDir.mkdir();
        }
        //准备目标文件对象
        File targetFile = new File(storageDir,storageFileName);
        //写入这个目标文件对象到硬盘里面
        file.transferTo(targetFile);
        //准备数据库部分的记录
        //创建数据库的实体类的对象
        UserFile userfile = new UserFile();
        //名字
        userfile.setFilename(originalFilename);
        //类型
        userfile.setType(type);
        //大小
        userfile.setSize(size);
        //路径
        userfile.setFilePath(targetFile.getAbsolutePath());
        //上传时间
        userfile.setUploadTime(new Date());
        //上传用户
        userfile.setUserId(userId);

        //保存该实体类对象到数据库中
        return userFileRepository.save(userfile);
    }

    //根据ID获取文件信息
    public UserFile getFileById(Long id){
        return userFileRepository.findById(id).orElse(null);
    }

    //查询图片所有列表
    public List<UserFile> getFileList(Long userId){
        //创建排序规则
            //Sort.by(。。。)：创建一个排序规则
            //Direction.DESC：降序（Descend），从大到小，最新的时间排到最前面
        Sort sort = Sort.by(Sort.Direction.DESC,"uploadTime");
        //按照排序规则返回,findALL(sort),这个是JpaRepository给的方法，待排序功能的查询
        return userFileRepository.findByUserId(userId,sort);
    }

    //删除图片
    public void deleteFile(Long fileId,Long userId) {
        //先查文件存不存在(此处文件应该是数据库中存到那条数据)
        UserFile file = userFileRepository.findById(fileId).orElse(null);//通过orElse安全地返回null
            //不存在就输出不存在
        if(file == null){
            throw new RuntimeException("文件不存在");
        }
            //或者文件不属于该用户，则进行拦截
        if(!file.getUserId().equals(userId)){
            throw new RuntimeException("无权删除文件");
        }
        //先删除物理文件
        File physicalFile = new File(file.getFilePath()); //找到物理文件

        boolean finishDelete = physicalFile.delete();//注意要看一下是不是真的删除成功了，有的时候文件被占用就没有办法进行删除了
        if(physicalFile.exists()){

            if(!finishDelete){
                throw new RuntimeException("警告，硬盘文件删除失败，路径：" + file.getFilePath());
            }
        }
        //删除之后再删除数据库文件
        userFileRepository.delete(file);
    }
}
