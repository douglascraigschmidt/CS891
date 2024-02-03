package edu.vandy.recommender.movies.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * This class provides the entry point for the MoviesApplication
 * microservice, whose methods return a {@link List} of objects
 * containing information about movies.  It also configures the
 * use of Java 19 virtual threads to process incoming HTTP requests.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * automatic configuration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @ComponentnScan} annotation instructs Spring to scan
 * components in the "edu.vandy.recommender.movies" package.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.recommender.movies")
public class MoviesApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(MoviesApplication.class, args);
    }

    /**
     * Configure the use of Java virtual threads to handle all
     * incoming HTTP requests.
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors
                                       .newVirtualThreadPerTaskExecutor());
    }

    /**
     * Customize the ProtocolHandler on the TomCat Connector to
     * use Java virtual threads to handle all incoming HTTP requests.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler
                .setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
