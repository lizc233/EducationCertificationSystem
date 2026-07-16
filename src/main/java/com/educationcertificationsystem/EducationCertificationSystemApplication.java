package com.educationcertificationsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.educationcertificationsystem.mapper")
public class EducationCertificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationCertificationSystemApplication.class, args);
    }

}
