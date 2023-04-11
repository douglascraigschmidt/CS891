package edu.vandy.recommender.timer.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class provides the entry point for the {@link TimerApplication}
 * microservice, which provides asynchronous and synchronous
 * computation of method execution times.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 */
@SpringBootApplication
public class TimerApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(TimerApplication.class, args);
    }

    /**
     * Configure the use of Java virtual threads to handle all
     * incoming HTTP requests.

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors
                .newVirtualThreadPerTaskExecutor());
    }

     * Customize the ProtocolHandler on the TomCat Connector to
     * use Java virtual threads to handle all incoming HTTP requests.

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler
                    .setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
    */

}
