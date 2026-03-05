package com.vduczz.mini_project.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hello")
class MyController {

    @GetMapping
    fun hello(
        @RequestParam(value = "name", defaultValue = "Đạt") name: String
    ): String {
        val emp = "<span style=\"font-size: 80px; color: hotpink;\">"
        val eEmp = "</span>"

        val html = when (name.lowercase()) {
            "dd" -> "<h1>Hello, <b><i>${emp}Đậu Đậu${eEmp} :))</i></b></h1>"
            "qa" -> "<h1><b>${emp}Đức${eEmp}</> yêu <b><i>${emp}Quỳnh Anh${eEmp}</i></b></h1>"
            else -> "<h1>Hello, <b>con chó <i>${emp}$name${eEmp}</i></b></h1>"
        }

        return """
            <div 
                style="
                    width:100vw;
                    height:100vh;
                    display:flex; 
                    justify-content:center; 
                    align-items:center;
                    font-size:20px;
                    background: black;
                    color:white;
            ">$html</div>
        """.trimIndent()
    }

}