//实现秒传与上传的逻辑

import { checkFileMd5, uploadFileRequest } from "./api";
import { CURRENT_USER_ID } from "./config";
import { calculateFileMd5 } from "./utils";

//onProgress可以获取这个进程的状态,获取是否在执行
export async function uploadTask(file,onProgress) {
    try{
        //计算指纹
        if(onProgress) onProgress("正在计算文件指纹...");
        console.log("开始计算MD5...");
        const md5 = await calculateFileMd5(file);
        console.log("Md5 计算完毕",md5);

        //秒传检查
        if(onProgress) onProgress("正在检查服务器库存...");
        const isExist = await checkFileMd5(md5);

        if(isExist){
            //触发秒传
            console.log("发现重文件，触发秒传");
            if(onProgress) onProgress("急速秒传中...");

            const formData = new FormData();
            formData.append('file',null);
            formData.append('userId',CURRENT_USER_ID);
            formData.append('md5',md5);

            const result = await uploadFileRequest(formData);
            return {success: true , type:'fast',msg:result};
        }else{
            //触发物理上传
            console.log("新文件，开始物理上传");
            if(onProgress) onProgress("正在上传数据...");

            const formData = new FormData();
            formData.append('file',file);
            formData.append('userId',CURRENT_USER_ID);
            formData.append('md5',md5);

            const result = await uploadFileRequest(formData);
            return {success: true , type:'real',msg:result};
        }
    }catch(error){
        console.error("上传失败",error);
        throw error;//把错误抛出去给UI处理
    }
}