package edu.vandy.recommender.gateway;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * The entry point for the {@link GatewayApplication} microservice,
 * which provides an API Gateway for all the backend microservices
 * in the recommender-server project.
 */
@SpringBootApplication
@Configuration
public class GatewayApplication {
    /**
     * Create a {@link Logger} to show {@link GatewayProperties}.
     */
    private static final Logger logger =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Spring {@link GatewayProperties} used to track what
     * microservices have been registered with this gateway.
     */
    @Autowired
    private GatewayProperties props;

    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(GatewayApplication.class,
                              args);
    }

    /**
     * Runs after application has been constructed and displays the
     * current {@link GatewayProperties} that shows all gateway
     * routing paths.
     */
    @PostConstruct
    public void init() {
        logger.info(Objects.toString(props));
    }
}
