package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories(basePackages = ["com.example.demo.repositories"])
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}