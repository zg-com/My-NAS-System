<template>
    <div>
        <AddFiles></AddFiles>       
    </div>
    <div class="layout">
        <div class="subSiderba">
            <subSiderba></subSiderba>
        </div>
        <div class="galleryView">
            <galleryView></galleryView>
        </div>
        
    </div>
    
</template>

<script setup>
import AddFiles from '@/components/addFiles.vue';
import galleryView from '@/components/galleryView.vue';
import subSiderba from '@/components/subSiderba.vue';
</script>

<style lang="scss" scoped>
    .layout{
        display: flex;
        height: 100vh;
        overflow: hidden;
        .subSiderba{
            height: 100vh;
            overflow: hidden;

        }
        .galleryView{
            flex:1;//自动填满侧边栏剩下的宽度
            height: 100%;
            overflow-y: auto;
            margin-left: 20px;
        }
    }
</style>