package com.nas.cloud.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoUtils {
    /**
     * 获取视频缩略图 (截取第1秒的帧)
     * @param videoFile 源视频文件
     * @param targetFile 目标图片文件 (xxx.jpg)
     */
    //InterruptedException是线程异常，因为这个地方的功能实现是通过调用工具实现的，所以有可能被线程中断，线程出现问题后要抛出异常
    public static void generateVideoThumbnail(File videoFile, File targetFile) throws IOException,InterruptedException{
        // 构建命令: ffmpeg -i video.mp4 -ss 00:00:01 -vframes 1 -q:v 2 thumb.jpg
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(videoFile.getAbsolutePath());
        command.add("-ss");
        command.add("00:00:01"); // 截取第1秒
        command.add("-vframes");
        command.add("1"); // 只截1帧
        command.add("-q:v");
        command.add("2"); // 图片质量 (2-31, 越小越好)
        command.add(targetFile.getAbsolutePath());

        //创建命令
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);//选择将错误信息打印出来

        Process process = builder.start();//创建命令执行的代理，能通过process控制进程的进行
        int exitCode = process.waitFor();//读取执行完毕后的错误码，是0的话就是没有错误
        if(exitCode != 0){
            throw new IOException("FFmpeg 执行失败，错误码: " + exitCode);
        }
    }

    //对Raw格式图片进行缩略图生成
    public static void generateRawThumbnail(File rawFile, File targetFile) throws InterruptedException, IOException {
        // 构造 Linux 管道命令:
        // exiftool -b -PreviewImage "source.cr3" | convert - -resize 800x800 "target.jpg"
        // 解释：
        // 1. exiftool -b -PreviewImage: 以二进制(-b)提取预览图(PreviewImage)
        // 2. | : 管道，把提取出来的JPG数据直接传给下一个命令
        // 3. convert - : 那个减号(-)表示从标准输入(stdin)读取图片数据

        String commandStr = String.format(
                "exiftool -b -PreviewImage \"%s\" | convert - -resize 800x800 \"%s\"",
                rawFile.getAbsolutePath(),
                targetFile.getAbsolutePath()
        );

        // 在 Java 中执行带管道的命令，必须调用 "sh -c"
        ProcessBuilder builder = new ProcessBuilder("sh", "-c", commandStr);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        // 打印日志 (如果 ExifTool 找不到预览图或者 convert 失败，这里能看到)
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RawUtils Log]: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("RAW 转换失败 (ExifTool+IM)，错误码: " + exitCode);
        }
    }
}
