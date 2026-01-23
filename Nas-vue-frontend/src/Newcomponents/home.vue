<script setup lang="ts">
    import Siderbar from './Homecomponents/Siderba.vue'; 
    import { ref } from 'vue';
    import Subhome from './Subhome.vue';


</script>
<template>
    <div class="bar">
        <Siderbar />
        <Subhome />
    </div>
    
    
</template>



<style scoped>
.bar{
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: row;
    overflow: hidden;
}
</style>