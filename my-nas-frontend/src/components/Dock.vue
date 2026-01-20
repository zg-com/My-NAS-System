<script setup lang="ts">
    import {ref} from 'vue';

    //定义Dock栏上的应用信息
    const apps = ref([
        { id: 99, name: '登录', icon: '👤' }, // ID 99 对应我们在 App.vue 里的判断
        {id : 1,name:'文件',icon:'📂'},
        { id: 2, name: '相册', icon: '🖼️' },
        { id: 3, name: '设置', icon: '⚙️' },
        { id: 4, name: '回收站', icon: '🗑️' },
    ]);

    const emit = defineEmits<{
  (e: 'open-app', app: any): void
}>();

</script>

<template>
    <div class="fixed bottom-4 left-1/2 -translate-x-1/2 px-4 py-3 rounded-2xl flex gap-4 bg-black/20 backdrop-blur-md border border-white/10 shadow-lg z-50">
    <div 
        v-for = "app in apps" 
        :key="app.id"
        class="w-12 h-12 bg-white/10 rounded-xl flex justify-center items-center text-2xl cursor-pointer hover:bg-white/20 hover:scale-110 transition-all duration-300 relative group" 
        @click="emit('open-app',app)"
        >
        {{ app.icon }}
        <div class="absolute -top-10 bg-black/80 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity">
            {{ app.name }}
        </div>
    </div>
    </div>


</template>

<style>

</style>