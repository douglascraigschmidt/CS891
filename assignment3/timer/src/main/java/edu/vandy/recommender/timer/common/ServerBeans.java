package edu.vandy.recommender.timer.common;

import edu.vandy.recommender.timer.server.TimerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
public class ServerBeans {
    /**
     * @return A new {@link ConcurrentHashMap}.
     */
    @Bean
    public ConcurrentHashMap<String, TimerService.Timing>
    getConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
