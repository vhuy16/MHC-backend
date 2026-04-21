package com.topick.superapp.mhc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MhcApplication {

    public static void main(String[] args) {
        SpringApplication.run(MhcApplication.class, args);
    }

}
