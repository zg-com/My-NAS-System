<script setup lang="ts">
    import {ref} from 'vue'
    import icon from '../../source/icon/SideDocker/icon.png'
    const fistPageIcon = ref(icon)
    const FilePageIcon = ref(icon)
    const DataPageIcon = ref(icon)
    const userImgUrl = ref(icon)

</script>

<template>
    <div>
        <div class="barspace">
            <div class="topLogo" :style="{backgroundImage:`url(${userImgUrl})`}"></div>
            <div class="choiceButton">
                <div class="buttonItem">
                    <div class="buttonIcon" :style="{backgroundImage:`url(${userImgUrl})`}"></div>
                    <div class="buttonContent">首页</div>
                </div>
                <div class="buttonItem" >
                    <div class="buttonIcon" :style="{backgroundImage:`url(${userImgUrl})`}"></div>
                    <div class="buttonContent">文件</div>
                </div>
                <div class="buttonItem">
                    <div class="buttonIcon" :style="{backgroundImage:`url(${userImgUrl})`}"></div>
                    <div class="buttonContent">信息</div>
                </div>

            </div>
            <div class="userHeadImg" :style="{backgroundImage:`url(${userImgUrl})`}"></div>
        </div>
    </div>
</template>

<style scoped>
    
    .barspace{
        width: 77px;
        height: 100vh;
        padding:  0;
        margin-left: 0;
        background-color: antiquewhite;
        position: fixed;
        top: 0;
        left: 0;
        display: flex;
        align-items: center;
        flex-direction: column;
    }
    .topLogo{
        width: 50px;
        height: 50px;
        background-size: cover;
        margin-top: 10px;
        margin-bottom: 30px;
    }
    .buttonItem{
        width: 60px;
        height: 65px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        font-size: 14px;
        border-radius: 5px;
        margin-bottom: 10px;
    }
    .buttonItem-active{
        width: 60px;
        height: 65px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        font-size: 14px;
        border-radius: 5px;
        margin-bottom: 10px;
        background-color: antiquewhite;
    }
    
    .buttonIcon{
        width: 40px;
        height: 40px;
        background-size: cover;
        margin-top: 5px;
    }
    .userHeadImg{
        width: 46px;
        height: 46px;
        position:absolute;
        background-size: cover;
        border-radius: 20px;
        bottom: 20px;
    }
    

</style>