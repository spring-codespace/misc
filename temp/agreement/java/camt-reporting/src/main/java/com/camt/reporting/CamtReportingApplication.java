package com.camt.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CamtReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamtReportingApplication.class, args);
    }
}
