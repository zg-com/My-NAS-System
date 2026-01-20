<script setup lang="ts">
import { ref, onUnmounted } from 'vue';

// 1. 接收父组件传来的初始位置
// 以前我们是写死 top-20 left-20，现在要让父组件告诉我们从哪出来
const props = defineProps<{
  id: number;
  title: string;
  initialX: number; // 初始横坐标
  initialY: number; // 初始纵坐标
  zIndex: number; // 1. 新增：接收层级数值
}>();

const emit = defineEmits<{
  (e: 'close-window', id: number): void
  (e: 'focus', id: number): void; // 2. 新增：通知父亲"我被聚焦了"
}>();

// 2. 定义窗口的实时位置
// 我们用 ref 来存当前窗口的 x (left) 和 y (top)
// 初始值就用父亲传进来的
const position = ref({
  x: props.initialX,
  y: props.initialY
});

// --- 核心拖拽逻辑 ---

let isDragging = false; // 标记是否正在拖拽
let dragOffset = { x: 0, y: 0 }; // 鼠标距离窗口左上角的偏移量

// A. 开始拖拽 (按下鼠标)
const startDrag = (event: MouseEvent) => {
  isDragging = true;
  
  // 计算偏移量：鼠标坐标 - 窗口当前坐标
  // 比如窗口在 x=100，鼠标点在 x=120，偏移量就是 20
  dragOffset.x = event.clientX - position.value.x;
  dragOffset.y = event.clientY - position.value.y;

  // 在全屏幕上监听移动和松开
  // 为什么要绑定到 document？因为鼠标甩太快会移出窗口区域！
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
};

// B. 正在拖拽 (鼠标移动)
const onDrag = (event: MouseEvent) => {
  if (!isDragging) return;

  // 新位置 = 当前鼠标位置 - 偏移量
  position.value.x = event.clientX - dragOffset.x;
  position.value.y = event.clientY - dragOffset.y;
};

// C. 停止拖拽 (松开鼠标)
const stopDrag = () => {
  isDragging = false;
  // 移除监听，打扫战场
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
};

// 组件销毁时，确保监听器都被移除 (防止内存泄漏)
onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
});
</script>
<template>
  <div 
    class="fixed w-[800px] h-[500px] bg-white/90 backdrop-blur-xl rounded-xl shadow-2xl border border-white/20 flex flex-col overflow-hidden animate-pop-in"
    :style="{ 
      left: position.x + 'px', 
      top: position.y + 'px' ,
      zIndex: zIndex
      
    }"
    @mousedown="emit('focus', id)"
  >
    
    <div 
      @mousedown="startDrag"
      class="h-10 bg-gray-100/50 border-b border-gray-200 flex items-center justify-between px-4 select-none cursor-move"
    >
      <span class="text-sm font-medium text-gray-600">{{ title }}</span>
      
      <button 
        @mousedown.stop
        @click="emit('close-window', id)"
        class="w-4 h-4 rounded-full bg-red-500 hover:bg-red-600 flex items-center justify-center text-xs text-transparent hover:text-white transition-colors"
      >
        ×
      </button>
    </div>

    <div class="flex-1 overflow-auto p-4">
      <slot></slot>
    </div>

  </div>
</template>

<style scoped>
@keyframes pop-in {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.animate-pop-in {
  animation: pop-in 0.2s ease-out;
}
</style>