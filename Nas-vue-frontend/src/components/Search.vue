<template>
    <div class="space">
        <div class="icon">
            搜索
        </div>
        <div class="searchItem">
            <input type="text" v-model="searchContent" placeholder="搜索盘内文件">
        </div>
     </div>
</template>

<script setup lang="ts">
    import { ref } from 'vue';
    const searchContent = ref('');
</script>

<style scoped>
    .space{
        width: 230px;
        height: 40px;
        /* background-color: aqua; */
        
        display: flex;
        flex-direction: row;
        border-radius: 8px;
        overflow: hidden;
        background-color: #F6F6F7;
    }
    .icon{
        width: 40px;
        height: 40px;
        /* background-color: aquamarine; */
        flex-shrink: 0;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .searchItem{
        flex-grow: 1;
        /* background-color: antiquewhite; */
        
    }
    input{
        width: 100%;
        height: 100%;
        border: none;
        outline: none;
        background-color: #F6F6F7;
        
    }
</style>