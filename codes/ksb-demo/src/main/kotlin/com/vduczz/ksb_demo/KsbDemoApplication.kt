package com.vduczz.ksb_demo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication
class KsbDemoApplication

fun main(args: Array<String>) {
    runApplication<KsbDemoApplication>(*args)

    // example: print list of beans
    //val appContext: ApplicationContext = runApplication<KsbDemoApplication>(*args)
    //println("list of beans: ")
    //for (bean in appContext.beanDefinitionNames) {
    //    println(bean)
    //}
}
