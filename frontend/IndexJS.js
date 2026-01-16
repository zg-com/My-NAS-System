
    // 定义后端地址
    const API_BASE = 'http://192.168.177.133:8080';
    const CURRENT_USER_ID = 1;
    // 页面加载完就加载这个照片列表
    window.onload = function(){
        loadPhotos();
    }
    //加载照片的核心函数
    function loadPhotos(){
        //发送请求
        fetch(API_BASE + '/list?userId='+ CURRENT_USER_ID)
            .then(response => response.json())// 字符串变成js数组
            .then(data => {
                const gallery = document.getElementById('gallery');
                gallery.innerHTML = ''; //清空当前页面
                // 循环加载图片
                data.forEach(file => {
                    //HTML模板
                    const cardHtml = `
                        <div class="photo-card">
                            <a href="${API_BASE}/file/${file.id}" target="_blank">
                                <img src="${API_BASE}/file/${file.id}" alt="${file.filename}">
                            </a>
                            <div class="photo-info">
                                <span>${file.filename}</span>
                                <div>
                                    <button class="icon-btn" onclick="downloadFile(${file.id})">⬇️</button>
                                    <button class="icon-btn delete-btn" onclick="deleteFile(${file.id})">🗑️</button>
                                </div>
                            </div>
                        </div>
                    `;
                    gallery.innerHTML += cardHtml;
                });
            })
            .catch(err => console.error('加载失败',err));
    }

    //上传文件函数
    function uploadFile(){
        const input = document.getElementById('uploadInput');
        const file = input.files[0]; //选择的图片默认是放到0索引上的
        if(!file) return alert("请先选择文件");

        //打包文件，并写好收货人“'file'”
        const formData = new FormData();
        formData.append('file',file);
        formData.append('userId',CURRENT_USER_ID);

        fetch(API_BASE+'/upload',{
            method:'POST',
            body:formData
        })
        .then(res => res.text())
        .then(result => {
            alert(result);
            loadPhotos();
        })
        .catch(err => alert("上传出错"));
    }

    //删除文件函数 
    window.deleteFile = function(fileId){
        if(!confirm("确定要删除图片吗?")) return;
        fetch(`${API_BASE}/file/${fileId}?userId=${CURRENT_USER_ID}`,{
            method:'DELETE'
        })
        .then(res => res.text)
        .then(msg =>{
            alert(msg);
            loadPhotos();
        })
        .catch(err => alert("删除请求失败"));
    }

    //下载函数
    window.downloadFile = function(fileId){
        window.location.href = `${API_BASE}/download/${fileId}?userId=${CURRENT_USER_ID}`;
    }
