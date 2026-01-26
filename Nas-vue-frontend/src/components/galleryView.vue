<template>
    <search></search>
    <div class="gallerySpace">
        <div class="topMode">
            <span>相册</span>
        </div>
        <div class="photoSpace" >
            <div class="groupSpace" v-for="group in galleryGroups" :key="group.title" @click.stop>
                <div class="topDate">{{ group.year+ " 年 " + group.title+" 月" }}</div>
                <div class="grid-container">
                    <div class="photoItems" v-for="photodata in group.files" :key="photodata.index" @click="handlePhotos(photodata)">
                    <img :src="photodata.thumbnailUrl" alt="">
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
    import Search from './Search.vue';
    import{ref,onMounted} from 'vue'
    import { getGalleryFileListApi,getFilePreviewApi,getGalleryTimeLineApi,getFileThumbnailApi } from '@/api/fileApi';
    import { type UserFile ,type TimeGroup,type NameGroup} from '@/api/fileApi';
    import lightBox from './lightBox.vue';
    import { all } from 'axios';
    //----------------------数据部分--------------------
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
    //图片是否被选择
    const IsChoose = ref(false) //注意加载时初始化
    //每个组的内容,也包括月份
    const galleryGroups = ref<TimeGroup[]>([])
    //三种视图模式
    const viewModle = [
        {id:1,title:"时间线视图"},
        {id:2,title:"月份分组视图"},
        {id:3,title:"相册分组视图"}
    ]
    //图片组别
    const nameGroups = ref<NameGroup[]>([])
    const nameGroupsTemp:NameGroup[] = []
    //需要存照片
    const needSavePhoto:UserFile[] =[] 
    //正在加载的状态
    const isLoading = ref<boolean[]>([true])
    //正在浏览的图片
    const currentPreviewFile = ref<UserFile>()
    //正在浏览的图片的索引值
    const currentPreIndex = ref(-1)
    //时间分组
    let groups:TimeGroup[] = []

    //相册分组
    //月份分组
    //当前已经选择了的照片
    let havechosePhotos:UserFile[] = []
    const userId = localStorage.getItem('userId')
    //---------------------方法部分---------------------
    //获取图片并按照时间分组,然后放入月份分组，再放入相册分组，默认相册：全部照片
    const fetchPhotos = async () => {
        try{
        const res = await getGalleryFileListApi(userId)
        const photodata = res.data//获取图片信息
        const timeLineRes = await getGalleryTimeLineApi(userId)
        const timeLineData = timeLineRes.data//获取时间线信息
        groups = [];//清空临时数据
        // 用于从 photodata 中按顺序取图的全局游标
        let globalPhotoIndex = 0;
        for(const timeLine of timeLineData){
             let group = {} as TimeGroup
               //遍历返回的所有时间线
            //将每个时间线的信息都装到groups中
            group.year = timeLine.year
            group.title = timeLine.month
            group.files=[]//必须显式初始化数组，否则就是undefined，无法push
            for(let i = 0; i < timeLine.count;i++){
                // 边界检查：防止数据对不上导致越界
                if (globalPhotoIndex >= photodata.length) break;
                let file:UserFile = photodata[globalPhotoIndex]
                if(file.isDeleted) continue //被删除了的不要
                file.index = globalPhotoIndex
                sumCount.value++
                isLoading.value[file.index] = true
                group.files.push(file)
                // 准备获取这张图的缩略图
                
                // 游标后移，准备取下一张
                globalPhotoIndex++;
            }
            groups.push(group)//装入
            
        }
        galleryGroups.value = groups;
        for(const group of galleryGroups.value){
            for(const file of group.files){
                getThumbnailAndPre(file);
            }
        }
    }catch(e){
        console.error("加载相册失败",e);
    }
    }
    //获取缩略图
    const getThumbnailAndPre = async (file?:UserFile) =>{
        if(file == null) return;
        try{
            const thumbnailRes = await getFileThumbnailApi(file.id,userId)
            file.thumbnailUrl = URL.createObjectURL(thumbnailRes.data)//创建临时src，方便组件调用
            const previewRes = await getFilePreviewApi(file.id,userId)
            file.previewUrl = URL.createObjectURL(previewRes.data)
            isLoading.value[file.index] = false
        }catch(e){
            console.error(e)
        }
    }
    //查看预览图片
    const handlePhotos= (file?:UserFile) => {
        showLightBox.value = true
        currentPreviewFile.value = file
    }
    //关闭预览图片
    const closeLightBox = ()=>[
        showLightBox.value = false
    ]
    //选择照片
    //批量删除照片
    //批量下载照片
    //批量添加到相册分组
    //取消多选
    //更新列表
    const updateList = () =>{
        fetchPhotos()
    }
    onMounted(()=>{
    sumCount.value = 0 //注意加载时初始化
    IsAllChoose.value=false//注意加载时初始化
    //图片的index索引,临时
    
    //图片是否被选择
    IsChoose.value=false//注意加载时初始化
    
    for(let i = 0;i < index;i++){
        isLoading.value[i] = true;
    }
     index = 0 //注意加载时初始化
        fetchPhotos();
    })
    
</script>

<style lang="scss" scoped>
    .gallerySpace{
        user-select: none;
        width: 100%;
        height: 100%;
        // background-color: rgb(241, 136, 0);
        .topMode{
            width: 100%;
            height: 50px;
            // background-color: aqua;
            font-size: 26px;
            font-weight: 700;
        }
        .photoSpace{
            width: 100%;
            height: 100%;
            // background-color: rgb(132, 158, 150);
            .groupSpace{
                width: 100%;
                
                .topDate{
                    width: 100%;
                    height: 40px;
                    font-size: 24px;
                    font-weight: 600;
                    // background-color: rgb(42, 81, 81);
                }
                .grid-container{
                    display: grid;
                    grid-template-columns: repeat(auto-fill,minmax(200px,1fr));//每个格子不能小于200px，1fr是上限，如果右边不足一个格子，就自动撑满白边
                    gap: 20px; //设置每个各自之间的间隔

                    .photoItems{
                    
                    //repeat是重复生成列，auto-fill是自动填充，自己根据屏幕宽度自动生成多少列，minmax是设置每个格子最大最小值 
                    aspect-ratio: 1/1;//每个格子必须是正方形
                        img{
                            width: 100%;
                            height: 100%;
                            object-fit:cover;//自动裁剪不变形
                            display: block;//消除图片底部的小空隙
                            cursor: pointer;
                        }
                }
                }
                
            }
        }
    }
</style>