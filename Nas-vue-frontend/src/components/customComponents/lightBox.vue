<template >
    <Teleport to="body" v-if="visible">
        
        <div class="boxSpace" 
            @wheel.prevent="handleWheel"
            @mousedown="startDrag"
            @mousemove="onDrag"
            @mouseup="stopDrag"
            @mouseleave="stopDrag"
            @touchstart="handleTouchStart"
            @touchend="handleTouchEnd"
            tabindex="0"
            ref="boxRef">
            <div class="topUtils">
                <div class="close" @click="closeLightBox">x</div>
                <div class="rightUtils">
                    <div class="showOrigin">
                        <button @click="showOrigin">查看原图</button>
                    </div>
                    <div class="downloadOrigin">
                        <button @click="downloadOrigin">下载原图</button>
                    </div>
                    <div class="deleteToBin">
                        <button @click="deleteToBin">删除</button>
                    </div>
                    <div class="showMoreDetails">
                        <button @click="showMoreDetails">更多信息</button>
                    </div>
                </div>
            </div>
            <div class="previewSpace">
                <div class="preButtonSpace">
                    <button @click="toPre"><</button>
                </div>
                <img ref="imgRef" :style="imgStyle" :src="currentFile?.originalUrl?currentFile?.originalUrl:file?.previewUrl" alt="" draggable="false" @dragstart.prevent >
                <div class="nextButtonSpace">
                    <button @click="toNext">></button>
                </div>
            </div>
            <div class="changeViewUtils"></div>
        </div>
    </Teleport>
    <deleteWindow :show-window="showDeleteWindow" 
                  :file = "file"
                  @close="closeDeleteWindow"
                  @deleteSuccess="deleteSuccess"
                  ></deleteWindow>
</template>

