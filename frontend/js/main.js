// 负责操作页面DOM，调用其他模块
// js/main.js
import { getFileList } from './api.js';
import { uploadTask } from './uploader.js';
import { API_BASE, CURRENT_USER_ID } from './config.js';

// 页面加载完毕
window.onload = function() {
    loadPhotos();
    
    // 绑定上传按钮点击事件
    const btn = document.querySelector('button[onclick="uploadFile()"]');
    // 因为用了模块化，原来的 onclick="uploadFile()" 在 HTML 里可能找不到函数了
    // 建议直接用 JS 绑定，或者把函数挂载到 window 对象上
    if(btn) btn.onclick = handleUploadClick;
};

// 暴露给 window 以便 HTML 里的 onclick 能删掉，改用更现代的写法
// 或者我们在 HTML 里改一下按钮： <button id="uploadBtn">上传新照片</button>

// 处理点击上传
async function handleUploadClick() {
    const input = document.getElementById('uploadInput');
    const file = input.files[0];
    if (!file) return alert("请先选择文件");

    // 创建一个临时的状态提示条
    const statusDiv = document.createElement('div');
    statusDiv.style.cssText = "position:fixed; top:10px; right:10px; background:#333; color:#fff; padding:10px; border-radius:5px;";
    document.body.appendChild(statusDiv);

    try {
        // --- 调用我们封装好的上传模块 ---
        // 只需要这一行！
        const result = await uploadTask(file, (msg) => {
            // 这里是回调函数，模块会告诉我们当前干嘛了
            statusDiv.innerText = msg;
        });

        // 结果处理
        if (result.type === 'fast') {
            alert("⚡️ 秒传成功！\n" + result.msg);
        } else {
            alert("✅ 上传成功！\n" + result.msg);
        }
        
        // 刷新列表
        loadPhotos();

    } catch (err) {
        alert("❌ 操作失败: " + err);
    } finally {
        // 2秒后移除提示条
        setTimeout(() => document.body.removeChild(statusDiv), 2000);
        input.value = ''; // 清空选择
    }
}

// 加载照片列表 (复用你之前的逻辑，稍微改造下)
async function loadPhotos() {
    try {
        const data = await getFileList(CURRENT_USER_ID);
        renderGallery(data);
    } catch (err) {
        console.error("加载列表失败", err);
    }
}

// 渲染相册 DOM (把 HTML 拼接逻辑拆出来，干净！)
function renderGallery(data) {
    const gallery = document.getElementById('gallery');
    gallery.innerHTML = ''; 
    data.forEach(file => {
        // ... 这里放你原来 IndexJS.js 里的 cardHtml 模板代码 ...
        // 注意把原来的 API_BASE 变量替换成 config.js 里的 import 变量
        // 这里的 HTML 模板代码可以直接复制你之前的，记得修改 img src 的拼接方式
        const cardHtml = `
            <div class="photo-card">
                <a href="${API_BASE}/file/${file.id}?userId=${CURRENT_USER_ID}" target="_blank">
                    <img src="${API_BASE}/file/${file.id}?userId=${CURRENT_USER_ID}" alt="${file.filename}">
                </a>
                <div class="photo-info">
                    <span>${file.filename}</span>
                    </div>
            </div>
        `;
        gallery.innerHTML += cardHtml;
    });
}