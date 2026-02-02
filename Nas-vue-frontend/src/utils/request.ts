import router from "@/router";
import axios from "axios";

export const BASE_URL = 'http://192.168.3.132:8080'
// axios.defaults.baseURL = '/api'
const request = axios.create({
    // baseURL:'http://100.87.189.97:8080',
    // baseURL:'http://localhost:8080',
    // baseURL: '/api',
    baseURL:'http://192.168.3.132:8080/api',
    timeout:30000
})

//请求拦截器，每次请求带上token
request.interceptors.request.use(config =>{
    const token = localStorage.getItem('token')
    if(token){
        config.headers['Authorization'] = `Bearer ${token}` //对应后端jwt接收的字段
    }
    return config
})

//新增响应拦截器,判断token是否过期
request.interceptors.response.use(
    response => {
        return response;
    },
    error =>{
        if(error.response && error.response.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('isLogin')
            
            alert('登录已过期，请重新登录');

            router.push('/login')
        }
        return Promise.reject(error)
    }
    
)


export default request