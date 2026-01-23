<template>
    <div>
        <div class="registerSpace">
            <div class="topContent">注册账户</div>
            <div class="accountSpace">
                账户：<input type="text" v-model="username">
            </div>
            <div class="passwordSpace">
                密码：<input type="password" v-model="password">
            </div>
            <button class="registerButton" @click="handleRegister">注册</button>
        </div>
    </div>
</template>

<script setup>
    import{ref} from 'vue'
    import { useRouter } from 'vue-router';
    import request from '@/utils/request';
    const router = useRouter()
    const username = ref('')
    const password = ref('')

    const handleRegister = async() =>{
        try{
            if(username.value == null){
                alert('用户名不能为空！')
                return ;
            }
            if(password.value.length < 6){
                alert('密码不少于六位')
                return;
            }
            if(username.value != null && password.value.length >=6){

            
            const res = await request.post('/register',{
                username:username.value,
                password:password.value
            })
            
            if(res.data.code === 200 ){
                alert('注册成功')
                router.push('/login')
            }else{
                alert(res.data.msg || '注册失败')
            }
        }
        }catch(e){
            console.error('注册失败：',e)
            alert('注册失败')
        }
    }



</script>

<style scoped>
    .registerSpace{
        width: 300px;
        height: 200px;
        background-color: aquamarine;
    }
    .topContent{
        width: 100%;
        height: 20%;
        background-color: aqua;
        font-size: 24px;
        font-weight: 900;
        text-align: center;
    }
    .accountSpace{
        width: 100%;
        height: 20%;
    }

</style>