package com.boontory.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BoontoryBackendApplication

fun main(args: Array<String>) {
    runApplication<BoontoryBackendApplication>(*args)
}
