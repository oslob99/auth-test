package com.server.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootTest
class AuthApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void makeSeceretKey(){
		SecureRandom random = new SecureRandom();
		byte[] key = new byte[64];
		random.nextBytes(key);
		String encodedKey = Base64.getEncoder().encodeToString(key);
		System.out.println("//////");
		System.out.println(encodedKey);
		System.out.println("//////");

	}

}
