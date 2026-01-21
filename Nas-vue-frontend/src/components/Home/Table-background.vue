<script setup lang="ts">
import {ref,onMounted} from 'vue';
import xueshanImg from '../../source/tableBackground/image/xueshan.png';
    const tableBackgroundImageUrl = ref(xueshanImg);
    
</script>

<template>
    <div class="table-background" :style="{backgroundImage:`url(${tableBackgroundImageUrl})`}">
        
    </div>
</template>
<style>
    .table-background {
        width: 100vw;
        height: 100vh;
        /* 删掉这里的 background-image */
        background-size: cover;

        /* 消除滚动条 */
        position: fixed;
        top: 0;
        left: 0;
        z-index: -1;
    }
</style>

