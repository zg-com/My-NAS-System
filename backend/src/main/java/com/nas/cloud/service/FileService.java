/*service这个包里放的就是服务层,负责干脏活累活,controller只负责接待*/
package com.nas.cloud.service;

import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.nas.cloud.entity.UserFile;
import com.nas.cloud.repository.UserFileRepository;
import com.nas.cloud.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
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
    public UserFile upload(MultipartFile file, Long userId) throws IOException {//MultipartFile是Spring专门用来封装上传文件的对象的,里面包含了文件的所有数据
        //计算文件的MD5指纹
        String fileMd5 = DigestUtils.md5DigestAsHex(file.getInputStream());
        /*---------------------逻辑去重--------------------------------------*/
        UserFile selfFile = userFileRepository.findByUserIdAndMd5(userId,fileMd5);
        if(selfFile != null){
            System.out.println("用户已拥有该文件，跳过上传: " + file.getOriginalFilename());
            return selfFile;
        }
        /*---------------------秒传与查重（物理）--------------------------------------*/

        //查一下有没有人传过
        UserFile existingFile = userFileRepository.findFirstByMd5(fileMd5);
        if (existingFile != null) {//传过了！
            System.out.println("触发秒传！文件已存在：" + existingFile.getFilename());

            //只创建新的数据库记录，不存物理文件
            UserFile userFile = new UserFile();
            userFile.setUserId(userId); // 当前用户
            userFile.setFilename(file.getOriginalFilename()); // 用当前上传的文件名
            userFile.setType(existingFile.getType()); // 复用已有类型
            userFile.setSize(existingFile.getSize()); // 复用已有大小
            userFile.setUploadTime(new Date());

            // 【关键】复用已有文件的物理路径！
            userFile.setFilePath(existingFile.getFilePath());
            userFile.setThumbnailMinPath(existingFile.getThumbnailMinPath());
            userFile.setThumbnailPrePath(existingFile.getThumbnailPrePath());
            userFile.setIsImage(existingFile.getIsImage());

            // 复用 Exif 信息 (既然是同一张图，Exif 肯定一样)
            userFile.setShootTime(existingFile.getShootTime());
            userFile.setWidth(existingFile.getWidth());
            userFile.setHeight(existingFile.getHeight());
            userFile.setCameraModel(existingFile.getCameraModel());
            userFile.setSoftware(existingFile.getSoftware());
            userFile.setISO(userFile.getISO());
            userFile.setShutterSpeed(existingFile.getShutterSpeed());
            userFile.setFNumber(existingFile.getFNumber());
            userFile.setFocalLength(existingFile.getFocalLength());
            userFile.setFlash(existingFile.getFlash());
            userFile.setExposureBias(existingFile.getExposureBias());
            userFile.setMeteringMode(existingFile.getMeteringMode());
            userFile.setWhiteBalance(existingFile.getWhiteBalance());
            //记下md5
            userFile.setMD5(fileMd5);
            return userFileRepository.save(userFile);
        }
        // --- 3. 物理上传 (新文件) ---
        System.out.println("新文件，执行物理上传...");
        /*---------------------缩略图部分与物理存储部分！--------------------------------*/
        //获取基础根目录
        String basePath = uploadPath;
        //先判断一下基础根目录后面有没有分隔符：File.separator（windows是\,Linux和MaxOs是/）这样能自动适配
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;//没有的话就自己加上
        }
        //定义三个子目录
        File originalDir = new File(basePath + "original");
        File thumbDir = new File(basePath + "thumbnail");
        File previewDir = new File(basePath + "preview");
        //自动创建文件夹
        if (!originalDir.exists()) originalDir.mkdirs();
        if (!thumbDir.exists()) thumbDir.mkdirs();
        if (!previewDir.exists()) previewDir.mkdirs();
        //生成文件名,使用UUID 防止重名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        //设定三个文件的目标路径
        File physicalFile = new File(originalDir, newFileName);//原图
        File thumbFile = new File(thumbDir, "thumbmin-" + newFileName);//小缩略图
        File previewFile = new File(previewDir, "thumbpre-" + newFileName);//大缩略图
        //保存原图
        file.transferTo(physicalFile);

        //准备数据库部分的记录
        //创建数据库的实体类的对象
        UserFile userfile = new UserFile();
        //判断一下文件是不是图片
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            userfile.setIsImage(true);

            //生成缩略图
            try {
                //小缩略图
                ImageUtils.generateThumbnail(physicalFile, thumbFile, 400, 400);
                userfile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                //预览大图
                ImageUtils.generateThumbnail(physicalFile, previewFile, 1920, 1920);
                userfile.setThumbnailPrePath(previewFile.getAbsolutePath());
                //提取Exif和宽高
                ImageUtils.extractExif(physicalFile, userfile);

            } catch (Exception e) {
                System.out.println("缩略图生成失败: " + e.getMessage());
            }
        } else {
            userfile.setIsImage(false);
        }
        //获取文件类型
        String type = file.getContentType();
        //获取文件大小
        Long size = file.getSize();
        //写入数据库
        userfile.setFilename(originalFilename);
        //类型
        userfile.setType(type);
        //大小
        userfile.setSize(size);
        //路径
        userfile.setFilePath(physicalFile.getAbsolutePath());
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
        deletePhysicalFile(file.getFilePath(), true); //找到物理文件
        //缩略图
        deletePhysicalFile(file.getThumbnailMinPath(), false);
        //预览图
        deletePhysicalFile(file.getThumbnailPrePath(), false);

        //删除之后再删除数据库文件
        userFileRepository.delete(file);
    }

    //辅助方法：安全删除硬盘文件
     //@param path文件路径 @param isCritical是否为关键文件，原图是关键的，必须删！缩略图删不掉可以忍
    private void deletePhysicalFile(String path,boolean isCritical){
        //路径为空直接跳过
        if(path == null || path.isEmpty()){
            return ;
        }

        File file = new File(path);
        if(file.exists()){
            boolean deleted = file.delete();
            if(!deleted){
                String msg = "警告：硬盘文件删除失败，路径：" + path;
                if(isCritical){
                    throw new RuntimeException(msg);
                }else{
                    //如果缩略图删不掉，记录日志就可以了,不要卡用户
                    System.out.println(msg);
                }
            }
        }
    }
}
