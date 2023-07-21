package com.paymybuddy.paymybuddy;

import com.paymybuddy.paymybuddy.service.AppPmbService;
import com.paymybuddy.paymybuddy.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class PaymybuddyApplicationTests {

	@Autowired
	private AppUserService appUserService;

	@Autowired
	private AppPmbService appPmbService;

	@Test
	void contextLoads() {
		assertNotNull(appUserService);
		assertNotNull(appPmbService);
	}

}
