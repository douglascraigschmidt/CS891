package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseApplication;
import edu.vandy.recommender.common.ServerBeans;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.ParallelFlux;

import static edu.vandy.recommender.common.BaseApplication.run;
import static java.util.Collections.singletonMap;

/**
 * This class provides the entry point for the {@link
 * ParallelFluxApplication} microservice, which uses the Project
 * Reactor framework and its {@link ParallelFlux} class to reactively
 * provide movie recommendations to clients.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @ComponentScan} annotation tells Spring the packages to
 * scan for annotated components (i.e., tagged with
 * {@code @Component}).
 *
 * The {@code @PropertySources} and {@code @PropertySource}
 * annotations are used to provide a properties file to the Spring
 * Environment.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {
    ParallelFluxApplication.class,
    ServerBeans.class})
public class ParallelFluxApplication extends BaseApplication {
//    /**
//     * Helper method that builds a Spring Boot application using the
//     * passed {@link Class} parameter and also sets the application
//     * name to the package name of the passed {@link Class} parameter.
//     *
//     * @param clazz Any microservice {@link Class} type
//     * @param args  Command line arguments
//     */
//    public static void run(Class<?> clazz, String[] args) {
//        var name = getName(clazz);
//        var app = new SpringApplicationBuilder(clazz)
//            .properties(singletonMap("spring.application.name", name))
//            .build();
//        app.setAdditionalProfiles(name);
//        app.setLazyInitialization(true);
//        app.run(args);
//    }
//
//    /**
//     * Gets the name of the application, which is the last part of the
//     * package name.
//     *
//     * @param clazz Any microservice {@link Class} type
//     * @return A {@link String} containing the application name, which is the
//     * last part of package name
//     */
//    private static String getName(Class<?> clazz) {
//        // Get the package name.
//        String pkg = clazz.getPackage().getName();
//
//        // Return the last part of the package name.
//        return pkg.substring(pkg.lastIndexOf('.') + 1);
//    }

    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call BaseApplication helper to build and run this
        // application.
        run(ParallelFluxApplication.class, args);
    }
}
