package in.respondlyai.auth.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Service
class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService)
    private static final long OTP_TTL_MINUTES = 5

    private final StringRedisTemplate redisTemplate

    OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate
    }

    String generateAndStoreOtp(String email) {
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000))
        String key = "otp:" + email
        
        // Save to Redis
        redisTemplate.opsForValue().set(key, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES)
        
        log.info("Generated OTP for email: {}", email)
        return otp
    }

    boolean validateOtp(String email, String otp) {
        String key = "otp:" + email
        String storedOtp = redisTemplate.opsForValue().get(key)
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key)
            return true
        }
        return false
    }
}
