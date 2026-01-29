<template>
    <Teleport to="body">
        <div class="window-mask" v-if="visible">
            <div class="windowSpace">
                <div class="titleSpace">新建文件夹</div>
                <div class="inputSpace">
                    <input type="text" v-model="folderName" placeholder="请输入文件夹名称" @keyup.enter="confirm">
                </div>
                <div class="choiceSpace">
                    <div class="cancle">
                        <button @click="close">取消</button>
                    </div>
                    <div class="confirm">
                        <button @click="confirm">确定</button>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>

<script setup lang="ts">
    import { ref, watch } from 'vue';

    const props = defineProps<{
        visible: boolean
    }>()

    // 定义它可以触发的事件：close用于关闭，confirm用于传回数据
    const emit = defineEmits(['close', 'confirm'])

    const folderName = ref('')

    // 监听 visible 变化，每次打开窗口时清空输入框，并自动聚焦（可选）
    watch(() => props.visible, (newVal) => {
        if (newVal) {
            folderName.value = ''
        }
    })

    const close = () => {
        emit('close')
    }

    const confirm = () => {
        if (!folderName.value.trim()) {
            alert("名称不能为空")
            return
        }
        // ✅ 核心点：emit 的第二个参数就是你要传给父组件的数据
        emit('confirm', folderName.value)
    }

</script>

<style lang="scss" scoped>
    /* 遮罩层，防止点击到底部内容 */
    .window-mask {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 2000;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .windowSpace {
        width: 340px;
        background-color: white;
        padding: 20px;
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        gap: 15px;

        .titleSpace {
            font-size: 18px;
            font-weight: 600;
            color: #333;
        }

        .inputSpace {
            input {
                width: 100%;
                height: 40px;
                border: 1px solid #ddd;
                border-radius: 6px;
                padding: 0 10px;
                outline: none;
                &:focus {
                    border-color: #627CFC;
                }
            }
        }

        .choiceSpace {
            display: flex;
            justify-content: flex-end;
            gap: 10px;

            button {
                padding: 8px 20px;
                border-radius: 6px;
                border: none;
                cursor: pointer;
                font-size: 14px;
            }

            .cancle button {
                background-color: #f5f5f5;
                color: #666;
            }

            .confirm button {
                background-color: #627CFC;
                color: white;
            }
        }
    }
</style>