//配置线程池，防止同时多人上传CPU爆满
package com.nas.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration //告诉Spring这是一个配置类，启动时要加载我
public class AsyncConfig {
    @Bean("taskExecutor") // 给线程池起个名
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //平时保留几个线程
        executor.setCorePoolSize(4);
        //忙的时候最大扩容
        executor.setMaxPoolSize(8);
        //队列大小，都忙的时候让后面100个任务排队
        executor.setQueueCapacity(100);
        //设置线程前缀，方便在日志中查看
        executor.setThreadNamePrefix("nas-async-");
        //如果队列也满了，就让主线程自己跑，谁的任务谁自己搞
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
