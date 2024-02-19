package edu.vandy.recommender.database.server;

import edu.vandy.recommender.database.common.model.Movie;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * This class provides the entry point for the DataApplication
 * microservice, whose methods return a {@link List} of objects
 * containing information about {@link Movie} objects.  It also
 * configures the use of Java 19 virtual threads to process incoming
 * HTTP requests.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * automatic configuration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @EntityScan} annotation is used when entity classes are
 * not placed in the main application package or its sub-packages.
 *
 * The {@code @ComponentScan} annotation tells Spring the packages to
 * scan for annotated components (i.e., tagged with
 * {@code @Component}).  In this case, this annotation instructs
 * Spring to scan components in the "edu.vandy.recommender.movies"
 * package.
 *
 * The {@code @EnableJpaRepositories} annotation enables the use of
 * JPA repositories by scanning the package of the annotated
 * configuration class for Spring Data repositories.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender.database"
 * package.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.recommender.database")
@EntityScan(basePackageClasses = Movie.class)
@EnableJpaRepositories(basePackageClasses = DatabaseRepository.class)
public class DatabaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(DatabaseApplication.class, args);
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
