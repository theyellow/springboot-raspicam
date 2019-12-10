package io.github.theyellow.springbootraspicam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("io.github.theyellow.springbootraspicam")
public class SpringbootRaspicamApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootRaspicamApplication.class, args);
	}

}
