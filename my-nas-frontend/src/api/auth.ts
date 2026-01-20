import request from '../utils/request';

//定义登录需要的数据类型
interface LoginParams{
    username:string;
    password:string;
}

//定义登录返回类型
interface LoginResult {
    code : number;
    msg : string;
    token:string;
    userId:number;
    username:string;
    role:string;
}

//导出登录函数
export function login(data:LoginParams){
    //发送请求到/login
    //<LoginResult>告诉TS，返回类型数据长什么样子
    return request.post<any,LoginResult>('/login',data);
}