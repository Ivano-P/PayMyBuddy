package com.paymybuddy.paymybuddy;

import com.paymybuddy.paymybuddy.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PaymybuddyApplication {



	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(PaymybuddyApplication.class, args);
		UserService userService = context.getBean(UserService.class);
		userService.creatAdminAppUser();
	}

	@Bean()
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

}
