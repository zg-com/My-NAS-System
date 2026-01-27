<template>
    <div class="barSpace">
        <div class="itemsSpace">
            <div class="dataChoiceSpace"  >
                <div class="itemSpace" >
                    <div class="item" v-for="item in dataItems" :key="item.title"  @click="routerTo(item.position)">
                        <img :src="item.icon" alt="">
                        <div class="title">{{ item.title }}</div>
                    </div>
                </div>
            </div>
            <div class="personalGroupSpace">
                <div class="itemSpace" >
                    <div class="item" v-for="item in personalItems" :key="item.title" @click="routerTo(item.position)">
                        <img :src="item.icon" alt="">
                        <div class="title" >{{ item.title }}</div>
                    </div>
                </div>
            </div>
            <div class="otherUtilsSPace"></div>
        </div>
    </div>
</template>

<script setup lang="ts">
    import { ref } from 'vue';
    import { useRouter } from 'vue-router';
    const router = useRouter()
    //定义侧边栏数据
    const dataItems = [
        {title:"相册" , icon:"",position:"/home/homePage/galleryView"},
        {title:"全部文件",icon:"",position:"/home/homePage/fileManager"},
        {title:"密码箱",icon:"",position:"/home/homePage/codeCase"}
    ]
    const personalItems = [
        {title:"我的分享" , icon:"",position:"/home/homePage/galleryView"},
        {title:"我的收藏",icon:"",position:"/home/homePage/myLike"},
        {title:"回收站",icon:"",position:"/home/homePage/fileBin"}
    ]
    const otherItems = [{}]
    const routerTo = (position:string) => {
        router.push(position)
    }


</script>

<style lang="scss" scoped>
    $activeColor:#E6E7E8;
    $hoverColor:rgb(245, 245, 246);
    .barSpace{
        width: 180px;
        height: 100vh;
        // background-color: aquamarine;
        .itemsSpace{
            .dataChoiceSpace{
                width: 100%;
                height: 180px;
                // background-color: bisque;
                padding-inline: 10px;
                display: flex;
                
                .itemSpace{
                    width: 100%;
                    // background-color:aliceblue;
                    display: flex;
                    flex-direction: column;
                    margin-top: 20px;
                    border-bottom: 1px solid $activeColor;
                    .item{
                        width: 100%;
                        height: 40px;
                        // background-color:aquamarine;
                        margin-top: 5px;
                        display: flex;
                        justify-content: center;
                        // align-content: center;
                        align-items: center;
                        border-radius: 6px;
                        background-color: white;
                        transition: all 0.3s;
                        cursor: pointer;
                    }
                    .item:hover{
                        background-color: $hoverColor;
                    }
                }
            }
            .personalGroupSpace{
                width: 100%;
                height: 180px;
                padding-inline: 10px;
                // background-color: rgb(255, 140, 0);
                display: flex;
                .itemSpace{
                    width: 100%;
                    // background-color:aliceblue;
                    display: flex;
                    flex-direction: column;
                    margin-top: 15px;
                    margin-top: 20px;
                    .item{
                        
                        width: 100%;
                        height: 40px;
                        // background-color:aquamarine;
                        margin-top: 5px;
                        display: flex;
                        justify-content: center;
                        // align-content: center;
                        align-items: center;
                        border-radius: 6px;
                        transition: all 0.3s;
                        background-color: white;
                        cursor: pointer;
                    }
                    .item:hover{
                        background-color: $hoverColor;
                    }
                }
            }
        }
    }
    
</style>