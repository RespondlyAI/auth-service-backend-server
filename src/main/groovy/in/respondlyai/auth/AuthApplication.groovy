package in.respondlyai.auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = "in.respondlyai.auth.repository")
@EnableRedisRepositories(basePackages = [])
class AuthApplication {

	static void main(String[] args) {
		SpringApplication.run(AuthApplication, args)
	}

}
