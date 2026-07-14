package com.feuca.facturacion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
		"jwt.expiration-ms=900000",
		"jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class FacturacionApplicationTests {

	@Test
	void contextLoads() {
	}

}
