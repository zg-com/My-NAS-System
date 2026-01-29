<template>
    <div class="barSpace">
        <div class="itemsSpace">
            <div class="dataChoiceSpace">
                <div class="itemSpace">
                    <div class="item" 
                         v-for="item in dataItems" 
                         :key="item.title"  
                         :class="{ 'active': route.path === item.position }"
                         @click="routerTo(item.position)">
                        <div class="title">{{ item.title }}</div>
                    </div>
                </div>
            </div>
            <div class="personalGroupSpace">
                <div class="itemSpace">
                    <div class="item" 
                         v-for="item in personalItems" 
                         :key="item.title" 
                         :class="{ 'active': route.path === item.position }"
                         @click="routerTo(item.position)">
                        <div class="title">{{ item.title }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
    import { useRouter, useRoute } from 'vue-router'; // 引入 useRoute
    const router = useRouter()
    const route = useRoute() // 获取当前路由对象

    // ... dataItems 和 personalItems 保持不变 ...
    const dataItems = [
        {title:"相册" , icon:"",position:"/home/homePage/galleryView"},
        {title:"全部文件",icon:"",position:"/home/homePage/fileManager"},
        {title:"密码箱",icon:"",position:"/home/homePage/codeCase"}
    ]
    const personalItems = [
        // {title:"我的分享" , icon:"",position:"/home/homePage/galleryView"},
        {title:"我的收藏",icon:"",position:"/home/homePage/myLike"},
        {title:"回收站",icon:"",position:"/home/homePage/fileBin"}
    ]

    const routerTo = (position:string) => {
        router.push(position)
    }
</script>

<style lang="scss" scoped>
    $activeBg: #EBF2FF; // 选中时的背景色（淡蓝）
    $activeText: #3B73F0; // 选中时的文字色
    $hoverColor: #f5f5f6;

    .barSpace {
        width: 160px;
        height: 100vh;
        border-right: 1px solid #E6E7E8;
        background-color: #fff;
        
        .itemsSpace {
            padding-top: 20px;
            
            .dataChoiceSpace, .personalGroupSpace {
                width: 100%;
                padding: 0 10px;
                margin-bottom: 20px;
                
                .itemSpace {
                    display: flex;
                    flex-direction: column;
                    gap: 5px; // 每一项之间的间距

                    .item {
                        width: 100%;
                        height: 40px;
                        display: flex;
                        align-items: center;
                        justify-content: center; // 文字居中
                        border-radius: 8px;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        font-size: 14px;
                        color: #606266;

                        &:hover {
                            background-color: $hoverColor;
                        }

                        // 激活状态样式
                        &.active {
                            background-color: $activeBg;
                            color: $activeText;
                            font-weight: 600;
                        }
                    }
                }
            }
        }
    }
</style>