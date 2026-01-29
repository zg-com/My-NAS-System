<template>
    <Teleport to="body" v-if="showWindow">
        <div class="windowSpace">
            <div class="topclose">
                <span>放入回收站</span>
            </div>
            <div class="prompt">
                <span>30天内可在回收站中找回已删文件。放入回收站的文件仍占用云盘容量，请及时去回收站清理。</span>
            </div>
            <div class="buttons">
                <div class="cancle" @click="closeWindow">取消</div>
                <div class="removeToBin" @click="removeToBin">移至回收站</div>
                <div class="deletePhysically" @click="removePhysically">彻底删除</div>
            </div>
        </div>
        
    </Teleport>
</template>

<script setup lang="ts">
    import { ref } from 'vue';
    import type { UserFile } from '@/api/fileApi';
    import { deleteFileApi,deletePhysicalFileApi } from '@/api/fileApi';
    //定义可以接受的数据
    const props = defineProps<{
        showWindow:boolean
        file?:UserFile | UserFile[]
    }>()
    //定义发送的数据
    const emit = defineEmits(['close','deleteSuccess'])
    const closeWindow = ()=>{
        emit('close')
    }
    const userId = localStorage.getItem('userId')
    //移至回收站
    const removeToBin= async() => {
        if(!props.file) return
        //是数组的话就保持不变，不是数组的话是单个文件就转化为数组
        const fileToprocess = Array.isArray(props.file)?props.file:[props.file] 
        try{
            const deletePromises = fileToprocess.map(item => deleteFileApi(item.id,userId))
            await Promise.all(deletePromises)
            alert('删除成功!')
            emit('close')
            emit('deleteSuccess')
        }catch(e){
            console.error("删除至回收站失败",e)
        }
    }
    //彻底删除
    const removePhysically = async() => {
        if(!props.file) return
        //是数组的话就保持不变，不是数组的话是单个文件就转化为数组
        const fileToprocess = Array.isArray(props.file)?props.file:[props.file] 
        try{
            const deletePromises = fileToprocess.map(item => deletePhysicalFileApi(item.id,userId))
            await Promise.all(deletePromises)
            alert('彻底删除成功!')
            emit('close')
            emit('deleteSuccess')
        }catch(e){
            console.error("彻底删除失败",e)
        }
    }
</script>

<style lang="scss" scoped>
    .windowSpace{
        width: 340px;
        height: 190px;
        background-color: white;
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);//向自己的左上方移动自身宽高的一半，实现居中
        z-index: 1000;
        display: flex;
        flex-direction: column;
        padding: 16px;
        border-radius: 12px;
        user-select: none;
        .topclose{
            width: 100%;
            height: 54px;
            // background-color: blanchedalmond;
            display: flex;
            line-height: 54px;
            justify-content:space-between;
            color: #25262B;
            span{
                font-size: 20px;
                font-weight: 400;
            }
            
        }
        .prompt{
            height: 62px;
            span{
                font-size:14px;
                display: block;
                position: relative;
                top: 50%;
                left: 50%;
                transform: translate(-50%,-50%);
                color:#7B7B7E ;
            }
        }
        .buttons{
            width: 100%;
            margin-top: 10px;
            display: flex;
            justify-content: flex-end;
            .cancle{
                border:2px solid #F1F1F3;
                height: 36px;
                width: 90px;
                text-align: center;
                color:#7B7B7E;
                // border:2px solid black;

                line-height: 36px;
                border-radius: 5px;
                margin-left: 12px;
                margin-left: 12px;
                cursor: pointer;
            }
            .removeToBin{
                height: 36px;
                width: 90px;
                text-align: center;
                color: white;
                // border:2px solid black;
                background-color: #F35B51;
                line-height: 36px;
                border-radius: 5px;
                margin-left: 12px;
                margin-left: 12px;
                cursor: pointer;
            }
            .deletePhysically{
                height: 36px;
                width: 90px;
                text-align: center;
                color: white;
                // border:2px solid black;
                background-color: #ff1b1b;
                line-height: 36px;
                border-radius: 5px;
                margin-left: 12px;
                cursor: pointer;
            }
        }
    }
</style>