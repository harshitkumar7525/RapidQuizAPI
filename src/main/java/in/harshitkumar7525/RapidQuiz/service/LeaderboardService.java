package in.harshitkumar7525.RapidQuiz.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LeaderboardService {

    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    public LeaderboardService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void recordScore(String gameId, String participantId, int score) {
        String key = leaderboardKey(gameId);
        redisTemplate.opsForZSet().incrementScore(key, participantId, score);
        redisTemplate.expire(key, TTL);
    }

    private String leaderboardKey(String gameId) {
        return "leaderboard:" + gameId;
    }
}