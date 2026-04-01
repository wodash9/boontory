package com.boontory.backend.config

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SpaController {
    // Forward Vue Router routes to index.html.
    // Each segment regex [^\\.]* ensures no dots → never matches static files like /assets/app.js
    @RequestMapping(
        "/{a:[^\\.]*}",
        "/{a:[^\\.]*}/{b:[^\\.]*}",
        "/{a:[^\\.]*}/{b:[^\\.]*}/{c:[^\\.]*}"
    )
    fun forward(): String = "forward:/index.html"
}
