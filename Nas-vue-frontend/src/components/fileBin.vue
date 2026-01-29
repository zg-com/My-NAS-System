<template>

    <div class="gallerySpace">
        <search></search>
        <div class="topMode">
            <span>回收站</span>
        </div>
        <div class="photoSpace" >
            <div class="groupSpace"  @click.stop>
                <!-- <div class="topDate">{{ group.year+ " 年 " + group.title+" 月" }}</div> -->
                <div class="grid-container">
                    <div class="photoItems" v-for="photodata in binFiles" :key="photodata.id" @click="handlePhotos(photodata)">
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
    import addFiles from './customComponents/addFiles.vue';
    import Search from '@/components/customComponents/Search.vue';
    import{ref,onMounted} from 'vue'
    import { getGalleryFileListApi,getFilePreviewApi,getGalleryTimeLineApi,getFileThumbnailApi ,getFileDataListApi, getRecycleToBinList} from '@/api/fileApi';
    import { type UserFile ,type TimeGroup,type NameGroup} from '@/api/fileApi';
    import lightBox from '@/components/customComponents/lightBox.vue';
    import { getThumbnailAndPre } from '@/utils/getThumbAndPre';
    import { all } from 'axios';
    //----------------------数据部分--------------------
    const currentStyleOfPosition = 'album'
    const binFiles = ref<UserFile[]> ([])
    
    const showLightBox = ref(false)
    //总共多少张
    const sumCount = ref(0) //注意加载时初始化
    //是否全选
    const IsAllChoose = ref(false) //注意加载时初始化
    //图片的index索引,临时
    let index = 0 //注意加载时初始化
    //图片是否被选择
    const IsChoose = ref(false) //注意加载时初始化
    //需要存照片
    const needSavePhoto:UserFile[] =[] 
    //正在加载的状态
    const isLoading = ref<boolean[]>([true])
    //正在浏览的图片
    const currentPreviewFile = ref<UserFile>()
    //正在浏览的图片的索引值
    const currentPreIndex = ref(-1)

    //相册分组
    //月份分组
    //当前已经选择了的照片
    let havechosePhotos:UserFile[] = []
    const userId = localStorage.getItem('userId')
    //---------------------方法部分---------------------
    //获取图片并按照时间分组,然后放入月份分组，再放入相册分组，默认相册：全部照片
    const fetchPhotos = async () => {
        try{
            if(userId == null) return
        const res = await getRecycleToBinList(userId)
        const photodata = res.data//获取图片信息
        const files:UserFile[] = []
        for(const item of photodata){
            if(item.isDeleted && item!= null && !item.isFolder){
                files.push(item)
            }
        }
        binFiles.value=files
        for(const item of binFiles.value){
            getThumbnailAndPre(item)
        }
    }catch(e){
        console.error("加载相册失败",e);
    }
    }
    //查看预览图片
    const handlePhotos= (file?:UserFile) => {
        showLightBox.value = true
        currentPreviewFile.value = file
    }
    //关闭预览图片
    const closeLightBox = ()=>{
        showLightBox.value = false
    }
        
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
    fetchPhotos();
    })
    
</script>

<style lang="scss" scoped>
    .gallerySpace{
        user-select: none;
        width: 100%;
        height: 100%;
        margin-left: 20px;
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
                    grid-template-columns: repeat(auto-fill,minmax(150px,1fr));//每个格子不能小于200px，1fr是上限，如果右边不足一个格子，就自动撑满白边
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