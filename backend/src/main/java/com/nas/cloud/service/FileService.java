/*service这个包里放的就是服务层,负责干脏活累活,controller只负责接待*/
package com.nas.cloud.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.nas.cloud.entity.UserFile;
import com.nas.cloud.repository.UserFileRepository;
import com.nas.cloud.util.ImageUtils;
import com.nas.cloud.util.VideoUtils;
import jakarta.security.auth.message.callback.PrivateKeyCallback;
import jdk.jfr.ContentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@Service //告诉Spring我是干活的
public class FileService {

    @Autowired //依赖注入：借用工具，借用这个UserFileRepository工具，因为FileSever自己不会SQL
    private UserFileRepository userFileRepository;
    //读取配置文件，读取application.properties中咱们设置的属性值
    @Value("${nas.upload.path}")
    private String uploadPath;

    //定义上传文件的方法，不要忘了可能发生错误，发生错误要丢给IOException处理
    public UserFile upload(@NotNull MultipartFile file, Long userId,String md5FromClient) throws IOException {
        //MultipartFile是Spring专门用来封装上传文件的对象的,里面包含了文件的所有数据
        String fileMd5;
        if(md5FromClient != null && !md5FromClient.isEmpty()){
            fileMd5 = md5FromClient;
        }else{
            //计算文件的MD5指纹
            fileMd5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            System.out.println("MD5计算完成");
        }


        /*---------------------逻辑去重--------------------------------------*/
        UserFile selfFile = userFileRepository.findByUserIdAndMd5(userId,fileMd5);
        if(selfFile != null){
            System.out.println("用户已拥有该文件，跳过上传: " + file.getOriginalFilename());
            return selfFile;
        }
        /*---------------------秒传与查重（物理）--------------------------------------*/

        //查一下有没有人传过
        UserFile existingFile = userFileRepository.findFirstByMd5(fileMd5);
        if (existingFile != null ) {//传过了！
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

            if(file.getContentType().startsWith("image/")){
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
            }

            //记下md5
            userFile.setMD5(fileMd5);
            return userFileRepository.save(userFile);
        }
        //执行到这里说明真没上传过这个文件
        if(file == null || file.isEmpty()){
            throw new IOException("服务器无此文件，必须上传实体文件！");
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

        //自动创建文件夹
        if (!originalDir.exists()) originalDir.mkdirs();


        //生成文件名,使用UUID 防止重名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        //设定三个文件的目标路径
        File physicalFile = new File(originalDir, newFileName);//原图

        //保存原图
        file.transferTo(physicalFile);

        //准备数据库部分的记录
        //创建数据库的实体类的对象
        UserFile userfile = new UserFile();
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
        //写入文件指纹
        userfile.setMD5(fileMd5);
        if(type.startsWith("image/")){
            userfile.setIsImage(true);
        }else if(type.startsWith("video/")){
            userfile.setIsVideo(true);
        }else if(originalFilename.toLowerCase().matches(".*\\.(cr2|nef|arw|dng|cr3|raf|rw2|pef|3fr)$")){
            userfile.setIsRawImg(true);
        }
        //先保存一个半成品，此时的缩略图路径都是空的
        UserFile savedFile = userFileRepository.save(userfile);
        //开启异步任务，去后台生成缩略图，这里瞬间返回，主线程会直接往下走
        CompletableFuture.runAsync(()->{
            try{
                processUserFileAsync(physicalFile,savedFile);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("后台处理图片失败:" + e.getMessage());
            }
        });

        //保存该实体类对象到数据库中
        return savedFile;
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

    //检查文件指纹是否存在
    public UserFile getFileByMd5(String md5){
        return userFileRepository.findFirstByMd5(md5);
    }

    //后台生成缩略图方法
    private void processUserFileAsync(File physicalFile,UserFile userFile){
        System.out.println("开始后台处理文件：" + userFile.getFilename());
        String contentType = userFile.getType();
        //创建路径
        File thumbDir = new File(uploadPath + File.separator + "thumbnail");
        File previewDir = new File(uploadPath + File.separator + "preview");
        if (!thumbDir.exists()) thumbDir.mkdirs();
        if (!previewDir.exists()) previewDir.mkdirs();

        //定义缩略图文件名
        String fileName = physicalFile.getName();
        File thumbFile = new File(thumbDir,"thumbmin-"+fileName);
        File previewFile = new File(previewDir,"thumbpre-" + fileName);

        boolean needUpdate = false;//标记是否需要更新数据库

        try{
            if(contentType != null && contentType.startsWith("image/")){
                userFile.setIsImage(true);
                //生成小图
                ImageUtils.generateThumbnail(physicalFile, thumbFile,400,400);
                userFile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                //生成大图
                ImageUtils.generateThumbnail(physicalFile,previewFile,1920,1920);
                userFile.setThumbnailPrePath(previewFile.getAbsolutePath());
                //提取Exif
                ImageUtils.extractExif(physicalFile,userFile);
                needUpdate = true;
            } else if (contentType != null && contentType.startsWith("video/")) {
                userFile.setIsVideo(true);
                VideoUtils.generateVideoThumbnail(physicalFile, thumbFile);
                userFile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                userFile.setThumbnailPrePath(thumbFile.getAbsolutePath());
                needUpdate = true;
            } else if (userFile.getFilename().toLowerCase().matches(".*\\.(cr2|nef|arw|dng|cr3|raf|rw2|pef|3fr)$")) {
                userFile.setIsRawImg(true);
                VideoUtils.generateVideoThumbnail(physicalFile, thumbFile);
                userFile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                userFile.setThumbnailPrePath(thumbFile.getAbsolutePath());
            }
            if(needUpdate){
                userFileRepository.save(userFile);
                System.out.println("后台处理完成，数据库已更新:" + userFile.getFilename());
            }

        } catch (IOException e) {
            System.out.println("异步处理出错: " + e.getMessage());
        } catch (ImageProcessingException e) {
            System.out.println("异步处理出错: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("异步处理出错: " + e.getMessage());
        }

    }


}
