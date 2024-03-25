package edu.vandy.recommender.common.autoconfigure;

import edu.vandy.recommender.common.RunTimer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;

import static edu.vandy.recommender.common.Constants.Service.TIMER;

/**
 * This class provides auto-configuration for the application,
 * specifically for the {@link RunTimer} class.
 *
 * It is part of the Java Spring framework and uses Spring Boot
 * auto-configuration annotations.  In particuar, the
 * {@code @AutoConfiguration} annotation automatically configures the
 * Spring application based on the jar dependencies.
 *
 * The {@code @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)}
 * annotation specifies the order of auto-configuration classes.  In
 * particular, the {@link CommonAutoConfiguration} class will be
 * processed before any other auto-configuration classes (if any) in
 * the application.
 *
 * The {@code ComponentScan} annotation specifies the {@link
 * RunTimer} class should be scanned for components.
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CommonAutoConfiguration {
    /**
     * Constructor for {@link RunTimer}.
     *
     * @param restTemplateBuilder A {@link RestTemplateBuilder}
     * @return An initialized {@link RestTemplate}
     */
    @Bean
    @ConditionalOnMissingBean
    RunTimer runTimer(RestTemplateBuilder restTemplateBuilder) {
        return new RunTimer(restTemplateBuilder.build());
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
     * Customize the Protocol Handler on the TomCat Connector to
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
