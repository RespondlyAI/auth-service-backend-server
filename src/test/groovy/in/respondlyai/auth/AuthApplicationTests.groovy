package in.respondlyai.auth

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = AuthApplication.class,
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"]
)
class AuthApplicationTests {

	@Test
	void contextLoads() {
	}

}
