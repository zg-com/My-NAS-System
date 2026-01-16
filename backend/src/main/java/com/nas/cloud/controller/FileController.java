package com.nas.cloud.controller;

import com.nas.cloud.entity.UserFile;
import com.nas.cloud.repository.UserFileRepository;
import com.nas.cloud.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController//告诉Spring我是负责Web接口的
@CrossOrigin(origins = "*") //允许任何前端访问我
public class FileController {

    //注入服务,就是前面咱写的那个服务,准备可以调用上传服务
    @Autowired
    private FileService fileService;
    //定义接口以及路径以及方法
    @PostMapping("/upload")//这个RequestParam中的这个file就是与前端中的那个组件的那个名字一样,不一样就收不到文件
    public String uploadFile(@RequestParam("file")MultipartFile file,@RequestParam("userId") Long userId) {


        //要注意异常处理,因为文件的上传有可能失败,也要给出上传成功与失败的提示
        try{
            //让服务层干活
            UserFile savedFile = fileService.upload(file,userId);
            //返回成功信息
            return "上传成功:" + savedFile.getId();
        }catch (IOException e) {//捕捉错误
            return "上传错误:" + e.getMessage();
        }
    }

    //根据id获取文件
    @GetMapping("/file/{id}")//注意使用@PathVariable 注解，让此处的id自动等于{id}中的id的值
    //注意虽然咱们使用void没有返回值，但是文件的回应是通过HttpServletResponse response进行返回的
    //注意此处也是IO，也有可能出现错误，注意要丢给IOException
    public void getFile(@PathVariable Long id , HttpServletResponse response) throws IOException {
        //1、从数据库查找这个信息
        UserFile userfile = fileService.getFileById(id);
        //2、没有这个id就报错，或者已经找到id对应的路径了，但是路径里面没有照片，也要进行报错
        if(userfile == null) {
            response.setStatus(404);
            return;
        }
        File file = new File(userfile.getFilePath());
        if(!file.exists()){
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
        while((bytesRead = in.read(buffer)) != -1) {
            //讲读到内存中的bytesRead个字节，写道输出流里；
            out.write(buffer, 0, bytesRead);
        }
        //6、关闭通道
        in.close();
        out.close();
    }

    //批量返回
    @GetMapping("/list")
    public List<UserFile> list(@RequestParam("userId") Long userId){
        //调用service拿数据
        List<UserFile> file = fileService.getFileList(userId);
        //直接返回List对象，Spring Boot会自动把它转换成JSON格式的字符串给浏览器
        return file;
    }

    //删除文件
    @DeleteMapping("/file/{id}")
    public String deleteFile(@PathVariable Long id,@RequestParam("userId") Long userId){
        try{
            fileService.deleteFile(id,userId);
            return "删除成功";
        }catch(RuntimeException e){
            return "删除失败" + e.getMessage();
        }
    }

    //下载文件
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id,@RequestParam("userId") Long userId,HttpServletResponse response) throws IOException {
        UserFile userFile = fileService.getFileById(id);

        if(userFile == null){
            response.setStatus(404);
            return;
        }
        if(!userFile.getUserId().equals(userId)){
            throw new RuntimeException("无权访问");//用户判断
        }
        File file = new File(userFile.getFilePath());
        if(!file.exists()){
            response.setStatus(404);
            return;
        }

        //设置强制下载的响应头
            //要处理一下中文名的乱码，如果文件名是中文不编码发给浏览器
            //就会变成？？？乱码
        String encodedFilename = URLEncoder.encode(userFile.getFilename(), StandardCharsets.UTF_8.toString());
        //然后空格的话编码后会变成+ 所以要替换为浏览器认识的%20，兼容性更好
        encodedFilename = encodedFilename.replaceAll("\\+","%20");
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
        while((bytesRead = in.read(buffer)) != -1){
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
    }
}
