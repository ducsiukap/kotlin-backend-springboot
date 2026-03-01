package com.vduczz.mini_project

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing // enable Auditing
class MiniProjectApplication

fun main(args: Array<String>) {
    runApplication<MiniProjectApplication>(*args)
}
