<template>
    <div>
        <div class="topspace">
            <div class="search">
                <div class="searchIcon">搜索</div>
                <input type="text" name="" id="">
            </div>
            <div class="remainSpace">
                <div class="context"></div>
                <div class="remainSpaceDetail">
                    <div class="topdata">
                        <div class="data"></div>
                        <div class="manage"></div>
                    </div>
                    <div class="dataLine"></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">

</script>

<style scoped>
    .topspace{
        width: 100%;
        height: 80px;
        /* background-color:rgb(245, 245, 52); */
        display: flex;
        align-items: center;
        justify-content:space-between;
    }
    .search{
        width: 300px;
        height: 40px;
        /* background-color: antiquewhite; */
        display: flex;
        align-items: center;
        justify-content:baseline;
        background-color: rgba(234, 233, 233, 0.605);
        margin-left: 40px;
    }
    .searchIcon{
        width: 40px;
        margin-right: 10px;
    }
    .search input{
        height: 100%;
        width: 100%;
        border:0;
        background-color: rgba(234, 233, 233, 0.605);
    }
    .remainSpace{
        width: 230px;
        height: 40px;
        background-color: antiquewhite;
        margin-right: 40px;
    }
</style>