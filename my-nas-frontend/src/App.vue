<!-- 写逻辑的地方 ,setup 是实现自动处理繁琐配置，我直接写变量与函数就可以了-->
<script setup lang="ts">

//引入ref工具，是vue的核心，把普通数据变成响应式数据，类似于arkts中@Local，数据变，UI刷新
import { ref, shallowRef } from 'vue';
import Dock from './components/Dock.vue';
import Window from './components/Window.vue';
import LoginApp from './components/LoginApp.vue';

//定义壁纸链接，使用ref包裹字符串
const backgroundUrl = ref('https://images.pexels.com/photos/17590723/pexels-photo-17590723.jpeg')

// --- 窗口管理系统核心逻辑 ---

// 定义一个类型，描述"打开的窗口"长什么样
interface OpenWindow {
  id: number;
  title: string;
 component: any; // 以前是 string content，现在改成组件
  x: number; // 新增
  y: number; // 新增
  zIndex: number; // 1. 新增字段
}
  // 定义应用类型
interface AppItem {
  id: number;
  name: string;
  icon: string;
  component: any; // 新增字段：每个应用对应的组件
}
// 2. 定义一个数组，存储当前屏幕上打开的所有窗口
const openWindows = ref<OpenWindow[]>([]);
// 2. 定义全局层级计数器
// 为什么从 10 开始？因为 Dock 栏可能是 z-50，如果我们希望窗口盖住 Dock，这里就得大于 50。
// 如果希望窗口永远在 Dock 下面，这里可以小一点。我们先设 10。
let nextZIndex = 10;

// 模拟 Dock 数据 (这里实际上应该传给 Dock 组件，或者用 Pinia 管理)
// 为了演示简单，我们先在这里定义好，然后传给 Dock (你需要去 Dock.vue 里把 apps 改成 defineProps 接收)
// 或者更简单：直接修改 Dock.vue 里的 apps 数据：

/* 请去 src/components/Dock.vue 修改 apps 数组，增加一个登录：
   { id: 99, name: '用户登录', icon: '👤' }
*/

// --- 关键：根据 ID 决定显示什么组件 ---
// 这是一个简单的工厂模式
const getComponentById = (id: number) => {
  if (id === 99) return LoginApp; // 如果 ID 是 99，就返回登录组件
  return null; // 其他的暂时返回 null
}
// 3. 打开窗口的函数 (响应 Dock 的呼叫)
const handleOpenApp = (app: any) => {
  // 检查是否已经打开了？如果打开了就不重复打开 (以后可以做成窗口置顶)
  const exists = openWindows.value.find(w => w.id === app.id);
  if (exists) {
    // 3. 优化：如果窗口已经打开了，再次点击 Dock 图标时，应该让它"聚焦"（跑到最前）
    handleFocusWindow(exists.id);
    return;
  }
  // 2. 计算新窗口的初始位置
  // 比如：第一个窗口在 (100, 100)，第二个在 (130, 130)，第三个在 (160, 160)
  // 这样能形成一种"层叠"的视觉效果
  const offset = openWindows.value.length * 30; 
  const startX = 100 + offset;
  const startY = 100 + offset;
  // 获取对应的组件
  const comp = getComponentById(app.id);

  // 如果没有做组件，就先不打开 (或者弹个提示)
  if (!comp) {
    alert("这个功能还没做呢！先试试【用户登录】吧");
    return;
  }

  // 往数组里推入一个新的窗口对象
  openWindows.value.push({
    id: app.id,
    title: app.name,
    component:shallowRef(comp),
    x: startX, // 传入
    y: startY,  // 传入
    zIndex: ++nextZIndex // 4. 新窗口直接拿最新的号牌 (先自增再赋值)
  });



};

  // 4. 关闭窗口的函数 (响应 Window 的呼叫)
const handleCloseWindow = (id: number) => {
  // 过滤掉那个 id 的窗口，相当于把它删了
  openWindows.value = openWindows.value.filter(w => w.id !== id);
};

// --- 5. 新增：聚焦窗口逻辑 ---
const handleFocusWindow = (id: number) => {
  const win = openWindows.value.find(w => w.id === id);
  if (win) {
    // 如果它当前的层级已经是最大的了，就不用动了（省点计算）
    if (win.zIndex === nextZIndex) return;
    
    // 否则，拿个新号牌
    win.zIndex = ++nextZIndex;
  }
};


</script>

<!-- 写HTML的地方 -->
<template>
<!-- 使用一个最常用的指令v-bind，简写为冒号:,将JS中的变量绑定到HTML属性中 -->
 <main class="h-screen w-screen bg-cover bg-center overflow-hidden"
        :style="{backgroundImage:`url(${backgroundUrl})`}">
        <Window 
      v-for="win in openWindows"
      :key="win.id"
      :id="win.id"
      :title="win.title"
      :initialX="win.x" 
      :initialY="win.y",
      :zIndex="win.zIndex"
      @close-window="handleCloseWindow"
      @focus="handleFocusWindow"
    >
      <component :is="win.component"></component>
    </Window>

    <Dock @open-app="handleOpenApp" />
      </main>



</template>

<!-- 写css的地方 ,scoped表示样式只在这个文件里生效，不会污染别的组件-->
 <style scoped>

 </style>