<template>
    <div>
        <addFiles
            :scene="currentStyleOfPosition"
            @needUpdate="updateList"></addFiles>       
    </div>
    <deleteWindow :show-window="showDeleteWindow" 
                  :file = "havechosePhotosList"
                  @close="closeDeleteWindow"
                  @deleteSuccess="deleteSuccess"></deleteWindow>
    
    <div class="gallerySpace">
        <div class="header-section search-wrapper">
            <search></search>
        </div>
        
        <div class="header-section topMode">
            <span>相册</span>
        </div>
        
        <div class="header-section topbarSpace">
            <subTopBar :sumCount="sumCount"
                       :yesOrAllChoice="IsAllChoose"
                       :isSelectMode="IsChoose"
                       :chooseCount="havechosePhotosList.length"
                       @Like="moveToLike"
                       @Delete="moveToBin"
                       @StartChoose="startChoose"
                       @CloseChoose="startChoose"
                       @AllChoose="allChoose"></subTopBar>
        </div>

        <div class="photoSpace" >
            <div class="groupSpace" v-for="group in galleryGroups" :key="group.title" @click.stop>
                <div class="topDate">{{ group.year+ " 年 " + group.title+" 月" }}</div>
                <div class="grid-container">
                    <div class="photoItems" v-for="photodata in group.files" :key="photodata.index" @click="handlePhotos(photodata)">
                        <choiceIcon class="choiceIcon" :visible="IsChoose" :choice="photodata.isChoose" @click.stop = "choiceItem(photodata)"></choiceIcon>
                        <img :src="photodata.thumbnailUrl" alt="">
                         <div class="overlay" v-if="photodata.isChoose"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <lightBox :visible="showLightBox"
              :file="currentPreviewFile"
              @close="closeLightBox"
              @needUpdate="updateList"></lightBox>
</template>

