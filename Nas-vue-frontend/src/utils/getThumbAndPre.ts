import type{ UserFile } from "@/api/fileApi";
import { getFileThumbnailApi,getFilePreviewApi } from "@/api/fileApi";

//获取缩略图
export const getThumbnailAndPre = async (file?:UserFile) =>{
        const userId = localStorage.getItem('userId')
        if(!userId) return
        if(file == null) return;
        try{
            const thumbnailRes = await getFileThumbnailApi(file.id,userId)
            file.thumbnailUrl = URL.createObjectURL(thumbnailRes.data)//创建临时src，方便组件调用
            const previewRes = await getFilePreviewApi(file.id,userId)
            file.previewUrl = URL.createObjectURL(previewRes.data)
            // isLoading.value[file.index] = false
        }catch(e){
            console.error(e)
        }
    }