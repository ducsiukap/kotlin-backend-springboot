package com.vduczz.mini_project.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync // enable asynchronous processing cho toàn bộ project
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {

        // create Thread Pool executor obj
        val executor = ThreadPoolTaskExecutor()

        executor.corePoolSize = 5 // số luồng lúc nào cũng có sẵn ở server
        executor.maxPoolSize = 10 // số luồng tối đa phục vụ cùng lúc
        executor.queueCapacity = 100 // khi 10 luồng phục vụ full -> cho phép tối đa 100 request chờ
        //  -> request 101 bị reject

        executor.setThreadNamePrefix("Async-Thread-")
        executor.initialize()

        return executor

    }
}