<template>
    <lightBox :visible="showLightBox"
              :file="currentPreviewFile"
              @close="closeLightBox"
              @needUpdate="updateList"></lightBox>
    <div class="addfile">
        <addFiles
            :scene="currentStyleOfPosition"
            :currentFolderId="currentFolderId"
            @needUpdate="updateList"></addFiles>
    </div>
    <deleteWindow :show-window="showDeleteWindow" 
                  :file = "havechosePhotosList"
                  @close="closeDeleteWindow"
                  @deleteSuccess="deleteSuccess"></deleteWindow>
    <div class="container">
        <div class="search">
            <Search></Search>
        </div>
        <div class="topTitle">
                文件管理
        </div>
        <div class="managerSpace">
            
            <div class="topbarSpace">
                <div class="topbar" >
                    <div class="titleItem" v-for="(item,index) in fileFolderMemory" :key="item.currentId">
                        <div class="row" v-if="index > 0">></div>
                        <div class="folderTile" 
                            :class="{'activeFolder' : currentFolderId === item.currentId}"
                            @click="moveTo(item.currentId)">{{ item.name }}</div>                        
                    </div>
                </div>  
            </div>
            <div class="folderModleAndDetails">
                <subTopBar :sumCount="currentFileList.length"
                       :yesOrAllChoice="IsAllChoose"
                       :isSelectMode="IsChoose"
                       :chooseCount="havechosePhotosList.length"
                       @Like="moveToLike"
                       @Delete="moveToBin"
                       @StartChoose="startChoose"
                       @CloseChoose="startChoose"
                       @AllChoose="allChoose"
                       @DownLoad="downloadChoiceFiles"></subTopBar>
            </div>
            <div class="folderAndFilesSpace">
                <div class="item" 
                     v-for="item in currentFileList" 
                     :key="item.id" 
                     @click="handleFile(item)"
                     :class="{'item-selected': item.isChoose}">
                        
                        <div class="icon-wrapper" @click.stop = "choiceItem(item)">
                             <choiceIcon :visible="IsChoose" :choice="item.isChoose"></choiceIcon>
                        </div>
                       
                        <img 
                            v-if="item.isFolder" 
                            class="viewIconIsFold" 
                            :src="folderIconUrl" 
                            alt="文件夹"
                        >
                        <div class="viewIcon" v-if="!item.isFolder" :style="{backgroundImage: item.thumbnailUrl ? `url(${item.thumbnailUrl})`:`url(${fileOtherIcon})`}"></div>
                        <div class="title" :title="item.filename">{{ item.filename }}</div>
                        <div class="fileDate">{{ formatDate(item.uploadTime) }}</div>
                    </div>
            </div>
        </div>
    </div>   
</template>

