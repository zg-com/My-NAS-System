<template>
    <div class="container">
        <div class="leftSpace">
            <div class="allChoiceSpace" 
                 :class="{'active': yesOrAllChoice}" 
                 @click="allChoose"
                 title="全选">
            </div>
            <div class="subTitleSpace">
                <div class="titleItem">共 {{ sumCount }} 项</div>
            </div>
        </div>

        <div class="rightSpace">
            <Transition name="fade-slide">
                <div class="viewItemSpace" v-if="isSelectMode">
                    <div class="chooseCount">
                        <span>已选择</span> <span class="count-highlight">{{ chooseCount }}</span> <span>项</span>
                    </div>
                    
                    <div class="action-btn" @click="moveToLike" title="收藏">
                        收藏
                    </div>
                    <div class="action-btn" @click="downloadFiles" title="下载">
                        下载
                    </div>
                    <div class="action-btn delete" @click="deleteFiles" title="删除">
                         删除
                    </div>
                </div>
            </Transition>

            <div class="chooseMode">
                <button class="mode-btn" :class="{'is-active': isSelectMode}" @click="startChoose">
                    {{ isSelectMode ? '取消选择' : '选择' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { downloadFile } from '@/api/fileApi'

    //定义入参
    const props = defineProps({
        sumCount:{
            type:Number,
            default:0
        },
        yesOrAllChoice:{
            type:Boolean,
            default:false
        },
        isSelectMode:{
            type:Boolean,
            default:false
        },
        chooseCount:{
            type:Number,
            default:0
        }
    })
    //定义回参
    const emit = defineEmits(['Like','Delete','StartChoose','CloseChoose','AllChoose','DownLoad'])

    //-------------------函数部分------------------------
    //收藏
    const moveToLike = () => {
        emit('Like')
    }
    //删除
    const deleteFiles = () => {
        emit('Delete')
    }
    //下载
    const downloadFiles = () => {
        emit('DownLoad')
    }
    //开始选择
    const startChoose = () => {
        if(props.isSelectMode){
            emit('CloseChoose')
        }else{
            emit('StartChoose')
        }
    }
    //全选
    const allChoose = ()=>{
        emit('AllChoose')
    }
    
</script>

<style lang="scss" scoped>
    .container {
        width: 100%;
        height: 50px; // 稍微增加高度
        display: flex;
        justify-content: space-between; // 关键：两端对齐
        align-items: center;
        padding: 0 10px;
        background-color: transparent; // 透明背景更融合
        user-select: none;
        flex-direction: row !important; //
        flex-wrap: nowrap !important; // 绝对禁止换行

        .leftSpace {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-shrink: 0; // 关键：左侧禁止被压缩
            flex-direction: row;
            .allChoiceSpace {
                width: 22px;
                height: 22px;
                border-radius: 50%;
                border: 2px solid #DCDFE6;
                cursor: pointer;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                position: relative;
                background-color: #fff;

                &:hover {
                    border-color: #627CFC;
                    transform: scale(1.05);
                }

                &.active {
                    background-color: #627CFC;
                    border-color: #627CFC;
                    box-shadow: 0 2px 6px rgba(98, 124, 252, 0.4);

                    &::after {
                        content: '';
                        position: absolute;
                        left: 6px;
                        top: 2px;
                        width: 5px;
                        height: 10px;
                        border: 2px solid white;
                        border-top: 0;
                        border-left: 0;
                        transform: rotate(45deg);
                    }
                }
            }

            .subTitleSpace {
                font-size: 14px;
                color: #909399;
                font-weight: 500;
                white-space: nowrap; // 防止文字换行
            }
        }

        .rightSpace {
            display: flex;
            align-items: center;
            height: 100%;
            flex-shrink: 0; // 关键：右侧禁止被压缩
            white-space: nowrap; // 关键：禁止右侧内容换行

            .viewItemSpace {
                display: flex;
                align-items: center;
                margin-right: 20px;
                padding-right: 20px;
                border-right: 1px solid #EBEEF5; // 分割线
                gap: 15px;

                .chooseCount {
                    font-size: 14px;
                    color: #606266;
                    margin-right: 5px;
                    .count-highlight {
                        color: #627CFC;
                        font-weight: bold;
                        font-size: 16px;
                    }
                }

                .action-btn {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    padding: 6px 12px;
                    border-radius: 6px;
                    font-size: 13px;
                    color: #606266;
                    cursor: pointer;
                    transition: all 0.2s;
                    background-color: #f4f4f5;

                    &:hover {
                        background-color: #EBF2FF;
                        color: #627CFC;
                    }

                    &.delete:hover {
                        background-color: #FEF0F0;
                        color: #F56C6C;
                    }
                }
            }

            .chooseMode {
                .mode-btn {
                    padding: 8px 18px;
                    border-radius: 20px;
                    border: 1px solid #DCDFE6;
                    background-color: white;
                    color: #606266;
                    font-size: 13px;
                    cursor: pointer;
                    transition: all 0.3s;
                    white-space: nowrap;

                    &:hover {
                        border-color: #627CFC;
                        color: #627CFC;
                    }

                    &.is-active {
                        background-color: #627CFC;
                        color: white;
                        border-color: #627CFC;
                        box-shadow: 0 2px 8px rgba(98, 124, 252, 0.3);
                    }
                }
            }
        }
    }

    // Vue 动画类
    .fade-slide-enter-active,
    .fade-slide-leave-active {
        transition: all 0.3s ease;
    }

    .fade-slide-enter-from,
    .fade-slide-leave-to {
        opacity: 0;
        transform: translateX(20px); // 从右侧滑入/滑出
    }
</style>