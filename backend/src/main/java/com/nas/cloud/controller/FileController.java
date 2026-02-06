package com.nas.cloud.controller;

import com.nas.cloud.entity.User;
import com.nas.cloud.entity.UserFile;
import com.nas.cloud.repository.TimelineSummary;
import com.nas.cloud.repository.UserFileRepository;
import com.nas.cloud.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.ranges.Range;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController//告诉Spring我是负责Web接口的
@CrossOrigin(origins = "*") //允许任何前端访问我
public class FileController {

    //注入服务,就是前面咱写的那个服务,准备可以调用上传服务
    @Autowired
    private FileService fileService;

    //定义接口以及路径以及方法
    @PostMapping("/upload")//这个RequestParam中的这个file就是与前端中的那个组件的那个名字一样,不一样就收不到文件
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "md5", required = false) String md5,
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId,
            @RequestParam(value = "relativePath", required = false) String relativePath,
            @RequestParam(value = "source", required = false) String source ) {

        // 定义返回结果容器
        Map<String, Object> result = new HashMap<>();

        //要注意异常处理,因为文件的上传有可能失败,也要给出上传成功与失败的提示
        try {
            // 如果来源是相册，强制修改 parentId，无视前端传来的 parentId
            if ("gallery".equals(source)) {
                // 获取专属的日期归档目录 ID
                parentId = fileService.getOrCreateGalleryFolder(userId);
            }
            //让服务层干活
            UserFile savedFile = fileService.upload(file, userId, md5, parentId, relativePath);
            //返回成功信息
            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("fileId", savedFile.getId());
        } catch (IOException e) {//捕捉错误
            result.put("code", 500);
            result.put("message", "上传错误: " + e.getMessage());
        }
        return result;
    }

    //根据id获取文件
    @GetMapping("/file/{id}")//注意使用@PathVariable 注解，让此处的id自动等于{id}中的id的值
    //注意虽然咱们使用void没有返回值，但是文件的回应是通过HttpServletResponse response进行返回的
    //注意此处也是IO，也有可能出现错误，注意要丢给IOException
    public void getFile(@PathVariable Long id, HttpServletResponse response, @RequestParam("userId") Long userId) throws IOException {
        //1、从数据库查找这个信息
        UserFile userfile = fileService.getFileById(id);
        if (!userfile.getUserId().equals(userId)) throw new RuntimeException("无权访问");
        //2、没有这个id就报错，或者已经找到id对应的路径了，但是路径里面没有照片，也要进行报错
        if (userfile == null) {
            response.setStatus(404);
            return;
        }
        File file = new File(userfile.getFilePath());
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }
        //3、设置响应头，告诉浏览器这个是什么类型的文件，方便浏览器做决定，是直接渲染，还是直接下载
        response.setContentType(userfile.getType());
        //4、建立连接管道
        //输入流，从硬盘读取数据到内存
        FileInputStream in = new FileInputStream(file);
        //输出流，从内存写数据到浏览器
        OutputStream out = response.getOutputStream();
        //5、开始搬运数据，每次搬运 4096 字节（4k）
        byte[] buffer = new byte[4096];
        int bytesRead;
        //循环读取，把数据读到buffer数组里，返回读取的字节数，如果返回为-1，就说明读完了
        while ((bytesRead = in.read(buffer)) != -1) {
            //讲读到内存中的bytesRead个字节，写道输出流里；
            out.write(buffer, 0, bytesRead);
        }
        //6、关闭通道
        in.close();
        out.close();
    }

    //批量返回
    @GetMapping("/list")
    public List<UserFile> list(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId) {
        //调用service拿数据
        List<UserFile> file = fileService.getFileList(userId, parentId);
        //直接返回List对象，Spring Boot会自动把它转换成JSON格式的字符串给浏览器
        return file;
    }

    //---------文件管理的文件列表获取---------------------
    @GetMapping("/file/list")
    public List<UserFile> getFileList(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId) {
        return fileService.getFileListForManager(userId, parentId);
    }

    //------相册专用（获取所有照片流）--------------------------------------------
    @GetMapping("/gallery/list")
    public List<UserFile> getGalleryList(@RequestParam("userId") Long userId) {
        return fileService.getGalleryList(userId);
    }

    //软删除文件
    @DeleteMapping("/file/{id}")
    public String deleteFile(@PathVariable Long id, @RequestParam("userId") Long userId) {
        try {
            fileService.deleteFileToRecycleBin(id, userId);
            return "删除成功";
        } catch (RuntimeException e) {
            return "删除失败" + e.getMessage();
        }
    }

    //还原软删除文件
    @DeleteMapping("/file/restore/{id}")
    public String restoreDeleteFile(@PathVariable Long id, @RequestParam("userId") Long userId) {
        try {
            fileService.restoreFile(id, userId);
            return "还原成功";
        } catch (RuntimeException e) {
            return "还原失败" + e.getMessage();
        }
    }

    //彻底删除物理文件
    @DeleteMapping("/file/physical/{id}")
    public String deleteFilePhysically(@PathVariable Long id, @RequestParam("userId") Long userId) {
        try {
            fileService.deleteFilePhysically(id, userId);
            return "彻底删除成功";
        } catch (RuntimeException e) {
            return "彻底删除失败" + e.getMessage();
        }
    }


    //下载文件
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, @RequestParam("userId") Long userId, HttpServletResponse response) throws IOException {
        UserFile userFile = fileService.getFileById(id);
        System.out.println("========== 下载接口被调用了！ID: " + id + " ==========");
        if (userFile == null) {
            response.setStatus(404);
            return;
        }
        if (!userFile.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问");//用户判断
        }
        File file = new File(userFile.getFilePath());
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }

        //设置强制下载的响应头
        //要处理一下中文名的乱码，如果文件名是中文不编码发给浏览器
        //就会变成？？？乱码
        String encodedFilename = URLEncoder.encode(userFile.getFilename(), StandardCharsets.UTF_8.toString());
        //然后空格的话编码后会变成+ 所以要替换为浏览器认识的%20，兼容性更好
        encodedFilename = encodedFilename.replaceAll("\\+", "%20");
        //设置响应头
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");

        //设置文件类型
        // application/octet-stream 是最通用的二进制流，告诉浏览器这是一坨数据，只管下载就行了
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(userFile.getSize());//告诉浏览器这个文件多大，这样下载才有进度条

        //搬运数据
        FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
    }

    //获取预览图
    @GetMapping("/file/img/preview/{id}")
    public void getPreview(@PathVariable Long id, HttpServletResponse response, @RequestParam("userId") Long userId) throws IOException {
        UserFile userFile = fileService.getFileById(id);
        if (!userFile.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问");
        }
        File file = null;
        if (userFile.getThumbnailPrePath() != null) {
            file = new File(userFile.getThumbnailPrePath());
        }
        // 只有当文件确实存在，且是一个图片文件时，才允许回退。
        // 如果是视频，且缩略图不存在，千万别返回原视频！宁愿返回 404 让前端显示默认图标。
        if (file == null || !file.exists()) {
            if (Boolean.TRUE.equals(userFile.getIsVideo())) {
                // 如果是视频且没缩略图，直接结束，不要返回原文件
                // 或者你可以读取一个项目里的默认 "video_icon.png" 返回
                System.out.println("视频缩略图缺失: " + userFile.getFilename());
                response.setStatus(404);
                return;
            }
            // 如果是图片，可以回退到原图（虽然慢点，但能显示）
            file = new File(userFile.getFilePath());
        }

        //设置强缓存，让浏览器缓存到本地
        response.setHeader("Cache-Control", "public,max-age=2592000");//2592000是告诉浏览器30天这图不会变，别来问我了，直接读缓存
        response.setContentType(userFile.getType());
        writeFileToResponse(file, response);
    }

    //获取微缩略图
    @GetMapping("/file/img/thumbnail/{id}")
    public void getThumbnail(@PathVariable Long id, HttpServletResponse response, @RequestParam("userId") Long userId) throws IOException {
        UserFile userFile = fileService.getFileById(id);
        if (!userFile.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问");
        }
        File file = null;
        if (userFile.getThumbnailMinPath() != null) {
            file = new File(userFile.getThumbnailMinPath());
        }
        // 只有当文件确实存在，且是一个图片文件时，才允许回退。
        // 如果是视频，且缩略图不存在，千万别返回原视频！宁愿返回 404 让前端显示默认图标。
        if (file == null || !file.exists()) {
            if (Boolean.TRUE.equals(userFile.getIsVideo())) {
                // 如果是视频且没缩略图，直接结束，不要返回原文件
                // 或者你可以读取一个项目里的默认 "video_icon.png" 返回
                System.out.println("视频缩略图缺失: " + userFile.getFilename());
                response.setStatus(404);
                return;
            }
            // 如果是图片，可以回退到原图（虽然慢点，但能显示）
            file = new File(userFile.getFilePath());
        }

        //设置强缓存，让浏览器缓存到本地
        response.setHeader("Cache-Control", "public,max-age=2592000");//2592000是告诉浏览器30天这图不会变，别来问我了，直接读缓存
        response.setContentType(userFile.getType());
        writeFileToResponse(file, response);
    }

    //图片输出到浏览器的方法
    private void writeFileToResponse(File file, HttpServletResponse response) throws IOException {
        FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
    }

    //校验文件MD5是否已经存在
    @GetMapping("/file/check-md5")
    public String checkMd5(@RequestParam("md5") String md5) {
        UserFile file = fileService.getFileByMd5(md5);
        if (file == null) {
            return "404";//前端收到404 就知道该上传了
        } else {
            return "200";//前端收到200 就知道不用上传了 {"code":200,"data":true}
        }
    }

    //创建文件夹接口
    @PostMapping("/folder/new")
    public String newFolder(
            @RequestParam("name") String name,
            @RequestParam("parentId") Long parentId,
            @RequestParam("userId") Long userId
    ) {
        fileService.createFolder(name, parentId, userId);
        return "创建成功";
    }

    //获取光轴数据（图片哪年哪月有多少张）
    @GetMapping("/gallery/timeline-summary")
    public List<TimelineSummary> getTimeLineSummary(@RequestParam("userId") Long userId) {
        return fileService.getTimeLineSummary(userId);
    }

    //获取原图数据
    @GetMapping("/file/img/original/{id}")
    public void getOriginal(@PathVariable Long id, HttpServletResponse response, @RequestParam("userId") Long userId) throws IOException {
        UserFile userFile = fileService.getFileById(id);
        if (!userFile.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问");
        }
        File file = null;
        if (userFile.getFilePath() != null) {
            file = new File(userFile.getFilePath());
        }

        response.setHeader("Cache-Control", "public,max-age = 86400");
        response.setContentType((userFile.getType()));
        writeFileToResponse(file, response);
    }

    //获取回收站文件列表
    @GetMapping("file/recycle")
    public List<UserFile> getRecycleToBinList(@RequestParam("userId") Long userId) {
        return fileService.getRecycleBinList(userId);
    }

    // FileController.java 中

    // ----------------- 视频流式播放专用接口 -----------------
    @GetMapping("/file/video/{id}")
    public void streamVideo(@PathVariable Long id,
                            @RequestParam("userId") Long userId,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        // 1. 打印日志，确认请求是否到达 Controller (排查 Token 拦截问题)
        System.out.println(">>> 收到视频播放请求: FileId=" + id + ", UserId=" + userId);

        UserFile userFile = fileService.getFileById(id);

        // 2. 权限检查
        if (userFile == null || !userFile.getUserId().equals(userId)) {
            System.out.println("❌ 视频播放权限验证失败");
            response.setStatus(404); // 为了安全，权限不足也报 404
            return;
        }

        File file = new File(userFile.getFilePath());
        if (!file.exists()) {
            System.out.println("❌ 物理文件不存在: " + userFile.getFilePath());
            response.setStatus(404);
            return;
        }

        // 3. 核心逻辑
        long fileLength = file.length();
        String rangeString = request.getHeader("Range");
        long rangeStart = 0;
        long rangeEnd = fileLength - 1;

        // 解析 Range 头
        if (rangeString != null && rangeString.startsWith("bytes=")) {
            rangeString = rangeString.substring(6);
            int dashIndex = rangeString.indexOf('-');

            if (dashIndex >= 0) { // 必须确保找到了 '-'
                String startStr = rangeString.substring(0, dashIndex);
                if (!startStr.isEmpty()) {
                    rangeStart = Long.parseLong(startStr);
                }

                String endStr = rangeString.substring(dashIndex + 1);
                if (!endStr.isEmpty()) {
                    rangeEnd = Long.parseLong(endStr);
                }
            }
        }

        long contentLength = rangeEnd - rangeStart + 1;

        // 4. ✨【关键修复】强制设置 Content-Type 为 video/mp4
        // 浏览器非常挑剔，如果数据库存的是 "application/octet-stream"，浏览器会拒绝播放
        response.setContentType("video/mp4");

        response.setHeader("Accept-Ranges", "bytes");
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206
        response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        response.setHeader("Content-Length", String.valueOf(contentLength));

        // 5. 传输数据
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
             OutputStream outputStream = response.getOutputStream()) {

            randomAccessFile.seek(rangeStart);
            byte[] buffer = new byte[1024 * 64]; // 64KB Buffer
            long bytesToRead = contentLength;

            while (bytesToRead > 0) {
                int len = randomAccessFile.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead));
                if (len == -1) break;
                outputStream.write(buffer, 0, len);
                bytesToRead -= len;
            }
            outputStream.flush();
        } catch (IOException e) {
            // 客户端中断连接是正常的 (比如拖动进度条，或者关闭窗口)
            // System.out.println("视频流传输中断 (正常现象)");
        }
    }
}
