package edu.vandy.recommender;

import edu.vandy.recommender.client.RecommenderAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This program tests the {@code GatewayApplication} and various
 * microservices that it encapsulates.
 */
@SpringBootApplication
public class RecommenderClientDriver
       implements CommandLineRunner {
    /**
     * This object connects {@link RecommenderClientDriver} to the
     * {@code RecommenderAsyncClient}.
     */
    @Autowired
    private RecommenderAsyncClient testAsyncClient;

    /**
     * The main entry point into the Spring applicaition.
     */
    public static void main(String[] args) {
        // Run the Spring application.
        SpringApplication.run(RecommenderClientDriver.class, args);
    }

    /**
     * Spring Boot automatically calls this method after the
     * application context has been loaded to exercise the
     * recommender-server microservices.
     */
    @Override
    public void run(String... args) {
        System.out.println("Entering the RecommenderClientDriver tests");

        testAsyncClient.runTests("parallelflux");

        testAsyncClient.printTestResults();

        System.out.println("Leaving the RecommenderClientDriver tests");
        System.exit(0);
    }                              
}
    
