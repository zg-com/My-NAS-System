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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@Service //告诉Spring我是干活的
public class FileService {

    @Autowired //依赖注入：借用工具，借用这个UserFileRepository工具，因为FileSever自己不会SQL
    private UserFileRepository userFileRepository;
    //读取配置文件，读取application.properties中咱们设置的属性值
    @Value("${nas.upload.path}")
    private String uploadPath;

    //注入线程池
    @Autowired
    @Qualifier("taskExecutor") //指定刚刚设置的那个叫做taskExecutor的线程池
    private Executor executor;

    //定义上传文件的方法，不要忘了可能发生错误，发生错误要丢给IOException处理
    public UserFile upload(@NotNull MultipartFile file, Long userId, String md5FromClient, Long parentId) throws IOException {
        //MultipartFile是Spring专门用来封装上传文件的对象的,里面包含了文件的所有数据
        String fileMd5;
        if (md5FromClient != null && !md5FromClient.isEmpty()) {
            fileMd5 = md5FromClient;
        } else {
            //计算文件的MD5指纹
            fileMd5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            System.out.println("MD5计算完成");
        }


        /*---------------------逻辑去重--------------------------------------*/
        UserFile selfFile = userFileRepository.findByUserIdAndMd5(userId, fileMd5);
        if (selfFile != null) {
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

            if (file.getContentType().startsWith("image/")) {
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
        if (file == null || file.isEmpty()) {
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
        if (type.startsWith("image/")) {
            userfile.setIsImage(true);
        } else if (type.startsWith("video/")) {
            userfile.setIsVideo(true);
        } else if (originalFilename.toLowerCase().matches(".*\\.(cr2|nef|arw|dng|cr3|raf|rw2|pef|3fr)$")) {
            userfile.setIsRawImg(true);
        }
        //设置初始状态为0
        userfile.setStatus(0);
        //设置父文件ID
        if (parentId == null) parentId = 0L;
        userfile.setParentId(parentId);

        //先保存一个半成品，此时的缩略图路径都是空的
        UserFile savedFile = userFileRepository.save(userfile);
        //开启异步任务，去后台生成缩略图，这里瞬间返回，主线程会直接往下走
        CompletableFuture.runAsync(() -> {
            try {
                processUserFileAsync(physicalFile, savedFile);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("后台处理图片失败:" + e.getMessage());
            }
        }, executor);//使用自己创建的线程池


        //保存该实体类对象到数据库中
        return savedFile;
    }


    //根据ID获取文件信息
    public UserFile getFileById(Long id) {
        return userFileRepository.findById(id).orElse(null);
    }

    //-------文件管理专用----------------------------------
    //获取文件列表
    public List<UserFile> getFileListForManager(Long userId, Long parentId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "isFolder", "uploadTime");

        if (parentId == null) {
            parentId = 0L;
        }
        return userFileRepository.findByUserIdAndParentId(userId, parentId, sort);
    }

    //------图库专用-------------------------------------------
    //获取图片列表
    public List<UserFile> getGalleryList(Long userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "shootTime", "uploadTime");//优先按照拍摄时间排序
        return userFileRepository.findByUserIdAndIsFolderFalse(userId, sort);
    }

    //查询图片所有列表，或图片下的所有内容
    public List<UserFile> getFileList(Long userId, Long parentId) {
        //创建排序规则
        //Sort.by(。。。)：创建一个排序规则
        //Direction.DESC：降序（Descend），从大到小，最新的时间排到最前面,以及文件夹排到前面
        Sort sort = Sort.by(Sort.Direction.DESC, "isFolder", "uploadTime");

        if (parentId == null) {
            parentId = 0L;//默认是根目录
        }

        //按照排序规则返回,findALL(sort),这个是JpaRepository给的方法，待排序功能的查询
        return userFileRepository.findByUserIdAndParentId(userId, parentId, sort);
    }

    //删除图片
    public void deleteFile(Long fileId, Long userId) {
        //先查文件存不存在(此处文件应该是数据库中存到那条数据)
        UserFile file = userFileRepository.findById(fileId).orElse(null);//通过orElse安全地返回null
        //不存在就输出不存在
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        //或者文件不属于该用户，则进行拦截
        if (!file.getUserId().equals(userId)) {
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

    //----------普通删除（放入回收站）--------------------------
    public void deleteFileToRecycleBin(Long fileId, Long userId) {
        UserFile file = userFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        if (!file.getUserId().equals(userId)) throw new RuntimeException("无权删除");

        //软删除
        file.setIsDeleted(true);
        file.setDeleteTime(new Date());

        userFileRepository.save(file);
    }
    //---------还原文件从回收站----------------------
    public void restoreFile(Long fileId,Long userId){
        UserFile file = userFileRepository.findById(fileId).orElse(null);
        if(file == null || !file.getUserId().equals(userId)) return;

        file.setIsDeleted(false);
        file.setDeleteTime(null);
        userFileRepository.save(file);
    }
    //---------彻底删除（物理删除）--------------------
    public void deleteFilePhysically(Long fileId,Long userId){
        UserFile file = userFileRepository.findById(fileId).orElse(null);
        if(file == null) return;
        if(userId != null && !file.getUserId().equals(userId)) throw new RuntimeException("无权操作");

        //删除物理文件
        deletePhysicalFile(file.getFilePath(), true);
        deletePhysicalFile(file.getThumbnailMinPath(), false);
        deletePhysicalFile(file.getThumbnailPrePath(), false);

        //删除数据库
        userFileRepository.delete(file);
    }

    //检查文件指纹是否存在
    public UserFile getFileByMd5(String md5) {
        return userFileRepository.findFirstByMd5(md5);
    }

    //新建文件夹方法
    public UserFile createFolder(String folderName, Long parentId, Long userId) {
        UserFile folder = new UserFile();
        folder.setFilename(folderName);
        folder.setUserId(userId);
        folder.setParentId(parentId);
        folder.setIsFolder(true);
        folder.setIsVideo(false);
        folder.setIsImage(false);
        folder.setIsLiveImg(false);
        folder.setIsRawImg(false);
        folder.setUploadTime(new Date());
        folder.setSize(0L);
        folder.setType("dir");

        return userFileRepository.save(folder);
    }


    //辅助方法：安全删除硬盘文件
    //@param path文件路径 @param isCritical是否为关键文件，原图是关键的，必须删！缩略图删不掉可以忍
    private void deletePhysicalFile(String path, boolean isCritical) {
        //路径为空直接跳过
        if (path == null || path.isEmpty()) {
            return;
        }

        File file = new File(path);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                String msg = "警告：硬盘文件删除失败，路径：" + path;
                if (isCritical) {
                    throw new RuntimeException(msg);
                } else {
                    //如果缩略图删不掉，记录日志就可以了,不要卡用户
                    System.out.println(msg);
                }
            }
        }
    }


    //后台生成缩略图方法
    private void processUserFileAsync(File physicalFile, UserFile userFile) {
        System.out.println("开始后台处理文件：" + userFile.getFilename());
        String contentType = userFile.getType();
        //创建路径
        File thumbDir = new File(uploadPath + File.separator + "thumbnail");
        File previewDir = new File(uploadPath + File.separator + "preview");
        if (!thumbDir.exists()) thumbDir.mkdirs();
        if (!previewDir.exists()) previewDir.mkdirs();

        //定义缩略图文件名
        String baseName = physicalFile.getName().substring(0, physicalFile.getName().lastIndexOf(".")); //读取后缀前面的名字，从名字索引为0的地方读到最后一个.的位置
        File thumbFile = new File(thumbDir, "thumbmin-" + baseName + ".jpg");
        File previewFile = new File(previewDir, "thumbpre-" + baseName + ".jpg");

        boolean needUpdate = false;//标记是否需要更新数据库

        try {

            if (contentType != null && contentType.startsWith("image/")) {
                userFile.setIsImage(true);
                //生成小图
                ImageUtils.generateThumbnail(physicalFile, thumbFile, 400, 400);
                userFile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                //生成大图
                ImageUtils.generateThumbnail(physicalFile, previewFile, 1920, 1920);
                userFile.setThumbnailPrePath(previewFile.getAbsolutePath());
                //提取Exif
                ImageUtils.extractExif(physicalFile, userFile);
                userFile.setStatus(1);//标记状态
                needUpdate = true;
            } else if (contentType != null && contentType.startsWith("video/")) {
                userFile.setIsVideo(true);
                VideoUtils.generateVideoThumbnail(physicalFile, thumbFile);
                userFile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                userFile.setThumbnailPrePath(thumbFile.getAbsolutePath());
                userFile.setStatus(1);//标记状态
                needUpdate = true;
            } else if (userFile.getFilename().toLowerCase().matches(".*\\.(cr2|nef|arw|dng|cr3|raf|rw2|pef|3fr)$")) {
                userFile.setIsRawImg(true);
                VideoUtils.generateRawThumbnail(physicalFile, thumbFile);
                userFile.setThumbnailMinPath(thumbFile.getAbsolutePath());
                userFile.setThumbnailPrePath(thumbFile.getAbsolutePath());
                userFile.setStatus(1);//标记状态
                needUpdate = true;
            }
            // 如果它既不是图片，也不是视频，也不是RAW
            if (!userFile.getIsImage() && !userFile.getIsVideo() && !userFile.getIsRawImg()) {
                // 普通文件不需要生成缩略图，直接标记为成功即可
                // 如果你想做的细一点，可以根据后缀名设置 icon，比如 userFile.setType("pdf")
                // 但最重要的是：要让代码走到下面去把 status 设为 1
                System.out.println("普通文件，无需处理缩略图: " + userFile.getFilename());
                needUpdate = true;
            }

            // --- 统一收尾 ---
            // 只要没报错，就标记为 1 (正常)
            userFile.setStatus(1);
            needUpdate = true;


        } catch (IOException e) {
            System.out.println("异步处理出错: " + e.getMessage());
            userFile.setStatus(2); //标记状态
        } catch (ImageProcessingException e) {
            System.out.println("异步处理出错: " + e.getMessage());
            userFile.setStatus(2);
        } catch (InterruptedException e) {
            System.out.println("异步处理出错: " + e.getMessage());
            userFile.setStatus(2);
        }
        if (needUpdate) {
            userFileRepository.save(userFile); //将内存中已经修改参数的File，保存到数据库中
            System.out.println("后台处理完成，数据库已更新:" + userFile.getFilename());
        }

    }


}
