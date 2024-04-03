package edu.vandy.recommender.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

import static java.util.Collections.singletonMap;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

/**
 * A static class with a single {@link #run} static method that is
 * used by all microservices to build a Spring Boot application
 * instance and to give a unique name that is used as a path component
 * in URLs and for routing by the gateway application.  It also
 * configures the use of Java 19 virtual threads to process incoming
 * HTTP requests.
 */
@Configuration
public class BaseApplication {
    /**
     * Helper method that builds a Spring Boot application using the
     * passed {@link Class} parameter and also sets the application
     * name to the package name of the passed {@link Class} parameter.
     *
     * @param clazz Any microservice {@link Class} type
     * @param args  Command line arguments
     */
    public static void run(Class<?> clazz, String[] args) {
        var name = getName(clazz);
        var app = new SpringApplicationBuilder(clazz)
            .properties(singletonMap("spring.application.name", name))
            .build();
        app.setAdditionalProfiles(name);
        app.setLazyInitialization(true);
        app.run(args);
    }

    /**
     * Gets the name of the application, which is the last part of the
     * package name.
     *
     * @param clazz Any microservice {@link Class} type
     * @return A {@link String} containing the application name, which is the
     * last part of package name
     */
    private static String getName(Class<?> clazz) {
        // Get the package name.
        String pkg = clazz.getPackage().getName();

        // Return the last part of the package name.
        return pkg.substring(pkg.lastIndexOf('.') + 1);
    }
}
