package com.streaker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StreakerApplicationTests extends PostgresTestContainerConfig{

	@Test
	void contextLoads() {
	}

}
