<template>
    <div class="addfile">
        <addFiles
            :scene="currentStyleOfPosition"></addFiles>
    </div>
    <div class="container">
        <div class="search">
            <Search></Search>
        </div>
        <div class="managerSpace">
            <div class="topbarSpace">
                <div class="topbar" >
                    <div class="titleItem" v-for="item in fileFolderMemory" :key="item.currentParentId">
                        <div class="folderTile"></div>
                        <div class="row" v-if="item.currentParentId != null"></div>
                    </div>
                </div>
            </div>
            <div class="folderModleAndDetails"></div>
            <div class="folderAndFilesSpace">
                <div class="item" v-for="item in currentFileList" :key="item.id">
                        <div class="viewIconIsFold" v-if="item.isFolder"></div>
                        <div class="viewIcaon" v-if="!item.isFolder"></div>
                        <div class="title"></div>
                        <div class="fileDate"></div>
                    </div>
            </div>
        </div>

    </div>
    

</template>

<script setup lang="ts">
    import addFiles from './customComponents/addFiles.vue';
    import { ref,onMounted } from 'vue';
    import type {UserFile } from '@/api/fileApi';
    import Search from './customComponents/Search.vue';
    //----------------数据部分------------------
    const currentStyleOfPosition = 'file'
        //存自己的上一级的parentId和自己的
    const fileFolderMemory = ref<UserFile[]> ([
        {title:"全部文件",currentParentId:null}
    ])
        //当前文件列表内容
    const currentFileList = ref<UserFile[]>([])
    //----------------方法部分------------------
        //获取文件列表
        
        //跳转文件列表
</script>

<style scoped>

</style>