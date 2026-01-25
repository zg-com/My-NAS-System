package com.nas.cloud.entity;

import jakarta.persistence.*;
import jdk.jshell.Snippet;
import lombok.Data;
import org.springframework.cglib.core.Block;

import java.time.LocalDateTime;
import java.util.Date;

//告诉jpa这个是实体类,让他在数据库中建个表
@Entity
@Table(name="nas_file",indexes = {
        @Index(name = "idx_user_shoot_time",columnList = "userId, shootTime"),//优化时间查询
        @Index(name = "idx_md5",columnList = "md5"),//优化秒传查询
        @Index(name = "idx_address",columnList = "locationAddress")//优化地点查询
}) //定义表的名称是啥，并且优化了索引
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
    private Boolean isImage=false;//是否是照片（用于快速过滤）
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
    private Boolean isVideo=false;//是否为视频
    private Boolean isRawImg=false;//是否为RAW格式照片
    private Boolean isLiveImg=false;//是否为实况图片
    private Integer status;//定义图片当前状态  0:缩略图正在处理中，1：正常，2：失败
    private Boolean isFolder = false; //定义是不是文件夹
    private Long relatedId;//关联文件id，主要用于LivePhoto，如果是jpg这个id对应mov，如果是mov，这个就对应jpg的id
    private Integer specialFlag;// 0:普通，1：LivePhoto主图，2=Livephoto视频，3=RAW原片



    // --- 1. 相册与文件管理 ---

    /**
     * 父文件夹ID / 相册ID
     * null 或 0 代表根目录
     * 以后做“多层级文件夹”或“自定义相册”时，就是通过这个字段关联
     */
    private Long parentId = 0L;

    // --- 2. 用户交互 ---
    /**
     * 是否收藏 (喜欢)
     * true = 在“我的收藏”里显示
     */
    private Boolean isFavorite = false;

    /**
     * 是否隐藏
     * true = 需要密码才能查看，不在主时间轴显示
     */
    private Boolean isHidden = false;

    // --- 3. 地图相册 (地理位置) ---
    /**
     * 经度 (从 Exif 提取)
     */
    private Double longitude;

    /**
     * 纬度 (从 Exif 提取)
     */
    private Double latitude;

    /**
     * 地点描述 (逆地理编码结果)
     * 例如："北京市海淀区中关村"
     * 以后上传时，后台调个地图API把经纬度转成文字存进去，方便搜"海淀"能搜出来
     */
    private String locationAddress;

    // --- 4. AI 与 搜索 (核心预埋) ---
    /**
     * AI 识别出的标签 (JSON字符串 或 逗号分隔)
     * 例如："dog,food,outdoor,sky"
     * 搜索时直接 SELECT * FROM table WHERE ai_tags LIKE '%dog%'
     */
    @Column(length = 1000) // 设长一点
    private String aiTags;

    /**
     * 图片中的文字 (OCR 结果)
     * 用于模糊搜索图片里的字
     */
    @Column(length = 2000) // 设长一点
    private String ocrContent;

    /**
     * 人脸分组 ID 列表
     * 例如："face_102,face_88"
     * 表示这张图里有这两个人
     */
    private String faceIds;

    // ... 之前添加的 isFavorite, isHidden 等 ...

    // --- 5. 回收站 (最近删除) ---



    /**
     * 是否已删除 (逻辑删除/软删除)
     * true = 在回收站里
     * false = 正常显示
     */
    private Boolean isDeleted = false;

    /**
     * 删除时间
     * 用于计算“还剩多少天自动清除”，以及定时任务判断是否超过30天
     */
    private Date deleteTime;

    // --- 6. 相册封面 ---
    /**
     * 相册封面图片的ID
     * 只有当 isFolder = true (是文件夹/相册) 时这个字段才有意义
     * 用户自定义封面时，就把那张图的 ID 填进来
     */
    private Long coverId;

    // --- 7. 存储优化 (只留预览图) ---
    /**
     * 原始文件是否已清理
     * true = 原图已删，只剩缩略图 (点击原图时不给看，或者提示已清理)
     * 配合 filePath = null 使用
     */
    private Boolean isOriginalDeleted = false;


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
    public String getMD5(){
        return this.md5;
    }
    public void setMD5(String md5){
        this.md5 = md5;
    }
    //isVideo
    public Boolean getIsVideo(){
        return this.isVideo;
    }
    public void setIsVideo(Boolean isVideo){
        this.isVideo = isVideo;
    }
    //isRawImg
    public Boolean getIsRawImg(){
        return  this.isRawImg;
    }
    public void setIsRawImg(Boolean isRawImg){
        this.isRawImg = isRawImg;
    }
    //isLiveImg
    public Boolean getIsLiveImg(){
        return this.isLiveImg;
    }
    public void setIsLiveImg(Boolean isLiveImg){
        this.isLiveImg = isLiveImg;
    }
    //status
    public Integer getStatus(){
        return this.status;
    }
    public void setStatus(Integer status){
        this.status = status;
    }


    //--------------后续维护-------------------------------------------------------
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getFaceIds() {
        return faceIds;
    }

    public void setFaceIds(String faceIds) {
        this.faceIds = faceIds;
    }

    public String getOcrContent() {
        return ocrContent;
    }

    public void setOcrContent(String ocrContent) {
        this.ocrContent = ocrContent;
    }

    public String getAiTags() {
        return aiTags;
    }

    public void setAiTags(String aiTags) {
        this.aiTags = aiTags;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getHidden() {
        return isHidden;
    }

    public void setHidden(Boolean hidden) {
        isHidden = hidden;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }
    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Long getCoverId() {
        return coverId;
    }

    public void setCoverId(Long coverId) {
        this.coverId = coverId;
    }

    public Boolean getOriginalDeleted() {
        return isOriginalDeleted;
    }

    public void setOriginalDeleted(Boolean originalDeleted) {
        isOriginalDeleted = originalDeleted;
    }
    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public Integer getSpecialFlag() {
        return specialFlag;
    }

    public void setSpecialFlag(Integer specialFlag) {
        this.specialFlag = specialFlag;
    }
}
