package com.nas.cloud.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.nas.cloud.entity.UserFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

public class ImageUtils {
    /**
     * 生成缩略图
     * @param sourceFile 原图文件
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩略图文件对象
     */
    public static File generateThumbnail(File sourceFile,File targetFile,int width,int height) throws IOException {

        Thumbnails.of(sourceFile)
                .size(width, height)
                .outputQuality(0.5f)
                .toFile(targetFile);
        return targetFile;
    }

    //解析图片Exif信息
    public static void extractExif(File file, UserFile userFile) throws ImageProcessingException, IOException {
        try{
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            //读取基础信息Exif(IFD0):设备型号、软件
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if(ifd0 != null){
                userFile.setCameraModel(ifd0.getDescription(ExifIFD0Directory.TAG_MODEL));
                userFile.setSoftware(ifd0.getDescription(ExifIFD0Directory.TAG_SOFTWARE));
            }

            //读取详细拍摄参数(SubIFD):ISO,快门，光圈等
            ExifSubIFDDirectory subIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if(subIfd != null){
                //拍摄时间
                Date date = subIfd.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if(date != null){
                    userFile.setShootTime(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
                //核心参数（直接获取字符串，库会自动加上单位）
                userFile.setISO(subIfd.getDescription(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                userFile.setShutterSpeed(subIfd.getDescription(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                userFile.setFNumber(subIfd.getDescription(ExifSubIFDDirectory.TAG_FNUMBER));
                userFile.setFocalLength(subIfd.getDescription(ExifSubIFDDirectory.TAG_FNUMBER));
                userFile.setFlash(subIfd.getDescription(ExifSubIFDDirectory.TAG_FLASH));
                userFile.setExposureBias(subIfd.getDescription(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS));
                userFile.setMeteringMode(subIfd.getDescription(ExifSubIFDDirectory.TAG_METERING_MODE));
                userFile.setWhiteBalance(subIfd.getDescription(ExifSubIFDDirectory.TAG_WHITE_BALANCE_MODE));
            }
        }catch(Exception e){
            System.out.println("Exif extraction skipped for:"+file.getName());
        }
    }
}