<script setup lang="ts">
    import addFiles from './customComponents/addFiles.vue';
    import Search from '@/components/customComponents/Search.vue';
    import{ref,onMounted} from 'vue'
    import { getGalleryFileListApi,getFilePreviewApi,getGalleryTimeLineApi,getFileThumbnailApi } from '@/api/fileApi';
    import { type UserFile ,type TimeGroup,type NameGroup} from '@/api/fileApi';
    import lightBox from '@/components/customComponents/lightBox.vue';
    import { getThumbnailAndPre } from '@/utils/getThumbAndPre';
    import { all } from 'axios';
    import subTopBar from './customComponents/subTopBar.vue';
    import choiceIcon from './customComponents/choiceIcon.vue';
    import deleteWindow from './customComponents/deleteWindow.vue';
    //----------------------数据部分--------------------
    //是否显示删除提示窗口
    const showDeleteWindow = ref(false)
    const currentStyleOfPosition = 'album'
    const showLightBox = ref(false)
    //顶部图库模式分类
    const topMode = [
        {title:"全部图片"},
        {title:"个人相册"}
    ] 
    //总共多少张
    const sumCount = ref(0) //注意加载时初始化
    //是否全选
    const IsAllChoose = ref(false) //注意加载时初始化
    //图片的index索引,临时
    let index = 0 //注意加载时初始化
    //是否在选择状态
    const IsChoose = ref(false) //注意加载时初始化
    //每个组的内容,也包括月份
    const galleryGroups = ref<TimeGroup[]>([])
    //所有照片，不带时间分组
    const allPhotos = ref<UserFile[]>([])
    //当前已经选择了的照片
    const havechosePhotosList = ref<UserFile[]> ([])
    const userId = localStorage.getItem('userId')
    const isLoading = ref<boolean[]>([true])
    const currentPreviewFile = ref<UserFile>()

    //---------------------方法部分---------------------
    //获取图片并按照时间分组
    const fetchPhotos = async () => {
        sumCount.value = 0
        try{
        const res = await getGalleryFileListApi(userId)
        const photodata = res.data//获取图片信息
        const timeLineRes = await getGalleryTimeLineApi(userId)
        const timeLineData = timeLineRes.data//获取时间线信息
        
        let groups:TimeGroup[] = [];
        let globalPhotoIndex = 0;
        allPhotos.value = [] // 清空旧数据

        for(const timeLine of timeLineData){
             let group = {} as TimeGroup
            group.year = timeLine.year
            group.title = timeLine.month
            group.files=[]
            for(let i = 0; i < timeLine.count;i++){
                if (globalPhotoIndex >= photodata.length) break;
                let file:UserFile = photodata[globalPhotoIndex]
                if(file.isDeleted) continue
                
                // 初始化属性
                file.index = globalPhotoIndex
                file.isChoose = false 

                sumCount.value++
                isLoading.value[file.index] = true
                group.files.push(file)
                allPhotos.value.push(file)
                
                globalPhotoIndex++;
            }
            groups.push(group)
        }
        galleryGroups.value = groups;
        
        // 重置状态
        havechosePhotosList.value = []
        IsChoose.value = false
        IsAllChoose.value = false

        for(const group of galleryGroups.value){
            for(const file of group.files){
                getThumbnailAndPre(file);
            }
        }
    }catch(e){
        console.error("加载相册失败",e);
    }
    }
    
    //查看预览图片
    const handlePhotos= (file?:UserFile) => {
        // 如果在选择模式下，点击图片也是选择
        if(IsChoose.value && file){
            choiceItem(file)
            return
        }
        showLightBox.value = true
        currentPreviewFile.value = file
    }
    //关闭预览图片
    const closeLightBox = ()=>{
        showLightBox.value = false
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
                havechosePhotosList.value.splice(index,1)
            }
        }
        if(havechosePhotosList.value.length !== sumCount.value) IsAllChoose.value=false
        else if(havechosePhotosList.value.length === sumCount.value && sumCount.value > 0) IsAllChoose.value=true
    }
    
    const updateList = () =>{
        fetchPhotos()
    }
    onMounted(()=>{
        fetchPhotos();
    })
    //收藏
    const moveToLike = () => {}
    //删除
    const moveToBin = () => {
        if(havechosePhotosList.value.length === 0) return
        showDeleteWindow.value = true
    }
    //开始选择
    const startChoose = () =>{
        if(IsChoose.value){
            IsChoose.value = false
            IsAllChoose.value = false
            for(const item of havechosePhotosList.value){
                    item.isChoose = false
            }
            havechosePhotosList.value = []
        }else{
            IsChoose.value = true
        }
    }
    //全选
    const allChoose = () => {
        if(!IsChoose.value) IsChoose.value = true

        if(havechosePhotosList.value.length === sumCount.value && sumCount.value > 0){
             for(const item of allPhotos.value){
                    item.isChoose = false
                }
             havechosePhotosList.value = []
             IsAllChoose.value = false
             // IsChoose.value = false
        }else{
             havechosePhotosList.value = []
             for(const item of allPhotos.value){
                 item.isChoose = true
                 havechosePhotosList.value.push(item)
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
        updateList()
    }
</script>

<style lang="scss" scoped>
    .gallerySpace{
        user-select: none;
        width: 100%;
        height: 100%;
        margin-left: 20px;
        
        // ✨✨✨ 核心修改：改为 Flex 列布局，并禁止父级滚动
        display: flex;
        flex-direction: column;
        overflow: hidden; 

        // 头部固定区域通用样式
        .header-section {
            flex-shrink: 0; // 禁止压缩，确保高度固定
            width: 100%;
            // background-color: #FAFAFA; // 防止透明背景导致内容重叠
            z-index: 10;
        }

        .search-wrapper {
            margin-top: 15px; // 保持间距
            padding-right: 20px;
        }

        .topMode{
            height: 50px;
            font-size: 26px;
            font-weight: 700;
            display: flex;
            align-items: center; 
        }

        .topbarSpace {
             padding-right: 20px; 
             margin-bottom: 5px; // 和下方图片留点空隙
        }

        // 滚动区域
        .photoSpace{
            flex: 1; // 占据剩余所有空间
            min-height: 0; // 关键：允许 flex 子元素小于内容高度，从而触发内部滚动
            width: 100%;
            overflow-y: auto; // ✨ 在这里滚动
            padding-bottom: 50px;
            padding-right: 5px; // 防止滚动条遮挡内容
            
            .groupSpace{
                width: 100%;
                margin-bottom: 30px;
                
                .topDate{
                    width: 100%;
                    height: 40px;
                    line-height: 40px;
                    font-size: 20px;
                    font-weight: 600;
                    color: #303133;
                    margin-bottom: 10px;
                    padding-left: 5px; // 微调对齐
                }
                .grid-container{
                    display: grid;
                    grid-template-columns: repeat(auto-fill,minmax(150px,1fr));
                    gap: 15px;
                    padding-right: 15px; // 留出滚动条空间

                    .photoItems{
                        position: relative;
                        aspect-ratio: 1/1;
                        border-radius: 12px;
                        overflow: hidden; 
                        transition: transform 0.3s;
                        cursor: pointer;
                        background-color: #f0f0f0; // 图片加载前的占位色

                        &:hover {
                            transform: scale(1.02);
                            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                        }

                        img{
                            width: 100%;
                            height: 100%;
                            object-fit:cover;
                            display: block;
                        }

                        .choiceIcon {
                            position: absolute;
                            top: 6px;
                            left: 6px;
                        }

                        // 选中时的蓝色遮罩
                        .overlay {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                            background-color: rgba(98, 124, 252, 0.2); 
                            border: 3px solid #627CFC;
                            box-sizing: border-box;
                            pointer-events: none;
                        }
                    }
                }
                
            }
        }
    }
</style>