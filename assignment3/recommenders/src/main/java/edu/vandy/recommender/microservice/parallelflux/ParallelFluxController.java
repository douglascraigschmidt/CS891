package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseController;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * The Spring WebFlux controller for the {@link ParallelFluxService}.
 *
 * {@code @RestController} is a convenience annotation for creating
 * Restful controllers. It is a specialization of {@code @Component}
 * and is automatically detected through classpath scanning. It adds
 * the {@code @Controller} and {@code @ResponseBody} annotations. It
 * also converts responses to JSON.
 */
@RestController
public class ParallelFluxController extends BaseController<Flux<Ranking>> {
}
