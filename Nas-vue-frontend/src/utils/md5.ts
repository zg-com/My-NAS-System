import SparkMD5 from 'spark-md5'

/**
 * 计算文件的 MD5 值
 * @param file HTML5 文件对象
 * @returns Promise<string> 返回文件的 MD5 字符串
 */
export const computeFileMD5 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
        const chunkSize = 2097152; // 2MB 一片
        const chunks = Math.ceil(file.size / chunkSize); // 总切片数
        let currentChunk = 0;
        
        const spark = new SparkMD5.ArrayBuffer();
        const fileReader = new FileReader();

        fileReader.onload = (e: any) => {
            if (!e.target?.result) return;
            
            spark.append(e.target.result); 
            currentChunk++;

            if (currentChunk < chunks) {
                loadNext();
            } else {
                const md5 = spark.end();
                resolve(md5);
            }
        };

        fileReader.onerror = () => {
            reject('MD5 计算失败，文件读取错误');
        };

        function loadNext() {
            const start = currentChunk * chunkSize;
            const end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
            
            // 【修正点】直接调用 file.slice，兼容性更好，且支持 Vue Proxy 对象
            // 现代浏览器（Chrome, Edge, Firefox）都支持这个原生方法
            const chunk = file.slice(start, end);
            
            fileReader.readAsArrayBuffer(chunk);
        }

        loadNext();
    });
}