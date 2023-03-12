package edu.vandy.recommender.database.common;

import edu.vandy.recommender.database.server.DatabaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

import static edu.vandy.recommender.database.common.Constants.Service.TIMER;

/**
 * This class contains a {@code Bean} annotation that can be injected into
 * classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
@PropertySource(
    value = "classpath:/application.yml",
    factory = YamlPropertySourceFactory.class)
public class ServerBeans {
    /**
     * @return A new {@link RestTemplate}.
     */
    // TODO -- you fill in here to make this a @Bean.
    @Qualifier(TIMER)
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
