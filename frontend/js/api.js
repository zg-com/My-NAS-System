// 专门负责跟后端发送请求，不处理UI

import { API_BASE } from "./config.js"; //导入配置

//检查MD5是否存在
export async function  checkFileMd5(md5) {
    const response = await fetch(`${API_BASE}/file/check-md5?md5=${md5}`);
    const status = await response.text();
    return status === "200"; //如果返回200，该函数就返回ture
    
}

//上传文件（物理上传+秒传参数)
export async function uploadFileRequest(fromData) {
    const response = await fetch(`${API_BASE}/upload`,{
        method:'POST',
        body : FormData
    });
    return await response.text();
}

// 获取文件列表
export async function getFileList(userId) {
    const response = await fetch(`${API_BASE}/list?userId=${userId}`);
    return await response.json;
}

//删除文件
export async function deleteFile(fileId) {
    const response = await fetch(`${API_BASE}/file/${fileId}?userId=${CURRENT_USER_ID}`,{
        method:'DELETE'
    })
    return await response.text();
}

//下载文件
export async function downloadFile(fileId) {
    window.location.href = `${API_BASE}/download/${fileId}?userId=${CURRENT_USER_ID}`;
}