<template>
  <div class="container">
    <div class="topbar">
      <Search></Search>
    </div>
    <div class="topnextTitle">
      <span>相册</span>
    </div>
    <div class="galleryModel">
    </div>
    <div class="photos">
      <div v-for="group in galleryGroups" :key="group.title" class="itemsSpace">
          <div class="time-header">
            {{ group.title }}
          </div>
          <div class="grid-layout">
            <div v-for="file in group.files" :key="file.id" class="items" >
              <img :src="file.thumbnailUrl" alt="" @click="showPreview(file)">
              
              <div v-if="file.isVideo" class="video-indicator">
                  <span class="play-icon">▶</span>
                  <span class="duration">00:00</span> </div>
            </div>
          </div>
      </div>
    </div>
  </div>

 <Teleport to="body">
    <div class="lightBox" v-if="show" 
         @click="closePreview"
         @wheel.prevent="handleWheel"
         @mousemove="onUserActivity" 
         @mouseup="stopDrag"
         @mouseleave="stopDrag"
    >
      <div class="close-btn" @click.stop="closePreview">×</div>

      <div class="top-tools" @click.stop>
         <button @click="handleViewOriginal" title="查看原图">👁️ 原图</button>
         <button @click="handleDownload" title="下载原图">⬇️ 下载</button>
         <button @click="openDeleteDialog" title="删除图片" class="btn-danger">🗑️ 删除</button>
      </div>

      <template v-if="currentFile && !currentFile.isVideo">
        <img 
            ref="imgRef"
            :src="currentFile.previewUrl" 
            alt="" 
            @click.stop
            @mousedown="startDrag"
            :class="{ 'is-dragging': isDragging }"
            :style="imageStyle"
            draggable="false" 
        >
        <div class="tools" @click.stop>
            <button @click="zoomOut">-</button>
            <span>{{ (transform.scale * 100).toFixed(0) }}%</span>
            <button @click="zoomIn">+</button>
            <button @click="resetImage">重置</button>
        </div>
      </template>

      <div v-else-if="currentFile && currentFile.isVideo" class="video-player-container" @click.stop>
          <video 
            ref="videoRef"
            :src="videoSrc"
            class="video-element"
            @timeupdate="onTimeUpdate"
            @ended="onVideoEnded"
            @click="togglePlay"
          ></video>

          <div class="center-play-btn" v-if="!isPlaying" @click="togglePlay">
             ▶
          </div>

          <div class="video-controls" :class="{ 'fade-out': !isControlsVisible && isPlaying }" @click.stop>
              <div class="control-row">
                  <span class="btn-play-small" @click="togglePlay">{{ isPlaying ? '⏸' : '▶' }}</span>
                  <span class="time-text">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
                  
                  <input 
                    type="range" 
                    class="progress-bar" 
                    min="0" 
                    :max="duration" 
                    v-model="currentTime" 
                    @input="onSeekInput"
                    @change="onSeekChange"
                    :style="{ backgroundSize: progressPercent + '% 100%' }"
                  >
              </div>
          </div>
      </div>


      <div class="delete-modal" v-if="showDeleteConfirm" @click.stop>
          <h3>删除文件</h3>
          <p>请选择删除方式：</p>
          <div class="modal-actions">
              <button @click="handleDelete('soft')">♻️ 移至回收站</button>
              <button @click="handleDelete('hard')" class="btn-danger">❌ 彻底删除</button>
          </div>
          <button class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
      </div>

    </div>
  </Teleport>
</template>