<script setup lang="ts">
    import fileOtherIcon from '@/source/icon/SideDocker/icon.png'
    import folderIcon from '@/source/icon/FolderIcon.svg?url'
    import addFiles from './customComponents/addFiles.vue';
    import { ref,onMounted } from 'vue';
    import {downloadFile, getFileDataListApi, type UserFile,type fileFolder } from '@/api/fileApi';
    import Search from './customComponents/Search.vue';
    import { createFolder,getFolderFileList } from '@/api/fileApi';
    import { getThumbnailAndPre } from '@/utils/getThumbAndPre';
    import lightBox from './customComponents/lightBox.vue';
    import { formatDate } from '@/utils/formatDate';
    import editorWindow from './customComponents/editorWindow.vue';
    import deleteWindow from './customComponents/deleteWindow.vue';
    import choiceIcon from './customComponents/choiceIcon.vue';
    import subTopBar from './customComponents/subTopBar.vue';
    import { downloadFiles } from '@/utils/downloadFiles';
    //----------------数据部分------------------
    const showLightBox = ref(false)//预览
    const currentPreviewFile = ref<UserFile>()//点击要预览的那个文件
    const sumFileCount = ref(0) //当前文件夹中总共多少项文件
    const IsAllChoose = ref(false) //当前是否全选
    const folderIconUrl = ref(folderIcon)
    const otherFileIcon = ref(fileOtherIcon)
    const currentStyleOfPosition = 'file'
    const IsChoose = ref(false) //当前是不是正在选择模式
    const havechosePhotosList = ref<UserFile[]> ([]) //当前已经选择的照片
    const showDeleteWindow = ref(false)

        //存自己的上一级的parentId和自己的
    const fileFolderMemory = ref<fileFolder[]> ([
        {name:"全部文件",parentId:0,currentId:0}
    ])
    const currentFolderId = ref(0)
        //当前文件列表内容
    const currentFileList = ref<UserFile[]>([])
    //----------------方法部分------------------
        //获取文件列表
    const fetchFiles = async(parentId:number = 0) => {
        const userId = localStorage.getItem('userId')
        if(!userId){
            alert("请重新登录")
            return
        }
        const tempList : UserFile[] = []
        const res = await getFolderFileList(userId,parentId)
        for(const item of res.data){
            if(item.isDeleted) continue
            // 初始化选中状态，防止undefined
            item.isChoose = false
            tempList.push(item)
        }
        currentFileList.value = tempList
        sumFileCount.value = currentFileList.value.length
        
        // 重置选择状态，防止切换文件夹时状态残留
        IsChoose.value = false
        IsAllChoose.value = false
        havechosePhotosList.value = []

        //图片文件缩略图获取
        for(const imgFile of currentFileList.value){
            if(imgFile.isImage || imgFile.isRawImg || imgFile.isVideo || imgFile.isLiveImg){
                imgFile.canPreview = true
                getThumbnailAndPre(imgFile)
            }
        }
    }
        
        //跳转文件列表
        const moveTo = (currentId:number) => {
            currentFolderId.value = currentId
            fetchFiles(currentFolderId.value)
            //获取要跳转到的在fileFolderMemory中的索引
            const index = fileFolderMemory.value.findIndex(item => item.currentId === currentId)
            if(index !== -1){
                fileFolderMemory.value = fileFolderMemory.value.slice(0,index+1)
            }
            
        }

        const handleFile = (file:UserFile) => {
            // 如果是选择模式，点击文件本体也是选择
            if(IsChoose.value) {
                choiceItem(file)
                return
            }

            if(file.isImage || file.isRawImg || file.isVideo || file.isLiveImg){
                showPreview(file)
            }
            else if(file.isFolder){
                openFolder(file)
            }
        }
        const openFolder = (file:UserFile )=> {
            fileFolderMemory.value.push({
                name:file.filename,
                parentId:file.parentId,
                currentId:file.id
            })
            currentFolderId.value = file.id
            fetchFiles(file.id)
        }
        const showPreview = (file:UserFile) => {
            openLightBox(file)
        }
        const openLightBox = (file : UserFile) =>{
            currentPreviewFile.value = file
            showLightBox.value = true
        }
        const closeLightBox = () => {
            showLightBox.value = false
        }
        //更新列表
        const updateList = () => {
            fetchFiles(currentFolderId.value)
        }

        //收藏
    const moveToLike = () => {
        
    }
    //删除
    const moveToBin = () => {
        if(havechosePhotosList.value.length === 0) return
        showDeleteWindow.value = true
    }
    //开始选择
    const startChoose = () =>{
        if(IsChoose.value){
            // 取消选择模式
            IsChoose.value = false
            IsAllChoose.value = false
            for(const item of havechosePhotosList.value){
                    item.isChoose = false
            }
            havechosePhotosList.value = []
        }else{
            // 开启选择模式
            IsChoose.value = true
        }
    }
    //全选
    const allChoose = () => {
        // 如果不在选择模式，先进入选择模式
        if(!IsChoose.value) {
            IsChoose.value = true
        }

        // 如果已经全选了（数量相等），则取消全选
        if(havechosePhotosList.value.length === sumFileCount.value && sumFileCount.value > 0){
             // 取消所有项的选中状态
            for(const item of currentFileList.value){
                item.isChoose = false
            }
            havechosePhotosList.value = []
            IsAllChoose.value = false
            // IsChoose.value = false // 保持选择模式不退出，体验更好
        } else {
            // 全选逻辑
             havechosePhotosList.value = [] // 先清空，防止重复
             for(const item of currentFileList.value){
                item.isChoose = true
                havechosePhotosList.value.push(item) // ✨ 关键：Push进去，而不是直接赋值数组引用
             }
             IsAllChoose.value = true
        }
    }
    //关闭删除提示窗
    const closeDeleteWindow = () => {
        showDeleteWindow.value = false 
    }
    const deleteSuccess = () => {
        IsChoose.value = false
        havechosePhotosList.value = []
        IsAllChoose.value = false
        updateList()
    }
    //选择照片
    const choiceItem = (file:UserFile) => {
        file.isChoose = !file.isChoose
        if(file.isChoose){
            const index = havechosePhotosList.value.findIndex(item => item.id === file.id)
            if(index === -1){
                havechosePhotosList.value.push(file)
            }
        }else if(!file.isChoose){
            const index = havechosePhotosList.value.findIndex(item => item.id === file.id)
            if(index !== -1){
                //splice(起始位置，删除几个)
                havechosePhotosList.value.splice(index,1)
            }
        }
        if(havechosePhotosList.value.length !== sumFileCount.value) IsAllChoose.value=false
        else if(havechosePhotosList.value.length === sumFileCount.value && sumFileCount.value > 0) IsAllChoose.value=true
    }
    //下载选中的文件
    const downloadChoiceFiles = async () => {
        await downloadFiles(havechosePhotosList.value)
    }
    onMounted(()=>{
        fetchFiles(0)
    })
