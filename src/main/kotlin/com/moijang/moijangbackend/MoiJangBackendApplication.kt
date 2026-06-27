package com.moijang.moijangbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration

@SpringBootApplication(
    // 임시로 DB, 보안 설정 끔
    exclude = [
        DataSourceAutoConfiguration::class,
        SecurityAutoConfiguration::class
    ]
)
class MoiJangBackendApplication

fun main(args: Array<String>) {
    runApplication<MoiJangBackendApplication>(*args)
}
