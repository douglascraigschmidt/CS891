package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseApplication;
import edu.vandy.recommender.common.ServerBeans;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static edu.vandy.recommender.common.BaseApplication.run;

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
public class ParallelFluxApplication
// @@ Monte, if I uncomment this line I get weird "duplicate bean"
// errors!
       /* extends BaseApplication */ {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call BaseApplication helper to build and run this
        // application.
        run(ParallelFluxApplication.class, args);
    }
}
