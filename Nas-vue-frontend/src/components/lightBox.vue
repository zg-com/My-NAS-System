<template >
    <Teleport to="body" v-if="visible">
        <div class="boxSpace">
            <div class="topUtils">
                <div class="close" @click="closeLightBox">x</div>
                <div class="rightUtils">
                    <div class="showOrigin">
                        <button @click="showOrigin">查看原图</button>
                    </div>
                    <div class="downloadOrigin">
                        <button @click="downloadOrigin">下载原图</button>
                    </div>
                    <div class="deleteToBin">
                        <button @click="deleteToBin">删除</button>
                    </div>
                    <div class="showMoreDetails">
                        <button @click="showMoreDetails">更多信息</button>
                    </div>
                </div>
            </div>
            <div class="previewSpace">
                <img :src="currentFile?.originalUrl?currentFile?.originalUrl:file?.previewUrl" alt="">
            </div>
            <div class="changeViewUtils"></div>
        </div>
    </Teleport>
    <deleteWindow :show-window="showDeleteWindow" 
                  :file = "file"
                  @close="closeDeleteWindow"
                  @deleteSuccess="deleteSuccess"></deleteWindow>
</template>

<script setup lang="ts">
    import { downloadFile, getOriginalFileApi, 
            type UserFile,BASE_URL} from '@/api/fileApi';
    import { ref ,watch} from 'vue';
    import deleteWindow from './deleteWindow.vue';

    //定义这个组件可以接收的数据
    const props = defineProps<{
        visible:Boolean
        file?:UserFile
    }>()
    //定义发送的数据
    const emit = defineEmits(['close','needUpdate'])
    
    const closeLightBox = () =>{
        showDeleteWindow.value = false
        emit('close')//发送关闭信号
    }
    const showDeleteWindow = ref(false)
    const currentFile = ref<UserFile | undefined>(props.file)
    const userId = localStorage.getItem('userId')
    //ref只在组件加载的时候进行一次，所以
    watch(() => props.file,(newVal) => {
        currentFile.value = newVal
    })
    //查看原图
    const showOrigin = async()=>{
        try{
            if(!currentFile.value){
                console.log("当前没有图片数据")
                return  
            } 
            const res = await getOriginalFileApi(currentFile.value.id,userId)
            currentFile.value.originalUrl = URL.createObjectURL(res.data)
        }catch(e){
            console.error('获取原图失败：',e)
            alert("获取原图失败")
        }
    }
    //下载原图
    const downloadOrigin = async()=>{
         try{             
            if(!currentFile.value || !props.file){
                console.log("当前没有图片数据")
                return  
            } 
            const ref = await getOriginalFileApi(props.file?.id,userId)
            const downloadUrl = URL.createObjectURL(ref.data)
            //创建一个a标签
            const link = document.createElement('a')
            link.href = downloadUrl
            link.target = '_blank'
            //将创建的标签放入文档流
            document.body.append(link);
            //配置a属性，让浏览器下载
            link.download = downloadUrl
            //触发点击
            link.click()
            //清理文档流
            document.body.removeChild(link)
        

         }catch(e){
            console.error("下载图片失败",e)
         }
    }
    //删除弹窗关闭
    const closeDeleteWindow =()=>{
        showDeleteWindow.value = false
    }
    //软删除
    const deleteToBin = ()=>{
        showDeleteWindow.value = true
    }
    //物理删除
    const deletePhysically = ()=>{
        
    }
    //更多信息
    const showMoreDetails = ()=>{
        
    }
    //删除成功关闭窗口
    const deleteSuccess = () => {
        emit('close')
        emit('needUpdate')
    }

</script>

<style lang="scss" scoped>
    .boxSpace{
        width: 100vw;
        height: 100vh;
        z-index: 999;
        background-color: rgba(0, 0, 0, 0.87);
        position: fixed;
        top: 0;
        left: 0;
        overflow: hidden;
        backdrop-filter: blur(8px);  
        .topUtils{
            width: 100vw;
            height: 40px;
            // background-color: antiquewhite;
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            position: fixed;
            .close{
                width: 60px;
                height: 40px;
                font-size: 40px;
                cursor: pointer;
                padding-left: 20px;
                // background-color: aqua;
                color: #ffffff;
            }
            .rightUtils{
                width: 400px;
                height: 40px;
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: space-around;
            }
        }
        .previewSpace{
            width: 100vw;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            img{
                height: 100%;
            }
        }
    }
    // Vue 3 内置的 Transition 动画样式
    .fade-enter-active,
    .fade-leave-active {
        transition: opacity 0.3s ease;
    }

    .fade-enter-from,
    .fade-leave-to {
        opacity: 0;
    }
</style>