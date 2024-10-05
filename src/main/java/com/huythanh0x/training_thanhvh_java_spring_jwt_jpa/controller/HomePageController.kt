package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/home")
class HomeController {
    @GetMapping
    fun home(): String {
        return "index" // This will return the static HTML file located at src/main/resources/static/index.html
    }
}