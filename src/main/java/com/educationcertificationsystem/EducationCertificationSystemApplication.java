package com.educationcertificationsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
        "com.educationcertificationsystem.ai.mapper",
        "com.educationcertificationsystem.notice.mapper",
        "com.educationcertificationsystem.user.mapper",
        "com.educationcertificationsystem.role.mapper",
        "com.educationcertificationsystem.org.mapper",
        "com.educationcertificationsystem.course.mapper",
        "com.educationcertificationsystem.eval.mapper",
        "com.educationcertificationsystem.survey.mapper",
        "com.educationcertificationsystem.report.mapper",
        "com.educationcertificationsystem.improve.mapper",
        "com.educationcertificationsystem.program.mapper",
        "com.educationcertificationsystem.system.mapper",
        "com.educationcertificationsystem.file.mapper"
})
public class EducationCertificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationCertificationSystemApplication.class, args);
    }

}
