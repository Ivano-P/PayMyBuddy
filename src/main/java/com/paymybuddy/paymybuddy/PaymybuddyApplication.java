package com.paymybuddy.paymybuddy;

import com.paymybuddy.paymybuddy.service.AppUserService;
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
		AppUserService appUserService = context.getBean(AppUserService.class);
		appUserService.creatAdminAppUser();
	}

	@Bean()
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

}
