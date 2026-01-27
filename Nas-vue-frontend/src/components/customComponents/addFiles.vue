<template>
    <Teleport to="body">
        <div class="addSpace">
            <div class="menu" v-if="showMenu" >
            <li v-for="item in currentOptions" :key="item.label" @click="handleAction(item.action)">
                {{ item.label }}
            </li>
        </div>
        <input type="file" 
            ref="fileInputRef" 
            style="display: none;" 
            multiple 
            :accept="acceptType" 
            @change="handleBatchUpload">
            
        <div class="space">
            
            <button @click="showMenu = !showMenu">
                <span>+</span>
            </button>
        </div>

        
    </div>
    
    </Teleport> 
</template>

<script setup lang="ts">
import{ ref, computed, nextTick } from 'vue' // ✨ 引入 nextTick
import request from '@/utils/request';
import { computeFileMD5 } from '@/utils/md5';
import { uploadFileApi } from '@/api/fileApi';

//定义接收
const props = defineProps({
    scene:{
        type: String,
        default:'album'
    }
})
const showMenu = ref(false);
const fileInputRef = ref<HTMLInputElement | null >(null);
// ✨ 新增：定义当前允许上传的文件类型，默认为所有
const acceptType = ref('*/*'); 

const currentOptions = computed(() => {
    if(props.scene === 'album'){
        return [
            { label: '上传照片', action: 'uploadPhoto' },
            { label: '上传视频', action: 'uploadVideo' }, // 这个按钮之前已经有了
            { label: '新建相册', action: 'createAlbum' }
        ];
    }else{
        return[
            { label: '上传文件', action: 'uploadFile' },
            { label: '上传文件夹', action: 'uploadDir' }, // 文件夹上传需要单独处理 input 的 webkitdirectory 属性，这里暂不展开
            { label: '新建文件', action: 'createFile' }
            ];
    }
})

// ✨ 修改：处理点击菜单项的操作
const handleAction = async (actionName:string) => {
    console.log('执行操作：',actionName);
    showMenu.value = false;
    
    // 根据不同的操作设置不同的文件过滤器
    if(actionName === 'uploadPhoto'){
        acceptType.value = 'image/*'; // 只看图片
    } else if(actionName === 'uploadVideo'){
        acceptType.value = 'video/*'; // 只看视频
    } else if(actionName === 'uploadFile'){
        acceptType.value = '*/*'; // 所有文件
    } else {
        // 其他操作如 createAlbum 等暂不处理文件上传逻辑
        return;
    }

    // ✨ 关键：等待 Vue 更新 DOM 中的 accept 属性后，再触发点击
    await nextTick();
    fileInputRef.value?.click();
}

// 批量上传+md5计算 (保持不变，后端会自动识别 mimeType 判断是视频还是图片)
const handleBatchUpload = async (event:Event) => {
    const input = event.target as HTMLInputElement;
    
    if(!input.files || input.files.length === 0) return;

    //获取userId
    const userId = localStorage.getItem('userId')
    if(!userId){
        alert("用户名丢失，请重新登录")
        return;
    }
    const files = Array.from(input.files);

    //循环处理每一个文件
    for(const file of files){
        try{
            console.log(`正在计算 ${file.name} 的 MD5...`);
            //计算md5
            const md5 = await computeFileMD5(file);
            console.log(`MD5计算完成: ${md5}`);

            //准备上传参数
            const formData = new FormData();
            formData.append('file',file);
            formData.append('userId',userId);
            formData.append('parentId','0');
            formData.append('md5',md5);
            
            // 发送请求
            const res = await uploadFileApi(formData)
            console.log(`文件 ${file.name} 上传成功:`, res.data);
        }catch(error){
            console.error(`文件 ${file.name} 上传失败`, error);
            alert(`文件 ${file.name} 上传出错`);
        }
    }
    alert('所有文件处理完毕！');
    input.value = ''; // 清空选择框
}
</script>
<style scoped>

.space{
        position: fixed;
        bottom: 5%;
        right: 5%;
        /* background-color: antiquewhite; */
        display: flex;
        align-items: end;
        justify-content: end;
        flex-direction: column;
    }
    button{
        width: 60px;
        height: 60px;
        border-radius: 50%;
        border: none;
        background-color: #627CFC;
        cursor: pointer;
        
    }
    span{
        font-size: 40px;
        font-weight: 100;
        color: #ffffff;
    }
    .menu{
        width: 160px;
        height: auto;
        position: absolute;
        bottom: 100px; 
        right: 60px; 
        margin-bottom: 10px;
        background-color: #627CFC;
        border-radius: 10px;
        list-style: none;
    }
    .menu li{
        cursor: pointer;
        padding: 10px 15px;
        border-bottom: 1px solid #f0f0f0; /* 每一项之间加一条很浅的线 */
        
    }
    .menu li:last-child {
        border-bottom: none; /* 最后一项不需要下划线 */
    }

    .menu li:hover {
        background-color: #f5f7ff; /* 鼠标滑过时有个浅蓝背景 */
    }
</style>