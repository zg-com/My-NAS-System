<script setup lang="ts">
import { ref } from 'vue';
import { login } from '../api/auth'; // 引入刚才写的接口

// 绑定输入框的变量
const username = ref('');
const password = ref('');
const isLoading = ref(false); // 加载状态 (转圈圈)

// 登录按钮点击事件
const handleLogin = async () => {
  if (!username.value || !password.value) {
    alert('请输入账号密码');
    return;
  }

  isLoading.value = true;

  try {
    // 1. 调用后端接口
    // 这里会自动触发 /api/login -> 代理转发 -> Java:8080/login
    const res = await login({
      username: username.value,
      password: password.value
    });

    // 2. 判断结果
    if (res.code === 200) {
      alert('登录成功！欢迎 ' + res.username);
      
      // 3. 【关键】把 Token 存起来！
      // 以后所有的请求都会自动带上这个 Token
      localStorage.setItem('token', res.token);
      localStorage.setItem('user', JSON.stringify(res));

    } else {
      alert('登录失败');
    }
  } catch (error) {
    alert('网络错误或账号密码错误');
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="flex flex-col items-center justify-center h-full space-y-6">
    <div class="w-20 h-20 bg-blue-500 rounded-full flex items-center justify-center text-4xl shadow-lg">
      🔒
    </div>

    <div class="w-2/3 space-y-4">
      
      <div>
        <label class="block text-sm text-gray-500 mb-1">账号</label>
        <input 
          v-model="username"
          type="text" 
          class="w-full px-4 py-2 bg-gray-100 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          placeholder="请输入用户名"
        >
      </div>

      <div>
        <label class="block text-sm text-gray-500 mb-1">密码</label>
        <input 
          v-model="password"
          type="password" 
          class="w-full px-4 py-2 bg-gray-100 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          placeholder="请输入密码"
          @keyup.enter="handleLogin"
        >
      </div>

      <button 
        @click="handleLogin"
        :disabled="isLoading"
        class="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors shadow-md disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center"
      >
        <span v-if="isLoading">登录中...</span>
        <span v-else>进入系统</span>
      </button>

    </div>
  </div>
</template>