<script setup lang="ts">
  import Search from './Search.vue';
  import { getFilePreviewApi, getFileThumbnailApi, type TimeGroup, type UserFile } from '@/api/fileApi';
  import { onMounted, ref ,computed,reactive,nextTick, watch} from 'vue'; // ✨ 引入 watch
  import request from '@/utils/request';
  import { getFileDataListApi ,getGalleryFileListApi,getGalleryTimeLineApi,deleteFileApi,deletePhysicalFileApi,BASE_URL} from '@/api/fileApi';

  // ... (保留之前的变量定义：showDeleteConfirm, galleryGroups, userId, groups 等) ...
  const showDeleteConfirm = ref(false);
  const galleryGroups = ref<TimeGroup[]>([]); 
  const userId = localStorage.getItem('userId')

  const currentFile = ref<UserFile | null>();
  const show = ref(false);
  const imgRef = ref<HTMLImageElement | null>(null);

  // ✨✨✨ 新增：视频相关变量 ✨✨✨
  const videoRef = ref<HTMLVideoElement | null>(null);
  const isPlaying = ref(false);
  const currentTime = ref(0);
  const duration = ref(0);
  const isControlsVisible = ref(true);
  let controlTimer: any = null;

  // 计算视频源地址 (直接使用流式接口，而不是Blob)
  const videoSrc = computed(() => {
     if(currentFile.value?.isVideo && userId){
         return `${BASE_URL}/file/${currentFile.value.id}?userId=${userId}&token=${localStorage.getItem('token')}`;
     }
     return '';
  });

  // 进度条百分比 (用于CSS样式)
  const progressPercent = computed(() => {
      if(duration.value === 0) return 0;
      return (currentTime.value / duration.value) * 100;
  });

  // 格式化时间 00:00
  const formatTime = (seconds: number) => {
      const m = Math.floor(seconds / 60);
      const s = Math.floor(seconds % 60);
      return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }

  // ✨✨✨ 视频交互逻辑 ✨✨✨

  // 1. 播放/暂停
  const togglePlay = () => {
      if(!videoRef.value) return;
      if(videoRef.value.paused){
          videoRef.value.play();
          isPlaying.value = true;
          startHideTimer(); // 开始计时隐藏控件
      } else {
          videoRef.value.pause();
          isPlaying.value = false;
          isControlsVisible.value = true; // 暂停时始终显示控件
          if(controlTimer) clearTimeout(controlTimer);
      }
  }

  // 2. 视频播放进度更新
  const onTimeUpdate = () => {
      if(videoRef.value) {
          currentTime.value = videoRef.value.currentTime;
          // 有些浏览器 metadata 加载慢，这里补充获取 duration
          if(duration.value === 0 && videoRef.value.duration){
              duration.value = videoRef.value.duration;
          }
      }
  }

  // 3. 拖拽进度条 (输入时暂停更新，释放时跳转)
  const onSeekInput = (e: Event) => {
      const val = Number((e.target as HTMLInputElement).value);
      currentTime.value = val;
      // 拖拽时不自动隐藏
      isControlsVisible.value = true;
      if(controlTimer) clearTimeout(controlTimer);
  }

  const onSeekChange = (e: Event) => {
      if(videoRef.value) {
          videoRef.value.currentTime = currentTime.value;
          if(isPlaying.value) {
            videoRef.value.play();
            startHideTimer();
          }
      }
  }

  // 4. 播放结束
  const onVideoEnded = () => {
      isPlaying.value = false;
      isControlsVisible.value = true;
  }

  // 5. 闲置检测逻辑
  const onUserActivity = () => {
      // 只有在播放视频时才处理
      if(currentFile.value?.isVideo){
          isControlsVisible.value = true;
          if(isPlaying.value){
              startHideTimer();
          }
      }
      // 如果是图片拖拽逻辑，这里复用之前的 onDrag ...
      onDrag(event as MouseEvent); 
  }

  const startHideTimer = () => {
      if(controlTimer) clearTimeout(controlTimer);
      controlTimer = setTimeout(() => {
          if(isPlaying.value) {
              isControlsVisible.value = false;
          }
      }, 2000); // 2秒后隐藏
  }
  
  // 监视 currentFile 变化，重置视频状态
  watch(currentFile, (newVal) => {
      if(newVal?.isVideo) {
          // 重置状态
          isPlaying.value = false;
          currentTime.value = 0;
          duration.value = 0;
          isControlsVisible.value = true;
      }
  });

  // ... (保留之前的 transform, isDragging, onMounted 等代码) ...

  const transform = reactive({
    scale: 1,
    x: 0,
    y: 0
  });
  
  const isDragging = ref(false);
  const startPos = { x: 0, y: 0 };
  const startOffset = { x: 0, y: 0 };

  onMounted(()=>{
    fetchGalleryData();
  })
  
  // ... (fetchGalleryData, loadThumbnailAndPre 等保持不变) ...
  const fetchGalleryData = async () => {
      const groups: TimeGroup[] = [];
      const res = await getGalleryFileListApi(userId)
       const allFiles = res.data
       if(!userId) return;
    for(const file of allFiles){
      const timeStr = file.shootTime ? file.shootTime : file.uploadTime;
      const date = new Date(timeStr);
      const title = `${date.getFullYear()}年 ${date.getMonth() + 1}月`;
      let currentGroup = groups.find(g => g.title === title);

      if(!currentGroup){
        currentGroup = {
          title : title,
          files:[]
        };
        groups.push(currentGroup);
      }
      currentGroup.files.push(file);
    }
    galleryGroups.value = groups;
  
    for(const group of galleryGroups.value){
        for(const file of group.files){
        loadThumbnailAndPre(file)
        }
    }
  }

  const loadThumbnailAndPre = async (file:UserFile) => {
    try{
      const resThumnail = await getFileThumbnailApi(file.id,userId);
      file.thumbnailUrl = URL.createObjectURL(resThumnail.data);
      
      // ✨ 优化：只有图片才去加载 Blob 预览，视频直接用 src 链接
      if(!file.isVideo) {
          const resPreview = await getFilePreviewApi(file.id,userId);
          file.previewUrl = URL.createObjectURL(resPreview.data);
      }
    }catch(e){
      console.error(e);
    }
  }

  // ... (handleViewOriginal, handleDownload, 删除逻辑保持不变) ...
  const handleViewOriginal = () => {
      if(!currentFile.value) return;
      const url = `${BASE_URL}/file/${currentFile.value.id}?userId=${userId}&token=${localStorage.getItem('token')}`;
      window.open(url, '_blank');
  }

  const handleDownload = () => {
      if(!currentFile.value) return;
      const url = `${BASE_URL}/download/${currentFile.value.id}?userId=${userId}&token=${localStorage.getItem('token')}`;
      const link = document.createElement('a');
      link.href = url;
      link.click();
  }

  const openDeleteDialog = () => {
      showDeleteConfirm.value = true;
  }
  
  const handleDelete = async (type: 'soft' | 'hard') => {
      if(!currentFile.value || !userId) return;
      try {
          if(type === 'soft') {
              await deleteFileApi(currentFile.value.id, userId);
              alert("已移至回收站");
          } else {
              if(!confirm("彻底删除后无法恢复，确定吗？")) return;
              await deletePhysicalFileApi(currentFile.value.id, userId);
              alert("已彻底删除");
          }
          closePreview();
          showDeleteConfirm.value = false;
          fetchGalleryData(); 
      } catch(e) {
          console.error(e);
          alert("删除失败");
      }
  }

  // ... (cursorStyle, imageStyle 计算属性保持不变) ...
  const cursorStyle = computed(() => {
    if (isDragging.value) return 'grabbing';
    if (transform.scale > 1) return 'grab'; 
    return 'default';
  });

  const imageStyle = computed(() => {
    return {
      transform: `translate(${transform.x}px, ${transform.y}px) scale(${transform.scale})`,
      transition: isDragging.value ? 'none' : 'transform 0.1s linear',
      cursor: cursorStyle.value
    }
  });

  const showPreview = async (file: UserFile) => {
    currentFile.value = file;
    show.value = true;
    resetImage(); // 视频打开时也会调用，不影响
    document.body.style.overflow = 'hidden';
    await nextTick();
  }

  const closePreview = () => {
    show.value = false;
    currentFile.value = null;
    document.body.style.overflow = 'auto';
    // ✨ 清除计时器
    if(controlTimer) clearTimeout(controlTimer);
    isPlaying.value = false;
  }

  const resetImage = () => {
    transform.scale = 1;
    transform.x = 0;
    transform.y = 0;
  }

  // ... (handleWheel, zoomIn, zoomOut, getBoundary, startDrag, onDrag, stopDrag, fixBoundary 保持不变) ...
  // 注意：onDrag 在上面被 onUserActivity 稍微包装了一下，确保功能兼容
  const handleWheel = (e: WheelEvent) => {
    // 只有非视频才允许缩放
    if(currentFile.value?.isVideo) return;
    
    const zoomSpeed = 0.1;
    let newScale = transform.scale;
    if (e.deltaY < 0) {
      newScale += zoomSpeed;
    } else {
      newScale -= zoomSpeed;
      if (newScale < 1) newScale = 1;
    }
    transform.scale = newScale;
    fixBoundary();
  }
  
  const zoomIn = () => { transform.scale += 0.2; fixBoundary(); }
  const zoomOut = () => { if(transform.scale > 1.2) transform.scale -= 0.2; else transform.scale = 1; fixBoundary(); }

  const getBoundary = () => {
    if (!imgRef.value) return { maxRangeX: 0, maxRangeY: 0 };
    const baseW = imgRef.value.offsetWidth;
    const baseH = imgRef.value.offsetHeight;
    const winW = window.innerWidth;
    const winH = window.innerHeight;
    const currentW = baseW * transform.scale;
    const currentH = baseH * transform.scale;
    const maxRangeX = Math.max(0, (currentW - winW) / 2);
    const maxRangeY = Math.max(0, (currentH - winH) / 2);
    return { maxRangeX, maxRangeY };
  }

  const startDrag = (e: MouseEvent) => {
    e.preventDefault();
    if (transform.scale <= 1) return;
    isDragging.value = true;
    startPos.x = e.clientX;
    startPos.y = e.clientY;
    startOffset.x = transform.x;
    startOffset.y = transform.y;
  }
  
  // onDrag 已被整合进 onUserActivity，或者你可以保留原名并在 template 中调用 onDrag
  const onDrag = (e: MouseEvent) => {
    if (!isDragging.value) return;
    const deltaX = e.clientX - startPos.x;
    const deltaY = e.clientY - startPos.y;
    let nextX = startOffset.x + deltaX;
    let nextY = startOffset.y + deltaY;
    const { maxRangeX, maxRangeY } = getBoundary();
    if (maxRangeX > 0) nextX = Math.min(Math.max(nextX, -maxRangeX), maxRangeX);
    else nextX = 0;
    if (maxRangeY > 0) nextY = Math.min(Math.max(nextY, -maxRangeY), maxRangeY);
    else nextY = 0;
    transform.x = nextX;
    transform.y = nextY;
  }

  const stopDrag = () => {
    isDragging.value = false;
  }

  const fixBoundary = () => {
     const { maxRangeX, maxRangeY } = getBoundary();
     transform.x = Math.min(Math.max(transform.x, -maxRangeX), maxRangeX);
     transform.y = Math.min(Math.max(transform.y, -maxRangeY), maxRangeY);
  }
