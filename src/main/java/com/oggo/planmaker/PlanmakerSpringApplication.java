package com.oggo.planmaker;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.oggo.planmaker.mapper")

public class PlanmakerSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanmakerSpringApplication.class, args);
	}

}
