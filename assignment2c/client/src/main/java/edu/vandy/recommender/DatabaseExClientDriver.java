package edu.vandy.recommender;

import edu.vandy.recommender.client.DatabaseExClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This program tests the {@code GatewayApplication} and the {@code
 * DatabaseEx} microservice that it encapsulates.
 */
@SpringBootApplication
public class DatabaseExClientDriver
       implements CommandLineRunner {
    /**
     * This object connects {@link DatabaseExClientDriver} to the
     * {@code DatabaseExClient}.
     */
    @Autowired
    private DatabaseExClient testClient;

    /**
     * The main entry point into the Spring applicaition.
     */
    public static void main(String[] args) {
        // Run the Spring application.
        SpringApplication.run(DatabaseExClientDriver.class, args);
    }

    /**
     * Spring Boot automatically calls this method after the
     * application context has been loaded to exercise the
     * recommender-server {@code DatabaseEx} microservice.
     */
    @Override
    public void run(String... args) {
        System.out.println("Entering the DatabaseExClientDriver tests");

        // Run the DatabaseEx microservice tests.
        testClient.runMoviesTests();

        // Print the results.
        testClient.printSyncTestResults();

        System.out.println("Leaving the DatabaseExDriver tests");
        System.exit(0);
    }                              
}
    
