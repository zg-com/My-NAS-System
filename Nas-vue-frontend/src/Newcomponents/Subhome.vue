<template>
    <div class="Subhome">
        <Subsidebar />
        <div class="subHomeMain">
            <SubhomeMain />
        </div>
    </div>
</template>

<script setup lang="ts">
import Subsidebar from './Homecomponents/Subsidebar.vue';
import SubhomeMain from './Homecomponents/Subhome-main.vue';
</script>


<style scoped>
.Subhome{
    width: 100%;
    height: 100%;
    display: flex;
}  
.subHomeMain{
    width: 100%;
    height: 100%;
}  

</style>