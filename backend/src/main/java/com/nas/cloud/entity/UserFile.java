package com.nas.cloud.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

//告诉jpa这个是实体类,让他在数据库中建个表
@Entity
@Table(name="nas_file") //定义表的名称是啥
@Data //方便lombok建立getter,setter方法
public class UserFile {

    @Id //定义主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自动递增1.2.3...
    private Long id;

    private String filename; //原始文件名
    private String type; // 文件类型
    private Long size; //文件大小(字节)
    private String filePath; //真实存储路径
    private Date uploadTime; //上传时间
    private Long userId;//这张照片属于哪个用户
    private Boolean isImage;//是否是照片（用于快速过滤）
    private LocalDateTime shootTime;//拍摄时间（区别于上传时间）
    private Integer width;//图片宽
    private Integer height;//图片高
    private String cameraModel;//拍摄设备
    private String ISO;//感光度
    private String shutterSpeed;//快门速度
    private String fNumber;//光圈大小
    private String flash;//闪光灯
    private String focalLength;//焦距
    private String exposureBias;//曝光补偿
    private String meteringMode;//测光模式
    private String whiteBalance;//白平衡
    private String software;//拍摄软件/模式
    private String thumbnailMinPath;//小缩略图路径
    private String thumbnailPrePath;//全屏预览缩略图路径
    private String md5;//文件指纹(MD5)用于去重和秒传


    //id的函数
    public Long getId(){
        return this.id;
    }
    public void setId(Long id){
        this.id = id;
    }

    //filename
    public String getFilename(){
        return this.filename;
    }
    public void setFilename(String filename){
        this.filename = filename;
    }
    //type
    public String getType(){
        return this.type;
    }
    public void setType(String type){
        this.type = type;
    }
    //size
    public Long getSize(){
        return this.size;
    }
    public void setSize(Long size){
        this.size = size;
    }
    //filePath
    public String getFilePath(){
        return this.filePath;
    }
    public void setFilePath(String filePath){
        this.filePath = filePath;
    }
    //uploadTime
    public Date getUploadTime(){
        return this.uploadTime;
    }
    public void setUploadTime(Date uploadTime){
        this.uploadTime = uploadTime;
    }
    //userId
    public Long getUserId(){
        return this.userId;
    }
    public void setUserId(Long id){
        this.userId = id;
    }
    //isImage
    public Boolean getIsImage(){
        return this.isImage;
    }
    public void setIsImage(Boolean isImage){
        this.isImage = isImage;
    }
    //shootTime
    public LocalDateTime getShootTime(){
        return this.shootTime;
    }
    public void setShootTime(LocalDateTime shootTime){
        this.shootTime = shootTime;
    }
    //width
    public Integer getWidth(){
        return this.width;
    }
    public void setWidth(Integer width){
        this.width = width;
    }
    //height
    public Integer getHeight(){
        return this.height;
    }
    public void setHeight(Integer height){
        this.height = height;
    }
    //cameraModel
    public String getCameraModel(){
        return this.cameraModel;
    }
    public void setCameraModel(String cameraModel){
        this.cameraModel = cameraModel;
    }
    //ISO
    public String getISO(){
        return this.ISO;
    }
    public void setISO(String ISO){
        this.ISO = ISO;
    }
    //shutterSpeed
    public String getShutterSpeed(){
        return this.shutterSpeed;
    }
    public void setShutterSpeed(String shutterSpeed){
        this.shutterSpeed = shutterSpeed;
    }
    //fNumber
    public String getFNumber(){
        return this.fNumber;
    }
    public void setFNumber(String fNumber){
        this.fNumber = fNumber;
    }
    //flash
    public String getFlash(){
        return this.flash;
    }
    public void setFlash(String flash){
        this.flash = flash;
    }
    //focalLength
    public String getFocalLength(){
        return this.focalLength;
    }
    public void setFocalLength(String focalLength){
        this.focalLength = focalLength;
    }
    //exposureBias
    public String getExposureBias(){
        return this.exposureBias;
    }
    public void setExposureBias(String exposureBias){
        this.exposureBias = exposureBias;
    }
    //meteringMode
    public String getMeteringMode(){
        return this.meteringMode;
    }
    public void setMeteringMode(String meteringMode){
        this.meteringMode = meteringMode;
    }
    //whiteBalance
    public String getWhiteBalance(){
        return this.whiteBalance;
    }
    public void setWhiteBalance(String whiteBalance){
        this.whiteBalance = whiteBalance;
    }
    //software
    public String getSoftware(){
        return this.software;
    }
    public void setSoftware(String software){
        this.software = software;
    }
    //thumbnailMinPath
    public String getThumbnailMinPath(){
        return this.thumbnailMinPath;
    }
    public void setThumbnailMinPath(String thumbnailMinPath){
        this.thumbnailMinPath = thumbnailMinPath;
    }
    //thumbnailPrePath
    public String getThumbnailPrePath(){
        return this.thumbnailPrePath;
    }
    public void setThumbnailPrePath(String thumbnailPrePath){
        this.thumbnailPrePath =thumbnailPrePath;
    }
    //md5
    public String setMD5(){
        return this.md5;
    }
    public void setMD5(String md5){
        this.md5 = md5;
    }
}
