import { createRouter,createWebHashHistory,createWebHistory } from "vue-router";

//引入自己的组件
import login from "@/views/Login/login.vue";
import home from "@/layout/home.vue";
import register from "@/views/Login/register.vue";
import dataPage from "@/views/Pages/dataPage.vue";
import homePage from "@/views/Pages/homePage.vue";
import filePage from "@/views/Pages/filePage.vue";

const router = createRouter({
    history:createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path:'/',//根路径，默认登录页
            redirect:'login' //重定向到/login
        },
        {
            path:'/login',//网址
            name:'login',//给路由起一个名字，方便调用
            component:login //对应显示的组件
        },
        {
            path:'/home',
            name:'home',
            redirect:'/home/homePage',
            component:home,
            children:[
                {
                    path:'homePage',
                    name:'homePage',
                    component: homePage
                },
                {
                    path:'filePage',
                    name:'filePage',
                    component:filePage 
                },
                {
                    path:'dataPage',
                    name:'dataPage',
                    component:dataPage 
                }
            ]
        },
        {
            path:'/register',
            name:'register',
            component:register 
        }
    ]
})

//防止不登陆就通过/home访问
router.beforeEach((to,from,next)=>{
    const token = localStorage.getItem('token')
    //如果要去主页，没有token就返回登录页
    if(to.name ==='home'){
        if(!token){
            next({ name : 'login'})
        }
        else{
            next();
        }
    }else{
        next();
    }
    
})

export default router