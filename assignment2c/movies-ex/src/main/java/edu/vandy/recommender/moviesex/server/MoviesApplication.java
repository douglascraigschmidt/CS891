package edu.vandy.recommender.moviesex.server;

import edu.vandy.recommender.common.BaseApplication;
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
@ComponentScan("edu.vandy.recommender.moviesex")
public class MoviesApplication
       extends BaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call the BaseApplication.run() method to build and run this
        // application.
        run(MoviesApplication.class, args);
    }
}
