<template>
    <div class="container" v-show="visible">
        <div class="choice" :class="{'active': choice}"></div>
    </div>
</template>

<script setup lang="ts">
    //定义入参
    const props = defineProps({
        visible:{
            type:Boolean,
            default:false
        },
        choice:{
            type:Boolean,
            default:false
        }
    })
    //定义出参
    const emit = defineEmits(['Choice'])
</script>

<style lang="scss" scoped>
    .container {
        position: absolute; 
        top: 8px;  // 左上角定位
        left: 8px; 
        width: 24px; // 减小容器尺寸，避免遮挡太多图片
        height: 24px;
        z-index: 20; 
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer; 
        pointer-events: auto; // 恢复点击响应

        .choice {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background-color: rgba(255, 255, 255, 0.9); 
            border: 2px solid #DCDFE6;
            transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275); // 弹性动画
            position: relative;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);

            &:hover {
                border-color: #627CFC;
                transform: scale(1.1);
            }

            &.active {
                background-color: #627CFC;
                border-color: #627CFC;
                transform: scale(1.1);
                
                // 对号样式
                &::after {
                    content: '';
                    position: absolute;
                    left: 5px;
                    top: 2px;
                    width: 5px;
                    height: 9px;
                    border: 2px solid white;
                    border-top: 0;
                    border-left: 0;
                    transform: rotate(45deg);
                }
            }
        }
    }
</style>