<script setup lang="ts">
    import { downloadFile, getOriginalFileApi, 
    type UserFile} from '@/api/fileApi';
    import { ref ,watch,computed,onMounted,onUnmounted} from 'vue';
    import deleteWindow from '@/components/customComponents/deleteWindow.vue';
    import { transform } from 'typescript';

    //定义这个组件可以接收的数据
    const props = defineProps<{
        visible:Boolean
        file?:UserFile
    }>()
    //定义发送的数据
    const emit = defineEmits(['close','needUpdate','NextItem','PreItem'])
    
    const closeLightBox = () =>{
        showDeleteWindow.value = false
        emit('close')//发送关闭信号
    }
    const showDeleteWindow = ref(false)
    const currentFile = ref<UserFile | undefined>(props.file)
    const userId = localStorage.getItem('userId')
    //缩放移动与手势相关
    const boxRef = ref<HTMLElement | null> (null)
    //获取预览的图片的dom元素
    const imgRef  = ref<HTMLImageElement | null> (null)
    //定义状态变量
    const scale = ref(1) //放大缩小倍数
    const translateX = ref(0) //X轴的偏移量
    const translateY = ref(0)//Y轴偏移量
    const isDragging = ref(false)//默认不是在拖拽状态
    const startX = ref(0)//拖拽起始的位置
    const startY = ref(0)
    
    //css样式计算
    const imgStyle = computed(()=>{
        return {
            transform:`translate(${translateX.value}px,${translateY.value}px) scale(${scale.value}) `,
            transition: isDragging.value? 'none' : 'transform 0.1s ease-out'//拖拽不加过渡动画更跟手一点 
        }
    })

    //键盘事件
    const handleKeydown = (e:KeyboardEvent) => {
        if(!props.visible) return
        if(e.key === 'ArrowLeft') toPre()
        if(e.key === 'ArrowRight') toNext()
        if(e.key === 'Escape') closeLightBox()
    }
    //滚轮缩放逻辑
    const handleWheel = (e:WheelEvent)=>{
        e.preventDefault();//阻止默认滚动行为
        let newScale = scale.value
        const zoomSensitivty = 0.2
        if(e.deltaY < 0){
            newScale = Math.min(scale.value + zoomSensitivty,10)//最大放大到10
        }else{
            newScale = Math.max(1.0,scale.value - zoomSensitivty) //最小缩放到0.1
        }

        //计算以鼠标为中心的位移补偿
        //获取鼠标在屏幕上的坐标
        const mouseX = e.clientX
        const mouseY = e.clientY
        //获取窗口中心坐标
        const centerX = window.innerWidth / 2
        const centerY = window.innerHeight / 2
        //计算缩放变化率
        const ratio = newScale / scale.value
        //更新偏移量
        translateX.value = translateX.value * ratio + (mouseX - centerX) * (1-ratio)
        translateY.value = translateY.value * ratio + (mouseY -centerY) * (1-ratio)
        scale.value = newScale
        limitBoundaries();//计算完记得看一下有没有越界，越界了给拉回来
    }
    //拖拽图片逻辑
    const startDrag = (e:MouseEvent) =>{
        if(scale.value <= 1) return
        isDragging.value = true
        startX.value = e.clientX - translateX.value
        startY.value = e.clientY - translateY.value
    }
    const onDrag = (e:MouseEvent) => {
        if(!isDragging.value) return
        e.preventDefault() //取消掉浏览器默认行为，比如默认滚动滚轮是上下页面滚动
        translateX.value = e.clientX - startX.value
        translateY.value = e.clientY - startY.value   
        limitBoundaries();//拖拽的时候也要记得检查边界
    }
    const stopDrag  = () => {
        isDragging.value = false
    }

    //滑动触摸逻辑
    let touchStartX = 0
    let touchStartTime = 0

    const handleTouchStart = (e:TouchEvent) => {
        if(e.touches.length === 1 && e.touches[0]){
            touchStartX = e.touches[0].clientX
            touchStartTime = Date.now()
        }
    }
    const handleTouchEnd = (e:TouchEvent) => {
        if(scale.value > 1 ) return 
        if(e.changedTouches[0]){
        const touchEndX = e.changedTouches[0]?.clientX 
                const diffX = touchEndX - touchStartX //滑动距离
                const timeCost = Date.now() - touchStartTime //滑动时间

                if(timeCost < 500 && Math.abs(diffX) > 50){
                    if(diffX > 0){
                        toPre()
                    }else{
                        toNext()
                    }
                }
        }
        

    }

    //图片移动限界函数
    const limitBoundaries = () => {
    if (!imgRef.value) return

        // 1. 获取窗口尺寸
        const winW = window.innerWidth
        const winH = window.innerHeight

        // 2. 获取图片在当前缩放下的实际渲染尺寸
        // offsetWidth 是图片的原始布局宽度，乘以 scale 才是当前视觉宽度
        const curWidth = imgRef.value.offsetWidth * scale.value
        const curHeight = imgRef.value.offsetHeight * scale.value

        // 3. 计算水平方向 (X轴) 限制
        if (curWidth > winW) {
            // 图片比窗口宽：允许拖动，但不能露出黑边
            // 计算最大允许的偏移量：(图片宽 - 窗口宽) / 2
            const limitX = (curWidth - winW) / 2
            
            // 如果偏移量超过了限制，就强行重置为最大限制
            if (translateX.value > limitX) translateX.value = limitX
            if (translateX.value < -limitX) translateX.value = -limitX
        } else {
            // 图片比窗口窄：强制水平居中，禁止左右拖拽
            translateX.value = 0
        }

        // 4. 计算垂直方向 (Y轴) 限制
        if (curHeight > winH) {
            // 图片比窗口高：允许拖动
            const limitY = (curHeight - winH) / 2
            
            if (translateY.value > limitY) translateY.value = limitY
            if (translateY.value < -limitY) translateY.value = -limitY
        } else {
            // 图片比窗口矮：强制垂直居中
            translateY.value = 0
        }
    }

    //ref只在组件加载的时候进行一次，所以
    watch(() => props.file,(newVal) => {
        currentFile.value = newVal
        resetImage() //清除放大缩小状态
    })
    const resetImage = () => {
        scale.value = 1 
        translateX.value = 0
        translateY.value = 0
    }
    //查看原图
    const showOrigin = async()=>{
        try{
            if(!currentFile.value){
                console.log("当前没有图片数据")
                return  
            } 
            const res = await getOriginalFileApi(currentFile.value.id,userId)
            currentFile.value.originalUrl = URL.createObjectURL(res.data)
        }catch(e){
            console.error('获取原图失败：',e)
            alert("获取原图失败")
        }
    }
    //下载原图
    const downloadOrigin = async()=>{
         try{             
            if(!currentFile.value || !props.file){
                console.log("当前没有图片数据")
                return  
            } 
            const ref = await getOriginalFileApi(props.file?.id,userId)
            const downloadUrl = URL.createObjectURL(ref.data)
            //创建一个a标签
            const link = document.createElement('a')
            link.href = downloadUrl
            link.target = '_blank'
            //将创建的标签放入文档流
            document.body.append(link);
            //配置a属性，让浏览器下载
            link.download = currentFile.value.filename || 'download.png'
            //触发点击
            link.click()
            //清理文档流
            document.body.removeChild(link)
            URL.revokeObjectURL(downloadUrl)

         }catch(e){
            console.error("下载图片失败",e)
            alert("下载失败")
         }
    }
    //删除弹窗关闭
    const closeDeleteWindow =()=>{
        showDeleteWindow.value = false
    }
    //软删除
    const deleteToBin = ()=>{
        showDeleteWindow.value = true
    }
    //物理删除
    const deletePhysically = ()=>{
        
    }
    //更多信息
    const showMoreDetails = ()=>{
        
    }
    //删除成功关闭窗口
    const deleteSuccess = () => {
        emit('close')
        emit('needUpdate')
    }
    //上一张
    const toPre = () => {
        resetImage()
        emit('PreItem')
    }
    //下一张
    const toNext = () => {
        resetImage()
        emit('NextItem')
    }
    onMounted(()=>{
        window.addEventListener('keydown',handleKeydown)
    })
    onUnmounted(()=>{
        window.removeEventListener('keydown',handleKeydown)
    })