</script>

<style lang="scss" scoped>
    .container {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        // background-color: #FAFAFA; // 更柔和的背景

        .search {
            margin-top: 15px;
            padding: 0 20px; 
            flex-shrink: 0;
        }

        .topTitle {
            margin-top: 20px;
            padding: 0 25px;
            font-size: 24px;
            font-weight: 700;
            color: #1A1A1A;
            flex-shrink: 0;
            letter-spacing: 0.5px;
        }

        .managerSpace {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden; 
            margin-top: 15px;
            min-width: 0; // 防止被子元素撑开
            width: 100%;

            // 面包屑栏样式优化
            .topbarSpace {
                width: 100%;
                height: 40px;
                line-height: 40px;
                padding: 0 25px;
                flex-shrink: 0;
                
                
                .topbar {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    align-items: center;
                    // overflow-x: auto; 
                    
                    .titleItem {
                        display: flex;
                        align-items: center;
                        font-size: 14px;
                        color: #909399;
                        cursor: pointer;
                        white-space: nowrap; 

                        .row {
                            margin: 0 8px;
                            color: #DCDFE6;
                        }

                        .folderTile {
                            padding: 4px 8px;
                            border-radius: 6px;
                            transition: all 0.2s;

                            &:hover {
                                background-color: #EBF2FF;
                                color: #627CFC;
                            }
                        }

                        .activeFolder {
                            font-weight: 600;
                            color: #303133;
                            cursor: default;
                            &:hover { background: none; color: #303133; }
                        }
                    }
                }
            }

            .folderModleAndDetails {
                height: 50px;
                flex-shrink: 0;
                width: 100%;
                padding: 0 15px; // 稍微调整内边距适应subTopBar
                margin-bottom: 10px;
                border-bottom: 1px solid #f0f0f0;
                /* 确保内部的组件不会因为 flex 布局问题换行 */
                display: block; 
                overflow: hidden;
            }

            // 文件网格区域
            .folderAndFilesSpace {
                flex: 1;
                overflow-y: auto; 
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); // 调整最小宽度
                grid-auto-rows: max-content; 
                gap: 20px; // 增加间距
                padding: 20px 25px 50px 25px; 

                .item {
                    position: relative; 
                    background-color: white;
                    border-radius: 12px;
                    padding: 15px 10px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    cursor: pointer;
                    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
                    border: 1px solid transparent; 
                    box-shadow: 0 2px 6px rgba(0,0,0,0.02); // 默认轻微阴影

                    // 悬停效果：上浮 + 阴影
                    &:hover {
                        transform: translateY(-4px);
                        box-shadow: 0 8px 16px rgba(0,0,0,0.08);
                        border-color: #EBF2FF;
                    }

                    // 选中状态
                    &.item-selected {
                        background-color: #F0F5FF;
                        border-color: #627CFC;
                        box-shadow: 0 4px 12px rgba(98, 124, 252, 0.15);
                    }

                    // 关键修复：选择图标容器必须限制大小，否则遮挡文件
                    .icon-wrapper {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 44px;  /* 限制宽度 */
                        height: 44px; /* 限制高度 */
                        z-index: 5;
                        /* background-color: rgba(255,0,0,0.2); 调试用 */
                        pointer-events: auto; // 确保这块区域能被点击
                    }

                    .viewIconIsFold, .viewIcon {
                        width: 70px;
                        height: 70px;
                        object-fit: cover; 
                        margin-bottom: 12px;
                        background-repeat: no-repeat;
                        background-position: center;
                        background-size: cover;
                        border-radius: 8px; // 图标圆角
                    }

                    .title {
                        font-size: 14px;
                        color: #333;
                        width: 100%;
                        text-align: center;
                        line-height: 1.5;
                        margin-bottom: 4px;
                        padding: 0 4px;
                        
                        // 多行省略
                        display: -webkit-box;
                        -webkit-box-orient: vertical;
                        line-clamp: 2;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        word-break: break-all;
                    }

                    .fileDate {
                        font-size: 12px;
                        color: #C0C4CC;
                        transform: scale(0.9); 
                    }
                }
            }
        }
    }
</style>