</script>

<style  scoped>
.itemsSpace {
    margin-bottom: 20px;
}
.time-header {
    font-weight: bold;
    font-size: 18px;
    margin: 10px 0;
    padding-left: 10px;
}
.grid-layout {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding: 0 10px;
}
.items {
    width: 100px;
    height: 100px;
    border-radius: 4px;
    overflow: hidden;
    background-color: #f0f0f0;
}
.items img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    user-select: none;
    cursor: pointer;
}
/* Lightbox 样式 */
.lightBox {
    position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
    background-color: rgba(0, 0, 0, 0.9);
    z-index: 99999;
    display: flex; justify-content: center; align-items: center;
    overflow: hidden;
}

.lightBox img {
    /* 初始状态：保证完整显示 */
    max-width: 100vw;
    max-height: 100vh;
    object-fit: contain;
    user-select: none;
    /* 默认不允许拖拽，JS会控制 */
    touch-action: none; 
}

/* 按钮栏样式 */
.tools {
    position: absolute; bottom: 30px;
    background: rgba(255, 255, 255, 0.2);
    padding: 10px 20px; border-radius: 20px;
    display: flex; gap: 15px; align-items: center; z-index: 10000;
    backdrop-filter: blur(5px);
}
.tools button { background: rgba(0,0,0,0.5); color: white; border: none; padding: 5px 15px; cursor: pointer; }
.tools span { color: white; min-width: 50px; text-align: center; }
.close-btn { position: absolute; top: 20px; right: 30px; color: white; font-size: 40px; cursor: pointer; z-index: 10000; }
/* ✨✨✨ 新增：顶部功能栏样式 ✨✨✨ */
.top-tools {
    position: absolute; top: 20px; left: 50%; 
    transform: translateX(-50%); /* 居中 */
    background: rgba(255, 255, 255, 0.2);
    padding: 8px 20px; border-radius: 20px;
    display: flex; gap: 15px; align-items: center; z-index: 10000;
    backdrop-filter: blur(5px);
}
.top-tools button {
    background: transparent; border: 1px solid rgba(255,255,255,0.5);
    color: white; padding: 5px 12px; border-radius: 4px; cursor: pointer;
    font-size: 14px; transition: all 0.2s;
}
.top-tools button:hover {
    background: rgba(255,255,255,0.2);
}
.top-tools button.btn-danger {
    border-color: #ff4d4f; color: #ff4d4f;
}
.top-tools button.btn-danger:hover {
    background: #ff4d4f; color: white;
}

