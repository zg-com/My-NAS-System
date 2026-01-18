// 工具模块：放MD5计算等通用工具

import sparkMd5Min from "./spark-md5.min";

//计算文件MD5指纹
    /**
 * 计算文件的 MD5 指纹
 * @param {File} file - 文件对象
 * @returns {Promise<string>} - 返回一个 Promise(用于处理异步操作，比如文件读取、网络请求等)，结果是 md5 字符串
 */
export function calculateFileMd5(file){
    //resolve 是这个请求成功执行，放结果，通过.then 获取
    //reject 是没有成功，放异常，通过catch获取
    return new Promise((resolve,reject) => {
        //准备分片读取，防止大文件撑爆内存
        const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;//兼容不同浏览器的切片操作
        const chunkSize = 2097152;//每片2MB
        const chunks = Math.ceil(file.size / chunkSize);
        const spark = new sparkMd5Min.ArrayBuffer();//依赖全局的SparkMD5
        const fileReader = new FileReader();
        let currentChunk = 0;

        //监听读取完成事件
        fileReader.onload = function(e){
            spark.append(e.target.result);//这片数据丢进去计算
            currentChunk++;

            if(currentChunk < chunks){
                loadNext();
            }else{
                const md5 = spark.end();
                resolve(md5);
            }
        };
        fileReader.onerror = function(){
            reject("文件读取失败");
        };

        //开始读下一片
        function loadNext(){
            const start = currentChunk * chunkSize;
            const end = ((start + chunkSize) >= file.size)?file.size : start + chunkSize;
            //只读文件的一部分到内存
            fileReader.readAsArrayBuffer(blobSlice.call(file,start,end));
        }

        loadNext();//启动
    })
}

