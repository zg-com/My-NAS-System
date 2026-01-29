<template>
    
    <div class="layout">
        <div class="subSiderba">
            <subSiderba></subSiderba>
        </div>
        <div class="routerview">
            <router-view></router-view>
        </div> 
        
    </div>
    
</template>

<script setup>
import AddFiles from '@/components/customComponents/addFiles.vue';
import subSiderba from '@/components/customComponents/subSiderba.vue';
</script>

<style lang="scss" scoped>
    .layout{
        display: flex;
        height: 100vh;
        .subSiderba{
            height: 100vh;
            overflow: hidden;

        }
        .routerview{
            flex:1;//自动填满侧边栏剩下的宽度
            height: 100%;
            overflow-y: auto;
            margin-left: 20px;
            /* ✅ 【关键】防止 Flex 子元素被内容撑开导致 Grid 无法正确自适应 */
            min-width: 0;
        }
    }
</style>