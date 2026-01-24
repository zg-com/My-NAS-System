<script setup lang="ts">
    import Siderbar from '@/components/Siderba.vue'; 
    import { ref } from 'vue';


</script>
<template>
    <div class="container">
        <div class="sidebar">
            <Siderbar />
        </div>
        <div class="routerview">
            <router-view></router-view>
        </div>   
    </div>
    
    
    
</template>



<style scoped>
    .container{
        width: 100vw;
        height: 100vh;
        display: flex;
        flex-direction: row;
        overflow: hidden;
    }
    
.sidebar{
    flex-shrink:0; /**宽度固定 */
}
.routerview{
    flex-grow: 1;/**占用剩余  */
    height: 100%;
    overflow-y: auto;/**如果内容太长，只在内容区内部滚动，不影响侧边栏 */
    background-color: #f0f2f5;
}
</style>