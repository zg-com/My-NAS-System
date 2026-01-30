import request from "@/utils/request";

//定义文件类型
export interface UserFile{
    id:number 
    filename:string
    type:string
    size:number
    filePath:string
    uploadTime:Date
    userId:number
    isImage:boolean
    shootTime:Date
    width:number
    height:number
    cameraModel:string
    ISO:string
    shutterSpeed:string
    fNumber:string
    flash:string
    focalLength:string
    exposureBias:string
    meteringMode:string
    whiteBalance:string
    software:string
    thumbnailMinPath:string
    thumbnailPrePath:string
    md5:string
    isVideo:boolean
    isRawImg:boolean
    isLiveImg:boolean
    status:number
    isFolder:false
    relatedId:number
    specialFlag:number
    parentId:number
    isFavorite:boolean
    isHidden:boolean
    longitude:number
    latitude:number
    locationAddress:string
    aiTags:string
    ocrContent:string
    faceIds:string
    isDeleted:boolean
    deleteTime:Date
    coverId:number
    isOriginalDeleted:boolean

    previewUrl?:string //用于前端显示的临时字段
    thumbnailUrl?:string
    originalUrl?:string
    index:number //前端用于定位图片
    canPreview:boolean
    isChoose:boolean//是否被选择
}
//定义时间线文件类型
export interface TimeGroup{
    year:string
    title:string;
    files:UserFile[];
}
export interface NameGroup{
    groupname:string;
    files:UserFile[];
}
//定义当前文件夹信息
export interface fileFolder{
    name:string
    parentId:number | null
    currentId:number
}

//上传文件
export const uploadFileApi = (formData:FormData) => {
    return request.post('/upload',formData);
}
//获取文件信息表
export const getFileDataListApi = (userId:string | null,parentId:number = 0)=>{
    return request.get<UserFile[]>('/list',{
        params:{
            userId:userId,
            parentId:parentId
        }
    });
}
//图库专属：获取文件
export const getGalleryFileListApi = (userId:string | null) => {
    return request.get('/gallery/list',{
        params:{
            userId:userId
        }
    })
}
//图库时间轴专属
export const getGalleryTimeLineApi = (userId:string | null) => {
    return request.get('/gallery/timeline-summary',{
        params:{
            userId:userId
        }
    })
}

//获取图片微型预览
export const getFileThumbnailApi = (fileId:number,userId:string | null) => {
    return request.get(`/file/img/thumbnail/${fileId}`,{
        params:{
            userId:userId,
            
        },
        responseType:'blob'
    })
}
//获取图片预览
export const getFilePreviewApi = (fileId:number,userId:string | null) =>{
    return request.get(`/file/img/preview/${fileId}`,{
        params:{
            userId:userId
        },
    responseType:'blob'
})
}
//获取原图
export const getOriginalFileApi = (fileId:number,userId:string | null) =>{
    return request.get(`/file/img/original/${fileId}`,{
        params:{
            userId:userId
        },
        responseType:'blob'
    })
}


export const deletePhysically = (fileId:number,userId:string | null) => {
    return request.delete(`/file/physical/${fileId}`,{
        params:{
            userId:userId
        }
    })
}

// ✨ 新增：软删除（移入回收站）
export const deleteFileApi = (fileId: number, userId: string | null) => {
    return request.delete(`/file/${fileId}`, {
        params: { userId: userId }
    });
}

// ✨ 新增：物理删除（彻底删除）
export const deletePhysicalFileApi = (fileId: number, userId: string | null) => {
    return request.delete(`/file/physical/${fileId}`, {
        params: { userId: userId }
    });
}
//下载
export const downloadFile = (fileId:number,userId:string|null) =>{
    return request.get(`/download/${fileId}`,{
        params:{
            userId:userId
        },
        responseType:'blob'
    })
}
//创建文件夹
export const createFolder = (folderName:string,parentId:number,userId:string) => {
    return request.post('/folder/new',null,{
        params:{
            name:folderName,
            parentId:parentId,
            userId:userId
        }
    })
}

//获取文件列表
export const getFolderFileList = (userId:string,parentId:number) => {
    return request.get('/file/list',{
        params:{
            userId:userId,
            parentId:parentId
        }
    })
}

//获取回收站列表
export const getRecycleToBinList = (userId:string) => {
    return request.get('/file/recycle',{
        params:{
            userId:userId
        }
    })
}