</script>

<style lang="scss" scoped>
/* 核心容器：全屏固定，深色背景营造沉浸感
*/
.boxSpace {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  // 现代化：深色半透明背景 + 毛玻璃效果 (如果浏览器支持)
  background-color: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(20px); 
  // 确保 Lightbox 在最顶层
  z-index: 999; 
  
  // 使用 Flex 布局来组织内容：上(工具栏)-中(图片区域)-下(预留区域)
  display: flex;
  flex-direction: column;
  // 允许该容器聚焦以接收键盘事件，且不显示默认的聚焦边框
  outline: none; 

  // --- 通用按钮样式重置与美化 ---
  button {
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.8); //稍微一点点透明的白色，不那么刺眼
    cursor: pointer;
    padding: 8px 16px;
    font-size: 14px;
    border-radius: 6px; // 圆角看起来更现代柔和
    transition: all 0.2s ease; // 按钮交互的流畅过渡

    &:hover {
      color: #fff;
      background-color: rgba(255, 255, 255, 0.15); // hover时微亮背景
    }
    
    &:active {
      transform: scale(0.96); // 点击时的微小按压反馈
    }
  }


  /* =========================================
     1. 顶部工具栏 (Top Layer, Z-index: 10)
  ========================================= */
  .topUtils {
    // 固定在顶部，不随图片滚动/缩放而移动
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 60px; // 给一个固定高度
    display: flex;
    justify-content: space-between; // 两端对齐：关闭在左，工具在右
    align-items: center;
    padding: 0 24px;
    box-sizing: border-box;
    // 关键：层级高于图片
    z-index: 10; 
    // 可选：给顶部栏一个渐变背景，让文字更清晰
    background: linear-gradient(to bottom, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 100%);

    .close {
      font-size: 32px;
      color: rgba(255, 255, 255, 0.7);
      cursor: pointer;
      transition: color 0.2s;
      line-height: 1;
      padding: 8px;
      &:hover {
        color: #fff;
      }
    }

    .rightUtils {
      display: flex;
      gap: 8px; // 按钮之间的间距
      
      // 针对特定按钮的微调（如果需要）
      .deleteToBin button {
        &:hover {
           background-color: rgba(255, 69, 58, 0.2); // 删除按钮 hover 变红
           color: #ff453a;
        }
      }
    }
  }


  /* =========================================
     2. 图片预览区域 (Middle Layer)
  ========================================= */
  .previewSpace {
    // 占据剩余空间
    flex: 1;
    position: relative; // 作为内部绝对定位元素的参考点
    display: flex;
    justify-content: center; // 水平居中
    align-items: center;     // 垂直居中
    overflow: hidden; // 这一点很重要：防止图片放大后撑出屏幕出现滚动条

    /* --- 图片本体 (Z-index: 1) --- */
    img {
      // 初始状态适应屏幕，不超过屏幕
      max-width: 100%;
      max-height: 100%;
      object-fit: contain; // 保持比例
      
      // 关键：层级低于按钮
      z-index: 1; 

      // 关键：优化交互流畅度
      // 当 JS 改变 transform 时，浏览器会用 0.3s 平滑过渡
      // 注意：JS实现拖拽功能时，在拖拽过程中可能需要临时把这个 transition 设为 none，否则不跟手
      transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94); 
      
      // 设置变换中心点在图片中心
      transform-origin: center center;
      
      // 防止用户选中图片本身，提升拖拽体验
      user-select: none; 
      -webkit-user-drag: none;
    }


    /* --- 左右导航按钮 (Top Layer, Z-index: 10) --- */
    // 定义左右按钮容器的公共样式
    .preButtonSpace, .nextButtonSpace {
      position: absolute;
      top: 50%; // 垂直居中
      transform: translateY(-50%); // 通过位移修正自身的50%高度来实现完美居中
      z-index: 10; // 关键：层级高于图片

      button {
        // 将导航按钮做成大圆圈箭头形状
        width: 56px;
        height: 56px;
        border-radius: 50%;
        background-color: rgba(255, 255, 255, 0.1); // 默认很淡的背景
        font-size: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0; // 重置掉上面通用的 padding

        &:hover {
          background-color: rgba(255, 255, 255, 0.3); // hover时变亮
          transform: scale(1.1); // 稍微放大一点强调
        }
        
        &:active {
           transform: scale(0.95);
        }
      }
    }

    .preButtonSpace {
      left: 24px; // 距离左边的距离
      // 优化：如果鼠标在最左侧，箭头往左边动一下提示
      &:hover button { padding-right: 4px; } 
    }

    .nextButtonSpace {
      right: 24px; // 距离右边的距离
      // 优化：如果鼠标在最右侧，箭头往右边动一下提示
      &:hover button { padding-left: 4px; }
    }
  }

  /* =========================================
     3. 底部预留区域 (Bottom Layer)
  ========================================= */
//   .changeViewUtils {
//     // 如果未来这里放缩略图导航条，可以在这里写样式
//     // 目前暂留空，高度为0或自适应
//     // height: 80px; 
//     // background: rgba(0,0,0,0.5);
//   }
}

/* 针对移动端的简单优化 */
@media (max-width: 768px) {
  .topUtils {
    padding: 0 12px; // 移动端减少边距
    .rightUtils button {
      padding: 8px; // 移动端按钮只显示图标或精简文字，减少padding
      font-size: 12px;
    }
  }
  // 移动端通常用手势滑动，可以隐藏左右大按钮，或者把它们做小一点
  .previewSpace .preButtonSpace, 
  .previewSpace .nextButtonSpace {
      display: none; // 移动端隐藏点击按钮，强迫使用手势（假设你实现了手势）
      // 或者做小一点:
      // button { width: 40px; height: 40px; font-size: 18px; }
      // left: 10px; right: 10px;
  }
}
</style>