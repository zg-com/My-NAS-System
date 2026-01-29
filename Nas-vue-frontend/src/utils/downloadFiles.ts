import type { UserFile } from "@/api/fileApi";
import { downloadFile } from "@/api/fileApi";

export const downloadFiles = async(files:UserFile[]|UserFile) => {
    const userId = localStorage.getItem('userId')
    if(userId == null) return 
    if(!files) return
    const filesToProcess = Array.isArray(files)? files : [files]

    try{
        const downloadProcess = filesToProcess.map(item => downloadSingleFile(item,userId))
        await Promise.all(downloadProcess)
        alert('下载成功!')
    }catch(e){
        console.error(e)
    }
}

export const downloadSingleFile = async (file:UserFile,userId:string)=>{
    if(file == null) return

    try{
        const ref = await downloadFile(file.id,userId)
        const downloadUrl  = URL.createObjectURL(ref.data)
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = file.filename
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(downloadUrl)
    }catch(e){
        console.error(e)
    }
}