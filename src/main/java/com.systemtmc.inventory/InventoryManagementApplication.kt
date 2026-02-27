package com.systemtmc.inventory

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Главный класс приложения для системы учета и выдачи ТМЦ
 * 
 * @author SystemTMC
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
object InventoryManagementApplication {
    fun main(args: Array<String?>?) {
        SpringApplication.run(InventoryManagementApplication::class.java, args)
    }
}
