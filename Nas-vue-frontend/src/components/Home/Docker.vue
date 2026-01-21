<script setup lang="ts">

</script>

<template>
    <div class="docker-space">
        <div class="docker-body">

            <div class="choiceItem"  style="margin-top: 50px;"></div>
            <div class="choiceItem"  ></div>
            <div class="choiceItem"  ></div>
            <div class="setup"></div>
        </div>
    </div>
</template>

<style>
    .docker-space{
        width: 80px;
        height: 90vh;
        display: flex;
        margin-left: 20px;
        background-color: rgba(250, 235, 215, 0.263);
        margin-top: 40px;
        
        justify-items: center;
    }
    .docker-body{
        width: 90%;
        height: 98%;
        background-color: aqua;
        
    }
    .choiceItem{
        width: 60px; 
        height: 60px; 
        background-color: blue;
        margin-bottom: 20px;
        margin-left: 6px;
        
    }
    .setup{
        width: 60px; 
        height: 60px;
        margin-bottom: 20px;
        margin-left: 6px;
        background-color: brown;
    }
    
</style>