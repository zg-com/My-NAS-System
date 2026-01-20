import axios from "axios";

//创建axiox实例
const service = axios.create({
    // 基础路径：所有请求都会自动加上 /api 前缀
  // 配合刚才配置的 vite proxy，请求会发给 http://localhost:5173/api/...
  baseURL: '/api', 
  timeout: 5000 // 超时时间：5秒
})

//请求拦截器
//在发送请求之前，先做点事
service.interceptors.request.use(
    (config) => {
        //从浏览器中拿token
        const token = localStorage.getItem('token');
    
    if(token){
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
},
(error) => {
    return Promise.reject(error);
}
)

//响应拦截器
//收到后端回复后，先过滤一遍
service.interceptors.response.use(
    (response) => {
        //获取返回数值
        const res = response.data;
        return res;
    },
    (error) => {
        console.error('请求出错：',error);
        return Promise.reject(error);
    }
);

export default service;