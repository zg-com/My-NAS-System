<template>
    <Teleport to="body">
        <editorWindow 
                :visible="showCreateFolderWindow" 
                @close="showCreateFolderWindow = false"
                @confirm="handleCreateFolderApi"
            ></editorWindow>
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
            <!-- 实现文件夹上传 -->
        <input type="file" 
            ref="dirInputRef" 
            style="display: none;" 
            webkitdirectory
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
import { uploadFileApi ,createFolder} from '@/api/fileApi';
import editorWindow from './editorWindow.vue';
//定义接收
const props = defineProps({
    scene:{
        type:String,
        default:'album'
    },
    currentFolderId:{
        type:Number,
        default:'0'
    }
})
//定义回传
const emit = defineEmits(['needUpdate'])
//是否开启编辑窗口
const showCreateFolderWindow = ref(false)
const showMenu = ref(false);
const fileInputRef = ref<HTMLInputElement | null >(null);
const dirInputRef = ref<HTMLInputElement | null> (null);
// ✨ 新增：定义当前允许上传的文件类型，默认为所有
const acceptType = ref('*/*'); 
//获取userId


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
            { label: '新建文件夹', action: 'createFile' }
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
    } else if(actionName === 'createAlbum'){
        return
    } else if(actionName === 'uploadDir'){
        await nextTick();
        dirInputRef.value?.click();
        return
    } else if(actionName === 'createFile'){
        handleCreateFolder();
        return 
    }

    // ✨ 关键：等待 Vue 更新 DOM 中的 accept 属性后，再触发点击
    await nextTick();
    fileInputRef.value?.click();
}
const handleCreateFolder = () => {
    showCreateFolderWindow.value = true;
}


// ✅ 新增：处理真正的创建逻辑
// 这里的 name 就是子组件 emit('confirm', folderName.value) 传过来的
const handleCreateFolderApi = async (name: string) => {
    try {
        const userId = localStorage.getItem('userId')
        if (!userId) return;

        // 调用你的后端接口
        // 假设你的 props.currentFolderId 是当前目录ID
        await createFolder(name, props.currentFolderId, userId);
        
        alert('文件夹创建成功');
        showCreateFolderWindow.value = false; // 关闭弹窗
        emit('needUpdate'); // 通知父列表刷新
    } catch (e) {
        console.error('创建失败', e);
        alert('创建失败');
    }
}


//上传文件夹逻辑
const handleDirUpload = async (event:Event) => {
    const input = event.target as HTMLInputElement
    if(!input.files || input.files.length === 0) return

    const files = Array.from(input.files);
    await processUpload(files);
    input.value = ''
}

// 批量上传+md5计算
const handleBatchUpload = async (event:Event) => {
    //获取input获取到的文件列表
    const input = event.target as HTMLInputElement;  
    if(!input.files || input.files.length === 0) return;
    const files = Array.from(input.files);
    //循环处理每一个文件
    await processUpload(files);
    alert('所有文件处理完毕！');
    input.value = ''; // 清空选择框
}

//统一上传处理函数
const processUpload = async (files:File[]) => {
    const userId = localStorage.getItem('userId')
    if(!userId) return
    
    for(const file of files){
        try{
            const md5 = await computeFileMD5(file);
            const formData = new FormData();
            formData.append('file', file);
            formData.append('userId', userId);
            formData.append('parentId', String(props.currentFolderId));
            formData.append('md5', md5);
            formData.append('relativePath', (file as any).webkitRelativePath || '');
            await uploadFileApi(formData);
            console.log(`上传成功: ${file.name}`);
        } catch (error) {
            console.error(`上传失败: ${file.name}`);
        }
    }
    alert('处理完毕');
    emit('needUpdate'); // 刷新列表
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