/* ✨✨✨ 新增：删除弹窗样式 ✨✨✨ */
.delete-modal {
    position: absolute;
    background: white;
    padding: 20px;
    border-radius: 8px;
    z-index: 10002;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    min-width: 250px;
}
.delete-modal h3 { margin-top: 0; color: #333; }
.delete-modal p { color: #666; margin-bottom: 20px; }
.modal-actions {
    display: flex; gap: 10px; justify-content: center; margin-bottom: 15px;
}
.modal-actions button {
    padding: 8px 15px; border: none; border-radius: 4px; cursor: pointer;
    background: #f0f0f0; color: #333;
}
.modal-actions button.btn-danger {
    background: #ff4d4f; color: white;
}
.btn-cancel {
    background: transparent; border: none; color: #999; cursor: pointer; text-decoration: underline;
}
/* ✨✨✨ 网格中的视频标识 ✨✨✨ */
.items {
    position: relative; /* 为子绝对定位元素做参考 */
}

.video-indicator {
    position: absolute;
    bottom: 5px;
    right: 5px;
    display: flex;
    align-items: center;
    gap: 4px;
    color: white;
    font-size: 10px;
    background-color: rgba(0, 0, 0, 0.5); /* 半透明黑底 */
    padding: 2px 6px;
    border-radius: 10px;
    pointer-events: none; /* 让点击穿透到图片上 */
}

.play-icon {
    font-size: 8px;
}

/* ✨✨✨ Lightbox 视频播放器样式 ✨✨✨ */
.video-player-container {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
}

.video-element {
    max-width: 90vw;
    max-height: 85vh; /* 留出一点空间给上下工具栏 */
    box-shadow: 0 0 20px rgba(0,0,0,0.5);
}

/* 中心大播放按钮 */
.center-play-btn {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80px;
    height: 80px;
    background-color: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-size: 40px;
    cursor: pointer;
    z-index: 10001;
    transition: all 0.2s;
    /* 让三角形稍微居中一点 */
    padding-left: 8px; 
}
.center-play-btn:hover {
    background-color: rgba(98, 124, 252, 0.8); /* 主题色 */
    transform: translate(-50%, -50%) scale(1.1);
}

/* 底部控制条 */
.video-controls {
    position: absolute;
    bottom: 80px; /* 在删除按钮上方 */
    left: 50%;
    transform: translateX(-50%);
    width: 60%;
    min-width: 300px;
    background-color: rgba(20, 20, 20, 0.7);
    backdrop-filter: blur(10px);
    border-radius: 30px;
    padding: 10px 20px;
    transition: opacity 0.5s ease;
    z-index: 10001;
}

/* 隐藏状态 */
.video-controls.fade-out {
    opacity: 0;
    pointer-events: none; /* 隐藏时不可点击 */
}

.control-row {
    display: flex;
    align-items: center;
    gap: 15px;
}

.btn-play-small {
    color: white;
    font-size: 20px;
    cursor: pointer;
    width: 20px;
    text-align: center;
}

.time-text {
    color: #ddd;
    font-size: 12px;
    min-width: 80px;
    font-family: monospace;
}

/* 自定义进度条 */
.progress-bar {
    flex-grow: 1;
    -webkit-appearance: none;
    height: 4px;
    background: #555;
    border-radius: 2px;
    outline: none;
    cursor: pointer;
    /* 进度条已播放部分的颜色技巧 */
    background-image: linear-gradient(#627CFC, #627CFC);
    background-repeat: no-repeat;
}

/* 进度条滑块样式 (Chrome/Safari) */
.progress-bar::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: white;
    cursor: pointer;
    transition: transform 0.1s;
}
.progress-bar::-webkit-slider-thumb:hover {
    transform: scale(1.5);
}
</style> 