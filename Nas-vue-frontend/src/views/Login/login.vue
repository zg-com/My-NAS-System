<script setup lang="ts">
    import {ref } from 'vue'
    import { useRouter } from 'vue-router';
    import request from '@/utils/request';

    const router = useRouter()
    const username = ref('')
    const password = ref('')
    

    const handleLoin = async () => {
        try{
            //调用后端登录接口
            const res = await request.post('/login',{
                username:username.value,
                password:password.value
            })

            if(res.data.code === 200){
                localStorage.setItem('token',res.data.token)
                localStorage.setItem('isLogin','true')
                localStorage.setItem('userId',res.data.userId)

                alert('登录成功')
                router.push('/home')
            }else{
                alert(res.data.msg || '登陆失败')
            }
        }catch(e){
                console.error('登陆失败:',e);
                alert('登录失败')
            }
    }
    
    
    
    
</script>

<template>
    <div class="backgroundImage">
        <div class="loginTable">
            <div class="login-title">欢迎登陆</div>
            <div class="login-form">
                <div class="input-username">
                    账号:<input v-model="username" type="text">
                </div>
                <div class="input-password">
                    密码:<input v-model="password" type="password">
                </div>
                <div class="login-choice">
                    <div class="forget-password">
                        <a href="">忘记密码</a>
                    </div>
                    <div class="register-court">
                        <a href="" @click="router.push('/register')">注册</a>
                    </div>
                </div>
                <button class="login-button" @click="handleLoin">登录</button>
            </div>
        </div>
    </div>
</template>

<style>

</style>