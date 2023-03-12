package edu.vandy.recommender;

import edu.vandy.recommender.client.DatabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This program tests the {@code GatewayApplication} and the {@code
 * Database} microservice that it encapsulates.
 */
@SpringBootApplication
public class DatabaseClientDriver
       implements CommandLineRunner {
    /**
     * This object connects {@link DatabaseClientDriver} to the
     * {@code DatabaseClient}.
     */
    @Autowired
    private DatabaseClient testClient;

    /**
     * The main entry point into the Spring applicaition.
     */
    public static void main(String[] args) {
        // Run the Spring application.
        SpringApplication.run(DatabaseClientDriver.class, args);
    }

    /**
     * Spring Boot automatically calls this method after the
     * application context has been loaded to exercise the
     * recommender-server {@code Database} microservice.
     */
    @Override
    public void run(String... args) {
        System.out.println("Entering the DatabaseClientDriver tests");

        // Run the Database microservice tests.
        testClient.runMoviesTests();

        // Print the results.
        testClient.printSyncTestResults();

        System.out.println("Leaving the DatabaseDriver tests");
        System.exit(0);
    }                              
}
    
