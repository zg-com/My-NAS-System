import request from "@/utils/request";

export const BASE_URL = 'http://100.87.189.97:8080';
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
}
//定义时间线文件类型
export interface TimeGroup{
    title:string;
    files:UserFile[];
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
export const getFileThumbnailApi = (fildId:number,userId:string | null) => {
    return request.get(`/file/img/thumbnail/${fildId}`,{
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



