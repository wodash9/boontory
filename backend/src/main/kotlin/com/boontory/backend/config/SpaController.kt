package com.boontory.backend.config

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SpaController {
    // Forward all non-file, non-api routes to index.html for Vue Router history mode
    @RequestMapping("/{path:[^\\.]*}", "/{path:(?!api)[^\\.]*}/**")
    fun forward(): String = "forward:/index.html"
}
