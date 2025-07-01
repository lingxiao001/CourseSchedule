package com.example.courseschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
@EnableSpringDataWebSupport
@SpringBootApplication
public class CourseScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseScheduleApplication.class, args);
	}

}
