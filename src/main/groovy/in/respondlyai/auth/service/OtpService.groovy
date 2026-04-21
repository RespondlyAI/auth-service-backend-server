package in.respondlyai.auth.service

import groovy.util.logging.Slf4j
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Slf4j
@Service
class OtpService {
    private static final long OTP_TTL_MINUTES = 5

    private final StringRedisTemplate redisTemplate

    OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate
    }

    String generateAndStoreOtp(String userId) {
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000))
        String key = "otp:" + userId
        
        // Save to Redis
        redisTemplate.opsForValue().set(key, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES)
        
        log.info("Generated OTP for userId: {}", userId)
        return otp
    }

    boolean validateOtp(String userId, String otp) {
        String key = "otp:" + userId
        String storedOtp = redisTemplate.opsForValue().get(key)
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key)
            return true
        }
        return false
    }
}
