/*主要就是起到一个网关的作用，让可以通过浏览器进行访问，触发存数据的操作*/
package com.nas.cloud.controller;

import com.nas.cloud.entity.User;
import com.nas.cloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//告诉Spring，我是负责处理HTTP请求的
@RestController
public class HelloController {
    //自动注入，让spring把做好的UserRepository塞给我们用
    @Autowired
    private UserRepository userRepository;
    //映射URL：http://localhost:8080/add-user?name = xx
    @GetMapping("/hello")
    public String sayHello(){
        return "Hello!咱的Nas后端已经跑起来了！";
    }

    @GetMapping("/add-user")
    public String addUser(@RequestParam String name){
        User user = new User();
        user.setUsername(name);
        user.setPassword("123456");

        //调用，实现讲数据转换SQL写入数据库
        userRepository.save(user);
        return "成功添加用户：" + name;
    }
    //测试一下数据库能不能写入

}
